# Scribe-Android Changelog

> [!NOTE]
> This repository contains the code for two different applications: Scribe-Android and Conjugate-Android (Scribe-Conjugate for Android). This is the changelog for Scribe-Android. See [CHANGELOG_CONJUGATE.md](/CHANGELOG_CONJUGATE.md) for the Conjugate-Android changelog.

See the [releases for this repository](https://github.com/scribe-org/Scribe-Android/releases) for an up to date list of versions and their release dates. Versions that are marked as released may not yet be on Google Play and other stores if it's within the submission review period.

Scribe-Android tries to follow [semantic versioning](https://semver.org/), a MAJOR.MINOR.PATCH version where increments are made of the:

- MAJOR version when we make incompatible API changes
- MINOR version when we add functionality in a backwards compatible manner
- PATCH version when we make backwards compatible bug fixes

Emojis for the following are chosen based on [gitmoji](https://gitmoji.dev/).

## Scribe-Android 1.0.0

### MVP release of Scribe - Language Keyboards on Android

### 🚀 Deployment

- Releasing to the Google Play Store.

### ⌨️ Keyboards

- Keyboards for English, French, German, Italian, Portuguese, Russian, Spanish and Swedish.

### ✨ New Features

- Keyboard extensions that can be used in any app.
- Annotation of words in the command bar including the genders of nouns and cases that follow prepositions.
- Basic translations from any keyboard language into any other keyboard language.
- Querying the plurals of nouns.
- Conjugations of verbs within an interactive UI.

### 🗃️ Data

- SQLite databases have been set up for all data needed for the keyboards ([#87](https://github.com/scribe-org/Scribe-Android/issues/87)).
- Calls are made to the [Scribe-Server API](https://scribe-server.toolforge.org/) hosted on Toolforge to download language data and insert it into SQLite tables ([#547](https://github.com/scribe-org/Scribe-Android/issues/547)).
- The user is directed to download data in the keyboard UI if they access it with empty databases ([#581](https://github.com/scribe-org/Scribe-Android/issues/581)).
- The data download UI was created to download data for any keyboards that have been installed ([#437](https://github.com/scribe-org/Scribe-Android/issues/437), [#439](https://github.com/scribe-org/Scribe-Android/issues/439), [#513](https://github.com/scribe-org/Scribe-Android/issues/513), [#520](https://github.com/scribe-org/Scribe-Android/issues/520), [#554](https://github.com/scribe-org/Scribe-Android/issues/554)).
- Network indicators for data request have been added to the application and are shown via toasts ([#651](https://github.com/scribe-org/Scribe-Android/issues/651)).

### 🎨 Design

- The Scribe key and command bar where Scribe commands are triggered.
- 3x2 conjugation tables from which conjugations can be selected in the `Conjugate` command.
- The return key is colored Scribe blue when commands are being triggered to let the user know that that is what they need to press to finish the command.
- Dark mode compatibility.

### 🌐 Localization

- The application has been localized into many languages using [Weblate](https://weblate.org/en/) and the [Scribe-i18n](https://github.com/scribe-org/Scribe-i18n) project as a central Git submodule of localizations ([#44](https://github.com/scribe-org/Scribe-Android/issues/44), [#214](https://github.com/scribe-org/Scribe-Android/issues/214), [#527](https://github.com/scribe-org/Scribe-Android/issues/527)).

### ✅ Tests

- [ktlint](https://github.com/ktlint/ktlint) and [detekt](https://github.com/detekt/detekt) linting was added to the project to validate code quality and standards ([#70](https://github.com/scribe-org/Scribe-Android/issues/70), [#354](https://github.com/scribe-org/Scribe-Android/issues/354)).
- Unit tests and code coverage were written for the project ([#181](https://github.com/scribe-org/Scribe-Android/issues/181), [#196](https://github.com/scribe-org/Scribe-Android/issues/196)).
- GitHub Actions based CI was set up with unit and instrumentation tests ([#195](https://github.com/scribe-org/Scribe-Android/issues/195), [#212](https://github.com/scribe-org/Scribe-Android/issues/212), [#422](https://github.com/scribe-org/Scribe-Android/issues/422), [#580](https://github.com/scribe-org/Scribe-Android/issues/580)).
- prek based pre-commit hooks were added to the repo to catch common mistakes on commit ([#215](https://github.com/scribe-org/Scribe-Android/issues/215)).

### ⚖️ Legal

- All code has been developed under the GNU General Public License (GPL-3.0).
