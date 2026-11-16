# Build prompt: Business Empire (Android demo)

You are building **Business Empire**, an original idle/tycoon business game for Android, in this folder
(`C:\Users\User\Desktop\business_empire`). The user will open this folder in **Android Studio**, so it must be a
complete, syncable Android Studio project. Build the whole game **except payments**. Work efficiently: don't ask
about things this brief already settles, and keep going phase by phase until the demo is done.

---

## 0. Ground rules

- **Original content only.** It's a business-tycoon game in its genre, but the business names, text, icons and look
  must be our own. Don't copy any existing game's content or art, and don't use real brands (car makers, companies,
  banks, tickers). Use fictional names everywhere, **except the Rich List of real people** (section 3.4b).
- **No payments, no ads.** Don't add Google Play Billing, ad SDKs, analytics SDKs or any network calls. The game
  runs fully offline (photos are bundled in the app, not loaded from the internet). Leave a clean hook for monetization (section 6) so the user can add it after the demo.
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
- Luxury items with fictional names: cars, homes, yachts, jets, art, islands. Each gives a small permanent
  **prestige bonus** (e.g. +1% global revenue) and counts toward net worth. This is the "show off" progression.
- **Every item has a picture** (see 3.8). Item cards show the picture large, with the name, price and bonus under it.
  Items the player doesn't own yet show a darkened version with a lock and price.
- **"My Empire" showcase**: a screen that shows everything the player owns as a visual gallery (home, garage of cars,
  yacht at the marina, jet, island). Make owning things feel rewarding to look at.

### 3.4b Rich List: compare yourself to the world. Part of the **Profile** tab
- **Real-world wealth milestones** (no names): a ladder the player climbs, with a progress bar to the next step, e.g.
  "Average household", "Millionaire ($1M)", "Top 1%", "Decamillionaire ($10M)", "Centimillionaire ($100M)",
  "Billionaire ($1B)", "Top 100 richest people", "Richest person on Earth". Keep the values in a data table, marked
  approximate, with a `// VERIFY:` comment so the user can update them with current figures. Show messages like
  "You are now richer than 99% of people on Earth".
- **Fun comparisons**: "Your net worth could buy 1,250 average houses / 40 private jets / a small country's yearly
  budget". Keep these in the same data table.
- **Real rich list (the user chose real names)**: about 60 well-known real people, from famous millionaires
  (athletes, musicians, actors, YouTubers) up to the world's richest billionaires, with net worths from about $1M to
  the top of the list. The player's rank moves up the list as their net worth grows, with a notification like
  "You just passed <name>!" and a "Next to beat: <name>, $X" card on the Profile tab.
- Rules for using real people:
  - **Names, approximate net worth and a one-line neutral description only** (e.g. "Founder, tech company"). Facts
    only: nothing negative, mocking or made up about anyone, and no implied endorsement ("X plays this game").
  - **Real photos (the user chose this)**: each person gets a real photo in a round avatar, and a larger photo on
    their detail card. Rules:
    - Only from **Wikimedia Commons**, and only files marked Public Domain, CC0, CC BY or CC BY-SA. Check the
      licence on each file page. Never from Google Images, news sites, social media, Getty or agency photos.
    - Prefer neutral, respectful photos (conference, portrait, public event). Crop and resize only: no editing,
      filters, stickers, face swaps, captions or anything that makes fun of them or changes the meaning.
    - Record each photo in `image_credits.json` like the other photos (author, source, licence) so it appears on the
      Credits screen. CC BY-SA photos must keep the same licence for the cropped version, so note that.
    - Use these photos **only inside the Rich List screens**. Never in the app icon, splash screen, store screenshots,
      promo graphics or notifications, so it never looks like they endorse the game.
    - If no properly licensed photo exists for someone, use the initials avatar for that person instead.
    - Ask the user before downloading, like the other photos (batches of about 20 with name, source and licence).
  - Don't put real company names or logos into gameplay (stocks, businesses stay fictional).
  - Store the list as **`app/src/main/assets/rich_list.json`** with fields `name`, `netWorthUsd`, `description`,
    and a top-level `asOf` date, so the user can update it without touching code. Use your best known figures,
    mark the file with a `"note": "approximate, VERIFY before release"` field, and list it in PROGRESS.md as something
    the user must check against a current source (e.g. Forbes or Bloomberg lists) before publishing.
  - Show a small disclaimer on the Rich List screen and in About: "Net worth figures are public estimates as of
    <asOf>. This game is not affiliated with or endorsed by anyone listed."
  - Keep the fictional tycoons as a fallback: if `rich_list.json` is missing or fails to parse, use a built-in
    fictional list so the game still works (and so the user can switch to fictional names quickly if a store
    reviewer objects).
