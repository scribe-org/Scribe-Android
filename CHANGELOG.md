# Scribe-Android Changelog

> [!NOTE]
> This repository contains the code for two different applications: Scribe-Android and Conjugate-Android (Scribe-Conjugate for Android). This is the changelog for Scribe-Android. See [CHANGELOG_CONJUGATE.md](/CHANGELOG_CONJUGATE.md) for the Conjugate-Android changelog.

See the [releases for this repository](https://github.com/scribe-org/Scribe-Android/releases) for an up to date list of versions and their release dates. Versions that are marked as released may not yet be on Google Play and other stores if it's within the submission review period.

Scribe-Android tries to follow [semantic versioning](https://semver.org/), a MAJOR.MINOR.PATCH version where increments are made of the:

- MAJOR version when we make incompatible API changes
- MINOR version when we add functionality in a backwards compatible manner
- PATCH version when we make backwards compatible bug fixes

Emojis for the following are chosen based on [gitmoji](https://gitmoji.dev/).

## [Upcoming] Scribe-Android 1.0.0

### MVP release of Scribe - Language Keyboards on Android

### 🚀 Deployment

- Releasing to the Google Play Store.

### ⌨️ Keyboards

- Keyboards for English, French, German, Italian, Portuguese, Russian, Spanish and Swedish.
- Keyboards can easily be individually installed based on the language the user speaks ([#15](https://github.com/scribe-org/Scribe-Android/issues/15)).

### ✨ New Features

- Keyboard extensions that can be used in any app.
- Annotation of words in the command bar including the genders of nouns and cases that follow prepositions ([#135](https://github.com/scribe-org/Scribe-Android/issues/135), [#136](https://github.com/scribe-org/Scribe-Android/issues/136), [#275](https://github.com/scribe-org/Scribe-Android/issues/275)).
- The Scribe command UI can be displayed by pressing the Scribe Key ([#16](https://github.com/scribe-org/Scribe-Android/issues/16), [#89](https://github.com/scribe-org/Scribe-Android/issues/89), [#137](https://github.com/scribe-org/Scribe-Android/issues/137)).
- Basic translations from any keyboard language into any other keyboard language ([#265](https://github.com/scribe-org/Scribe-Android/issues/265), [#266](https://github.com/scribe-org/Scribe-Android/issues/266)).
- Querying the plurals of nouns ([#272](https://github.com/scribe-org/Scribe-Android/issues/272), [#421](https://github.com/scribe-org/Scribe-Android/issues/421), [#466](https://github.com/scribe-org/Scribe-Android/issues/466), [#467](https://github.com/scribe-org/Scribe-Android/issues/467)).
- Conjugations of verbs within an interactive UI ([#268](https://github.com/scribe-org/Scribe-Android/issues/268), [#270](https://github.com/scribe-org/Scribe-Android/issues/270), [#598](https://github.com/scribe-org/Scribe-Android/issues/598)).
- Instructions for how to install keyboards are provided to the user on the Installation tab ([#113](https://github.com/scribe-org/Scribe-Android/issues/113)).
- Autosuggestions and autocompletions are displayed as the user types ([#408](https://github.com/scribe-org/Scribe-Android/issues/408), [#409](https://github.com/scribe-org/Scribe-Android/issues/409), [#551](https://github.com/scribe-org/Scribe-Android/issues/551)).
- Profanity is removed from autosuggestions and autocompletions ([#665](https://github.com/scribe-org/Scribe-Android/issues/665)).
- Emojis can be selected as autosuggestions and autocompletions ([#66](https://github.com/scribe-org/Scribe-Android/issues/66), [#138](https://github.com/scribe-org/Scribe-Android/issues/138), [#313](https://github.com/scribe-org/Scribe-Android/issues/313), [#428](https://github.com/scribe-org/Scribe-Android/issues/428), [#637](https://github.com/scribe-org/Scribe-Android/issues/637)).
- An integrated emoji keyboard is included in the keyboard ([#425](https://github.com/scribe-org/Scribe-Android/issues/425), [#654](https://github.com/scribe-org/Scribe-Android/issues/654)).
- An integrated numeric keyboard is included when the user is typing in numeric form fields ([#607](https://github.com/scribe-org/Scribe-Android/issues/607)).
- A clipboard can be accessed for copying and pasting texts by long holding the emoji key ([#459](https://github.com/scribe-org/Scribe-Android/issues/459), [#642](https://github.com/scribe-org/Scribe-Android/issues/642), [#663](https://github.com/scribe-org/Scribe-Android/issues/663)).
- The keyboard can float via a key that is pressed when long holding the emoji key ([#261](https://github.com/scribe-org/Scribe-Android/issues/261)).
- Text proxy labels guide the user through Scribe commands ([#271](https://github.com/scribe-org/Scribe-Android/issues/271)).
- Invalid state labels explain to the user why a command hasn't worked ([#273](https://github.com/scribe-org/Scribe-Android/issues/273), [#274](https://github.com/scribe-org/Scribe-Android/issues/274), [#421](https://github.com/scribe-org/Scribe-Android/issues/421), [#553](https://github.com/scribe-org/Scribe-Android/issues/553)).
- Base keyboard functionality like double space periods, auto capitalization auto spacing have been included ([#20](https://github.com/scribe-org/Scribe-Android/issues/20), [#447](https://github.com/scribe-org/Scribe-Android/issues/447), [#669](https://github.com/scribe-org/Scribe-Android/issues/669)).
- Menu items have been provided to enable or disable keyboard functionalities ([#64](https://github.com/scribe-org/Scribe-Android/issues/64), [#65](https://github.com/scribe-org/Scribe-Android/issues/65)).
- Menu items have been provided to customize the keyboard UI ([#148](https://github.com/scribe-org/Scribe-Android/issues/148)).
- The application and community's relationship to the Wikimedia movement is explained in app ([#52](https://github.com/scribe-org/Scribe-Android/issues/52)).
- Vibrate on keypress and key click functionalities are included ([#405](https://github.com/scribe-org/Scribe-Android/issues/405), [#406](https://github.com/scribe-org/Scribe-Android/issues/406)).
- An in-app tutorial is provided to detail functionalities of the application ([#602](https://github.com/scribe-org/Scribe-Android/issues/602), [#615](https://github.com/scribe-org/Scribe-Android/issues/615), [#616](https://github.com/scribe-org/Scribe-Android/issues/616)).
- The user is able to easily rate the application ([#165](https://github.com/scribe-org/Scribe-Android/issues/165), [#640](https://github.com/scribe-org/Scribe-Android/issues/640)).

### 🗃️ Data

- SQLite databases have been set up for all data needed for the keyboards ([#87](https://github.com/scribe-org/Scribe-Android/issues/87)).
- Calls are made to the [Scribe-Server API](https://scribe-server.toolforge.org/) hosted on Toolforge to download language data and insert it into SQLite tables ([#547](https://github.com/scribe-org/Scribe-Android/issues/547), [#626](github.com/scribe-org/Scribe-Android/issues/626)).
- The user is directed to download data in the keyboard UI if they access it with empty databases ([#581](https://github.com/scribe-org/Scribe-Android/issues/581), [#650](https://github.com/scribe-org/Scribe-Android/issues/650)).
- The data download UI was created to download data for any keyboards that have been installed ([#437](https://github.com/scribe-org/Scribe-Android/issues/437), [#439](https://github.com/scribe-org/Scribe-Android/issues/439), [#513](https://github.com/scribe-org/Scribe-Android/issues/513), [#520](https://github.com/scribe-org/Scribe-Android/issues/520), [#554](https://github.com/scribe-org/Scribe-Android/issues/554)).
- Network indicators for data request have been added to the application and are shown via toasts ([#651](https://github.com/scribe-org/Scribe-Android/issues/651)).

### 🎨 Design

- The Scribe key and command bar where Scribe commands are triggered ([#16](https://github.com/scribe-org/Scribe-Android/issues/16)).
- 3x2 and other conjugation tables from which conjugations can be selected in the Conjugate command ([#267](https://github.com/scribe-org/Scribe-Android/issues/267), [#270](https://github.com/scribe-org/Scribe-Android/issues/270), [#496](https://github.com/scribe-org/Scribe-Android/issues/496), [#523](https://github.com/scribe-org/Scribe-Android/issues/523)).
- The return key is colored Scribe blue when commands are being triggered to let the user know that that is what they need to press to finish the command ([#160](https://github.com/scribe-org/Scribe-Android/issues/160)).
- Dark mode compatibility through a responsive color scheme ([#25](https://github.com/scribe-org/Scribe-Android/issues/25), [#51](https://github.com/scribe-org/Scribe-Android/issues/51), [#116](https://github.com/scribe-org/Scribe-Android/issues/116), [#121](https://github.com/scribe-org/Scribe-Android/issues/121), [#155](https://github.com/scribe-org/Scribe-Android/issues/155), [#161](https://github.com/scribe-org/Scribe-Android/issues/161), [#543](https://github.com/scribe-org/Scribe-Android/issues/543)).
- The application menu follows modern Android styling ([#114](https://github.com/scribe-org/Scribe-Android/issues/114), [#150](https://github.com/scribe-org/Scribe-Android/issues/150), [#217](https://github.com/scribe-org/Scribe-Android/issues/217), [#246](https://github.com/scribe-org/Scribe-Android/issues/246), [#247](https://github.com/scribe-org/Scribe-Android/issues/247), [248](https://github.com/scribe-org/Scribe-Android/issues/248), [#256](https://github.com/scribe-org/Scribe-Android/issues/256)).

### 🌐 Localization

- The application has been localized into many languages using [Weblate](https://weblate.org/en/) and the [Scribe-i18n](https://github.com/scribe-org/Scribe-i18n) project as a central Git submodule of localizations ([#44](https://github.com/scribe-org/Scribe-Android/issues/44), [#95](https://github.com/scribe-org/Scribe-Android/issues/95), [#214](https://github.com/scribe-org/Scribe-Android/issues/214), [#527](https://github.com/scribe-org/Scribe-Android/issues/527)).

### ✅ Tests

- [ktlint](https://github.com/ktlint/ktlint) and [detekt](https://github.com/detekt/detekt) linting was added to the project to validate code quality and standards ([#70](https://github.com/scribe-org/Scribe-Android/issues/70), [#354](https://github.com/scribe-org/Scribe-Android/issues/354)).
- Unit tests and code coverage were written for the project ([#181](https://github.com/scribe-org/Scribe-Android/issues/181), [#196](https://github.com/scribe-org/Scribe-Android/issues/196)).
- GitHub Actions based CI was set up with unit and instrumentation tests ([#195](https://github.com/scribe-org/Scribe-Android/issues/195), [#212](https://github.com/scribe-org/Scribe-Android/issues/212), [#422](https://github.com/scribe-org/Scribe-Android/issues/422), [#580](https://github.com/scribe-org/Scribe-Android/issues/580)).
- prek based pre-commit hooks were added to the repo to catch common mistakes on commit ([#215](https://github.com/scribe-org/Scribe-Android/issues/215)).
- A CI workflow to enforces `CHANGELOG.md` or `CHANGELOG_CONJUGATE.md` updates on all PRs targeting `main`, with support for a `no-changelog` label to skip the check when appropriate.

### 📝 Documentation

- Functions in the application have been documented ([#18](https://github.com/scribe-org/Scribe-Android/issues/18), [#354](https://github.com/scribe-org/Scribe-Android/issues/354)).

### ⚖️ Legal

- All code has been developed under the GNU General Public License (GPL-3.0) ([#301](https://github.com/scribe-org/Scribe-Android/issues/301)).
- The legal policies of the application are displayed to the user in the About tab ([#58](https://github.com/scribe-org/Scribe-Android/issues/58)).
- A privacy policy was provided to make clear that policies around user data and their security ([#59](https://github.com/scribe-org/Scribe-Android/issues/59)).
- Third party licensed code used in the development of the project were detailed ([#60](https://github.com/scribe-org/Scribe-Android/issues/60)).

### ♻️ Code Refactoring

- Code quality improvements were continuously done to assure that the application is easy to maintain and meets Kotlin standards ([#426](https://github.com/scribe-org/Scribe-Android/issues/426)).
