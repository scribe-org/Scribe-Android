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

    const val INVALID_COMMAND_MSG_WIKIDATA = "Pas dans Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata est un réseau de connaissances collaboratif géré par la fondation Wikimedia. Il sert de source de données ouvertes pour des projets tels que Wikipédia et bien d'autres."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe utilise les données linguistiques de Wikidata pour un grand nombre de ses fonctionnalités de base. Nous obtenons des informations telles que le genre des noms, la conjugaison des verbes et bien plus encore !"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Vous pouvez créer un compte sur wikidata.org pour rejoindre la communauté qui soutient Scribe et bien d'autres projets. Contribuez à la diffusion d'informations gratuites dans le monde entier !"

    const val INVALID_COMMAND_MSG_WIKTIONARY = "Pas dans Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary est un dictionnaire collaboratif maintenu par la Fondation Wikimedia. Il sert de source de données linguistiques libres pour des projets comme Wikipédia et bien d'autres."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe utilise les données de Wiktionary pour fournir des traductions à sa commande « Traduire ». Nos données proviennent des nombreuses paires de langues créées par la communauté de Wiktionary !"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Vous pouvez créer un compte sur wiktionary.org pour rejoindre la communauté qui soutient Scribe et de nombreux autres projets. Aidez-nous à diffuser l'information libre dans le monde entier !"
    const val DOWNLOAD_DATA_MSG = "Veuillez télécharger les données linguistiques"

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