- **Leaderboard hook for real players**: add a `LeaderboardService` interface with a local-only implementation
  (shows the player's own best net worth and prestige runs). Mark it `// ONLINE:` so Google Play Games leaderboards
  can be plugged in later. Don't add the Play Games SDK now.

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
- UI icons (tabs, buttons): Material Symbols.
- An adaptive launcher icon (vector) with a simple original mark.

- Short sounds are optional. If you add them, generate simple tones in code or skip audio. Don't download sound files.
- Portrait only. Must work on small phones (360dp wide) and tablets.

### 3.8 Real photos for every item (the user chose real pictures)
The game must be visual, not just text lists. **Every business, property, luxury item and stock company gets a real
photo**: a real beach villa, a real private island, a real yacht, a real sports car, a real private jet, a real food
truck, a real office tower, a real rocket launch, and so on.

**Where photos may come from (licence is mandatory):**
- Only sources that allow free commercial use in an app: **Pexels**, **Unsplash**, **Pixabay**, or **Wikimedia
  Commons** files marked Public Domain, CC0 or CC BY / CC BY-SA. Check each file's licence on its page.
  Never use photos from Google Images, news sites, Instagram or car/yacht makers' websites.
- Prefer photos with **no visible brand logos, badges, licence plates or recognisable faces** (crop or pick another
  photo). Cars and jets should look luxurious but not show the maker's logo.
- **Downloading needs the user's OK.** Before downloading, show the user the list (file name, source page, licence)
  in batches of about 20 and wait for their yes.

**Recording credits:**
- Write every photo to `app/src/main/assets/image_credits.json`: `imageName`, `title`, `author`, `sourceUrl`,
  `licence`, `licenceUrl`, `modified` (e.g. "cropped, resized").
- Add a **Credits** screen (Settings → About → Photo credits) that lists them. This is required for CC BY / CC BY-SA
  and polite for the rest.

**Technical:**
- Crop to a consistent aspect ratio (4:3 for cards, 16:9 for headers), resize to 720 px wide, convert to **WebP**
  (quality ~80), and save in `res/drawable-nodpi/` named by `imageName` (e.g. `lux_island_1.webp`). Target total photo
  size under 15 MB. Use a script in a separate `tools/` folder for resizing (Python + Pillow is fine).
- Load images with **Coil** (`coil-compose`) for smooth scrolling. Use rounded corners, a soft dark gradient at the
  bottom of each card so white text stays readable, and a darkened, blurred version for locked items.
- Businesses show a **visual upgrade**: 2–3 photos per business (e.g. a small street lemonade stand → a busy juice bar
  → a juice factory) that change at milestones.
- **Keep a placeholder** while photos are missing: a coloured gradient card with the Material icon of the category.
  The game must build and run without any photos, so gameplay phases aren't blocked.
- Document every `imageName`, what it should show, and its size in `ART_LIST.md`, so the user can replace any photo
  by dropping a file with the same name into `res/drawable-nodpi/`.
- Real people on the Rich List: real Wikimedia Commons photos with the extra rules in 3.4b, saved as
  `person_<name>.webp`, cropped square, 400 px.
- Do the photos in their own phase (see section 7).

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
5. Prestige, achievements, random events, daily reward, statistics, settings, tutorial, Rich List.
5b. Photos: `ART_LIST.md`, find licensed photos, get the user's OK, download, resize to WebP, `image_credits.json`,
    Credits screen, business upgrade stages, "My Empire" showcase.
6. Balancing pass with the simulation, debug cheats, polish (animations, empty states, accessibility labels,
   content descriptions, 48dp touch targets), launcher icon, release build config.
7. Final check: clean build, all tests green. Write `README.md` (how to open in Android Studio, run, build a release
   APK/AAB, where to tune balance) and summarize what's done and what's left in PROGRESS.md.

When everything is done, tell the user: how to open the project in Android Studio, how to run it on an
emulator or device, and what's left for them (payments, store listing, final app name/applicationId).
