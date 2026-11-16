package com.businessempire.game

import com.businessempire.game.data.RichListCodec
import com.businessempire.game.domain.BusinessState
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.PrestigeEngine
import com.businessempire.game.domain.RichList
import com.businessempire.game.domain.RichListEngine
import com.businessempire.game.domain.RichPerson
import com.businessempire.game.domain.Stats
import com.businessempire.game.domain.content.Comparisons
import com.businessempire.game.domain.content.FictionalTycoons
import com.businessempire.game.domain.content.PrestigeRules
import com.businessempire.game.domain.content.WealthMilestones
import com.businessempire.game.online.Board
import com.businessempire.game.online.LocalLeaderboardService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RichListTest {
    private val list = RichList(
        people = listOf(
            RichPerson("Big Fish", 1e9, "Investor", real = true),
            RichPerson("Mid Fish", 1e7, "Actor", real = true),
            RichPerson("Small Fish", 1e6, "Singer", real = true),
        ),
        real = true,
        asOf = "test",
    )

    @Test fun bundledJsonParsesAndFollowsTheRules() {
        // Unit tests run with the module (app/) as working directory.
        val text = File("src/main/assets/rich_list.json").readText()
        val parsed = RichListCodec.parse(text)
        assertNotNull(parsed)
        parsed!!
        assertTrue(parsed.real)
        assertNotNull(parsed.asOf)
        assertTrue("about 60 people", parsed.people.size in 50..80)
        assertTrue("sorted richest first", parsed.people.zipWithNext().all { (a, b) -> a.netWorth >= b.netWorth })
        assertTrue("descriptions are short, one line", parsed.people.all { it.description!!.isNotBlank() && it.description.length <= 60 && '\n' !in it.description })
        assertTrue("photo names follow person_<slug>", parsed.people.all { it.imageName == RichListEngine.photoName(it.name) })
        assertEquals("names are unique", parsed.people.size, parsed.people.map { it.name }.toSet().size)
        assertTrue(text.contains("VERIFY"))
        assertTrue("spans millionaires to the top", parsed.people.last().netWorth <= 1e7 && parsed.people.first().netWorth >= 1e11)
    }

    @Test fun brokenJsonFallsBack() {
        assertNull(RichListCodec.parse("{not json"))
        assertNull(RichListCodec.parse("""{"asOf":"x","people":[]}"""))
        val fallback = RichListEngine.fictional
        assertFalse(fallback.real)
        assertEquals(FictionalTycoons.all.size, fallback.people.size)
        assertTrue("fictional tycoons use initials", fallback.people.all { it.imageName == null })
    }

    @Test fun rankAndNextToBeat() {
        assertEquals(4, RichListEngine.playerRank(list, 0.0))
        assertEquals("Small Fish", RichListEngine.nextToBeat(list, 0.0)!!.name)
        assertEquals(2, RichListEngine.playerRank(list, 5e8))
        assertEquals("Big Fish", RichListEngine.nextToBeat(list, 5e8)!!.name)
        assertEquals(1, RichListEngine.playerRank(list, 2e9))
        assertNull(RichListEngine.nextToBeat(list, 2e9))
    }

    @Test fun passingPeopleIsReportedOnce() {
        assertEquals(listOf("Mid Fish", "Small Fish"), RichListEngine.passed(list, 5e5, 2e7).map { it.name })
        assertTrue(RichListEngine.passed(list, 2e7, 2e7).isEmpty())
    }

    @Test fun photoNames() {
        assertEquals("person_carlos_slim_helu", RichListEngine.photoName("Carlos Slim Helú"))
        assertEquals("person_francoise_bettencourt_meyers", RichListEngine.photoName("Françoise Bettencourt Meyers"))
        assertEquals("person_shaquille_o_neal", RichListEngine.photoName("Shaquille O'Neal"))
        assertEquals("person_jay_z", RichListEngine.photoName("Jay-Z"))
        assertEquals("person_beyonce", RichListEngine.photoName("Beyoncé"))
    }

    @Test fun initials() {
        assertEquals("AP", RichListEngine.initials("Ada Pemberton"))
        assertEquals("CH", RichListEngine.initials("Carlos Slim Helú"))
        assertEquals("M", RichListEngine.initials("MrBeast"))
        assertEquals("NA", RichListEngine.initials("Noor Al-Rashidi"))
    }

    @Test fun wealthMilestonesAreOrdered() {
        assertTrue(WealthMilestones.all.zipWithNext().all { (a, b) -> a.netWorth < b.netWorth })
        assertEquals("millionaire", WealthMilestones.current(1.05e6)!!.id)
        assertEquals("top_1_percent", WealthMilestones.next(1.05e6)!!.id)
        assertEquals(listOf("millionaire", "top_1_percent"), WealthMilestones.crossed(5e5, 2e6).map { it.id })
    }

    @Test fun comparisonsAreReadable() {
        val c = Comparisons.readable(1e9)
        assertTrue(c.isNotEmpty())
        assertTrue(c.all { it.second in 1..1_000_000 })
        assertTrue(Comparisons.readable(10.0).isEmpty())
    }

    @Test fun prestigeRecordsRunsForTheLocalLeaderboard() = runBlocking {
        val rich = GameEngine.newGame(1L, 0L).copy(
            cash = PrestigeRules.UNLOCK_NET_WORTH,
            runEarnings = 4e12,
            clock = 5_000.0,
            runStartedAt = 1_000.0,
            businesses = mapOf("lemonade_stand" to BusinessState(owned = 10)),
            stats = Stats(lifetimeEarnings = 4e12),
        )
        val after = PrestigeEngine.prestige(rich)
        assertEquals(1, after.runHistory.size)
        assertEquals(4_000.0, after.runHistory[0].durationSeconds, 0.0)
        assertEquals(4e12, after.runHistory[0].earnings, 0.0)
        assertEquals(5_000.0, after.runStartedAt, 0.0)
        val board = LocalLeaderboardService { after }
        assertFalse(board.isOnline)
        assertEquals(1, board.entries(Board.PRESTIGE_RUNS).size)
        assertEquals(after.stats.bestNetWorth, board.entries(Board.BEST_NET_WORTH).single().value, 0.0)
    }
}
