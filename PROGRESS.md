# Business Empire: progress log

Read this first after a context reset. Build brief: `MAIN_CHAT_PROMPT.md`. How to build/run: `README.md`.

## Environment (checked 2026-11-16)
- Android Studio 2026.2.2, bundled JDK 25 at `C:\Program Files\Android\Android Studio\jbr`
- SDK `%LOCALAPPDATA%\Android\Sdk`: platform android-37.0, build-tools 36.0.0, AVD `Medium_Phone_API_37.0`
- Gradle 9.6.0 wrapper (copied from `AndroidStudioProjects/soap_alarm`), AGP 9.5.1 (built-in Kotlin), Kotlin 2.4.21,
  Compose BOM 2026.09.00, Navigation 2.10.2, DataStore 1.3.0, kotlinx.serialization 1.13.0
- Build from a shell: `set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"` then
  `gradlew.bat assembleDebug testDebugUnitTest`
- The project folder has its own git repo (branch `main`, one "Initial commit"). Nothing has been committed by the
  assistant; all work is uncommitted.

## Phases (all done)
- [x] 1. Skeleton: Gradle + version catalog + wrapper, M3 theme (green/gold, light/dark), 5-tab Navigation Compose shell,
      adaptive launcher icon (+ API 24-25 fallback, themed monochrome), `MonetizationService` + NoOp.
- [x] 2. Engine, 12 business tiers, managers, milestones, upgrades, big-number formatter, DataStore save/load
      (saveVersion 2, migration from v1), autosave (15 s + onStop), offline earnings (4 h cap, +2 h upgrades),
      Welcome-back dialog.
- [x] 3. Stock market (10 fictional companies, random walk + mean reversion + news shocks, 200-point history,
      Canvas chart, buy/sell any quantity, avg cost, P/L) + real estate (12 properties, rent, renovation, appreciation).
- [x] 4. Bank (savings interest, loans up to 50% of net worth, 30-min overdue rule), 20 luxury items (+% revenue),
      full net worth, 9 rank titles.
- [x] 5. Onboarding (CEO + company name), Profile hub, prestige ("Sell the Empire", sqrt Legacy Points, +2%/pt,
      unlock at $100B net worth), 39 achievements, 8 random choice events, daily streak reward, statistics, settings
      (sound/haptics/number format/reset/about), 5-step tutorial banner, hidden Shop, debug cheats.
- [x] 6. Balance simulation (`BalanceSimulationTest`) and tuning; rewards now scale with real (passive) income;
      generated sound effects; animated tutorial banner; R8 release build verified on the emulator
      (save survives force-stop, navigation works).
- [x] 7. Lint: 0 errors (fixed an API-24 crash from `java.time.LocalDate`, and stale-resource reads). README.md,
      MONETIZATION_TODO.md. Clean build + 55 unit tests green.

## Brief update (2026-11-16, 12:15 and 12:35): Rich List + real photos
- [x] 5 (Rich List). `domain/content/RichList.kt` (wealth milestones + fun comparisons, `// VERIFY:` figures),
      `assets/rich_list.json` (65 real people, $5M to $400B, asOf "2025", VERIFY note), fictional fallback tycoons
      (`BuildConfig.USE_REAL_RICH_LIST` switches to them), "You just passed X!" + wealth-milestone snackbars, "Next to
      beat" card on Profile, Rich List screen with person detail dialog, disclaimers (Rich List + About),
      `online/LeaderboardService` + local implementation (`// ONLINE:`), prestige run history. 64 unit tests.
- [x] 5b (Photos). Done 2026-11-16 (user approved all 7 batches):
      - 125 photos bundled in res/drawable-nodpi (5.8 MB WebP), 125 credits in assets/image_credits.json.
      - Removed the 10 people photos with a Commons "personality rights" warning (Springsteen, Ronaldo, Dwayne
        Johnson, Emma Watson, Jensen Huang, Messi, Bloomberg, Selena Gomez, Spielberg, Tom Cruise); they show
        initials. process_photos.py now refuses such photos.
      - Checked every photo: cropped out a worker's face (biz_lemonade_stand_3), a researcher (stock_quantis_labs),
        show-stand logos (lux_strata_gt), an airline name and "AIRBUS" text (lux_private_airliner), framed the solar
        panels (stock_solace_energy) and Stephen Curry's portrait. Dropped (placeholder): lux_aurora_roadster (other
        cars' logos and plates in every crop), lux_halberd_hypercar (maker badge on the nose).
      - Placeholders remain for those two plus lux_lost_harbor_masterpiece; 17 people use initials.
      - Verified on the emulator: business cards, Assets grid (locked = darkened + blurred), My Empire, Rich List
        with photos + initials, person detail card with photo credit. Release APK keeps all bundled photos.
      Code side:
      - Coil 3 (`coil-compose` only, no network module), `PhotoCard` (rounded, bottom scrim, darkened + blurred when
        locked, gradient + Material icon placeholder when the photo is missing), `GameArt` lookup by image name.
      - `imageName` on every business (3 stages: 0 / 25 / 100 owned), property, luxury item (new Islands category),
        stock company; `person_<slug>` for Rich List people (photos only inside the Rich List screens).
      - Assets tab is a picture grid; "My Empire" showcase screen; Settings > About > Photo credits screen reading
        `assets/image_credits.json`; `res/raw/keep_photos.xml` keeps photos through resource shrinking.
      - Tools: `tools/photos/catalog.py` (145 image names), `find_photos.py` (Wikimedia Commons search with licence
        filter), `pick.py` (records picks as unapproved), `process_photos.py` (download, crop, resize, WebP q80,
        credits, ART_LIST.md). Needs Pillow: `python -m pip install --user pillow`.
      - An earlier drawn-vector art set was removed when the brief switched to real photos.
      Picks and approvals are recorded in tools/photos/photos.json and BATCHES.md.
      No suitable photo found: lux_lost_harbor_masterpiece (placeholder). People with initials only: Amancio Ortega
      (lead image is a coat of arms), Françoise Bettencourt Meyers, Ken Griffin, MacKenzie Scott, Lewis Hamilton,
      Usain Bolt (GFDL only), Jenna Ortega, plus the 10 personality-rights people above.

## Must check before publishing
- `app/src/main/assets/rich_list.json`: every net worth is an approximate estimate from training data (2025 and
  earlier). Check each against a current Forbes/Bloomberg list and update `asOf`. The lowest entries (about $5M to
  $100M) are the least certain.
- `domain/content/RichList.kt`: wealth milestone and comparison figures marked `// VERIFY:`.
- Photos of real people: portraits with a Commons "personality rights" warning are excluded. The remaining 48 still
  show real people in a commercial app; get legal advice, or set USE_REAL_RICH_LIST=false.

## Balance (simulated optimal player)
Tier 2 at 20 s, first manager 2 min 12 s, tier 6 at 33 min, tier 7 at about 2 h, prestige at about 3 h 56 min.
Tiers 9-12 are post-prestige goals.

## Known issues / ideas for later
- Stock prices don't move while the app is closed (offline only pays managers, rent and interest).
- The simulator ignores stocks, property, bank, luxury and events; real pacing should be playtested.
- Lint has 5 harmless PluralsCandidate warnings ("Day %d streak", "%d of %d unlocked", "at %d owned").
- Business icons come from Material Icons Extended (Apache 2.0). R8 strips unused icons from release builds.
- Possible next steps: Play Billing (see MONETIZATION_TODO.md), translations, cloud save, more events.
