package com.businessempire.game

import android.content.Context
import com.businessempire.game.data.RichListRepository
import com.businessempire.game.data.SaveRepository
import com.businessempire.game.domain.GameState
import com.businessempire.game.online.LeaderboardService
import com.businessempire.game.online.LocalLeaderboardService
import com.businessempire.game.monetization.MonetizationService
import com.businessempire.game.monetization.NoOpMonetizationService

/** Manual dependency container. One instance per process, owned by [BusinessEmpireApplication]. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val saveRepository = SaveRepository(appContext)

    val richListRepository = RichListRepository(appContext, useRealNames = BuildConfig.USE_REAL_RICH_LIST)

    /**
     * ONLINE: return a Google Play Games implementation here to share leaderboards with other players.
     * The demo only keeps the player's own records, read from the save.
     */
    fun leaderboard(state: () -> GameState?): LeaderboardService = LocalLeaderboardService(state)

    // MONETIZATION: replace with a Play Billing implementation when the store goes live.
    val monetization: MonetizationService = NoOpMonetizationService()
}
