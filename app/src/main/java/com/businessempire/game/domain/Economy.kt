package com.businessempire.game.domain

import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.PrestigeRules
import com.businessempire.game.domain.content.UpgradeKind
import com.businessempire.game.domain.content.Upgrades

/**
 * Derived numbers for one [GameState]: multipliers, revenue per cycle, cycle time, income per second.
 * Cheap to build; create a fresh one whenever the state changes.
 */
class Economy(private val state: GameState) {

    /** Multiplier applied to every business. */
    val upgradeMultiplier: Double = run {
        var m = 1.0
        for (id in state.upgrades) {
            val u = Upgrades.byId[id] ?: continue
            if (u.kind == UpgradeKind.GLOBAL_REVENUE) m *= u.factor
        }
        m
    }

    /** Permanent bonus from luxury items: +1% per bonus point. */
    val luxuryMultiplier: Double = 1.0 + LuxuryEngine.bonusPercent(state) / 100.0

    /** Permanent bonus from Legacy Points (prestige). */
    val legacyMultiplier: Double = 1.0 + PrestigeRules.BONUS_PER_POINT * state.legacyPoints

    /** Permanent bonus from achievement rewards. */
    val achievementMultiplier: Double = 1.0 + AchievementEngine.bonusPercent(state) / 100.0

    /** Temporary event effects that apply to every business. */
    val eventMultiplier: Double = state.effects.filter { it.businessId == null }.fold(1.0) { acc, e -> acc * e.factor }

    // MONETIZATION: a purchased "2x income forever" would multiply in here (read from GameState once
    // the store is live; see MONETIZATION_TODO.md).
    val globalMultiplier: Double =
        upgradeMultiplier * luxuryMultiplier * legacyMultiplier * achievementMultiplier * eventMultiplier

    val offlineCapHours: Double = Upgrades.BASE_OFFLINE_CAP_HOURS +
        state.upgrades.mapNotNull { Upgrades.byId[it] }.filter { it.kind == UpgradeKind.OFFLINE_CAP }.sumOf { it.factor }

    private val businessUpgradeMultipliers: Map<String, Double> = buildMap {
        for (id in state.upgrades) {
            val u = Upgrades.byId[id] ?: continue
            if (u.kind == UpgradeKind.BUSINESS_REVENUE && u.businessId != null) {
                put(u.businessId, (get(u.businessId) ?: 1.0) * u.factor)
            }
        }
    }

    fun revenueMultiplier(id: String): Double {
        val owned = state.business(id).owned
        val eventFactor = state.effects.filter { it.businessId == id }.fold(1.0) { acc, e -> acc * e.factor }
        return globalMultiplier * (businessUpgradeMultipliers[id] ?: 1.0) * Formulas.milestoneRevenue(owned) * eventFactor
    }

    fun revenuePerCycle(id: String): Double {
        val b = state.business(id)
        if (b.owned == 0) return 0.0
        return Businesses.get(id).baseRevenue * b.owned * revenueMultiplier(id)
    }

    fun cycleSeconds(id: String): Double =
        Businesses.get(id).cycleSeconds / Formulas.milestoneSpeed(state.business(id).owned)

    /** Income per second of one business if it runs non-stop. */
    fun businessIncomePerSecond(id: String): Double = revenuePerCycle(id) / cycleSeconds(id)

    /** Income per second from businesses that run on their own (have a manager). */
    val passiveIncomePerSecond: Double = Businesses.all
        .filter { state.business(it.id).manager }
        .sumOf { businessIncomePerSecond(it.id) }

    /** Income per second if every owned business ran non-stop. Used to scale rewards. */
    val potentialIncomePerSecond: Double = Businesses.all.sumOf { businessIncomePerSecond(it.id) }

    /** Total value of the businesses for net worth: half of what was paid for them. */
    val businessValue: Double = Businesses.all.sumOf {
        Formulas.totalSpent(it, state.business(it.id).owned) * BUSINESS_RESALE_FRACTION
    }

    val propertyValue: Double = PropertyEngine.totalValue(state)

    val portfolioValue: Double = MarketEngine.portfolioValue(state.market)

    val rentPerSecond: Double = PropertyEngine.totalRentPerSecond(state)

    val luxuryValue: Double = LuxuryEngine.value(state)

    /** cash + businesses + properties + stocks + luxury + savings - loans */
    val netWorth: Double = state.cash + businessValue + propertyValue + portfolioValue + luxuryValue +
        state.bank.savings - state.bank.loan

    /** Everything that flows in without tapping. Shown in the header. */
    val totalPassiveIncomePerSecond: Double get() = passiveIncomePerSecond + rentPerSecond

    /**
     * Income used to scale rewards and event amounts: what the player really earns on autopilot,
     * with a small floor so early players still get something meaningful.
     */
    val rewardIncomePerSecond: Double
        get() = maxOf(totalPassiveIncomePerSecond, potentialIncomePerSecond * REWARD_POTENTIAL_FLOOR)

    companion object {
        const val BUSINESS_RESALE_FRACTION = 0.5
        const val REWARD_POTENTIAL_FLOOR = 0.1
    }
}
