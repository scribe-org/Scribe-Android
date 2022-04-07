<div align="center">
  <a href="https://github.com/scribe-org/Scribe-Android"><img src="https://raw.githubusercontent.com/scribe-org/Organization/main/logo/ScribeAppLogo.png" width=512 height=230 alt="Scribe Logo"></a>
</div>

---

<!---
[![ci](https://img.shields.io/github/workflow/status/scribe-org/Scribe-Android/CI?logo=github)](https://github.com/scribe-org/Scribe-Android/actions?query=workflow%3ACI)
--->
<!-- [![version](https://img.shields.io/github/v/release/scribe-org/Scribe-Android?color=%2300550&sort=semver)](https://github.com/scribe-org/Scribe-Android/releases/) -->

[![platform](https://img.shields.io/badge/platform-Android-999999.svg)](https://github.com/scribe-org/Scribe-Android)
[![issues](https://img.shields.io/github/issues/scribe-org/Scribe-Android)](https://github.com/scribe-org/Scribe-Android/issues)
[![discussions](https://img.shields.io/github/discussions/scribe-org/Scribe-Android)](https://github.com/scribe-org/Scribe-Android/discussions)
[![language](https://img.shields.io/badge/Kotlin-1-A97AFE.svg?logo=kotlin&logoColor=ffffff)](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md)
[![license](https://img.shields.io/github/license/scribe-org/Scribe-Android.svg)](https://github.com/scribe-org/Scribe-Android/blob/main/LICENSE.txt)
[![coc](https://img.shields.io/badge/coc-Contributor%20Covenant-ff69b4.svg)](https://github.com/scribe-org/Scribe-Android/blob/main/.github/CODE_OF_CONDUCT.md)

<!-- <a href='https://play.google.com/store/apps'><img alt='Get it on Google Play' src='https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/Resources/GitHub/Images/google_play_badge.png' height='60px'/></a>
<a href='https://f-droid.org/packages/'><img src='https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/Resources/GitHub/Images/f_droid_badge.png' alt='Get it on F-Droid' height='60px' /></a> -->

Also available on [iOS](https://github.com/scribe-org/Scribe-iOS) and [Desktop](https://github.com/scribe-org/Scribe-Desktop) (planned).

# WIP port of [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS): see [Issues](https://github.com/scribe-org/Scribe-Android/issues)

### Language learning keyboards for Android

**Scribe-Android** is a pack of Android keyboards for language learners. Features include translation **`(beta)`**, verb conjugation and word annotation that give users the tools needed to communicate with confidence.

Scribe is fully open-source and does not collect usage data or ask for system access. Feature data is sourced from [Wikidata](https://www.wikidata.org/) and stored in-app, meaning Scribe is a highly responsive experience that does not require an internet connection.

See the [presentation slides](https://docs.google.com/presentation/d/16ld_rCbwJCiAdRrfhF-Fq9Wm_ciHCbk_HCzGQs6TB1Q/edit?usp=sharing) for [Wikidata Data Reuse Days 2022](https://diff.wikimedia.org/event/wikidata-data-reuse-days-2022/) and the articles about Scribe in the [Featured By](#featured-by) section for a full description of the project. Those interested in contributing are also welcome to join us in the [discussions](https://github.com/scribe-org/Scribe-Android/discussions)!

# **Contents**<a id="contents"></a>

<!-- - [Preview Videos](#preview-videos)
- [Setup](#setup)
  - [Base Functionality](#base-functionality) -->

- [Supported Languages](#supported-languages)
- [Keyboard Features](#keyboard-features)
  - [Translation](#translation)
  - [Verb Conjugation](#verb-conjugation)
  - [Noun Plurals](#noun-plurals)
  - [Word Annotation](#word-annotation)
- [Language Practice](#language-practice)
- [Contributing](#contributing)
- [Featured By](#featured-by)

# Supported Languages [`⇧`](#contents) <a id="supported-languages"></a>

Scribe's goal is functional, feature-rich keyboards for all languages. Check [scribe_data/extract_transform](https://github.com/scribe-org/Scribe-Data/tree/main/src/scribe_data/extract_transform) for queries for currently supported languages and those that have substantial data on [Wikidata](https://www.wikidata.org/).

The following table shows the supported languages and the amount of data available for each on [Wikidata](https://www.wikidata.org/):

| Languages  |   Nouns | Verbs | Translations\* | Adjectives† | Prepositions‡ |
| :--------- | ------: | ----: | -------------: | ----------: | ------------: |
| French     |  15,788 | 1,246 |         67,652 |           - |             - |
| German     |  28,089 | 3,130 |         67,652 |           - |           187 |
| Italian    |     783 |    71 |         67,652 |           - |             - |
| Portuguese |   4,662 |   189 |         67,652 |           - |             - |
| Russian    | 194,394 |    11 |         67,652 |           - |            12 |
| Spanish    |   9,452 | 2,062 |         67,652 |           - |             - |
| Swedish    |  41,187 | 4,138 |         67,652 |           - |             - |

`*` Given the current **`beta`** status where words are machine translated.

`†` Adjective-preposition support is in progress [(see iOS issue)](https://github.com/scribe-org/Scribe-iOS/issues/86).

`‡` Only for languages for which preposition annotation is needed.

Updates to the above data can be done using [scribe_data/load/update_data.py](https://github.com/scribe-org/Scribe-Data/tree/main/src/scribe_data/load/update_data.py).

# Keyboard Features [`⇧`](#contents) <a id="keyboard-features"></a>

Keyboard features are accessed via the `Scribe key` at the top left of any Scribe keyboard. Pressing this key gives the user three new selectable options: `Translate`, `Conjugate` and `Plural` in the keyboard's language. These keys allow for words to be queried and inserted into the text field followed by a space.

**Current features include:**

### • Translation [`⇧`](#contents) <a id="translation"></a>

The **`beta`** `Translate` feature can translate single words or phrases from English into the language of the current keyboard when the `return` key is pressed.

As of now translations are not widely available on [Wikidata](https://www.wikidata.org/) [(see iOS issue)](https://github.com/scribe-org/Scribe-iOS/issues/40). The current functionality is thus based on [🤗 Transformers](https://github.com/huggingface/transformers) machine translations of words queried from [Wikidata](https://www.wikidata.org/). The ultimate goal is for the translations and synonyms to all be directly queried.

### • Verb Conjugation [`⇧`](#contents) <a id="verb-conjugation"></a>

With the `Conjugate` feature, a user is presented with the grammar charts for an entered verb instead of the keyboard. Pressing an example in the charts inserts the chosen conjugation into the text field.

### • Noun Plurals [`⇧`](#contents) <a id="noun-plurals"></a>

The `Plural` feature allows a user to enter a noun and then insert its plural into the text field when the `return` key is pressed.

### • Word Annotation [`⇧`](#contents) <a id="word-annotation"></a>

Scribe further annotates words in the command bar to help users understand the context of what they're typing. Annotations are displayed once a user has typed a given word and pressed space or by pressing the `Scribe key` while it is selected. The hope is that annotation will help a user remember grammar rules even when not using Scribe.

#### Nouns

Scribe annotates nouns in the command bar according to the following conventions:

- Feminine: colored red 🟥 and marked with (F)
- Masculine: colored blue 🟦 and marked with (M)
- Common: colored purple 🟪 and marked with (C)
- Neutral: colored green 🟩 and marked with (N)
- Plural: colored orange 🟧 and marked with (PL)
- More than one: marked with all their forms

The above form abbreviations are translated into their equivalents in the keyboard's language.

#### Prepositions

Scribe also annotates the grammatical cases (accusative, dative, etc) of prepositions in the command bar if there is a relation in the given language.

# Language Practice [`⇧`](#contents) <a id="language-practice"></a>

A future feature of Scribe is language practice within the app itself. Scribe presents users with information that is directly relevant to their current struggles with a second language. This information can be saved in-app and used to create personalized lessons such as flashcards to reinforce the information that Scribe has provided.

# Contributing [`⇧`](#contents) <a id="contributing"></a>

Work that is in progress or could be implemented is tracked in the [Issues](https://github.com/scribe-org/Scribe-Android/issues). Please see the [contribution guidelines](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md) if you are interested in contributing to Scribe-Android. Also check the [`-next release-`](https://github.com/scribe-org/Scribe-Android/labels/-next%20release-) and [`-priority-`](https://github.com/scribe-org/Scribe-Android/labels/-priority-) labels in the [Issues](https://github.com/scribe-org/Scribe-Android/issues) for those that are most important, as well as those marked [`good first issue`](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) that are tailored for first time contributors.

### Ways to Help

- Join us in the [Discussions](https://github.com/scribe-org/Scribe-Android/discussions) 👋
- [Reporting bugs](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=bug&template=bug_report.yml) as they're found
- Working on [new features](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Afeature)
- [Localization](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Alocalization) for the app and Google Play
- [Documentation](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Adocumentation) for onboarding and project cohesion
- Adding language data to [Scribe-Data](https://github.com/scribe-org/Scribe-Data/issues) via [Wikidata](https://www.wikidata.org/)!

### Data Edits

Scribe does not accept direct edits to the grammar JSON files as they are sourced from [Wikidata](https://www.wikidata.org/). Edits can be discussed and the queries themselves will be changed and ran before an update. If there is a problem with one of the files, then the fix should be made on [Wikidata](https://www.wikidata.org/) and not on Scribe. Feel free to let us know that edits have been made by [opening a data issue](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=data&template=data_wikidata.yml) or contacting us in the [issues for Scribe-Data](https://github.com/scribe-org/Scribe-Data/issues) and we'll be happy to integrate them!

# Featured By [`⇧`](#contents) <a id="featured-by"></a>

<details><summary><strong>List of articles featuring Scribe</strong></summary>
<p>

- [Blog post](https://tech-news.wikimedia.de/en/2022/03/18/lexicographical-data-for-language-learners-the-wikidata-based-app-scribe/) on [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS) for [Wikimedia Tech News](https://tech-news.wikimedia.de/en/homepage/) ([DE](https://tech-news.wikimedia.de/2022/03/18/sprachenlernen-mit-lexikografische-daten-die-wikidata-basierte-app-scribe/) / [Tweet](https://twitter.com/wikidata/status/1507335538596106257?s=20&t=YGRGamftI-5B_VwQ_bFRhA))

</p>
</details>

<div align="center">
  <br>
  <a href="https://tech-news.wikimedia.de/en/2022/03/18/lexicographical-data-for-language-learners-the-wikidata-based-app-scribe/"><img height="100"src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/wikimedia_deutschland_logo.png" alt="Wikimedia Tech News"></a>
  <br>
</div>

# Powered By

<div align="center">
  <br>
  <a href="https://www.wikidata.org/"><img height="175" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/wikidata_logo.png" alt="Wikidata"></a>
  <br>
</div>
