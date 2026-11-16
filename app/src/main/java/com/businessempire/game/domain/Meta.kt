package com.businessempire.game.domain

import com.businessempire.game.domain.content.AchievementDef
import com.businessempire.game.domain.content.AchievementType
import com.businessempire.game.domain.content.Achievements
import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.DailyRules
import com.businessempire.game.domain.content.EventOutcome
import com.businessempire.game.domain.content.EventRules
import com.businessempire.game.domain.content.EventType
import com.businessempire.game.domain.content.PrestigeRules
import com.businessempire.game.domain.content.Stocks
import com.businessempire.game.domain.content.TutorialStep
import kotlinx.serialization.Serializable
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** A temporary revenue modifier from an event. [businessId] null = all businesses. */
@Serializable
data class TempEffect(
    val factor: Double,
    val remainingSeconds: Double,
    val businessId: String? = null,
)

@Serializable
data class PendingEvent(
    val type: EventType,
    /** Cash offered or cost to accept, fixed when the event fires. */
    val amount: Double,
    val businessId: String? = null,
    val stockId: String? = null,
)

@Serializable
data class EventState(
    val pending: PendingEvent? = null,
    /** Seconds of active play until the next event may fire. */
    val cooldown: Double = EventRules.FIRST_EVENT_AFTER,
)

@Serializable
data class DailyState(
    /** Local calendar day (epoch day) of the last claim. */
    val lastClaimDay: Long = -1,
    val streak: Int = 0,
)

@Serializable
data class Profile(
    val ceoName: String = "",
    val companyName: String = "",
) {
    val isSet: Boolean get() = ceoName.isNotBlank() && companyName.isNotBlank()
}

data class EventResult(val state: GameState, val outcome: EventOutcome, val amount: Double)

// -------------------------------------------------------------------------------------------------

/** "Sell the Empire": reset most progress for permanent Legacy Points. */
object PrestigeEngine {
    const val MAX_RUNS_KEPT = 50

    fun totalPointsFor(lifetimeEarnings: Double): Double =
        floor(sqrt(max(lifetimeEarnings, 0.0) / PrestigeRules.POINT_DIVISOR))

    /** Points the player would gain by prestiging now. */
    fun pointsAvailable(state: GameState): Double =
        max(0.0, totalPointsFor(state.stats.lifetimeEarnings) - state.legacyPoints)

    fun canPrestige(state: GameState): Boolean =
        Economy(state).netWorth >= PrestigeRules.UNLOCK_NET_WORTH && pointsAvailable(state) >= 1.0

    /**
     * Resets businesses, cash, upgrades, stocks, properties and the bank.
     * Keeps achievements, luxury items, Legacy Points, statistics, profile, settings and the daily streak.
     */
    fun prestige(state: GameState): GameState {
        if (!canPrestige(state)) return state
        val gained = pointsAvailable(state)
        val record = RunRecord(
            run = state.stats.prestigeCount + 1,
            earnings = state.runEarnings,
            pointsGained = gained,
            durationSeconds = state.clock - state.runStartedAt,
        )
        return state.copy(
            cash = com.businessempire.game.domain.GameRules.START_CASH,
            runEarnings = 0.0,
            businesses = emptyMap(),
            upgrades = emptySet(),
            market = MarketEngine.clearHoldings(state.market),
            properties = emptyMap(),
            bank = BankState(),
            effects = emptyList(),
            event = EventState(pending = null, cooldown = EventRules.MIN_COOLDOWN),
            legacyPoints = state.legacyPoints + gained,
            tutorialStep = TutorialStep.DONE.ordinal,
            runHistory = (state.runHistory + record).takeLast(MAX_RUNS_KEPT),
            runStartedAt = state.clock,
            stats = state.stats.copy(prestigeCount = state.stats.prestigeCount + 1),
        )
    }
}

