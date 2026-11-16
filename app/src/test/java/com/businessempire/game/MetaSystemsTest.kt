package com.businessempire.game

import com.businessempire.game.data.SaveCodec
import com.businessempire.game.domain.AchievementEngine
import com.businessempire.game.domain.BankState
import com.businessempire.game.domain.BusinessState
import com.businessempire.game.domain.DailyEngine
import com.businessempire.game.domain.DailyState
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.EventEngine
import com.businessempire.game.domain.EventState
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.PendingEvent
import com.businessempire.game.domain.PrestigeEngine
import com.businessempire.game.domain.PropertyState
import com.businessempire.game.domain.Rng
import com.businessempire.game.domain.Stats
import com.businessempire.game.domain.TutorialEngine
import com.businessempire.game.domain.content.Achievements
import com.businessempire.game.domain.content.EventOutcome
import com.businessempire.game.domain.content.EventRules
import com.businessempire.game.domain.content.EventType
import com.businessempire.game.domain.content.PrestigeRules
import com.businessempire.game.domain.content.TutorialStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.floor
import kotlin.math.sqrt

class MetaSystemsTest {
    private val lemon = "lemonade_stand"

    // --- Prestige ----------------------------------------------------------------------------------

    @Test fun legacyPointsFollowASquareRootCurve() {
        assertEquals(0.0, PrestigeEngine.totalPointsFor(0.0), 0.0)
        assertEquals(1.0, PrestigeEngine.totalPointsFor(PrestigeRules.POINT_DIVISOR), 0.0)
        assertEquals(10.0, PrestigeEngine.totalPointsFor(PrestigeRules.POINT_DIVISOR * 100), 0.0)
        assertEquals(floor(sqrt(12_345.0)), PrestigeEngine.totalPointsFor(PrestigeRules.POINT_DIVISOR * 12_345), 0.0)
    }

    @Test fun pointsAlreadyClaimedAreNotCountedTwice() {
        val s = GameState(stats = Stats(lifetimeEarnings = PrestigeRules.POINT_DIVISOR * 400), legacyPoints = 15.0)
        assertEquals(5.0, PrestigeEngine.pointsAvailable(s), 0.0)
    }

    @Test fun prestigeNeedsTheNetWorthThreshold() {
        val poor = GameState(cash = PrestigeRules.UNLOCK_NET_WORTH / 2, stats = Stats(lifetimeEarnings = 1e15))
        assertFalse(PrestigeEngine.canPrestige(poor))
        assertEquals(poor, PrestigeEngine.prestige(poor))
    }

    @Test fun prestigeResetsTheRunAndKeepsTheRest() {
        val rich = GameEngine.newGame(1L, 0L).copy(
            cash = PrestigeRules.UNLOCK_NET_WORTH,
            businesses = mapOf(lemon to BusinessState(owned = 300, manager = true)),
            upgrades = setOf("global_x2_0"),
            properties = mapOf("studio_flat" to PropertyState(level = 2)),
            bank = BankState(savings = 5.0, loan = 3.0),
            luxury = setOf("aurora_roadster"),
            achievements = setOf("earn_0"),
            legacyPoints = 2.0,
            stats = Stats(lifetimeEarnings = PrestigeRules.POINT_DIVISOR * 10_000),
        )
        val gained = PrestigeEngine.pointsAvailable(rich)
        val after = PrestigeEngine.prestige(rich)
        assertEquals(50.0, after.cash, 0.0)
        assertTrue(after.businesses.isEmpty())
        assertTrue(after.upgrades.isEmpty())
        assertTrue(after.properties.isEmpty())
        assertEquals(BankState(), after.bank)
        assertEquals(setOf("aurora_roadster"), after.luxury)
        assertEquals(setOf("earn_0"), after.achievements)
        assertEquals(2.0 + gained, after.legacyPoints, 0.0)
        assertEquals(1, after.stats.prestigeCount)
        assertEquals(rich.stats.lifetimeEarnings, after.stats.lifetimeEarnings, 0.0)
        assertEquals(1.0 + PrestigeRules.BONUS_PER_POINT * after.legacyPoints, Economy(after).legacyMultiplier, 1e-9)
    }

    // --- Achievements --------------------------------------------------------------------------------

    @Test fun achievementsUnlockOnceAndPayRewards() {
        val s = GameState(cash = 0.0, businesses = mapOf(lemon to BusinessState(owned = 25)))
        val (after, unlocked) = AchievementEngine.check(s)
        assertTrue("own_25_$lemon" in unlocked)
        assertTrue(after.cash >= Achievements.MIN_CASH_REWARD)
        val (again, none) = AchievementEngine.check(after)
        assertTrue(none.isEmpty())
        assertEquals(after, again)
    }

    @Test fun percentRewardsBoostRevenue() {
        val s = GameState(businesses = mapOf(lemon to BusinessState(owned = 1)), achievements = setOf("earn_0", "managers_1"))
        assertEquals(1.03, Economy(s).achievementMultiplier, 1e-9)
    }

    @Test fun thereAreAtLeastThirtyAchievements() {
        assertTrue(Achievements.all.size >= 30)
        assertEquals(Achievements.all.size, Achievements.all.map { it.id }.toSet().size)
    }

    // --- Events --------------------------------------------------------------------------------------

    private fun withEvent(event: PendingEvent, cash: Double = 1_000.0) = GameEngine.newGame(5L, 0L).copy(
        cash = cash,
        businesses = mapOf(lemon to BusinessState(owned = 20, manager = true)),
        event = EventState(pending = event),
    )

