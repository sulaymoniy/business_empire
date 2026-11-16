# Business Empire

An original idle/tycoon game for Android. Start with $50 and a lemonade stand, grow to twelve business tiers,
trade stocks, collect rent, borrow from the bank, buy luxury items, and "Sell the Empire" for permanent
Legacy Points. Kotlin + Jetpack Compose (Material 3), fully offline, no ads, no payments (yet).

All companies, products and names in the game are fictional.

## Open in Android Studio

1. Android Studio → **File → Open** → select this folder (`business_empire`).
2. Let Gradle sync. The project uses the Gradle wrapper (9.6.0), AGP 9.5.1, Kotlin 2.4.21 and SDK 37.
   If Studio asks to install SDK Platform 37 or build tools, accept.
3. `local.properties` (not in git) holds your SDK path, usually `%LOCALAPPDATA%\Android\Sdk`. Studio creates it on sync.

## Run it

- **Emulator:** Tools → Device Manager → start a phone (e.g. the existing `Medium_Phone_API_37.0`), then press
  **Run ▶** with the `app` configuration.
- **Phone:** enable *Developer options → USB debugging*, plug it in, pick it in the device menu, press **Run ▶**.
- Debug builds have a **Debug tools** entry at the bottom of the Profile tab: add cash, skip 1 hour, unlock all
  businesses, trigger an event.

From a terminal (Windows):

```bat
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
gradlew.bat assembleDebug testDebugUnitTest
gradlew.bat installDebug
```

## Build a release

Release builds are minified with R8 (`isMinifyEnabled = true`, rules in `app/proguard-rules.pro`).

1. Create an upload key once: **Build → Generate Signed App Bundle / APK** → *Create new…* (keep the `.jks`
   file and passwords safe and out of git; `.gitignore` already excludes `*.jks`).
2. **Build → Generate Signed App Bundle / APK** → *Android App Bundle* → pick your key → *release*.
   The AAB lands in `app/release/` and is what you upload to Google Play.
3. For a local test APK instead: same dialog, choose *APK*.

Before publishing, change `applicationId` in `app/build.gradle.kts` (see the `TODO` there). It can never change
after the first upload. Bump `versionCode` for every upload.

## Project layout

```
app/src/main/java/com/businessempire/game/
  data/            SaveRepository (DataStore), SaveCodec (JSON + saveVersion), SaveMigrations,
                   RichListRepository (assets/rich_list.json), ImageCredits (assets/image_credits.json)
  domain/          GameState, GameEngine, Economy, Formulas, Rng, NumberFormatter,
                   Investments (stocks + property), Bank (bank + luxury), Meta (prestige, achievements,
                   events, daily reward, tutorial), RichListEngine. Pure Kotlin, no Android imports.
  domain/content/  All balance tables: Businesses, Upgrades, Stocks, Properties, Luxury (+ ranks, bank rules),
                   Achievements, Events (+ prestige, daily, tutorial rules), RichList (wealth milestones,
                   comparisons, fictional tycoons)
  ui/              GameViewModel (game loop, autosave, offline), AppRoot (navigation), screens/, components/,
                   art/GameArt (photo lookup by name), theme/
  monetization/    MonetizationService + NoOp (see MONETIZATION_TODO.md)
  online/          LeaderboardService + local implementation (marked // ONLINE: for Play Games later)
app/src/main/assets/          rich_list.json (real people, VERIFY), image_credits.json
app/src/main/res/drawable-nodpi/   125 bundled WebP photos (5.8 MB), see ART_LIST.md
app/src/test/      64 JVM unit tests, including the balance simulation
tools/photos/      photo pipeline (find, pick, download, crop, WebP, credits)
```

## Photos

Every business (3 stages), property, luxury item and stock company has a real photo, and Rich List people
have a portrait where a licensed one exists. All 125 are from Wikimedia Commons (Public Domain, CC0, CC BY or
CC BY-SA), cropped and resized only, and credited on Settings > About > Photo credits. Anything without a photo
shows a gradient placeholder card, so the game works with any subset.

- [ART_LIST.md](ART_LIST.md) lists every image name, what it should show, its size and whether it's added.
- To replace a photo, drop a WebP/PNG with the same name into `app/src/main/res/drawable-nodpi/` and update
  `image_credits.json`, or add it to `tools/photos/photos.json` and run `python tools/photos/process_photos.py`
  (needs Pillow: `python -m pip install --user pillow`).
- Real people's photos are only shown inside the Rich List screens. Set `USE_REAL_RICH_LIST` to `false` in
  `app/build.gradle.kts` to switch to fictional tycoons (no names, no photos).

How it runs: `GameViewModel` ticks `GameEngine.step()` about 10 times a second, autosaves every 15 s and on
`onStop`, and applies offline earnings (capped at 4 h, +2 h per offline upgrade) when the app comes back.
The engine is deterministic: all randomness comes from a seeded RNG stored in the save.

All user-facing text is in `app/src/main/res/values/strings.xml`, ready for translation.

## Tuning the balance

Every number lives in `domain/content/`:

| What | File |
|---|---|
| Business cost, growth, revenue, cycle time, manager price | `Businesses.kt` (also milestones) |
| Cash upgrades and offline cap | `Upgrades.kt` |
| Stock sectors (drift, volatility), news odds | `Stocks.kt` |
| Property prices, rent, renovation | `Properties.kt` |
| Luxury items, rank thresholds, bank interest and loan rules | `Luxury.kt` |
| Achievement targets and rewards | `Achievements.kt` |
| Event amounts and durations, prestige threshold and points, daily reward | `Events.kt` |

After changing numbers, run the simulator. It plays as a greedy, attentive player on the real engine and prints
when each tier unlocks:

```bat
gradlew.bat testDebugUnitTest --tests "*BalanceSimulationTest*" -i
```

Current result (optimal play): tier 2 at 20 s, first manager at 2 min, tier 6 at 33 min, prestige at about 4 h.
The test fails if pacing drifts outside the targets.

## What's left before launch

- Payments: see `MONETIZATION_TODO.md`.
- Final app name, icon review, `applicationId`, store listing, screenshots, privacy policy, Data safety form.
- Playtesting with real players (the simulator ignores stocks, property, the bank and events).
- Rich List: check every figure in `rich_list.json` and the `// VERIFY:` values in `RichList.kt` against a current
  source, and get advice on using real people's names and photos. Portraits with a Commons "personality rights"
  warning are excluded (those people show initials) and `process_photos.py` refuses them. Store screenshots must
  not show the Rich List photos.
- 3 items still use placeholders (no clean photo found): Aurora Roadster, Halberd Hypercar, Lost Harbor painting.