/** Unlocks achievements and pays their rewards. */
object AchievementEngine {
    fun progress(state: GameState, def: AchievementDef): Double = when (def.type) {
        AchievementType.OWN_BUSINESS -> state.business(def.businessId!!).owned.toDouble()
        AchievementType.LIFETIME_EARNINGS -> state.stats.lifetimeEarnings
        AchievementType.NET_WORTH -> state.stats.bestNetWorth
        AchievementType.TAPS -> state.stats.taps.toDouble()
        AchievementType.MANAGERS -> state.businesses.values.count { it.manager }.toDouble()
        AchievementType.PROPERTIES -> state.properties.size.toDouble()
        AchievementType.LUXURY -> state.luxury.size.toDouble()
        AchievementType.STOCK_TRADES -> state.stats.stockTrades.toDouble()
        AchievementType.PROFITABLE_SELLS -> state.stats.profitableSells.toDouble()
        AchievementType.PRESTIGES -> state.stats.prestigeCount.toDouble()
        AchievementType.DAILY_STREAK -> state.stats.bestStreak.toDouble()
        AchievementType.LOANS_REPAID -> state.stats.loansRepaid.toDouble()
        AchievementType.EVENTS -> state.stats.eventsResolved.toDouble()
    }

    fun cashReward(state: GameState, def: AchievementDef): Double =
        if (def.rewardCashSeconds <= 0) 0.0
        else max(Achievements.MIN_CASH_REWARD, Economy(state).rewardIncomePerSecond * def.rewardCashSeconds)

    /** Sum of permanent revenue bonuses (percent) from unlocked achievements. */
    fun bonusPercent(state: GameState): Double = state.achievements.sumOf { Achievements.byId[it]?.rewardPercent ?: 0.0 }

    /** Unlocks everything newly reached. Returns the new state and the ids unlocked. */
    fun check(state: GameState): Pair<GameState, List<String>> {
        var next = state
        val unlocked = mutableListOf<String>()
        for (def in Achievements.all) {
            if (def.id in next.achievements) continue
            if (progress(next, def) < def.target) continue
            val cash = cashReward(next, def)
            next = next.copy(cash = next.cash + cash, achievements = next.achievements + def.id)
            unlocked += def.id
        }
        return next to unlocked
    }
}

/** Random choice events. */
object EventEngine {
    private fun income(state: GameState) = max(Economy(state).rewardIncomePerSecond, 1.0)

    /** Counts down and maybe fires a new event. */
    fun advance(state: GameState, dt: Double, rng: Rng): GameState {
        if (state.event.pending != null) return state
        if (state.businesses.values.none { it.owned > 0 }) return state
        val cooldown = state.event.cooldown - dt
        if (cooldown > 0) return state.copy(event = state.event.copy(cooldown = cooldown))
        val event = create(state, rng)
        return state.copy(
            event = EventState(
                pending = event,
                cooldown = rng.range(EventRules.MIN_COOLDOWN, EventRules.MAX_COOLDOWN),
            ),
        )
    }

    fun create(state: GameState, rng: Rng): PendingEvent? {
        val owned = Businesses.all.filter { state.business(it.id).owned > 0 }.map { it.id }
        if (owned.isEmpty()) return null
        val bigOnes = owned.filter { state.business(it).owned >= EventRules.RIVAL_MIN_OWNED }
        val types = EventType.entries.filter { it != EventType.RIVAL_OFFER || bigOnes.isNotEmpty() }
        val type = rng.pick(types)
        val inc = income(state)
        fun amount(v: Double) = max(EventRules.MIN_AMOUNT, v)
        return when (type) {
            EventType.RIVAL_OFFER -> PendingEvent(type, amount(inc * EventRules.RIVAL_OFFER_SECONDS), businessId = rng.pick(bigOnes))
            EventType.VIRAL_POST -> PendingEvent(type, amount(inc * EventRules.VIRAL_COST_SECONDS))
            EventType.SUPPLY_SHORTAGE -> PendingEvent(type, amount(inc * EventRules.SHORTAGE_COST_SECONDS), businessId = rng.pick(owned))
            EventType.TAX_AUDIT -> PendingEvent(type, amount(state.cash * EventRules.AUDIT_SETTLE_SHARE))
            EventType.MARKET_TIP -> PendingEvent(type, amount(state.cash * EventRules.TIP_INVEST_SHARE), stockId = rng.pick(Stocks.all).id)
            EventType.CHARITY_GALA -> PendingEvent(type, amount(state.cash * EventRules.GALA_COST_SHARE))
            EventType.STAFF_STRIKE -> PendingEvent(type, amount(inc * EventRules.STRIKE_COST_SECONDS), businessId = rng.pick(owned))
            EventType.INVESTOR_PITCH -> PendingEvent(type, amount(inc * EventRules.PITCH_CASH_SECONDS))
        }
    }