    @Test fun eventsFireAfterTheCooldown() {
        var s = GameEngine.newGame(9L, 0L).copy(businesses = mapOf(lemon to BusinessState(owned = 5, manager = true)))
        s = s.copy(event = EventState(cooldown = 1.0))
        s = GameEngine.tick(s, 2.0)
        assertNotNull(s.event.pending)
        assertTrue(s.event.cooldown >= EventRules.MIN_COOLDOWN)
    }

    @Test fun viralPostAddsATimedBoost() {
        val s = withEvent(PendingEvent(EventType.VIRAL_POST, amount = 100.0))
        val before = Economy(s).revenuePerCycle(lemon)
        val r = EventEngine.resolve(s, accept = true, Rng(1))!!
        assertEquals(EventOutcome.VIRAL_BOOST, r.outcome)
        assertEquals(900.0, r.state.cash, 1e-9)
        assertEquals(before * EventRules.VIRAL_FACTOR, Economy(r.state).revenuePerCycle(lemon), 1e-9)
        // The boost runs out.
        val later = GameEngine.tick(r.state, EventRules.VIRAL_DURATION + 1)
        assertTrue(later.effects.isEmpty())
    }

    @Test fun cannotAcceptWhatYouCannotAfford() {
        val s = withEvent(PendingEvent(EventType.CHARITY_GALA, amount = 5_000.0), cash = 10.0)
        assertEquals(null, EventEngine.resolve(s, accept = true, Rng(1)))
        val declined = EventEngine.resolve(s, accept = false, Rng(1))!!
        assertEquals(EventOutcome.DECLINED, declined.outcome)
        assertEquals(null, declined.state.event.pending)
        assertEquals(1, declined.state.stats.eventsResolved)
    }

    @Test fun rivalOfferSellsTenPercent() {
        val s = withEvent(PendingEvent(EventType.RIVAL_OFFER, amount = 777.0, businessId = lemon))
        val r = EventEngine.resolve(s, accept = true, Rng(1))!!
        assertEquals(18, r.state.business(lemon).owned)
        assertEquals(1_777.0, r.state.cash, 1e-9)
    }

    @Test fun strikeStopsOneBusiness() {
        val s = withEvent(PendingEvent(EventType.STAFF_STRIKE, amount = 50.0, businessId = lemon))
        val r = EventEngine.resolve(s, accept = false, Rng(1))!!
        assertEquals(0.0, Economy(r.state).revenuePerCycle(lemon), 0.0)
    }

    @Test fun pendingEventSurvivesSaveAndLoad() {
        val s = withEvent(PendingEvent(EventType.MARKET_TIP, amount = 100.0, stockId = "nimbus_cloud"))
        assertEquals(s, SaveCodec.decode(SaveCodec.encode(s)))
    }

    // --- Daily reward --------------------------------------------------------------------------------

    @Test fun dailyStreakGrowsOnConsecutiveDaysAndResets() {
        var s = GameState()
        s = DailyEngine.claim(s, today = 100)!!.first
        assertEquals(1, s.daily.streak)
        assertEquals(null, DailyEngine.claim(s, today = 100))
        s = DailyEngine.claim(s, today = 101)!!.first
        assertEquals(2, s.daily.streak)
        s = DailyEngine.claim(s, today = 105)!!.first
        assertEquals(1, s.daily.streak)
        assertEquals(2, s.stats.bestStreak)
    }

    @Test fun dailyRewardScalesWithIncomeAndStreak() {
        val poor = GameState()
        assertEquals(100.0, DailyEngine.reward(poor, 1), 0.0)
        val rich = GameState(businesses = mapOf(lemon to BusinessState(owned = 100, manager = true)))
        assertTrue(DailyEngine.reward(rich, 7) > DailyEngine.reward(rich, 1))
        assertEquals(DailyEngine.reward(rich, 7), DailyEngine.reward(rich, 30), 0.0)
        val s = GameState(daily = DailyState(lastClaimDay = 9, streak = 3))
        assertEquals(4, DailyEngine.nextStreak(s, 10))
    }

    // --- Tutorial ------------------------------------------------------------------------------------

    @Test fun tutorialAdvancesWithPlay() {
        var s = GameEngine.newGame(3L, 0L).copy(cash = 10_000.0)
        assertEquals(TutorialStep.BUY_STAND, TutorialEngine.step(s))
        s = GameEngine.buyBusiness(s, lemon, com.businessempire.game.domain.BuyMode.ONE)
        s = GameEngine.tick(s, 0.1)
        assertEquals(TutorialStep.TAP_STAND, TutorialEngine.step(s))
        s = GameEngine.tapBusiness(s, lemon)
        s = GameEngine.tick(s, 0.1)
        assertEquals(TutorialStep.BUY_TEN, TutorialEngine.step(s))
        s = GameEngine.buyBusiness(s, lemon, com.businessempire.game.domain.BuyMode.TEN)
        s = GameEngine.tick(s, 0.1)
        assertEquals(TutorialStep.HIRE_MANAGER, TutorialEngine.step(s))
        s = GameEngine.hireManager(s, lemon)
        s = GameEngine.tick(s, 0.1)
        assertEquals(TutorialStep.VISIT_INVEST, TutorialEngine.step(s))
        s = TutorialEngine.visitedInvest(s)
        assertEquals(TutorialStep.DONE, TutorialEngine.step(s))
    }
}
