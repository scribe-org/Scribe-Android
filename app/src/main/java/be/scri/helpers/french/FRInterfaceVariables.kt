// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Interface variables for French language keyboards.
 */

package be.scri.helpers.french

object FRInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "espace"
    const val LANGUAGE = "Français"
    const val INVALID_COMMAND_MSG = "Pas dans Wikidata"
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
