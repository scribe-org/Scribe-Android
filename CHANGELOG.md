# Scribe-Android Changelog

> [!NOTE]
> This repository contains the code for two different applications: Scribe-Android and Conjugate-Android (Scribe-Conjugate for iOS). This is the changelog for Scribe-Android. See [CHANGELOG_CONJUGATE.md](/CHANGELOG_CONJUGATE.md) for the Conjugate-Android changelog.

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

### 🎨 Design

- The Scribe key and command bar where Scribe commands are triggered.
- 3x2 conjugation tables from which conjugations can be selected in the `Conjugate` command.
- The return key is colored Scribe blue when commands are being triggered to let the user know that that is what they need to press to finish the command.
- Dark mode compatibility.

### 🌐 Localization

- The application has been localized into many languages using [Weblate](https://weblate.org/en/) and the [Scribe-i18n](https://github.com/scribe-org/Scribe-i18n) project as a central source of localizations ([#44](https://github.com/scribe-org/Scribe-Android/issues/44), [#214](https://github.com/scribe-org/Scribe-Android/issues/214)).

### ✅ Tests

-

### ⚖️ Legal

- All code has been developed under the GNU General Public License (GPL-3.0).