    /** True if accepting costs cash the player doesn't have. */
    fun acceptCost(event: PendingEvent): Double = when (event.type) {
        EventType.RIVAL_OFFER, EventType.INVESTOR_PITCH -> 0.0
        else -> event.amount
    }

    fun canAccept(state: GameState, event: PendingEvent): Boolean = state.cash >= acceptCost(event)

    /** Applies the player's choice. Returns null if there is no pending event or accept is unaffordable. */
    fun resolve(state: GameState, accept: Boolean, rng: Rng): EventResult? {
        val e = state.event.pending ?: return null
        if (accept && !canAccept(state, e)) return null
        var s = state.copy(
            event = state.event.copy(pending = null),
            stats = state.stats.copy(eventsResolved = state.stats.eventsResolved + 1),
        )
        fun effect(factor: Double, seconds: Double, businessId: String? = null) {
            s = s.copy(effects = s.effects + TempEffect(factor, seconds, businessId))
        }
        fun pay(amount: Double) {
            s = s.copy(cash = max(0.0, s.cash - amount))
        }
        val outcome: EventOutcome
        var amount = e.amount
        when (e.type) {
            EventType.RIVAL_OFFER -> if (accept) {
                val id = e.businessId!!
                val b = s.business(id)
                val sold = max(1, floor(b.owned * EventRules.RIVAL_UNITS_SOLD).toInt())
                s = s.copy(businesses = s.businesses + (id to b.copy(owned = max(0, b.owned - sold))))
                s = GameEngine.earn(s, e.amount)
                outcome = EventOutcome.RIVAL_SOLD
            } else {
                effect(EventRules.RIVAL_MORALE_FACTOR, EventRules.RIVAL_MORALE_SECONDS, e.businessId)
                outcome = EventOutcome.RIVAL_MORALE
            }
            EventType.VIRAL_POST -> if (accept) {
                pay(e.amount)
                effect(EventRules.VIRAL_FACTOR, EventRules.VIRAL_DURATION)
                outcome = EventOutcome.VIRAL_BOOST
            } else outcome = EventOutcome.DECLINED
            EventType.SUPPLY_SHORTAGE -> if (accept) {
                pay(e.amount)
                outcome = EventOutcome.SHORTAGE_AVOIDED
            } else {
                effect(EventRules.SHORTAGE_FACTOR, EventRules.SHORTAGE_DURATION, e.businessId)
                outcome = EventOutcome.SHORTAGE_HIT
            }
            EventType.TAX_AUDIT -> if (accept) {
                pay(e.amount)
                outcome = EventOutcome.AUDIT_SETTLED
            } else if (rng.chance(EventRules.AUDIT_FINE_CHANCE)) {
                amount = s.cash * EventRules.AUDIT_FINE_SHARE
                pay(amount)
                outcome = EventOutcome.AUDIT_FINED
            } else outcome = EventOutcome.AUDIT_CLEARED
            EventType.MARKET_TIP -> if (accept) {
                val id = e.stockId!!
                val price = s.market.stocks[id]?.price ?: 1.0
                val qty = floor(min(e.amount, s.cash) / price).toLong()
                s = MarketEngine.buy(s, id, qty)
                val win = rng.chance(EventRules.TIP_WIN_CHANCE)
                s = s.copy(market = MarketEngine.shock(s.market, id, if (win) EventRules.TIP_WIN_CHANGE else EventRules.TIP_LOSS_CHANGE))
                outcome = if (win) EventOutcome.TIP_WIN else EventOutcome.TIP_LOSS
            } else outcome = EventOutcome.DECLINED
            EventType.CHARITY_GALA -> if (accept) {
                pay(e.amount)
                effect(EventRules.GALA_FACTOR, EventRules.GALA_DURATION)
                outcome = EventOutcome.GALA_BOOST
            } else outcome = EventOutcome.DECLINED
            EventType.STAFF_STRIKE -> if (accept) {
                pay(e.amount)
                outcome = EventOutcome.STRIKE_SETTLED
            } else {
                effect(EventRules.STRIKE_FACTOR, EventRules.STRIKE_DURATION, e.businessId)
                outcome = EventOutcome.STRIKE_HIT
            }
            EventType.INVESTOR_PITCH -> if (accept) {
                s = GameEngine.earn(s, e.amount)
                effect(EventRules.PITCH_FACTOR, EventRules.PITCH_DURATION)
                outcome = EventOutcome.PITCH_TAKEN
            } else outcome = EventOutcome.DECLINED
        }
        return EventResult(s, outcome, amount)
    }

