package com.businessempire.game.domain

import kotlinx.serialization.Serializable

/**
 * The whole game as one immutable, serializable value. The engine turns one GameState into the next;
 * the save file is just this object plus a version number (see data/SaveCodec).
 *
 * Every field has a default so older saves still load after new fields are added.
 */
@Serializable
data class GameState(
    val cash: Double = GameRules.START_CASH,
    /** Earned since the last prestige. */
    val runEarnings: Double = 0.0,
    val businesses: Map<String, BusinessState> = emptyMap(),
    val upgrades: Set<String> = emptySet(),
    val market: MarketState = MarketState(),
    /** Owned properties by id. */
    val properties: Map<String, PropertyState> = emptyMap(),
    val bank: BankState = BankState(),
    /** Owned luxury item ids. Kept through prestige. */
    val luxury: Set<String> = emptySet(),
    /** Kept through prestige. Each point adds [com.businessempire.game.domain.content.PrestigeRules.BONUS_PER_POINT]. */
    val legacyPoints: Double = 0.0,
    /** Unlocked achievement ids. Kept through prestige. */
    val achievements: Set<String> = emptySet(),
    /** Temporary boosts/penalties from events. */
    val effects: List<TempEffect> = emptyList(),
    val event: EventState = EventState(),
    val daily: DailyState = DailyState(),
    val profile: Profile = Profile(),
    /** Index into [com.businessempire.game.domain.content.TutorialStep]. */
    val tutorialStep: Int = 0,
    val stats: Stats = Stats(),
    /** One record per finished prestige run, for the local leaderboard. */
    val runHistory: List<RunRecord> = emptyList(),
    /** Game clock when the current run started. */
    val runStartedAt: Double = 0.0,
    val settings: Settings = Settings(),
    /** Simulated seconds since this save was created (drives timers). */
    val clock: Double = 0.0,
    val rngState: Long = 0L,
    /** Wall-clock time of the last save, used for offline earnings. */
    val lastSavedAtMs: Long = 0L,
) {
    fun business(id: String): BusinessState = businesses[id] ?: BusinessState()
}

@Serializable
data class BusinessState(
    val owned: Int = 0,
    val manager: Boolean = false,
    /** Seconds into the current cycle. */
    val progress: Double = 0.0,
    /** Without a manager a cycle only runs after a tap. */
    val running: Boolean = false,
)

@Serializable
data class Stats(
    /** All-time earnings across every prestige. Drives Legacy Points. */
    val lifetimeEarnings: Double = 0.0,
    val timePlayedSeconds: Double = 0.0,
    val taps: Long = 0,
    val bestNetWorth: Double = 0.0,
    val unitsBought: Long = 0,
    val managersHired: Int = 0,
    val stockTrades: Long = 0,
    val profitableSells: Long = 0,
    val loansRepaid: Int = 0,
    val prestigeCount: Int = 0,
    val eventsResolved: Int = 0,
    val bestStreak: Int = 0,
)

@Serializable
data class RunRecord(
    val run: Int,
    val earnings: Double,
    val pointsGained: Double,
    val durationSeconds: Double,
)

@Serializable
data class Settings(
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val numberStyle: NumberStyle = NumberStyle.SHORT,
)

enum class BuyMode(val count: Int) { ONE(1), TEN(10), HUNDRED(100), MAX(-1) }

object GameRules {
    const val START_CASH = 50.0
}
