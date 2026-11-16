package com.businessempire.game.online

import com.businessempire.game.domain.GameState

/** Leaderboards the game knows about. */
enum class Board {
    /** Best net worth ever reached. */
    BEST_NET_WORTH,
    /** Earnings per prestige run ("Sell the Empire"). */
    PRESTIGE_RUNS,
}

data class LeaderboardEntry(
    val rank: Int,
    val label: String,
    val value: Double,
    val isPlayer: Boolean,
    /** For prestige runs: how long the run took, in seconds. */
    val durationSeconds: Double? = null,
)

/**
 * Leaderboards seam.
 *
 * ONLINE: implement this with Google Play Games Services leaderboards (submitScore / loadTopScores) and
 * swap the implementation in AppContainer. Nothing else in the app talks to an online service. The demo
 * stays fully offline and ships only [LocalLeaderboardService].
 */
interface LeaderboardService {
    /** True when scores are shared with other players. */
    val isOnline: Boolean

    /** ONLINE: submit to the matching Play Games leaderboard. Local: no-op (the save already has it). */
    suspend fun submit(board: Board, value: Double)

    suspend fun entries(board: Board): List<LeaderboardEntry>
}

/** Offline leaderboards: only the player's own records, read from the save. */
class LocalLeaderboardService(private val state: () -> GameState?) : LeaderboardService {
    override val isOnline: Boolean = false

    override suspend fun submit(board: Board, value: Double) = Unit

    override suspend fun entries(board: Board): List<LeaderboardEntry> {
        val s = state() ?: return emptyList()
        return when (board) {
            Board.BEST_NET_WORTH -> listOf(
                LeaderboardEntry(rank = 1, label = s.profile.companyName, value = s.stats.bestNetWorth, isPlayer = true),
            )
            Board.PRESTIGE_RUNS -> s.runHistory
                .sortedByDescending { it.earnings }
                .mapIndexed { i, run ->
                    LeaderboardEntry(i + 1, "#${run.run}", run.earnings, isPlayer = true, durationSeconds = run.durationSeconds)
                }
        }
    }
}
