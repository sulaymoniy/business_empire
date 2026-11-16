package com.businessempire.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.businessempire.game.BusinessEmpireApplication
import com.businessempire.game.data.RichListRepository
import com.businessempire.game.data.SaveRepository
import com.businessempire.game.domain.BankEngine
import com.businessempire.game.domain.BuyMode
import com.businessempire.game.domain.DailyEngine
import com.businessempire.game.domain.EventEngine
import com.businessempire.game.domain.Formulas
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.LuxuryEngine
import com.businessempire.game.domain.MarketEngine
import com.businessempire.game.domain.NewsItem
import com.businessempire.game.domain.NumberStyle
import com.businessempire.game.domain.OfflineReport
import com.businessempire.game.domain.PendingEvent
import com.businessempire.game.domain.PrestigeEngine
import com.businessempire.game.domain.Profile
import com.businessempire.game.domain.PropertyEngine
import com.businessempire.game.domain.RichList
import com.businessempire.game.domain.RichListEngine
import com.businessempire.game.domain.RichPerson
import com.businessempire.game.domain.Rng
import com.businessempire.game.domain.TutorialEngine
import com.businessempire.game.domain.content.EventOutcome
import com.businessempire.game.domain.content.Milestone
import com.businessempire.game.domain.content.WealthMilestone
import com.businessempire.game.domain.content.WealthMilestones
import com.businessempire.game.online.LeaderboardService
import com.businessempire.game.online.LocalLeaderboardService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.TimeZone

/** Today's date in the device's time zone, as days since 1970-01-01. Works on every API level. */
fun localEpochDay(nowMs: Long = System.currentTimeMillis()): Long =
    Math.floorDiv(nowMs + TimeZone.getDefault().getOffset(nowMs), 86_400_000L)

/** One-off messages for snackbars. */
sealed interface Notice {
    data class MilestoneReached(val businessId: String, val milestone: Milestone) : Notice
    /** News about a stock the player holds. */
    data class StockNews(val item: NewsItem) : Notice
    data class AchievementUnlocked(val id: String) : Notice
    data class EventResolved(val event: PendingEvent, val outcome: EventOutcome, val amount: Double) : Notice
    data class DailyClaimed(val amount: Double, val streak: Int) : Notice
    data class Prestiged(val points: Double) : Notice
    /** The player overtook [person] on the Rich List ([others] more were passed at the same time). */
    data class PassedPerson(val person: RichPerson, val others: Int) : Notice
    data class WealthMilestoneReached(val milestone: WealthMilestone) : Notice
}

/**
 * Owns the live [GameState], runs the game loop (about 10 ticks per second), autosaves and handles
 * offline time. All rules live in the domain engines; this class only schedules and persists.
 */
