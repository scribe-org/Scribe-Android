<div align="center">
  <a href="https://github.com/scribe-org/Scribe-Android"><img src="https://raw.githubusercontent.com/scribe-org/Organization/main/logo/ScribeGitHubOrgBanner.png" width=1024 alt="Scribe Logo"></a>
</div>

<!---
[![ci](https://img.shields.io/github/workflow/status/scribe-org/Scribe-Android/CI?logo=github)](https://github.com/scribe-org/Scribe-Android/actions?query=workflow%3ACI)
--->
<!-- [![version](https://img.shields.io/github/v/release/scribe-org/Scribe-Android?color=%2300550&sort=semver)](https://github.com/scribe-org/Scribe-Android/releases/) -->

[![platforms](https://img.shields.io/static/v1?message=Android&logo=android&color=32DE84&logoColor=white&label=%20)](https://github.com/scribe-org/Scribe-Android)
[![issues](https://img.shields.io/github/issues/scribe-org/Scribe-Android?label=%20&logo=github)](https://github.com/scribe-org/Scribe-Android/issues)
[![language](https://img.shields.io/badge/Kotlin%201-A97AFE.svg?logo=kotlin&logoColor=ffffff)](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md)
[![license](https://img.shields.io/github/license/scribe-org/Scribe-Android.svg?label=%20)](https://github.com/scribe-org/Scribe-Android/blob/main/LICENSE.txt)
[![coc](https://img.shields.io/badge/Contributor%20Covenant-ff69b4.svg)](https://github.com/scribe-org/Scribe-Android/blob/main/.github/CODE_OF_CONDUCT.md)

<!-- <a href='https://play.google.com/store/apps'><img alt='Get it on Google Play' src='https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/.github/resources/images/google_play_badge.png' height='60px'/></a>
<a href='https://f-droid.org/packages/'><img src='https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/.github/resources/images/f_droid_badge.png' alt='Get it on F-Droid' height='60px' /></a> -->

## Android app with keyboards for language learners

### WIP port of [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS): see [Issues](https://github.com/scribe-org/Scribe-Android/issues)

**Scribe-Android** is a pack of Android keyboards for language learners. Features include translation **`(beta)`**, verb conjugation and word annotation that give users the tools needed to communicate with confidence.

Scribe is fully open-source and does not collect usage data or ask for system access. Feature data is sourced from [Wikidata](https://www.wikidata.org/) and stored in-app, meaning Scribe is a highly responsive experience that does not require an internet connection.

The [contributing](#contributing) section has information for those interested, with the articles and presentations in [featured by](#featured-by) also being good resources for learning more about Scribe.

Also available on [iOS](https://github.com/scribe-org/Scribe-iOS), [Desktop](https://github.com/scribe-org/Scribe-Desktop) (planned) and for the data processes see [Scribe-Data](https://github.com/scribe-org/Scribe-Data).

<a id="contents"></a>

# **Contents**

-   [Preview Images](#preview-images)
-   [Contributing](#contributing)
-   [Setup](#setup)
-   [Keyboard Features](#keyboard-features)
    -   [Base Functionality](#base-functionality)
-   [Featured By](#featured-by)

<a id="preview-images"></a>

# Preview Images [`⇧`](#contents)

#### Current WIP status

<div align="center">
  <br>
  <a href="https://github.com/scribe-org/Scribe-Android/blob/main/.github/resources/images/android_preview.png"><img height="512" src="https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/.github/resources/images/android_preview.png" alt="Android Preview"></a>
  <br>
</div>

<a id="contributing"></a>

# Contributing [`⇧`](#contents)

Work that is in progress or could be implemented is tracked in the [issues](https://github.com/scribe-org/Scribe-Android/issues) and [projects](https://github.com/scribe-org/Scribe-Android/projects). Please also see the [contribution guidelines](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md) if you are interested in contributing to Scribe-Android. Those interested can further check the [`-next release-`](https://github.com/scribe-org/Scribe-Android/labels/-next%20release-) and [`-priority-`](https://github.com/scribe-org/Scribe-Android/labels/-priority-) labels in the [issues](https://github.com/scribe-org/Scribe-Android/issues) for those that are most important, as well as those marked [`good first issue`](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) that are tailored for first time contributors.

### Ways to Help

-   [Reporting bugs](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=bug&template=bug_report.yml) as they're found 🐞
-   Working on [new features](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Afeature) ✨
-   [Localization](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Alocalization) for the app and Google Play 🌐
-   [Documentation](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Adocumentation) for onboarding and project cohesion 📝
-   Adding language data to [Scribe-Data](https://github.com/scribe-org/Scribe-Data/issues) via [Wikidata](https://www.wikidata.org/)! 🗃️

### Data Edits

Scribe does not accept direct edits to the grammar JSON files as they are sourced from [Wikidata](https://www.wikidata.org/). Edits can be discussed and the [Scribe-Data](https://github.com/scribe-org/Scribe-Data) queries will be changed and ran before an update. If there is a problem with one of the files, then the fix should be made on [Wikidata](https://www.wikidata.org/) and not on Scribe. Feel free to let us know that edits have been made by [opening a data issue](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=data&template=data_wikidata.yml) or contacting us in the [issues for Scribe-Data](https://github.com/scribe-org/Scribe-Data/issues) and we'll be happy to integrate them!

### Designs

<a href="https://www.figma.com/file/c8945w2iyoPYVhsqW7vRn6/scribe_public_designs?node-id=405%3A464"><img src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/figma_logo.png" height="50" alt="Public Figma Designs" align="right"></a>

The [designs for Scribe](https://www.figma.com/file/c8945w2iyoPYVhsqW7vRn6/scribe_public_designs?node-id=405%3A464) are made using [Figma](https://www.figma.com). Those with interest in contributing can [open a design issue](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=design&template=design_improvement.yml) to make suggestions! Design related issues are marked with the [`design`](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aopen+is%3Aissue+label%3Adesign) label.

<a id="setup"></a>

# Setup [`⇧`](#contents)

Users access Scribe language keyboards through the following:

-   Open the app and press **`Enable Keyboard`**
    -   Or: Settings -> System -> Languages & input -> On-screen keyboard -> Manage on-screen keyboards
-   Choose from the available Scribe language keyboards
-   When typing press `🌐` or the keyboard button to select keyboards

<a id="keyboard-features"></a>

# Keyboard Features [`⇧`](#contents)

Scribe-Android is currently a work in progress and only has base keyboard functionality. The goal is to slowly add Scribe features before an initial release, and then iterate to match the functionality of [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS).

**Current features include:**

### • Base Functionality [`⇧`](#contents)

The goal is for Scribe to have all the functionality of system keyboards. Currently the keyboards include:

-   Dynamic layouts for cross-device performance
-   Auto-capitalization following `.`, `?` and `!`
-   Typing symbols and numbers followed by a space returns keyboard to letters
-   Hold-to-select characters for letters and symbols
-   Key pop up views for letters and symbols

<a id="featured-by"></a>

# Featured By [`⇧`](#contents)

<details><summary><strong>Articles and Presentations on Scribe</strong></summary>
<p>

<strong>2022</strong>

-   [Presentation slides](https://docs.google.com/presentation/d/12WNSt5xgNIAmSxPfvjno9-sBMGlvxG_xSaAxmHQDRNQ/edit?usp=sharing) for a session at the [2022 Wikimania Hackathon](https://wikimania.wikimedia.org/wiki/Hackathon)
-   [Presentation slides](https://docs.google.com/presentation/d/10Ai0-b8XUj5u9Hw4UgBtB7ufiPhvfFrb1vEUEyXYr5w/edit?usp=sharing) for a talk with [CocoaHeads Berlin](https://www.meetup.com/cocoaheads-berlin/)
-   [Video on Scribe](https://www.youtube.com/watch?v=4GpFN0gGmy4&list=PL66MRMNlLyR7p9wsYVfuqJOjKZpbuwp8U&index=6) for [Wikimedia Celtic Knot 2022](https://meta.wikimedia.org/wiki/Celtic_Knot_Conference_2022)
-   [Presentation slides](https://docs.google.com/presentation/d/1K2lj8PPgdx12I-xuhm--CBLrGm-Cz50NJmbp96zpGrk/edit?usp=sharing) for a talk with the [LD4 Wikidata Affinity Group](https://www.wikidata.org/wiki/Wikidata:WikiProject_LD4_Wikidata_Affinity_Group)
-   [Scribe](https://github.com/scribe-org) featured for new developers on [MediaWiki](https://www.mediawiki.org/wiki/New_Developers)
-   [Presentation slides](https://docs.google.com/presentation/d/1Cu3VwQ3lJUp5W84YDe0AFYS-6zfBxKsm0MI-OMl_IzY/edit?usp=sharing) for [Wikimedia Hackathon 2022](https://www.mediawiki.org/wiki/Wikimedia_Hackathon_2022)
-   [Blog post](https://tech-news.wikimedia.de/en/2022/03/18/lexicographical-data-for-language-learners-the-wikidata-based-app-scribe/) on [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS) for [Wikimedia Tech News](https://tech-news.wikimedia.de/en/homepage/) ([DE](https://tech-news.wikimedia.de/2022/03/18/sprachenlernen-mit-lexikografische-daten-die-wikidata-basierte-app-scribe/) / [Tweet](https://twitter.com/wikidata/status/1507335538596106257?s=20&t=YGRGamftI-5B_VwQ_bFRhA))
-   [Presentation slides](https://docs.google.com/presentation/d/16ld_rCbwJCiAdRrfhF-Fq9Wm_ciHCbk_HCzGQs6TB1Q/edit?usp=sharing) for [Wikidata Data Reuse Days 2022](https://diff.wikimedia.org/event/wikidata-data-reuse-days-2022/)

</p>
</details>

<div align="center">
  <br>
    <a href="https://tech-news.wikimedia.de/en/2022/03/18/lexicographical-data-for-language-learners-the-wikidata-based-app-scribe/"><img height="120"src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/wikimedia_deutschland_logo.png" alt="Wikimedia Tech News"></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="https://www.mediawiki.org/wiki/New_Developers"><img height="120" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/mediawiki_logo.png" alt="MediaWiki"></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <br>
</div>

# Powered By

<details><summary><strong>List of referenced codes</strong></summary>
<p>

-   [Simple-Keyboard](https://github.com/SimpleMobileTools/Simple-Keyboard) by [SimpleMobileTools](https://github.com/SimpleMobileTools) ([License](https://github.com/SimpleMobileTools/Simple-Keyboard/blob/main/LICENSE))
-   [Simple-Commons](https://github.com/SimpleMobileTools/Simple-Commons) by [SimpleMobileTools](https://github.com/SimpleMobileTools) ([License](https://github.com/SimpleMobileTools/Simple-Commons/blob/master/LICENSE))

</p>
</details>

<div align="center">
  <br>
  <a href="https://www.wikidata.org/"><img height="175" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/wikidata_logo.png" alt="Wikidata logo"></a>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://www.wikipedia.org/"><img height="190" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/wikipedia_logo.png" alt="Wikipedia logo"></a>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <br>
</div>
