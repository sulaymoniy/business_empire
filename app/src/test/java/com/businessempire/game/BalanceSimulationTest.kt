package com.businessempire.game

import com.businessempire.game.domain.BuyMode
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.Formulas
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.NumberFormatter
import com.businessempire.game.domain.PrestigeEngine
import com.businessempire.game.domain.Profile
import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.TutorialStep
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Balance check: simulates an attentive, greedy player on the real engine and prints when each tier
 * unlocks. Run it after changing numbers in domain/content:
 *
 *   gradlew.bat testDebugUnitTest --tests "*BalanceSimulationTest*" -i
 *
 * The player:
 *  - taps every idle, unmanaged business every [TAP_INTERVAL] seconds,
 *  - picks the purchase with the lowest "time to afford + payback time" and saves up for it,
 *  - values a manager at [MANAGER_VALUE] of the business's income (attention + offline earnings).
 * Stocks, property, the bank, luxury and events are ignored, so real players can go a bit faster.
 */
class BalanceSimulationTest {

    data class Timeline(
        val tierUnlocked: Map<String, Double>,
        val firstManager: Double?,
        val prestigeAvailable: Double?,
        val netWorthAt: Map<Int, Double>,
    )

    private sealed interface Action {
        val cost: Double
        data class Unit(val id: String, val count: Int, override val cost: Double) : Action
        data class Manager(val id: String, override val cost: Double) : Action
        data class Upgrade(val id: String, override val cost: Double) : Action
    }

    private fun effectiveIncome(s: GameState): Double {
        val eco = Economy(s)
        return Businesses.all.sumOf { def ->
            val b = s.business(def.id)
            val inc = eco.businessIncomePerSecond(def.id)
            when {
                b.owned == 0 -> 0.0
                b.manager -> inc
                // Tapping every TAP_INTERVAL seconds keeps a business busy cycle/(cycle+wait) of the time.
                else -> inc * eco.cycleSeconds(def.id) / (eco.cycleSeconds(def.id) + TAP_INTERVAL / 2) * MANAGER_VALUE
            }
        }
    }

    private fun candidates(s: GameState): List<Action> = buildList {
        for (def in Businesses.all) {
            if (!GameEngine.isUnlocked(s, def.id)) continue
            val owned = s.business(def.id).owned
            add(Action.Unit(def.id, 1, Formulas.costForCount(def, owned, 1)))
            Formulas.nextMilestone(owned)?.let { m ->
                val n = m.owned - owned
                if (n in 2..100) add(Action.Unit(def.id, n, Formulas.costForCount(def, owned, n)))
            }
            if (owned > 0 && !s.business(def.id).manager) add(Action.Manager(def.id, def.managerCost))
        }
        GameEngine.visibleUpgrades(s).take(12).forEach { add(Action.Upgrade(it.id, it.cost)) }
    }

    private fun apply(s: GameState, a: Action): GameState = when (a) {
        is Action.Unit -> {
            val b = s.business(a.id)
            s.copy(cash = s.cash - a.cost, businesses = s.businesses + (a.id to b.copy(owned = b.owned + a.count)))
        }
        is Action.Manager -> GameEngine.hireManager(s, a.id)
        is Action.Upgrade -> GameEngine.buyUpgrade(s, a.id)
    }

    fun simulate(hours: Double, seed: Long = 1L): Timeline {
        var s = GameEngine.newGame(seed, 0L).copy(
            profile = Profile("Sim", "Sim Co"),
            tutorialStep = TutorialStep.DONE.ordinal,
        )
        val tiers = linkedMapOf<String, Double>()
        var firstManager: Double? = null
        var prestige: Double? = null
        val netWorthAt = linkedMapOf<Int, Double>()
        var t = 0.0
        val end = hours * 3600
        var nextTap = 0.0
        while (t < end) {
            if (t >= nextTap) {
                Businesses.all.forEach { def ->
                    val b = s.business(def.id)
                    if (b.owned > 0 && !b.manager && !b.running) s = GameEngine.tapBusiness(s, def.id)
                }
                nextTap = t + TAP_INTERVAL
            }
            // Buy as long as the best option is affordable; otherwise keep saving for it.
            var purchases = 0
            while (purchases < 50) {
                val income = effectiveIncome(s).coerceAtLeast(0.01)
                val best = candidates(s).minByOrNull { a ->
                    val gain = (effectiveIncome(apply(s.copy(cash = Double.MAX_VALUE), a)) - income).coerceAtLeast(1e-9)
                    val wait = ((a.cost - s.cash) / income).coerceAtLeast(0.0)
                    wait + a.cost / gain
                } ?: break
                if (best.cost > s.cash) break
                s = when (best) {
                    is Action.Unit -> (1..best.count).fold(s) { acc, _ -> GameEngine.buyBusiness(acc, best.id, BuyMode.ONE) }
                    else -> apply(s, best)
                }
                purchases++
            }
            s = GameEngine.tick(s, STEP)
            t += STEP
            Businesses.all.forEach { if (s.business(it.id).owned > 0 && it.id !in tiers) tiers[it.id] = t }
            if (firstManager == null && s.businesses.values.any { it.manager }) firstManager = t
            if (prestige == null && PrestigeEngine.canPrestige(s)) prestige = t
            // Events need a human; the simulator always declines them.
            if (s.event.pending != null) s = s.copy(event = s.event.copy(pending = null))
            val hour = (t / 3600).toInt()
            if (t % 3600 < STEP && hour > 0) netWorthAt[hour] = Economy(s).netWorth
        }
        return Timeline(tiers, firstManager, prestige, netWorthAt)
    }

    @Test fun pacingIsInTheTargetRange() {
        val tl = simulate(hours = 7.0)
        println("=== Balance simulation (greedy active player) ===")
        Businesses.all.forEachIndexed { i, def ->
            println("Tier ${i + 1} %-18s %s".format(def.id, tl.tierUnlocked[def.id]?.let(::fmt) ?: "not reached"))
        }
        println("First manager          ${tl.firstManager?.let(::fmt)}")
        println("Prestige available     ${tl.prestigeAvailable?.let(::fmt) ?: "not reached"}")
        tl.netWorthAt.forEach { (h, nw) -> println("Net worth after ${h}h     $" + NumberFormatter.short(nw)) }

        val ids = Businesses.all.map { it.id }
        // Targets from the design brief, with some slack for the simulator's simplifications.
        assertTrue("tier 2 within ~1 min", tl.tierUnlocked.getValue(ids[1]) <= 90)
        assertTrue("first manager within ~3 min", tl.firstManager!! in 60.0..300.0)
        assertTrue("tier 6 around 1 h", tl.tierUnlocked.getValue(ids[5]) in 1_800.0..5_400.0)
        assertTrue("first prestige in 3-6 h", tl.prestigeAvailable != null && tl.prestigeAvailable in 2.5 * 3600..6.5 * 3600)
    }

    private fun fmt(seconds: Double): String {
        val s = seconds.toLong()
        return "%dh %02dm %02ds".format(s / 3600, (s % 3600) / 60, s % 60)
    }

    companion object {
        const val STEP = 1.0
        const val TAP_INTERVAL = 2.0
        const val MANAGER_VALUE = 0.5
    }
}
