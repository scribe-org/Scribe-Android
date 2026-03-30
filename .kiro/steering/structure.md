# Project Structure

```
Scribe-Android/
├── app/
│   ├── build.gradle.kts          # App-level build config (deps, flavors, lint, coverage)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   ├── data/             # Bundled SQLite language databases (DE, EN, ES, FR, IT, PT, RU, SV)
│       │   │   ├── data-contracts/   # JSON data contracts per language
│       │   │   └── i18n/             # Scribe-i18n git submodule (localization strings)
│       │   └── java/be/scri/
│       │       ├── App.kt                  # Root Composable (ScribeApp) — NavHost + HorizontalPager
│       │       ├── activities/
│       │       │   └── MainActivity.kt     # Single activity; sets up Compose content
│       │       ├── services/               # IME services — one per language + GeneralKeyboardIME base
│       │       ├── helpers/                # Utilities, DB helpers, config, per-language interface variables
│       │       │   ├── <language>/         # e.g. english/, german/ — language-specific constants/variables
│       │       │   └── keyboardDBHelper/   # DB access helpers for keyboard data
│       │       ├── models/                 # Data models
│       │       ├── navigation/
│       │       │   └── Screen.kt           # Sealed class defining all nav routes
│       │       ├── views/                  # Custom Android Views (KeyboardView — XML-based)
│       │       ├── extensions/             # Kotlin extension functions
│       │       └── ui/
│       │           ├── common/             # Shared Compose components (bottom bar, dialogs, base screen)
│       │           ├── models/             # UI-layer data models
│       │           ├── screens/            # Top-level Compose screens (Installation, Settings, About, etc.)
│       │           │   ├── about/
│       │           │   └── settings/
│       │           └── theme/              # ScribeTheme, colors, typography
│       └── test/
│           └── kotlin/                     # Unit tests mirroring main package structure
├── build.gradle.kts              # Root build config
├── detekt.yml                    # Detekt static analysis rules
└── .pre-commit-config.yaml       # Pre-commit hook configuration
```

## Architecture Patterns

### App UI (Compose)
- Single `MainActivity` using `setContent` — no fragments for app screens
- `ScribeApp` composable owns the `NavHost` and a `HorizontalPager` (3 tabs: Installation, Settings, About)
- Navigation uses `NavHostController` with `Screen` sealed class routes
- Theme applied at the top level via `ScribeTheme`

### Keyboard IME (XML Views)
- `GeneralKeyboardIME` is the abstract base class extending `InputMethodService`
- Each language has its own IME service (e.g., `EnglishKeyboardIME`) that extends `GeneralKeyboardIME`
- Keyboard views use XML layouts with `ViewBinding` (not Compose)
- Language-specific constants/variables live in `helpers/<language>/` (e.g., `ENInterfaceVariables`)

### Data Layer
- Language data is stored in SQLite databases bundled under `assets/data/`
- Room is used for structured DB access; `DatabaseHelper` and `DatabaseManagers` handle queries
- `PreferencesHelper` manages SharedPreferences for user settings

## Conventions
- All source files start with `// SPDX-License-Identifier: GPL-3.0-or-later`
- Package root: `be.scri`
- Test files mirror the main package structure and are named `<ClassName>Test.kt`
- Test method naming: `methodName_StateUnderTest_ExpectedBehavior()`
- Tests follow the AAA pattern (Arrange, Act, Assert)
- Grammar/language data is never edited directly — changes go through Wikidata → Scribe-Data
