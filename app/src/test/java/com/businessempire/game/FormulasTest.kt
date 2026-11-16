package com.businessempire.game

import com.businessempire.game.domain.BuyMode
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.Formulas
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.BusinessState
import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.BusinessDef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class FormulasTest {
    private val def = BusinessDef("test", baseCost = 10.0, costGrowth = 1.1, baseRevenue = 1.0, cycleSeconds = 1.0, managerCost = 100.0)

    @Test fun unitCostGrowsGeometrically() {
        assertEquals(10.0, Formulas.unitCost(def, 0), 1e-9)
        assertEquals(10.0 * 1.1.pow(5), Formulas.unitCost(def, 5), 1e-9)
    }

    @Test fun costForCountMatchesSumOfUnitCosts() {
        val owned = 7
        val expected = (0 until 13).sumOf { Formulas.unitCost(def, owned + it) }
        assertEquals(expected, Formulas.costForCount(def, owned, 13), 1e-6)
        assertEquals(0.0, Formulas.costForCount(def, owned, 0), 0.0)
    }

    @Test fun maxAffordableIsTheLargestCountThatFits() {
        for (cash in listOf(9.99, 10.0, 21.0, 1_000.0, 123_456.0, 1e12)) {
            val n = Formulas.maxAffordable(def, 3, cash)
            if (n == 0) {
                assertTrue(Formulas.unitCost(def, 3) > cash)
            } else {
                assertTrue("n=$n fits", Formulas.costForCount(def, 3, n) <= cash)
                assertTrue("n+1 does not fit", Formulas.costForCount(def, 3, n + 1) > cash)
            }
        }
    }

    @Test fun maxModeAlwaysShowsAtLeastOneUnit() {
        assertEquals(1, Formulas.countFor(BuyMode.MAX, def, 0, 0.0))
        assertEquals(10, Formulas.countFor(BuyMode.TEN, def, 0, 0.0))
    }

    @Test fun milestoneMultipliersStack() {
        assertEquals(1.0, Formulas.milestoneSpeed(24), 0.0)
        assertEquals(2.0, Formulas.milestoneSpeed(25), 0.0)
        assertEquals(8.0, Formulas.milestoneSpeed(100), 0.0)
        assertEquals(8.0, Formulas.milestoneSpeed(500), 0.0)
        assertEquals(1.0, Formulas.milestoneRevenue(199), 0.0)
        assertEquals(2.0, Formulas.milestoneRevenue(200), 0.0)
        assertEquals(8.0 * 3.0, Formulas.milestoneRevenue(500), 0.0)
        assertEquals(listOf(25, 50), Formulas.milestonesCrossed(20, 60).map { it.owned })
    }

    @Test fun milestonesAffectEconomy() {
        val id = Businesses.all.first().id
        val s24 = GameState(businesses = mapOf(id to BusinessState(owned = 24)))
        val s25 = GameState(businesses = mapOf(id to BusinessState(owned = 25)))
        assertEquals(Economy(s24).cycleSeconds(id) / 2.0, Economy(s25).cycleSeconds(id), 1e-9)
    }

    @Test fun buyingSpendsExactlyTheQuotedCost() {
        val id = Businesses.all.first().id
        val start = GameState(cash = 1_000.0)
        val (count, cost) = GameEngine.buyCost(start, id, BuyMode.MAX)
        val after = GameEngine.buyBusiness(start, id, BuyMode.MAX)
        assertEquals(count, after.business(id).owned)
        assertEquals(1_000.0 - cost, after.cash, 1e-6)
        assertTrue(after.cash < Formulas.unitCost(Businesses.get(id), count))
    }

    @Test fun lockedTierCannotBeBought() {
        val second = Businesses.all[1].id
        val s = GameState(cash = 1e9)
        assertEquals(s, GameEngine.buyBusiness(s, second, BuyMode.ONE))
    }
}
