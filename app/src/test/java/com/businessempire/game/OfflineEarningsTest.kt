package com.businessempire.game

import com.businessempire.game.domain.BusinessState
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.content.Upgrades
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineEarningsTest {
    private val id = "lemonade_stand"
    private val managed = GameState(cash = 0.0, businesses = mapOf(id to BusinessState(owned = 10, manager = true, running = true)))

    @Test fun managedBusinessesEarnForElapsedTime() {
        val perSecond = Economy(managed).passiveIncomePerSecond
        val (after, report) = GameEngine.applyOffline(managed, 600.0)
        assertEquals(600.0, report.countedSeconds, 0.0)
        assertEquals(perSecond * 600.0, report.earned, 1e-6)
        assertEquals(report.earned, after.cash, 1e-6)
        assertEquals(report.earned, after.stats.lifetimeEarnings, 1e-6)
    }

    @Test fun offlineTimeIsCappedAtFourHoursByDefault() {
        val perSecond = Economy(managed).passiveIncomePerSecond
        val (_, report) = GameEngine.applyOffline(managed, 24 * 3600.0)
        assertEquals(4.0, report.capHours, 0.0)
        assertEquals(4 * 3600.0, report.countedSeconds, 0.0)
        assertEquals(perSecond * 4 * 3600.0, report.earned, 1e-6)
    }

    @Test fun capUpgradesRaiseTheCap() {
        val offlineUpgrade = Upgrades.all.first { it.id == "offline_cap_0" }
        val s = managed.copy(upgrades = setOf(offlineUpgrade.id))
        val (_, report) = GameEngine.applyOffline(s, 24 * 3600.0)
        assertEquals(6.0, report.capHours, 0.0)
        assertEquals(6 * 3600.0, report.countedSeconds, 0.0)
    }

    @Test fun unmanagedBusinessFinishesAtMostOneCycle() {
        val s = GameState(cash = 0.0, businesses = mapOf(id to BusinessState(owned = 10, running = true, progress = 0.5)))
        val perCycle = Economy(s).revenuePerCycle(id)
        val (after, report) = GameEngine.applyOffline(s, 3600.0)
        assertEquals(perCycle, report.earned, 1e-9)
        assertTrue(!after.business(id).running)
    }

    @Test fun idleUnmanagedBusinessEarnsNothing() {
        val s = GameState(cash = 0.0, businesses = mapOf(id to BusinessState(owned = 10)))
        val (_, report) = GameEngine.applyOffline(s, 3600.0)
        assertEquals(0.0, report.earned, 0.0)
    }
}
