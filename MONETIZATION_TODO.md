# Adding payments later (Google Play Billing)

The demo ships with **no billing, no ads, no analytics and no network access**. Everything a store needs
already has a place to plug in. Search the code for `// MONETIZATION:` to see every hook.

## What exists today

| Piece | Where | State |
|---|---|---|
| `MonetizationService` interface (`isPremium()`, `purchase(productId)`, `restorePurchases()`) | `app/src/main/java/com/businessempire/game/monetization/MonetizationService.kt` | Done |
| `NoOpMonetizationService` (nothing owned, purchases "unavailable") | same file | Used by the demo |
| Product ids (`premium_2x_income`, `remove_ads`, `cash_pack_small`) | `ProductIds` in the same file | Placeholders |
| Wiring point | `AppContainer.monetization` in `AppContainer.kt` | Returns the NoOp |
| Shop screen ("Coming soon") | `ShopScreen` in `ui/screens/SettingsScreens.kt` | Hidden |
| Store switch | `buildConfigField("boolean", "STORE_ENABLED", "false")` in `app/build.gradle.kts` | Off |
| Income multiplier hook | `Economy.globalMultiplier` in `domain/Economy.kt` | Comment only |

## Steps

1. **Play Console:** create the app, upload a signed AAB to an internal testing track (billing only works on
   builds installed from Play), and create the in-app products with the ids in `ProductIds`
   (or change the ids to match yours).
2. **Dependency:** add the Play Billing library to `gradle/libs.versions.toml` and `app/build.gradle.kts`
   (check the current version on the Android developer site), plus `<uses-permission android:name="android.permission.INTERNET"/>`
   if your billing version needs it. `com.android.vending.BILLING` is added by the library.
3. **Implementation:** create `monetization/PlayBillingMonetizationService.kt` implementing
   `MonetizationService` with a `BillingClient`:
   - connect in `init`, query `ProductDetails` for the ids in `ProductIds`;
   - `purchase()` launches the billing flow (it needs the current `Activity`; pass it in through a small
     `ActivityProvider` or change the signature to `purchase(activity, productId)`);
   - **acknowledge** non-consumables (`premium_2x_income`, `remove_ads`) and **consume** `cash_pack_small`;
   - `restorePurchases()` calls `queryPurchasesAsync` and returns owned product ids;
   - cache `isPremium()` in memory (re-check on start; Play is the source of truth).
4. **Swap it in:** in `AppContainer`, replace `NoOpMonetizationService()` with your implementation.
5. **Apply the rewards in the game rules:**
   - *2× income:* add a field such as `premium: Boolean = false` to `GameState` (default keeps old saves
     loading), set it from `monetization.isPremium()` when the app starts, and multiply it into
     `Economy.globalMultiplier` at the `// MONETIZATION:` comment. Add a unit test for it.
   - *Cash pack:* after a successful consumable purchase, call `GameEngine.earn(state, amount)` from the ViewModel.
6. **Show the store:** set `STORE_ENABLED` to `true`. The Shop entry appears on the Profile tab and the route is
   registered in `AppRoot`. Replace the "Coming soon" cards in `ShopScreen` with real product names and
   prices from `ProductDetails`, and add a "Restore purchases" button.
7. **Policy and testing:** add license testers in Play Console, test buy / cancel / pending / refund, update
   the privacy policy and the Data safety form, and keep the Play Billing ProGuard rules (the library ships
   its own consumer rules).

## If you ever add ads

There is a `remove_ads` product id reserved. Ads are not part of this demo; add an ad SDK only after deciding
on consent handling (GDPR/UMP) and updating the Data safety form.
