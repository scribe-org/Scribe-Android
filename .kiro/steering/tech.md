# Tech Stack & Build System

## Language & Platform
- **Kotlin** (2.0.0), targeting Android API 26–34
- **JVM target**: Java 1.8
- **Build system**: Gradle with Kotlin DSL (`build.gradle.kts`)

## Key Frameworks & Libraries
- **Jetpack Compose** (BOM 2024.10.00) — primary UI framework for app screens
- **Material3** — design system
- **AndroidX Navigation Compose** — in-app navigation
- **Room** (2.6.1) with KSP — local SQLite database access
- **ViewBinding** — used in keyboard IME views (legacy XML views)
- **Kotlinx Serialization** (1.6.3)
- **Joda-Time** (2.10.13)
- **Glide** (4.14.2) — image loading

## Testing
- **JUnit 5** (5.11.2) — unit tests
- **MockK** (1.13.13) — mocking
- **Espresso** — UI/instrumentation tests
- **Compose UI Test** (`ui-test-junit4`)
- **Kover** (0.6.1) — code coverage (minimum 60% enforced)
- **Jacoco** (0.8.12) — coverage reports

## Linting & Static Analysis
- **ktlint** via `kotlinter` (4.4.1) — Kotlin code style
- **Detekt** (1.23.6) with `detekt.yml` config — static analysis
- **Compose lint checks** (`compose-lint-checks:1.4.2`)
- **pre-commit** hooks for automated checks

## Product Flavors
- `core` and `fdroid` variants under the `variants` flavor dimension

## Common Commands

```bash
# Lint and format checks
./gradlew lintKotlin detekt

# Auto-fix formatting
ktlint --format

# Run unit tests
./gradlew test

# Run all tests with coverage
./gradlew jacocoTestReport

# Sync i18n string resources from Scribe-i18n submodule
./gradlew moveFromi18n
# or run the shell script:
./update_i18n_keys.sh

# Clean build
./gradlew clean

# Pre-commit setup
pip install pre-commit
pre-commit install
pre-commit run --all-files
```

## i18n
- Localization strings live in `app/src/main/assets/i18n/Scribe-i18n/` (git submodule)
- The `moveFromi18n` Gradle task copies them into `src/main/res/values-*` before every build
- Translations are managed via [Weblate](https://hosted.weblate.org/projects/scribe/scribe-i18n)