class GameViewModel(
    private val repo: SaveRepository,
    private val richListRepo: RichListRepository? = null,
    leaderboardFactory: ((() -> GameState?) -> LeaderboardService)? = null,
    private val now: () -> Long = System::currentTimeMillis,
    private val today: () -> Long = ::localEpochDay,
) : ViewModel() {

    private val _state = MutableStateFlow<GameState?>(null)
    /** Null until the save has loaded. */
    val state: StateFlow<GameState?> = _state.asStateFlow()

    private val _offline = MutableStateFlow<OfflineReport?>(null)
    val offlineReport: StateFlow<OfflineReport?> = _offline.asStateFlow()

    private val _buyMode = MutableStateFlow(BuyMode.ONE)
    val buyMode: StateFlow<BuyMode> = _buyMode.asStateFlow()

    private val _notices = MutableSharedFlow<Notice>(extraBufferCapacity = 32)
    val notices: SharedFlow<Notice> = _notices.asSharedFlow()

    private val _richList = MutableStateFlow(RichListEngine.fictional)
    /** Real names from assets/rich_list.json, or the fictional fallback. */
    val richList: StateFlow<RichList> = _richList.asStateFlow()

    /** ONLINE: swap the implementation in AppContainer.leaderboard(). */
    val leaderboard: LeaderboardService = leaderboardFactory?.invoke { _state.value } ?: LocalLeaderboardService { _state.value }

    private var paused = false
    private var pausedAtMs = 0L
    private var secondsSinceSave = 0.0

    init {
        viewModelScope.launch {
            richListRepo?.let { _richList.value = it.load() }
            loadOrCreate()
            runLoop()
        }
    }

    private suspend fun loadOrCreate() {
        val nowMs = now()
        val saved = repo.load()
        _state.value = if (saved == null) {
            GameEngine.newGame(seed = nowMs, nowMs = nowMs)
        } else {
            applyOfflineTime(GameEngine.normalize(saved), (nowMs - saved.lastSavedAtMs) / 1000.0)
        }
    }

    private suspend fun runLoop() {
        var last = System.nanoTime()
        while (viewModelScope.isActive) {
            delay(TICK_MS)
            val t = System.nanoTime()
            val dt = ((t - last) / 1e9).coerceIn(0.0, MAX_TICK_SECONDS)
            last = t
            if (paused) continue
            val before = _state.value ?: continue
            // Don't run the clock until the player has named their company.
            if (!before.profile.isSet) continue
            val result = GameEngine.step(before, dt)
            _state.value = result.state
            emitTickNotices(result.state, result.news, result.achievements)
            emitWealthNotices(before.stats.bestNetWorth, result.state.stats.bestNetWorth)
            secondsSinceSave += dt
            if (secondsSinceSave >= AUTOSAVE_SECONDS) save()
        }
    }

    private fun emitTickNotices(state: GameState, news: List<NewsItem>, achievements: List<String>) {
        news.filter { (state.market.stocks[it.stockId]?.shares ?: 0.0) > 0.0 }
            .forEach { _notices.tryEmit(Notice.StockNews(it)) }
        achievements.forEach { _notices.tryEmit(Notice.AchievementUnlocked(it)) }
    }

    /** Rich List overtakes and real-world wealth milestones. Uses best-ever net worth, so prestige doesn't repeat them. */
    private fun emitWealthNotices(before: Double, after: Double) {
        if (after <= before) return
        val passed = RichListEngine.passed(_richList.value, before, after)
        passed.firstOrNull()?.let { _notices.tryEmit(Notice.PassedPerson(it, passed.size - 1)) }
        WealthMilestones.crossed(before, after).lastOrNull()?.let { _notices.tryEmit(Notice.WealthMilestoneReached(it)) }
    }

    private fun applyOfflineTime(state: GameState, elapsedSeconds: Double): GameState {
        if (elapsedSeconds < 1.0 || !state.profile.isSet) return state
        val (next, report) = GameEngine.applyOffline(state, elapsedSeconds)
        emitWealthNotices(state.stats.bestNetWorth, next.stats.bestNetWorth)
        if (elapsedSeconds >= OFFLINE_DIALOG_MIN_SECONDS && report.earned > 0.0) _offline.value = report
        return next
    }

    private inline fun update(transform: (GameState) -> GameState) {
        val current = _state.value ?: return
        _state.value = transform(current)
    }

    fun save() {
        secondsSinceSave = 0.0
        val stamped = _state.value?.copy(lastSavedAtMs = now()) ?: return
        _state.value = stamped
        viewModelScope.launch { repo.save(stamped) }
    }

    // --- Lifecycle (called from the activity) ----------------------------------------------------

    fun onAppStop() {
        if (_state.value == null) return
        paused = true
        pausedAtMs = now()
        save()
    }

    fun onAppStart() {
        if (!paused) return
        paused = false
        val elapsed = (now() - pausedAtMs) / 1000.0
        update { applyOfflineTime(it, elapsed) }
    }

    fun dismissOfflineReport() {
        _offline.value = null
    }

    // --- Businesses ----------------------------------------------------------------------------------

    fun setBuyMode(mode: BuyMode) {
        _buyMode.value = mode
    }

    fun buyBusiness(id: String) {
        val before = _state.value?.business(id)?.owned ?: return
        update { GameEngine.buyBusiness(it, id, _buyMode.value) }
        val after = _state.value?.business(id)?.owned ?: return
        Formulas.milestonesCrossed(before, after).forEach { _notices.tryEmit(Notice.MilestoneReached(id, it)) }
    }

    fun tapBusiness(id: String) = update { GameEngine.tapBusiness(it, id) }

    fun hireManager(id: String) = update { GameEngine.hireManager(it, id) }

    fun buyUpgrade(id: String) = update { GameEngine.buyUpgrade(it, id) }

    // --- Investments, bank, assets -------------------------------------------------------------------

    fun buyStock(id: String, quantity: Long) = update { MarketEngine.buy(it, id, quantity) }

    fun sellStock(id: String, quantity: Long) = update { MarketEngine.sell(it, id, quantity) }

    fun buyProperty(id: String) = update { PropertyEngine.buy(it, id) }

    fun renovateProperty(id: String) = update { PropertyEngine.renovate(it, id) }

    fun sellProperty(id: String) = update { PropertyEngine.sell(it, id) }

    fun deposit(amount: Double) = update { BankEngine.deposit(it, amount) }

    fun withdraw(amount: Double) = update { BankEngine.withdraw(it, amount) }

    fun borrow(amount: Double) = update { BankEngine.borrow(it, amount) }

    fun repay(amount: Double) = update { BankEngine.repay(it, amount) }

    fun buyLuxury(id: String) = update { LuxuryEngine.buy(it, id) }

    // --- Meta ----------------------------------------------------------------------------------------

    fun setProfile(ceoName: String, companyName: String) {
        update { it.copy(profile = Profile(ceoName.trim(), companyName.trim())) }
        save()
    }

    fun canClaimDaily(state: GameState): Boolean = DailyEngine.canClaim(state, today())

    fun dailyPreview(state: GameState): Pair<Int, Double> {
        val streak = DailyEngine.nextStreak(state, today())
        return streak to DailyEngine.reward(state, streak)
    }

    fun claimDaily() {
        val current = _state.value ?: return
        val (next, amount) = DailyEngine.claim(current, today()) ?: return
        _state.value = next
        _notices.tryEmit(Notice.DailyClaimed(amount, next.daily.streak))
        save()
    }

    fun resolveEvent(accept: Boolean) {
        val current = _state.value ?: return
        val event = current.event.pending ?: return
        val rng = Rng(current.rngState)
        val result = EventEngine.resolve(current, accept, rng) ?: return
        _state.value = result.state.copy(rngState = rng.state)
        _notices.tryEmit(Notice.EventResolved(event, result.outcome, result.amount))
    }

    fun prestige() {
        val current = _state.value ?: return
        val points = PrestigeEngine.pointsAvailable(current)
        val next = PrestigeEngine.prestige(current)
        if (next === current) return
        _state.value = next
        _notices.tryEmit(Notice.Prestiged(points))
        save()
    }

    fun skipTutorial() = update { TutorialEngine.skip(it) }

    fun onInvestOpened() = update { TutorialEngine.visitedInvest(it) }

    // --- Settings ------------------------------------------------------------------------------------

    fun setSound(on: Boolean) = update { it.copy(settings = it.settings.copy(sound = on)) }

    fun setHaptics(on: Boolean) = update { it.copy(settings = it.settings.copy(haptics = on)) }

    fun setNumberStyle(style: NumberStyle) = update { it.copy(settings = it.settings.copy(numberStyle = style)) }

    /** Wipes the save and starts over (keeps nothing). */
    fun resetGame() {
        viewModelScope.launch {
            repo.clear()
            val nowMs = now()
            _offline.value = null
            _state.value = GameEngine.newGame(seed = nowMs, nowMs = nowMs)
        }
    }

    // --- Debug cheats (only reachable from the debug-only Cheats screen) ----------------------------

    fun debugAddCash(amount: Double) = update { GameEngine.earn(it, amount) }

    fun debugSkipTime(seconds: Double) {
        update {
            val (next, report) = GameEngine.applyOffline(it, seconds, ignoreCap = true)
            _offline.value = report
            next
        }
    }

    fun debugUnlockAll() = update { s ->
        var next = s
        com.businessempire.game.domain.content.Businesses.all.forEach { def ->
            val b = next.business(def.id)
            next = next.copy(
                businesses = next.businesses + (def.id to b.copy(owned = maxOf(b.owned, 1), manager = true, running = true)),
            )
        }
        next
    }

    fun debugTriggerEvent() = update { s ->
        val rng = Rng(s.rngState)
        val event = EventEngine.create(s, rng) ?: return@update s
        s.copy(event = s.event.copy(pending = event), rngState = rng.state)
    }

    companion object {
        const val TICK_MS = 100L
        const val MAX_TICK_SECONDS = 5.0
        const val AUTOSAVE_SECONDS = 15.0
        const val OFFLINE_DIALOG_MIN_SECONDS = 60.0

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BusinessEmpireApplication
                GameViewModel(
                    repo = app.container.saveRepository,
                    richListRepo = app.container.richListRepository,
                    leaderboardFactory = app.container::leaderboard,
                )
            }
        }
    }
}
