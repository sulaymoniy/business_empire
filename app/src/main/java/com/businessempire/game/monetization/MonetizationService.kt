package com.businessempire.game.monetization

/**
 * The single seam for future in-app purchases. The demo ships with [NoOpMonetizationService].
 *
 * MONETIZATION: implement this with Google Play Billing (see MONETIZATION_TODO.md) and swap the
 * implementation in AppContainer. Nothing else in the app talks to a billing library directly.
 */
interface MonetizationService {
    /** True if the player owns the "premium" product (e.g. permanent 2x income). */
    fun isPremium(): Boolean

    /** Starts a purchase flow. Returns the result once the flow finishes. */
    suspend fun purchase(productId: String): PurchaseResult

    /** Re-checks the store for owned products (new device, reinstall). */
    suspend fun restorePurchases(): List<String>
}

sealed interface PurchaseResult {
    data class Success(val productId: String) : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Unavailable(val reason: String) : PurchaseResult
}

/** Product ids the Shop screen will list once the store is enabled. */
object ProductIds {
    const val PREMIUM_2X_INCOME = "premium_2x_income" // MONETIZATION: "2x income forever"
    const val REMOVE_ADS = "remove_ads" // MONETIZATION: only relevant if ads are ever added
    const val CASH_PACK_SMALL = "cash_pack_small" // MONETIZATION: consumable cash bundle
}

/** Does nothing: no store, no network, nothing owned. */
class NoOpMonetizationService : MonetizationService {
    override fun isPremium(): Boolean = false
    override suspend fun purchase(productId: String): PurchaseResult =
        PurchaseResult.Unavailable("Store not enabled in this build")
    override suspend fun restorePurchases(): List<String> = emptyList()
}
