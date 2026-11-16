package com.businessempire.game.domain

import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.Stocks
import com.businessempire.game.domain.content.Upgrades
import kotlin.math.floor
import kotlin.math.min

/** Result of applying time that passed while the app was closed. */
data class OfflineReport(
    val elapsedSeconds: Double,
    val countedSeconds: Double,
    val capHours: Double,
    val earned: Double,
)

/** One simulation step: the new state plus things worth telling the player about. */
data class TickResult(
    val state: GameState,
    val news: List<NewsItem> = emptyList(),
    val achievements: List<String> = emptyList(),
)

/**
 * All game rules. Pure Kotlin: no Android imports, no wall clock, no global randomness.
 * Every function takes a [GameState] and returns the next one, so it is easy to unit test.
 * Feature-specific rules live in MarketEngine, PropertyEngine, BankEngine, LuxuryEngine,
 * PrestigeEngine, AchievementEngine, EventEngine, DailyEngine and TutorialEngine.
 */
object GameEngine {

    fun newGame(seed: Long, nowMs: Long): GameState =
        normalize(GameState(rngState = seed, lastSavedAtMs = nowMs))

    /** Fills in anything an older or brand-new save is missing (e.g. the stock market). */
    fun normalize(state: GameState): GameState {
        if (state.market.stocks.keys.containsAll(Stocks.byId.keys)) return state
        val rng = Rng(state.rngState)
        val fresh = MarketEngine.initialMarket(rng)
        val merged = fresh.copy(stocks = fresh.stocks + state.market.stocks, news = state.market.news)
        return state.copy(market = merged, rngState = rng.state)
    }

    // ---------------------------------------------------------------------------------------------
    // Simulation
    // ---------------------------------------------------------------------------------------------

    /** Advances the game by [dt] seconds of active play. */
    fun tick(state: GameState, dt: Double): GameState = step(state, dt).state

    /** Like [tick] but also reports news and unlocked achievements. */
    fun step(state: GameState, dt: Double): TickResult {
        if (dt <= 0.0) return TickResult(state)
        val eco = Economy(state)
        var earned = 0.0
        val businesses = state.businesses.mapValues { (id, b) ->
            if (b.owned == 0 || !(b.running || b.manager)) return@mapValues b
            val cycle = eco.cycleSeconds(id)
            val progress = b.progress + dt
            when {
                progress < cycle -> b.copy(progress = progress, running = true)
                b.manager -> {
                    val cycles = floor(progress / cycle)
                    earned += eco.revenuePerCycle(id) * cycles
                    b.copy(progress = progress - cycles * cycle, running = true)
                }
                else -> {
                    earned += eco.revenuePerCycle(id)
                    b.copy(progress = 0.0, running = false)
                }
            }
        }
        val rng = Rng(state.rngState)
        val (market, news) = MarketEngine.advance(state.market, dt, rng, state.clock)
        val (properties, rent) = PropertyEngine.advance(state, dt)
        var next = state.copy(
            businesses = businesses,
            market = market,
            properties = properties,
            effects = EventEngine.advanceEffects(state.effects, dt),
            clock = state.clock + dt,
            stats = state.stats.copy(timePlayedSeconds = state.stats.timePlayedSeconds + dt),
        )
        next = earn(next, earned + rent)
        next = BankEngine.advance(next, dt)
        next = EventEngine.advance(next, dt, rng)
        next = next.copy(rngState = rng.state)
        next = TutorialEngine.advance(next)
        next = trackNetWorth(next)
        val (withAchievements, unlocked) = AchievementEngine.check(next)
        return TickResult(withAchievements, news, unlocked)
    }

