<div align="center">
  <a href="https://github.com/scribe-org/Scribe-Android"><img src="https://raw.githubusercontent.com/scribe-org/Organization/main/logo/ScribeGitHubOrgBanner.png" width=1024 alt="Scribe Logo"></a>
</div>

[![platforms](https://img.shields.io/static/v1?message=Android&logo=android&color=32DE84&logoColor=white&label=%20)](https://github.com/scribe-org/Scribe-Android)
[![version](https://img.shields.io/github/v/release/scribe-org/Scribe-Android?color=%2300550&sort=semver&label=%20)](https://github.com/scribe-org/Scribe-Android/releases/)
[![issues](https://img.shields.io/github/issues/scribe-org/Scribe-Android?label=%20&logo=github)](https://github.com/scribe-org/Scribe-Android/issues)
[![language](https://img.shields.io/badge/Kotlin%201-A97AFE.svg?logo=kotlin&logoColor=ffffff)](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md)
[![license](https://img.shields.io/github/license/scribe-org/Scribe-Android.svg?label=%20)](https://github.com/scribe-org/Scribe-Android/blob/main/LICENSE.txt)
[![coc](https://img.shields.io/badge/Contributor%20Covenant-ff69b4.svg)](https://github.com/scribe-org/Scribe-Android/blob/main/.github/CODE_OF_CONDUCT.md)
[![weblate](https://img.shields.io/badge/Weblate-144D3F.svg?logo=weblate&logoColor=ffffff)](https://hosted.weblate.org/projects/scribe/scribe-i18n)
[![mastodon](https://img.shields.io/badge/Mastodon-6364FF.svg?logo=mastodon&logoColor=ffffff)](https://wikis.world/@scribe)
[![matrix](https://img.shields.io/badge/Matrix-000000.svg?logo=matrix&logoColor=ffffff)](https://matrix.to/#/#scribe_community:matrix.org)

<!-- <a href='https://play.google.com/store/apps'><img alt='Get it on Google Play' src='https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/.github/resources/images/google_play_badge.png' height='60px'/></a>
<a href='https://f-droid.org/packages/'><img src='https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/.github/resources/images/f_droid_badge.png' alt='Get it on F-Droid' height='60px' /></a> -->

<details><summary>🌐 Language</summary>
<p>

-   [Open a localization issue](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=localization&template=localization.yml) to add a new readme to the [README](https://github.com/scribe-org/Scribe-Android/tree/main/README) directory

</p>
</details>

### Android app with keyboards for language learners

#### WIP port of [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS): see [Issues](https://github.com/scribe-org/Scribe-Android/issues)

**Scribe-Android** is a pack of Android keyboards for language learners. Features include translation **`(beta)`**, verb conjugation and word annotation that give users the tools needed to communicate with confidence.

Scribe is fully open-source and does not collect usage data or ask for system access. Feature data is sourced from [Wikidata](https://www.wikidata.org/) and stored in-app, meaning Scribe is a highly responsive experience that does not require an internet connection.

> [!NOTE]\
> The [contributing](#contributing) section has information for those interested, with the articles and presentations in [featured by](#featured-by) also being good resources for learning more about Scribe.

Also available on [iOS](https://github.com/scribe-org/Scribe-iOS), [Desktop](https://github.com/scribe-org/Scribe-Desktop) (planned) and for the data processes see [Scribe-Data](https://github.com/scribe-org/Scribe-Data).

Check out Scribe's [architecture diagrams](https://github.com/scribe-org/Organization/blob/main/ARCHITECTURE.md) for an overview of the organization including our applications, services and processes. It depicts the projects that [Scribe](https://github.com/scribe-org) is developing as well as the relationships between them and the external systems with which they interact.

<a id="contents"></a>

# **Contents**

-   [Preview Images](#preview-images)
-   [Contributing](#contributing)
-   [Environment Setup](#environment-setup)
-   [App Setup](#app-setup)
-   [Keyboard Features](#keyboard-features)
-   [Featured By](#featured-by)

<a id="preview-images"></a>

# Preview Images [`⇧`](#contents)

### Current WIP status

<div align="center">
  <br>
  <a href="https://github.com/scribe-org/Scribe-Android/blob/main/.github/resources/images/android_preview.png"><img width="548" height="auto" src="https://raw.githubusercontent.com/scribe-org/Scribe-Android/main/.github/resources/images/android_preview.png" alt="Android Preview"></a>
  <br>
</div>

<a id="contributing"></a>

# Contributing [`⇧`](#contents)

<a href="https://matrix.to/#/#scribe_community:matrix.org"><img src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/MatrixLogoGrey.png" height="50" alt="Public Matrix Chat" align="right"></a>

Scribe uses [Matrix](https://matrix.org/) for communications. You're more than welcome to [join us in our public chat rooms](https://matrix.to/#/#scribe_community:matrix.org) to share ideas, ask questions or just say hi :)

Please see the [contribution guidelines](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md) if you are interested in contributing to Scribe-Android. Work that is in progress or could be implemented is tracked in the [issues](https://github.com/scribe-org/Scribe-Android/issues) and [projects](https://github.com/scribe-org/Scribe-Android/projects).

> [!NOTE]\
> Just because an issue is assigned on GitHub doesn't mean that the team isn't interested in your contribution! Feel free to write [in the issues](https://github.com/scribe-org/Scribe-Android/issues) and we can potentially reassign it to you.

Those interested can further check the [`-next release-`](https://github.com/scribe-org/Scribe-Android/labels/-next%20release-) and [`-priority-`](https://github.com/scribe-org/Scribe-Android/labels/-priority-) labels in the [issues](https://github.com/scribe-org/Scribe-Android/issues) for those that are most important, as well as those marked [`good first issue`](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) that are tailored for first time contributors. For those new to coding or our tech stack, we've collected [links to helpful documentation pages](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md#learning-the-tech) in the [contribution guidelines](https://github.com/scribe-org/Scribe-Android/blob/main/CONTRIBUTING.md).

After your first few pull requests organization members would be happy to discuss granting you further rights as a contributor, with a maintainer role then being possible after continued interest in the project. Scribe seeks to be an inclusive and supportive organization. We'd love to have you on the team!

### Ways to Help [`⇧`](#contents)

-   [Reporting bugs](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=bug&template=bug_report.yml) as they're found 🐞
-   Working on [new features](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Afeature) ✨
-   [Localization](https://github.com/scribe-org/Scribe-i18n) for the app and App Store via our [Weblate project](https://hosted.weblate.org/projects/scribe/scribe-i18n) 🌐
-   [Documentation](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aissue+is%3Aopen+label%3Adocumentation) for onboarding and project cohesion 📝
-   Adding language data to [Scribe-Data](https://github.com/scribe-org/Scribe-Data/issues) via [Wikidata](https://www.wikidata.org/)! 🗃️
-   [Sharing Scribe-Android](https://github.com/scribe-org/Scribe-iOS/issues/62) with others! 🚀

### Road Map [`⇧`](#contents)

The Scribe road map can be followed in the organization's [project board](https://github.com/orgs/scribe-org/projects/1) where we list the most important issues along with their priority, status and an indication of which sub projects they're included in (if applicable).

> [!NOTE]\
> Consider joining our [bi-weekly developer syncs](https://etherpad.wikimedia.org/p/scribe-dev-sync)!

### Designs [`⇧`](#contents)

<a href="https://www.figma.com/file/c8945w2iyoPYVhsqW7vRn6/scribe_public_designs?node-id=405%3A464"><img src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/FigmaLogo.png" height="50" alt="Public Figma Designs" align="right"></a>

The [designs for Scribe](https://www.figma.com/file/c8945w2iyoPYVhsqW7vRn6/scribe_public_designs?node-id=405%3A464) are made using [Figma](https://www.figma.com). Those with interest in contributing can [open a design issue](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=design&template=design_improvement.yml) to make suggestions! Design related issues are marked with the [`design`](https://github.com/scribe-org/Scribe-Android/issues?q=is%3Aopen+is%3Aissue+label%3Adesign) label.

### Data Edits [`⇧`](#contents)

> [!NOTE]\
> Please see the [Wikidata and Scribe Guide](https://github.com/scribe-org/Organization/blob/main/WIKIDATAGUIDE.md) for an overview of [Wikidata](https://www.wikidata.org/) and how Scribe uses it.

Scribe does not accept direct edits to the grammar JSON files as they are sourced from [Wikidata](https://www.wikidata.org/). Edits can be discussed and the [Scribe-Data](https://github.com/scribe-org/Scribe-Data) queries will be changed and ran before an update. If there is a problem with one of the files, then the fix should be made on [Wikidata](https://www.wikidata.org/) and not on Scribe. Feel free to let us know that edits have been made by [opening a data issue](https://github.com/scribe-org/Scribe-Android/issues/new?assignees=&labels=data&template=data_wikidata.yml) or contacting us in the [issues for Scribe-Data](https://github.com/scribe-org/Scribe-Data/issues) and we'll be happy to integrate them!

<a id="environment-setup"></a>

# Environment Setup [`⇧`](#contents)

Scribe-Android is developed using the [Kotlin](https://kotlinlang.org/) coding language. Those new to Kotlin or wanting to develop their skills are more than welcome to contribute! The first step on your Kotlin journey would be to read through the [Kotlin documentation](https://kotlinlang.org/docs/getting-started.html). The general steps to setting up a development environment are:

1. Download [Android Studio](https://developer.android.com/studio)

2. [Fork](https://docs.github.com/en/get-started/quickstart/fork-a-repo) the [Scribe-Android repo](https://github.com/scribe-org/Scribe-Android), clone your fork, and configure the remotes:

> [!NOTE]
>
> <details><summary>Consider using SSH</summary>
>
> <p>
>
> Alternatively to using HTTPS as in the instructions below, consider SSH to interact with GitHub from the terminal. SSH allows you to connect without a user-pass authentication flow.
>
> To run git commands with SSH, remember then to substitute the HTTPS URL, `https://github.com/...`, with the SSH one, `git@github.com:...`.
>
> -   e.g. Cloning now becomes `git clone git@github.com:<your-username>/Scribe-Android.git`
>
> GitHub also has their documentation on how to [Generate a new SSH key](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent) 🔑
>
> </p>
> </details>

```bash
# Clone your fork of the repo into the current directory.
git clone https://github.com/<your-username>/Scribe-Android.git
# Navigate to the newly cloned directory.
cd Scribe-Android
# Assign the original repo to a remote called "upstream".
git remote add upstream https://github.com/scribe-org/Scribe-Android.git
```

-   Now, if you run `git remote -v` you should see two remote repositories named:
    -   `origin` (forked repository)
    -   `upstream` (Scribe-Android repository)

3. Open the Scribe-Android directory in Android Studio

4. In order to [run Scribe on an emulator](https://developer.android.com/studio/run/emulator):

    - In the top bar find and select the "Device Manager" option
    - [Create a device](https://developer.android.com/studio/run/managing-avds) and select it once it's been made
    - Press the play button marked "Run App"
    - From here code edits that are made will be reflected in the app each time it is ran.

After activating your emulator, consider setting up [pre-commit](https://pre-commit.com/) to fix common errors in the codebase before they're committed by running:

```bash
pip install --upgrade pip  # make sure that pip is at the latest version
pip install pre-commit
pre-commit install  # install pre-commit hooks
# pre-commit run --all-files  # lint and fix common problems in the codebase
```

> [!NOTE]
> Feel free to contact the team in the [Android room on Matrix](https://matrix.to/#/#ScribeAndroid:matrix.org) if you're having problems getting your environment setup!

<a id="app-setup"></a>

# App Setup [`⇧`](#contents)

Users access Scribe language keyboards through the following:

-   Open the app and press **`Enable Keyboard`**
    -   Or: Settings -> System -> Languages & input -> On-screen keyboard -> Manage on-screen keyboards
-   Choose from the available Scribe language keyboards
-   When typing press 🌐 or the keyboard button to select keyboards

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

<details open><summary><strong>Articles and Presentations on Scribe</strong></summary>
<p>

<strong>2024</strong>

-   April: [Blog post on Medium](https://medium.com/@mhmohona/scribe-data-a-guide-to-open-source-language-data-a801c59db4c9) about [Scribe-Data](https://github.com/scribe-org/Scribe-Data) and its functionalities
-   February: [Presentation slides](https://docs.google.com/presentation/d/1lMhYiQx1R99SVGhbikUGjOVaFgPPASvbzM2Bsu3NXSg/edit?usp=sharing) for Scribe's participation at the [Wikimedia Tech Safari Program](https://www.mediawiki.org/wiki/Wikimedia_Tech_Safari_Program)

<strong>2023</strong>

-   August: [Scribe-iOS final submission report for Google Summer of Code 2023](https://saurabhjamadagni.hashnode.dev/gsoc-23-final-work-submission)
-   June: [Scribe-iOS development blog post on Nested UITableViews & Apple's built-in ViewControllers in app menu](https://saurabhjamadagni.hashnode.dev/nested-uitableviews-apples-built-in-viewcontrollers) for [GSoC '23](https://www.mediawiki.org/wiki/Google_Summer_of_Code/2023#Accepted_projects:~:text=links%3A%20Phabricator%20issue-,3.%20Adding%20a%20Menu%20and%20Keyboards%20to%20Scribe%2DiOS,-%5Bedit%5D)
-   March: [Presentation slides](https://docs.google.com/presentation/d/1W4ZkGi9UDDiTxM_silEij0gTE8YEubluHxe78xoqEP0/edit?usp=sharing) for a talk at [Berlin Hack and Tell](https://bhnt.c-base.org/) ([Hack of the month winner 🏆](https://bhnt.c-base.org/2023-03-28-no87-moore-hacks))

<strong>2022</strong>

-   August: [Presentation slides](https://docs.google.com/presentation/d/12WNSt5xgNIAmSxPfvjno9-sBMGlvxG_xSaAxmHQDRNQ/edit?usp=sharing) for a session at the [2022 Wikimania Hackathon](https://wikimania.wikimedia.org/wiki/2022:Hackathon)
-   July: [Presentation slides](https://docs.google.com/presentation/d/10Ai0-b8XUj5u9Hw4UgBtB7ufiPhvfFrb1vEUEyXYr5w/edit?usp=sharing) for a talk at [CocoaHeads Berlin](https://www.meetup.com/cocoaheads-berlin/)
-   July: [Video on Scribe](https://www.youtube.com/watch?v=4GpFN0gGmy4&list=PL66MRMNlLyR7p9wsYVfuqJOjKZpbuwp8U&index=6) for [Wikimedia Celtic Knot 2022](https://meta.wikimedia.org/wiki/Celtic_Knot_Conference_2022)
-   June: [Presentation slides](https://docs.google.com/presentation/d/1K2lj8PPgdx12I-xuhm--CBLrGm-Cz50NJmbp96zpGrk/edit?usp=sharing) for a talk with the [LD4 Wikidata Affinity Group](https://www.wikidata.org/wiki/Wikidata:WikiProject_LD4_Wikidata_Affinity_Group)
-   June: [Scribe](https://github.com/scribe-org) featured for new developers on [MediaWiki](https://www.mediawiki.org/wiki/New_Developers#Scribe)
-   May: [Presentation slides](https://docs.google.com/presentation/d/1Cu3VwQ3lJUp5W84YDe0AFYS-6zfBxKsm0MI-OMl_IzY/edit?usp=sharing) for [Wikimedia Hackathon 2022](https://www.mediawiki.org/wiki/Wikimedia_Hackathon_2022)
-   March: [Blog post](https://tech-news.wikimedia.de/en/2022/03/18/lexicographical-data-for-language-learners-the-wikidata-based-app-scribe/) on [Scribe-iOS](https://github.com/scribe-org/Scribe-iOS) for [Wikimedia Tech News](https://tech-news.wikimedia.de/en/homepage/) ([DE](https://tech-news.wikimedia.de/2022/03/18/sprachenlernen-mit-lexikografische-daten-die-wikidata-basierte-app-scribe/) / [Tweet](https://twitter.com/wikidata/status/1507335538596106257?s=20&t=YGRGamftI-5B_VwQ_bFRhA))
-   March: [Presentation slides](https://docs.google.com/presentation/d/16ld_rCbwJCiAdRrfhF-Fq9Wm_ciHCbk_HCzGQs6TB1Q/edit?usp=sharing) for [Wikidata Data Reuse Days 2022](https://diff.wikimedia.org/event/wikidata-data-reuse-days-2022/)

</p>
</details>

<div align="center">
  <br>
    <a href="https://tech-news.wikimedia.de/en/2022/03/18/lexicographical-data-for-language-learners-the-wikidata-based-app-scribe/"><img height="120"src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/WikimediaDeutschlandLogo.png" alt="Wikimedia Deutschland logo linking to an article on Scribe in the tech news blog."></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="https://www.mediawiki.org/wiki/New_Developers#Scribe"><img height="120" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/WikimediaFoundationLogo.png" alt="Wikimedia Foundation logo linking to the MediaWiki new developers page."></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="https://summerofcode.withgoogle.com/"><img height="120" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/GSoCLogo.png" alt="Google Summer of Code logo linking to its website."></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <br>
</div>

# Powered By [`⇧`](#contents)

### Contributors

Many thanks to all the [Scribe-Android contributors](https://github.com/scribe-org/Scribe-Android/graphs/contributors)! 🚀

<a href="https://github.com/scribe-org/Scribe-Android/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=scribe-org/Scribe-Android" />
</a>

### Code

<details><summary><strong>List of referenced code</strong></summary>
<p>

-   [Simple-Keyboard](https://github.com/SimpleMobileTools/Simple-Keyboard) by [SimpleMobileTools](https://github.com/SimpleMobileTools) ([License](https://github.com/SimpleMobileTools/Simple-Keyboard/blob/main/LICENSE))
-   [Simple-Commons](https://github.com/SimpleMobileTools/Simple-Commons) by [SimpleMobileTools](https://github.com/SimpleMobileTools) ([License](https://github.com/SimpleMobileTools/Simple-Commons/blob/master/LICENSE))

</p>
</details>

### Wikimedia Communities

<div align="center">
  <br>
  <a href="https://www.wikidata.org/"><img height="175" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/WikidataLogo.png" alt="Wikidata logo"></a>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://www.wikipedia.org/"><img height="190" src="https://raw.githubusercontent.com/scribe-org/Organization/main/resources/images/logos/WikipediaLogo.png" alt="Wikipedia logo"></a>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <br>
</div>
