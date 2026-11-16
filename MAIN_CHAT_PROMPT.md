# Build prompt: Business Empire (Android demo)

You are building **Business Empire**, an original idle/tycoon business game for Android, in this folder
(`C:\Users\User\Desktop\business_empire`). The user will open this folder in **Android Studio**, so it must be a
complete, syncable Android Studio project. Build the whole game **except payments**. Work efficiently: don't ask
about things this brief already settles, and keep going phase by phase until the demo is done.

---

## 0. Ground rules

- **Original content only.** It's a business-tycoon game in its genre, but the business names, text, icons and look
  must be our own. Don't copy any existing game's content or art, and don't use real brands (car makers, companies,
  banks, tickers). Use fictional names everywhere.
- **No payments, no ads.** Don't add Google Play Billing, ad SDKs, analytics SDKs or any network calls. The game
  runs fully offline. Leave a clean hook for monetization (section 6) so the user can add it after the demo.
- **Git:** the home folder `C:\Users\User` is itself a git repo. **Don't commit to it.** If you want version
  control, ask the user before running `git init` inside this project folder.
- `index.html` in this folder is a leftover "Hello World" placeholder. Delete it.
- Windows machine: use `gradlew.bat`. Android Studio's bundled JDK is usually at
  `C:\Program Files\Android\Android Studio\jbr`. Set `JAVA_HOME` to it for command-line builds if needed.
  The Android SDK is usually at `%LOCALAPPDATA%\Android\Sdk`. Write `local.properties` with `sdk.dir`.
- Keep `PROGRESS.md` up to date: phases done, what's next, known issues. If the chat gets compacted, read it first.

## 1. Tech stack

- **Kotlin + Jetpack Compose (Material 3)**, single activity, Navigation Compose.
- Gradle **Kotlin DSL** with a **version catalog** (`gradle/libs.versions.toml`). Use the **latest stable**
  AGP / Kotlin / Compose BOM that work with the installed Android Studio. Check what's installed instead of guessing.
- `minSdk 24`, `compileSdk`/`targetSdk` = the newest stable API installed (36 or newer if available).
- Include the Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`). If a Gradle install exists, generate
  it with `gradle wrapper`. Otherwise copy one from an existing project under `C:\Users\User\AndroidStudioProjects`.
- State: `ViewModel` + `StateFlow`. Persistence: **DataStore + kotlinx.serialization** (one JSON save blob with a
  `saveVersion` field for future migrations). No Room unless clearly needed.
- No DI framework. Use a simple manual `AppContainer`.
- Package: `com.businessempire.game` (leave a `TODO` in `build.gradle.kts` noting the user may change `applicationId`
  before publishing).

## 2. Architecture

```
app/src/main/java/com/businessempire/game/
  data/        SaveRepository (DataStore), SaveModels (serializable), migrations
  domain/      GameState, GameEngine (pure Kotlin, no Android imports), formulas, content definitions
  domain/content/  businesses, upgrades, properties, stocks, luxury items, achievements, events (data tables)
  ui/          screens per tab, shared components, theme
  monetization/ MonetizationService interface + NoOp implementation
