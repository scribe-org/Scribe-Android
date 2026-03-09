// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.french

/**
 * Interface variables for French language keyboards.
 */
object FRInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "espace"
    const val LANGUAGE = "Français"
    const val INVALID_COMMAND_MSG = "Pas dans Wiktionary"
    const val INVALID_COMMAND_MSG_WIKIDATA = "Pas dans Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata is a collaboratively edited knowledge graph that's maintained by the Wikimedia Foundation. It serves as a source of open data for projects like Wikipedia and countless others."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe uses Wikidata's language data for many of its core features. We get information like noun genders, verb conjugations and much more!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "You can make an account at wikidata.org to join the community that's supporting Scribe and so many other projects. Help us bring free information to the world!"
    const val INVALID_COMMAND_MSG_WIKTIONARY = "Pas dans Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary est un réseau de connaissances collaboratif géré par la fondation Wikimedia. Il sert de source de données ouvertes pour des projets tels que Wikipédia et bien d'autres."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe utilise les données linguistiques de Wiktionary pour un grand nombre de ses fonctionnalités de base. Nous obtenons des informations telles que le genre des noms, la conjugaison des verbes et bien plus encore !"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Vous pouvez créer un compte sur wiktionary.org pour rejoindre la communauté qui soutient Scribe et bien d'autres projets. Contribuez à la diffusion d'informations gratuites dans le monde entier !"
    val BASE_AUTOSUGGESTIONS = listOf("je", "il", "le")
    val NUMERIC_AUTOSUGGESTIONS = listOf("je", "que", "c’est")

    // MARK: Suggestion Pronouns

    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("être", "avoir", "ne")

    // MARK: Pronoun Conjugation

    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
            "je" to "presFPS",
            "tu" to "presSPS",
            "il" to "presTPS",
            "elle" to "presTPS",
            "on" to "presTPS",
            "nous" to "presFPP",
            "vous" to "presSPP",
            "ils" to "presTPP",
            "elles" to "presTPP",
        )

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Traduire"
    const val TRANSLATE_PLACEHOLDER = "Entrez un mot"
    const val TRANSLATE_PROMPT = "fr -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Conjuguer"
    const val CONJUGATE_PLACEHOLDER = "Entrez un verbe"
    const val CONJUGATE_PROMPT = "Conjuguer: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Pluriel"
    const val PLURAL_PLACEHOLDER = "Entrez un nom"
    const val PLURAL_PROMPT = "Pluriel: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Déjà pluriel"
}
