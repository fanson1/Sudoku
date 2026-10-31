# AGENTS.md — Sudoku KMP build/playbook

## Gradle wrapper is broken — use a direct `gradle`
`./gradlew` fails: `gradle/wrapper/gradle-wrapper.jar` is missing (gitignored, never committed).
Use instead (both work; the dist matches the wrapper's pinned 9.1.0):
- `gradle …` (Homebrew Gradle 9.0.0) — e.g. `gradle :app:shared:compileDevKotlinJvm`
- `~/.gradle/wrapper/dists/gradle-9.6.0-bin/*/gradle-9.6.0/bin/gradle …`

## Build & test commands
- `gradle :app:shared:compileDevKotlinJvm` — fastest shared-module typecheck
- `gradle :app:shared:jvmTest` — shared unit tests (commonTest + jvmTest)
- `gradle :core:jvmTest` — engine tests in `core/src/commonTest`
- `gradle :app:androidApp:assembleDebug` / `assembleRelease` / `lintDebug` / `lintFix`
- `gradle :app:androidApp:bundleRelease` — **signed AAB for Play Console** (requires `keystore/keystore.properties`)
- `gradle :app:desktopApp:run` — desktop
- `gradle :app:webApp:wasmJsBrowserDevelopmentRun` — web dev server

## Modules
- `:core` — pure KMP engine (no UI): `SudokuGenerator`, `SudokuSolver`, `DifficultyGrader`, `GameRules`, models.
- `:app:shared` — Compose UI, ViewModels, **offline only**; targets jvm/android/js/wasmJs/ios.
- `:server` — **EXCLUDED from build** (source kept in `/server` for reference only). Ktor JVM backend with auth, Exposed ORM, H2.
- Clients: `:app:androidApp`, `:app:desktopApp`, `:app:webApp`, plus Xcode project `app/iosApp`.

## Architecture: Fully Offline Single-Player + Pro IAP
- **No network permission** in AndroidManifest (`INTERNET` removed, only `com.android.vending.BILLING`)
- **No Ktor client** in `:app:shared` (removed from `build.gradle.kts`)
- **No login / profile / leaderboard / network screens** (all deleted)
- **Pro layer**: `pro/ProController.kt` interface + `LocalProController` (non-Android) + `ProController.android.kt` (Google Play Billing 9.1.0)
- **Product ID**: `sudoku_pro_lifetime` (non-consumable, one-time, lifetime)
- **Free tier**: levels 1-10, notes, undo/redo, 3 hints/level, light/dark/system themes
- **Pro tier**: levels 11-50, Daily Challenge, Timed Challenge, unlimited hints, detailed stats, all themes
- **Free/Pro boundary**: `util/FreeTier.kt` (`TOTAL_LEVELS=50`, `MAX_FREE_LEVEL=10`)

## In-App Language Switching (8 languages + System)
- **Custom map-based i18n** (not Compose resources): `i18n/StringKey` enum + `i18n/Strings` class
- Language packs: `EnglishStrings`, `ZhHansStrings`, `ZhHantStrings`, `JaStrings`, `KoStrings`, `EsStrings`, `FrStrings`, `DeStrings`
- `Language.kt`: `Language` enum + `resolveLanguage()` + `stringsFor()` + `expect/actual systemLanguageTag()`
- Runtime switch via `CompositionLocalProvider(LocalStrings provides stringsFor(resolveLanguage(pref)))`
- All user-visible strings go through `LocalStrings.current[StringKey.KEY]` or `.fmt(KEY, "ph" to value)`

## Key files (new / rewritten)
| File | Purpose |
| --- | --- |
| `App.kt` | Navigation stack (HOME/GAME/STATS), ProController wiring, paywall state |
| `ui/LevelSelectionScreen.kt` | Home: progress, Pro banner, challenges, level grid with Pro gating |
| `ui/StatsScreen.kt` | Pro-only statistics screen |
| `ui/components/PaywallDialog.kt` | Purchase UI with `PaywallReason` (LEVELS/CHALLENGE/STATS/GENERAL) |
| `ui/components/SettingsSheet.kt` | Language chips, theme options, Pro section, game settings |
| `ui/game/GameScreen.kt` | Fully localized (title, stats row, progress, keypad, pause, dialogs) |
| `ui/game/GameState.kt` | Added `unlimitedHints: Boolean` |
| `ui/game/GameViewModel.kt` | Pro-aware hint limits in `loadPuzzle`/`requestHint` |
| `util/Persistence.kt` | Offline-only: language + Pro + win rate helper (auth removed) |
| `util/FreeTier.kt` | Level gating constants + helpers |
| `i18n/Strings.kt` | `StringKey` enum (134 keys) + `Strings` class with `.fmt()` |
| `i18n/Language.kt` | Language enum, resolution, platform `systemLanguageTag()` actuals |
| `pro/ProController.kt` | Interface + `ProProduct.ID` + `ProMessage` + `rememberProController()` |
| `pro/ProController.android.kt` | BillingClient 9.1.0: queryProductDetailsAsync, queryPurchasesAsync, acknowledge |
| `pro/LocalProController.kt` | Desktop/iOS/Web: local unlock for dev |
| `androidApp/build.gradle.kts` | Release signing from `keystore.properties`, minify, ProGuard, bundle config |
| `androidApp/src/main/AndroidManifest.xml` | Offline: BILLING only, `allowBackup=false`, `localeConfig` |

## Gotchas
- **Android base URL** no longer used (offline). `Platform.*.kt` files deleted.
- JDK toolchain pinned to 21 (Amazon Corretto) via foojay resolver; JDK 17+ required.
- `shared-tests/fixtures/*.json` are *planned* cross-language golden fixtures — not yet wired.
- Debug builds get `applicationIdSuffix = ".debug"` — **cannot test IAP** (must use release-signed bundle).
- Billing 9.1.0 API: `ProductDetailsResponseListener.onProductDetailsResponse(BillingResult, QueryProductDetailsResult)`; use `queryResult.productDetailsList`.
- Non-Android `LocalProController.purchase()` unlocks locally (documented as Play-exclusive billing).
- `keystore/upload-keystore.jks` + `keystore/keystore.properties` are **gitignored**; back up to password manager.

## Code style
- Kotlin, idiomatic, no redundant comments.
- Compose Material 3 UI.
- State is single-source-of-truth via `collectAsStateWithLifecycle`.
- One-shot UI events flow through `GameEffect` channel collected in the screen.
- All strings via `LocalStrings.current[StringKey.KEY]` — **no hardcoded user-visible text**.

## Play Store publishing docs (in `docs/`)
- `GOOGLE_PLAY_PUBLISHING.md` — end-to-end upload guide
- `IAP_SETUP.md` — product config, code paths, testing
- `KEYSTORE_SETUP.md` — keystore generation, CI, recovery
- `PLAY_CONSOLE_DATA_SAFETY.md` — data safety form answers (no data collected)
- `PLAY_STORE_LISTING.md` — store copy in 8 languages
- `PRIVACY_POLICY.md` — privacy policy (EN + ZH)
- `SCREENSHOT_GUIDE.md` — asset specs, generation script
- `RELEASE_CHECKLIST.md` — pre-launch verification
- `CHANGELOG.md` — version history

## Release workflow
```bash
# 1. Verify keystore
cat keystore/keystore.properties
ls -l keystore/upload-keystore.jks

# 2. Build signed AAB
gradle :app:androidApp:bundleRelease

# 3. Verify signature
$JAVA_HOME/bin/jarsigner -verify -verbose -certs \
  app/androidApp/build/outputs/bundle/release/androidApp-release.aab

# 4. (Optional) Local install test via bundletool
java -jar bundletool.jar build-apks --bundle=... --output=sudoku.apks --mode=universal
adb install -r sudoku.apks/universal.apk
```

## Store assets generation
```bash
cd /path/to/Sudoku
python3 -m pip install pillow
python3 tools/make_store_assets.py
# Outputs to play-store-assets/
```

---

*Today's date: Sun Nov 01 2026*