```

- `GameEngine` is **pure Kotlin and deterministic** given time + a seeded RNG, so it can be unit tested.
- Game loop: a coroutine ticks the engine (about 10 times per second for smooth UI). Simulation uses real elapsed time.
- Autosave every 15 s, plus on `onStop`. On launch, compute **offline earnings** from elapsed wall-clock time
  (cap 4 h base, upgradable). Show a "Welcome back" dialog.
- Numbers are `Double`. Write a formatter for big numbers: 1.23K, M, B, T, Qa, Qi, Sx, Sp, Oc, No, Dc, then aa, ab…
- All user-facing text goes in `strings.xml` (the game must be translatable later).

## 3. Game design (demo scope)

The player starts with **$50** and builds from a lemonade stand to a global empire.
**Net worth** = cash + business value + properties + stock portfolio + luxury assets − loans.

### 3.1 Businesses (core idle loop). Bottom tab: **Business**
12 tiers, unlocked in order:
Lemonade Stand → Food Truck → Coffee Shop → Car Wash → Bakery Chain → Fitness Club → Tech Startup → Boutique Hotel →
Shipping Company → Regional Airline → Energy Plant → Orbital Launch Co.

- Each business has: base cost, cost growth (1.07 to 1.15), base revenue, cycle time (1 s up to about 1 h).
- Buy x1 / x10 / x100 / Max.
- **Milestones** at 25/50/100/200/300/400/500 owned: revenue ×2 or cycle speed ×2.
- **Managers**: one per business, a one-time purchase that automates its cycle. Without a manager the player taps to
  start each cycle.
- **Upgrades** list: global and per-business multipliers, bought with cash.
- Progress bars per business, tap feedback (small haptic + scale animation).

### 3.2 Investments. Bottom tab: **Invest**
- **Stock market**: 10 fictional companies across sectors. Price = random walk with drift + volatility per sector,
  plus occasional news events ("Regulators probe Nimbus Cloud", -15%). Buy/sell any quantity, show average cost,
  P/L and a simple line chart (draw it with Compose Canvas, no chart library). Prices update every few seconds and
  persist history (last ~200 points).
- **Real estate**: about 12 properties (studio flat → office tower → private island resort). Each has price and
  rent per minute. Upgrades (renovate) raise rent. Can sell at a value that slowly appreciates.

### 3.3 Bank. Bottom tab: **Bank**
- Savings deposit with interest (per in-game minute; small, compounding).
- Loans: borrow up to a % of net worth, interest accrues, repay any time. Ignoring it too long blocks new loans.

### 3.4 Lifestyle / assets. Bottom tab: **Assets**
- Luxury items with fictional names: cars, homes, yachts, jets, art. Each gives a small permanent **prestige bonus**
  (e.g. +1% global revenue) and counts toward net worth. This is the "show off" progression.

### 3.5 Prestige: "Sell the Empire"
- Unlocks at a net-worth threshold. Resets businesses, cash, stocks and properties. Grants **Legacy Points** based on
  lifetime earnings (square-root curve). Each point gives +2% revenue permanently. Keeps achievements and luxury items.

### 3.6 Meta systems. Bottom tab: **Profile** (or a "More" menu)
- Player profile: CEO name and company name chosen at first launch, rank title based on net worth
  (Street Vendor → Shop Owner → … → Tycoon → Mogul).
- **Achievements** (30+): unlock toasts, small cash or multiplier rewards.
- **Random events** every few minutes: choice dialogs (e.g. "A rival wants to buy your Coffee Shops", accept/decline)
  with temporary boosts or penalties.
- **Daily reward** (free, streak-based, cash scaled to progress). No real money involved.
- **Statistics**: lifetime earnings, time played, taps, best net worth, prestige count.
- **Settings**: sound on/off, haptics on/off, number format (short/scientific), reset save (with confirmation),
  about screen.
- **Tutorial**: 4–5 step first-run coach marks (buy stand → tap → buy 10 → hire manager → check Invest tab).

### 3.7 Look & feel
- Material 3, custom color scheme (deep green + gold "money" palette), light and dark themes.
- Icons: Material Symbols / vector drawables you create. No downloaded or copyrighted art.
- An adaptive launcher icon (vector) with a simple original mark.
- Short sounds are optional. If you add them, generate simple tones in code or skip audio. Don't download files.
- Portrait only. Must work on small phones (360dp wide) and tablets.

## 4. Balancing
- Put all numbers in the `domain/content` tables so they're easy to tweak.
- Target pacing: tier 2 within ~1 min, manager #1 within ~3 min, tier 6 within ~1 h, first prestige around 3–6 h of
  active play. Write a small unit test or debug function that simulates an optimal player and prints when each tier
  unlocks. Use it to sanity-check the curve.
- Debug-only cheats menu (`BuildConfig.DEBUG`): add cash, skip time 1 h, unlock all, to speed up testing.

## 5. Quality bar
- Unit tests for: cost formulas, buy-max, milestone multipliers, offline earnings (incl. cap), prestige points,
  loan interest, save → load round-trip, and save migration from version 1.
- `gradlew.bat assembleDebug` and `gradlew.bat testDebugUnitTest` must pass at the end of **every** phase.
  Fix failures before moving on.
- No crashes on rotation or process death (state comes from the save plus the engine, not just memory).
- R8/minify enabled for `release` with working ProGuard rules for kotlinx.serialization.

## 6. Monetization hook (no payments now)
- `monetization/MonetizationService.kt`: an interface with methods such as `isPremium()`, `purchase(productId)` and
  `restorePurchases()`, plus `NoOpMonetizationService` that does nothing / returns false.
- A **Shop** screen exists but stays hidden behind `BuildConfig.STORE_ENABLED = false`. It can show "Coming soon".
- Mark every place a future purchase could plug in (e.g. "2× income forever", "remove ads") with `// MONETIZATION:` comments.
- Write `MONETIZATION_TODO.md` explaining exactly where to add Google Play Billing later.

## 7. Phases (do them in order, build + test after each, update PROGRESS.md)
1. Project skeleton: Gradle, wrapper, theme, navigation with 5 bottom tabs, empty screens. Builds and launches.
2. Engine + businesses + managers + milestones + upgrades + number formatting + save/load + offline earnings.
3. Stock market + real estate.
4. Bank + luxury assets + net worth + rank titles.
5. Prestige, achievements, random events, daily reward, statistics, settings, tutorial.
6. Balancing pass with the simulation, debug cheats, polish (animations, empty states, accessibility labels,
   content descriptions, 48dp touch targets), launcher icon, release build config.
7. Final check: clean build, all tests green. Write `README.md` (how to open in Android Studio, run, build a release
   APK/AAB, where to tune balance) and summarize what's done and what's left in PROGRESS.md.

When everything is done, tell the user: how to open the project in Android Studio, how to run it on an
emulator or device, and what's left for them (payments, store listing, final app name/applicationId).
