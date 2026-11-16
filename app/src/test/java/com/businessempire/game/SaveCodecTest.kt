package com.businessempire.game

import com.businessempire.game.data.SaveCodec
import com.businessempire.game.domain.BusinessState
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.NumberStyle
import com.businessempire.game.domain.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCodecTest {

    @Test fun roundTripKeepsEverything() {
        var s = GameEngine.newGame(seed = 42L, nowMs = 1_000L)
        s = s.copy(
            cash = 12_345.67,
            businesses = mapOf("lemonade_stand" to BusinessState(owned = 30, manager = true, progress = 0.3, running = true)),
            upgrades = setOf("lemonade_stand_x3_0", "global_x2_0"),
            settings = Settings(sound = false, haptics = true, numberStyle = NumberStyle.SCIENTIFIC),
        )
        s = GameEngine.tick(s, 5.0)
        val decoded = SaveCodec.decode(SaveCodec.encode(s))
        assertEquals(s, decoded)
    }

    @Test fun encodedSaveCarriesTheVersion() {
        val text = SaveCodec.encode(GameState())
        assertTrue(text.contains("\"saveVersion\":${SaveCodec.CURRENT_VERSION}"))
    }

    @Test fun garbageReturnsNull() {
        assertNull(SaveCodec.decode("not json"))
    }

    @Test fun unknownFieldsAreIgnored() {
        val text = """{"saveVersion":${SaveCodec.CURRENT_VERSION},"state":{"cash":99.0,"futureField":true}}"""
        assertEquals(99.0, SaveCodec.decode(text)!!.cash, 0.0)
    }

    @Test fun migratesVersionOneSave() {
        val v1 = """
            {
              "saveVersion": 1,
              "cash": 2500.5,
              "owned": {"lemonade_stand": 12, "food_truck": 3},
              "managers": ["lemonade_stand"],
              "upgrades": ["lemonade_stand_x3_0"],
              "lifetimeEarnings": 9000.0,
              "lastSavedAtMs": 1700000000000
            }
        """.trimIndent()
        val s = SaveCodec.decode(v1)
        assertNotNull(s)
        s!!
        assertEquals(2500.5, s.cash, 0.0)
        assertEquals(12, s.business("lemonade_stand").owned)
        assertTrue(s.business("lemonade_stand").manager)
        assertEquals(3, s.business("food_truck").owned)
        assertTrue(!s.business("food_truck").manager)
        assertEquals(setOf("lemonade_stand_x3_0"), s.upgrades)
        assertEquals(9000.0, s.stats.lifetimeEarnings, 0.0)
        assertEquals(1700000000000L, s.lastSavedAtMs)
    }
}
