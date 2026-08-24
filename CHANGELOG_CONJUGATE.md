# Conjugate-Android Changelog

> [!NOTE]
> This repository contains the code for two different applications: Scribe-Android and Conjugate-Android (Scribe-Conjugate for Android). This is the changelog for Conjugate-Android. See [CHANGELOG.md](/CHANGELOG.md) for the Scribe-Android changelog.

See the [releases for this repository](https://github.com/scribe-org/Scribe-Android/releases) for an up to date list of versions and their release dates. Versions that are marked as released may not yet be on Google Play and other stores if it's within the submission review period.

Conjugate-Android tries to follow [semantic versioning](https://semver.org/), a MAJOR.MINOR.PATCH version where increments are made of the:

- MAJOR version when we make incompatible API changes
- MINOR version when we add functionality in a backwards compatible manner
- PATCH version when we make backwards compatible bug fixes

Emojis for the following are chosen based on [gitmoji](https://gitmoji.dev/).

## [Upcoming] Conjugate-Android 1.0.0

### MVP release of Scribe Conjugate on Android

### 🚀 Deployment

- Releasing for the Google Play Store

### ✨ New Features

- A verb Conjugation tab was added to the application ([#563](https://github.com/scribe-org/Scribe-Android/issues/563)).
- Users can search for verbs across languages ([#566](https://github.com/scribe-org/Scribe-Android/issues/566)).
- A reactive conjugation selection UI was developed to easily copy desired conjugations ([#567](https://github.com/scribe-org/Scribe-Android/issues/567), [#570](https://github.com/scribe-org/Scribe-Android/issues/570)).
- Users are able to filter verb conjugations by tense ([#572](https://github.com/scribe-org/Scribe-Android/issues/572)).
- Recently conjugated verbs are displayed to the user in the Conjugation tab ([#568](https://github.com/scribe-org/Scribe-Android/issues/568), [#569](https://github.com/scribe-org/Scribe-Android/issues/569)).
- The Settings tab for the Scribe keyboard application was migrated to allow base settings for the app interface ([#562](https://github.com/scribe-org/Scribe-Android/issues/562)).
- The About tab for the Scribe keyboard application was migrated to provide information on the application and community ([#561](https://github.com/scribe-org/Scribe-Android/issues/561)).
- The application and community's relationship to the Wikimedia movement is explained in app ([#52](https://github.com/scribe-org/Scribe-Android/issues/52)).
- The user is able to easily rate the application ([#165](https://github.com/scribe-org/Scribe-Android/issues/165), [#640](https://github.com/scribe-org/Scribe-Android/issues/640)).

### 🗃️ Data

- SQLite databases have been set up for all data needed for the conjugate UI ([#87](https://github.com/scribe-org/Scribe-Android/issues/87), [#571](https://github.com/scribe-org/Scribe-Android/issues/571)).
- Calls are made to the [Scribe-Server API](https://scribe-server.toolforge.org/) hosted on Toolforge to download language data and insert it into SQLite tables ([#547](https://github.com/scribe-org/Scribe-Android/issues/547), [#565](https://github.com/scribe-org/Scribe-Android/issues/565), [#626](github.com/scribe-org/Scribe-Android/issues/626)).
- The data download UI was created to download data for any keyboards that have been installed ([#437](https://github.com/scribe-org/Scribe-Android/issues/437), [#439](https://github.com/scribe-org/Scribe-Android/issues/439), [#513](https://github.com/scribe-org/Scribe-Android/issues/513), [#554](https://github.com/scribe-org/Scribe-Android/issues/554), [#564](https://github.com/scribe-org/Scribe-Android/issues/564)).
- Network indicators for data request have been added to the application and are shown via toasts ([#651](https://github.com/scribe-org/Scribe-Android/issues/651)).

### 🎨 Design

- Dark mode compatibility through a responsive color scheme ([#25](https://github.com/scribe-org/Scribe-Android/issues/25), [#51](https://github.com/scribe-org/Scribe-Android/issues/51), [#116](https://github.com/scribe-org/Scribe-Android/issues/116), [#121](https://github.com/scribe-org/Scribe-Android/issues/121), [#155](https://github.com/scribe-org/Scribe-Android/issues/155), [#161](https://github.com/scribe-org/Scribe-Android/issues/161), [#543](https://github.com/scribe-org/Scribe-Android/issues/543)).
- The application menu follows modern Android styling ([#114](https://github.com/scribe-org/Scribe-Android/issues/114), [#150](https://github.com/scribe-org/Scribe-Android/issues/150), [#217](https://github.com/scribe-org/Scribe-Android/issues/217), [#246](https://github.com/scribe-org/Scribe-Android/issues/246), [#247](https://github.com/scribe-org/Scribe-Android/issues/247), [248](https://github.com/scribe-org/Scribe-Android/issues/248), [#256](https://github.com/scribe-org/Scribe-Android/issues/256)).

### 🌐 Localization

- The application has been localized into many languages using [Weblate](https://weblate.org/en/) and the [Scribe-i18n](https://github.com/scribe-org/Scribe-i18n) project as a central Git submodule of localizations ([#44](https://github.com/scribe-org/Scribe-Android/issues/44), [#95](https://github.com/scribe-org/Scribe-Android/issues/95), [#214](https://github.com/scribe-org/Scribe-Android/issues/214), [#527](https://github.com/scribe-org/Scribe-Android/issues/527)).

### ✅ Tests

- [ktlint](https://github.com/ktlint/ktlint) and [detekt](https://github.com/detekt/detekt) linting was added to the project to validate code quality and standards ([#70](https://github.com/scribe-org/Scribe-Android/issues/70), [#354](https://github.com/scribe-org/Scribe-Android/issues/354)).
- Unit tests and code coverage were written for the project ([#181](https://github.com/scribe-org/Scribe-Android/issues/181), [#196](https://github.com/scribe-org/Scribe-Android/issues/196)).
- GitHub Actions based CI was set up with unit and instrumentation tests ([#195](https://github.com/scribe-org/Scribe-Android/issues/195), [#212](https://github.com/scribe-org/Scribe-Android/issues/212), [#422](https://github.com/scribe-org/Scribe-Android/issues/422), [#580](https://github.com/scribe-org/Scribe-Android/issues/580)).
- prek based pre-commit hooks were added to the repo to catch common mistakes on commit ([#215](https://github.com/scribe-org/Scribe-Android/issues/215)).

### 📝 Documentation

- Functions in the application have been documented ([#18](https://github.com/scribe-org/Scribe-Android/issues/18), [#354](https://github.com/scribe-org/Scribe-Android/issues/354)).

### ⚖️ Legal

- All code has been developed under the GNU General Public License (GPL-3.0) ([#301](https://github.com/scribe-org/Scribe-Android/issues/301)).
- The legal policies of the application are displayed to the user in the About tab ([#58](https://github.com/scribe-org/Scribe-Android/issues/58), [#561](https://github.com/scribe-org/Scribe-Android/issues/561)).
- A privacy policy was provided to make clear that policies around user data and their security ([#59](https://github.com/scribe-org/Scribe-Android/issues/59)).
- Third party licensed code used in the development of the project were detailed ([#60](https://github.com/scribe-org/Scribe-Android/issues/60)).

### ♻️ Code Refactoring

- Code quality improvements were continuously done to assure that the application is easy to maintain and meets Kotlin standards ([#426](https://github.com/scribe-org/Scribe-Android/issues/426)).
