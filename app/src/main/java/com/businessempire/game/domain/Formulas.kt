package com.businessempire.game.domain

import com.businessempire.game.domain.content.BusinessDef
import com.businessempire.game.domain.content.Milestone
import com.businessempire.game.domain.content.MilestoneKind
import com.businessempire.game.domain.content.Milestones
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

/** Pure math helpers. No state, no randomness. */
object Formulas {
    /** Hard cap on a single "Max" purchase so the UI never freezes on absurd numbers. */
    const val MAX_BUY_CAP = 100_000

    /** Price of the next unit when [owned] are already owned. */
    fun unitCost(def: BusinessDef, owned: Int): Double = def.baseCost * def.costGrowth.pow(owned)

    /** Total price of buying [count] more units on top of [owned] (geometric series). */
    fun costForCount(def: BusinessDef, owned: Int, count: Int): Double {
        if (count <= 0) return 0.0
        val g = def.costGrowth
        return unitCost(def, owned) * (g.pow(count) - 1.0) / (g - 1.0)
    }

    /** How many units [cash] can buy right now (0 if not even one). */
    fun maxAffordable(def: BusinessDef, owned: Int, cash: Double): Int {
        val first = unitCost(def, owned)
        if (cash < first) return 0
        val g = def.costGrowth
        var n = floor(ln(cash * (g - 1.0) / first + 1.0) / ln(g)).toInt().coerceIn(1, MAX_BUY_CAP)
        // Guard against floating-point edge cases.
        while (n > 1 && costForCount(def, owned, n) > cash) n--
        while (n < MAX_BUY_CAP && costForCount(def, owned, n + 1) <= cash) n++
        return n
    }

    /** Units a [mode] purchase would buy. MAX returns at least 1 so the button can show a price. */
    fun countFor(mode: BuyMode, def: BusinessDef, owned: Int, cash: Double): Int =
        if (mode == BuyMode.MAX) maxAffordable(def, owned, cash).coerceAtLeast(1) else mode.count

    fun milestoneSpeed(owned: Int): Double = Milestones.all
        .filter { it.kind == MilestoneKind.SPEED && owned >= it.owned }
        .fold(1.0) { acc, m -> acc * m.factor }

    fun milestoneRevenue(owned: Int): Double = Milestones.all
        .filter { it.kind == MilestoneKind.REVENUE && owned >= it.owned }
        .fold(1.0) { acc, m -> acc * m.factor }

    fun nextMilestone(owned: Int): Milestone? = Milestones.all.firstOrNull { it.owned > owned }

    /** Milestones crossed when going from [before] to [after] units. */
    fun milestonesCrossed(before: Int, after: Int): List<Milestone> =
        Milestones.all.filter { it.owned in (before + 1)..after }

    /** What the player has paid in total for [owned] units. */
    fun totalSpent(def: BusinessDef, owned: Int): Double = costForCount(def, 0, owned)
}
