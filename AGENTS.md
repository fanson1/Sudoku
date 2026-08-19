# AGENTS.md — Sudoku KMP build/playbook

## Gradle wrapper is broken — use a direct `gradle`
`./gradlew` fails: `gradle/wrapper/gradle-wrapper.jar` is missing (gitignored, never committed).
Use instead (both work; the dist matches the wrapper's pinned 9.1.0):
- `gradle …` (Homebrew Gradle 9.0.0) — e.g. `gradle :app:shared:compileDevKotlinJvm`
- `~/.gradle/wrapper/dists/gradle-9.1.0-bin/*/gradle-9.1.0/bin/gradle …`

## Build & test commands
- `gradle :app:shared:compileDevKotlinJvm` — fastest shared-module typecheck
- `gradle :app:shared:jvmTest` — shared unit tests (commonTest + jvmTest)
- `gradle :core:jvmTest` — engine tests in `core/src/commonTest`
- `gradle :server:test` — Ktor server tests (`ApplicationTest`)
- `gradle :app:androidApp:assembleDebug` / `assembleRelease` / `lintDebug` / `lintFix`
- `gradle :server:run` — Ktor on `http://localhost:8080`; H2 file DB `server/build/db` (Exposed, JWT, BCrypt)
- `gradle :app:desktopApp:run` — desktop
- `gradle :app:webApp:wasmJsBrowserDevelopmentRun` — web dev server

## Modules
- `:core` — pure KMP engine (no UI): `SudokuGenerator`, `SudokuSolver`, `DifficultyGrader`, `GameRules`, models.
- `:app:shared` — Compose UI, ViewModels, `NetworkService` (Ktor client); targets jvm/android/js/wasmJs/ios.
- `:server` — Ktor JVM backend: auth routes, Exposed ORM over H2 file DB, schema auto-migrated in `DatabaseFactory.init()`.
- Clients: `:app:androidApp`, `:app:desktopApp`, `:app:webApp`, plus Xcode project `app/iosApp`.

## Gotchas
- Server base URL is hardcoded per platform via `expect fun getBaseUrl()` in `Platform.*.kt`. Android = `http://192.168.50.39:8080` (a dev LAN IP — edit for other environments), jvm = `localhost:8080`, ios/web = `10.0.2.2:8080`. Change these when the server host differs.
- JDK toolchain pinned to 21 (Amazon Corretto) via foojay resolver; JDK 17+ required.
- `shared-tests/fixtures/*.json` are *planned* cross-language golden fixtures (per `doc/数独游戏全平台研发策划书.md`) — not yet wired into any Gradle test.

## Code style
- Kotlin, idiomatic, no redundant comments.
- Compose Material 3 UI.
- State is single-source-of-truth via `collectAsStateWithLifecycle`.
- One-shot UI events flow through `GameEffect` channel collected in the screen.

## Key architecture
- `App.kt` owns the `Screen` enum stack + per-screen state; navigate by pushing onto `navigationStack`.
- `GameViewModel` exposes `state: StateFlow<GameState>` and `effects: Flow<GameEffect>`.
- Game modes live in `GameMode` (`NORMAL` / `DAILY` / `TIMED`); dispatched via `GameIntent.StartLevel/StartDailyChallenge/StartTimedChallenge`.
- `DailyUtil` (deterministic per-day seed) + `Persistence` (streak/completion) back the daily challenge.