    /**
     * Applies [elapsedSeconds] of offline time, capped by the offline-cap upgrades.
     * Managers, rent and bank interest keep working; event boosts don't apply while away.
     * Stock prices don't move offline.
     */
    fun applyOffline(state: GameState, elapsedSeconds: Double, ignoreCap: Boolean = false): Pair<GameState, OfflineReport> {
        val eco = Economy(state.copy(effects = emptyList()))
        val capHours = eco.offlineCapHours
        val counted = if (ignoreCap) elapsedSeconds.coerceAtLeast(0.0)
        else min(elapsedSeconds, capHours * 3600.0).coerceAtLeast(0.0)
        if (counted <= 0.0) return state to OfflineReport(elapsedSeconds, 0.0, capHours, 0.0)

        var earned = 0.0
        val businesses = state.businesses.mapValues { (id, b) ->
            if (b.owned == 0) return@mapValues b
            val cycle = eco.cycleSeconds(id)
            when {
                b.manager -> {
                    // Continuous approximation is exact enough over minutes or hours.
                    earned += eco.businessIncomePerSecond(id) * counted
                    b.copy(progress = (b.progress + counted) % cycle, running = true)
                }
                b.running && b.progress + counted >= cycle -> {
                    earned += eco.revenuePerCycle(id)
                    b.copy(progress = 0.0, running = false)
                }
                b.running -> b.copy(progress = b.progress + counted)
                else -> b
            }
        }
        val (properties, rent) = PropertyEngine.advance(state, counted)
        earned += rent
        var next = state.copy(
            businesses = businesses,
            properties = properties,
            effects = EventEngine.advanceEffects(state.effects, counted),
            clock = state.clock + counted,
        )
        next = earn(next, earned)
        next = BankEngine.advance(next, counted)
        next = trackNetWorth(next)
        return next to OfflineReport(elapsedSeconds, counted, capHours, earned)
    }

    fun earn(state: GameState, amount: Double): GameState {
        if (amount <= 0.0) return state
        return state.copy(
            cash = state.cash + amount,
            runEarnings = state.runEarnings + amount,
            stats = state.stats.copy(lifetimeEarnings = state.stats.lifetimeEarnings + amount),
        )
    }

    fun trackNetWorth(state: GameState): GameState {
        val nw = Economy(state).netWorth
        return if (nw > state.stats.bestNetWorth) state.copy(stats = state.stats.copy(bestNetWorth = nw)) else state
    }

    // ---------------------------------------------------------------------------------------------
    // Businesses
    // ---------------------------------------------------------------------------------------------

    /** A tier can be bought once the previous tier has at least one unit. */
    fun isUnlocked(state: GameState, id: String): Boolean {
        val index = Businesses.indexOf(id)
        if (index <= 0) return index == 0
        return state.business(Businesses.all[index - 1].id).owned > 0
    }

    fun buyCost(state: GameState, id: String, mode: BuyMode): Pair<Int, Double> {
        val def = Businesses.get(id)
        val owned = state.business(id).owned
        val count = Formulas.countFor(mode, def, owned, state.cash)
        return count to Formulas.costForCount(def, owned, count)
    }

    fun buyBusiness(state: GameState, id: String, mode: BuyMode): GameState {
        if (!isUnlocked(state, id)) return state
        val (count, cost) = buyCost(state, id, mode)
        if (count <= 0 || cost > state.cash) return state
        val b = state.business(id)
        return state.copy(
            cash = state.cash - cost,
            businesses = state.businesses + (id to b.copy(owned = b.owned + count)),
            stats = state.stats.copy(unitsBought = state.stats.unitsBought + count),
        )
    }

    /** Starts one production cycle by hand. No effect if a manager runs it or it is already running. */
    fun tapBusiness(state: GameState, id: String): GameState {
        val b = state.business(id)
        if (b.owned == 0) return state
        val counted = state.copy(stats = state.stats.copy(taps = state.stats.taps + 1))
        if (b.manager || b.running) return counted
        return counted.copy(businesses = state.businesses + (id to b.copy(running = true, progress = 0.0)))
    }

    fun hireManager(state: GameState, id: String): GameState {
        val b = state.business(id)
        val cost = Businesses.get(id).managerCost
        if (b.owned == 0 || b.manager || state.cash < cost) return state
        return state.copy(
            cash = state.cash - cost,
            businesses = state.businesses + (id to b.copy(manager = true, running = true)),
            stats = state.stats.copy(managersHired = state.stats.managersHired + 1),
        )
    }

    fun buyUpgrade(state: GameState, upgradeId: String): GameState {
        val u = Upgrades.byId[upgradeId] ?: return state
        if (upgradeId in state.upgrades || state.cash < u.cost) return state
        return state.copy(cash = state.cash - u.cost, upgrades = state.upgrades + upgradeId)
    }

    /** Upgrades the player can see: not bought yet, and (for business upgrades) the business is owned. */
    fun visibleUpgrades(state: GameState) = Upgrades.all.filter { u ->
        u.id !in state.upgrades && (u.businessId == null || state.business(u.businessId).owned > 0)
    }
}
