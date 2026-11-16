package com.businessempire.game.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import com.businessempire.game.R
import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes.
@Serializable data object BusinessRoute
@Serializable data object BankRoute
@Serializable data object AssetsGraph
@Serializable data object AssetsRoute
@Serializable data object MyEmpireRoute

// Invest tab: list + stock detail live in one nested graph so the tab stays selected.
@Serializable data object InvestGraph
@Serializable data object InvestRoute
@Serializable data class StockDetailRoute(val stockId: String)

// Profile tab: hub + sub-pages.
@Serializable data object ProfileGraph
@Serializable data object ProfileRoute
@Serializable data object AchievementsRoute
@Serializable data object RichListRoute
@Serializable data object StatisticsRoute
@Serializable data object SettingsRoute
@Serializable data object AboutRoute
@Serializable data object CreditsRoute
@Serializable data object PrestigeRoute
@Serializable data object ShopRoute
@Serializable data object CheatsRoute

/** Bottom tabs. [route] is what the tab navigates to and what marks it selected. */
enum class TopTab(@StringRes val label: Int, val icon: ImageVector, val route: Any) {
    BUSINESS(R.string.tab_business, Icons.Outlined.Storefront, BusinessRoute),
    INVEST(R.string.tab_invest, Icons.AutoMirrored.Outlined.ShowChart, InvestGraph),
    BANK(R.string.tab_bank, Icons.Outlined.AccountBalance, BankRoute),
    ASSETS(R.string.tab_assets, Icons.Outlined.Diamond, AssetsGraph),
    PROFILE(R.string.tab_profile, Icons.Outlined.Person, ProfileGraph),
}