    fun advanceEffects(effects: List<TempEffect>, dt: Double): List<TempEffect> =
        if (effects.isEmpty()) effects
        else effects.map { it.copy(remainingSeconds = it.remainingSeconds - dt) }.filter { it.remainingSeconds > 0 }
}

/** Free daily cash, bigger with a streak of consecutive days. */
object DailyEngine {
    fun canClaim(state: GameState, today: Long): Boolean = state.daily.lastClaimDay != today

    fun nextStreak(state: GameState, today: Long): Int =
        if (state.daily.lastClaimDay == today - 1) state.daily.streak + 1 else 1

    fun reward(state: GameState, streak: Int): Double {
        val days = min(streak, DailyRules.MAX_STREAK_BONUS_DAYS)
        val scaled = Economy(state).rewardIncomePerSecond * DailyRules.BASE_INCOME_SECONDS * (1.0 + DailyRules.STREAK_STEP * (days - 1))
        return max(DailyRules.MIN_REWARD_PER_DAY * days, scaled)
    }

    fun claim(state: GameState, today: Long): Pair<GameState, Double>? {
        if (!canClaim(state, today)) return null
        val streak = nextStreak(state, today)
        val cash = reward(state, streak)
        val next = GameEngine.earn(state, cash).let {
            it.copy(
                daily = DailyState(lastClaimDay = today, streak = streak),
                stats = it.stats.copy(bestStreak = max(it.stats.bestStreak, streak)),
            )
        }
        return next to cash
    }
}

/** First-run coach marks. Steps complete automatically as the player does them. */
object TutorialEngine {
    fun step(state: GameState): TutorialStep = TutorialStep.entries.getOrElse(state.tutorialStep) { TutorialStep.DONE }

    fun advance(state: GameState): GameState {
        val first = Businesses.all.first().id
        val done = when (step(state)) {
            TutorialStep.BUY_STAND -> state.business(first).owned >= 1
            TutorialStep.TAP_STAND -> state.stats.taps >= 1 || state.business(first).manager
            TutorialStep.BUY_TEN -> state.business(first).owned >= 10
            TutorialStep.HIRE_MANAGER -> state.businesses.values.any { it.manager }
            TutorialStep.VISIT_INVEST, TutorialStep.DONE -> false
        }
        return if (done) state.copy(tutorialStep = state.tutorialStep + 1) else state
    }

    fun visitedInvest(state: GameState): GameState =
        if (step(state) == TutorialStep.VISIT_INVEST) state.copy(tutorialStep = TutorialStep.DONE.ordinal) else state

    fun skip(state: GameState): GameState = state.copy(tutorialStep = TutorialStep.DONE.ordinal)
}
