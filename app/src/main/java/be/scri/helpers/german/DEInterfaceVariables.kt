// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Interface variables for German language keyboards.
 */

package be.scri.helpers.german

object DEInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "Leerzeichen"
    const val LANGUAGE = "Deutsch"
    const val INVALID_COMMAND_MSG = "Nicht in Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("ich", "die", "das")
    val NUMERIC_AUTOSUGGESTIONS = listOf("Prozent", "Milionen", "Meter")

    // MARK: Suggestion Pronouns

    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("haben", "sein", "können")

    // MARK: Pronoun Conjugation

    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
            "ich" to "presFPS",
            "du" to "presSPS",
            "er" to "presTPS",
            "sie" to "presTPS",
            "es" to "presTPS",
            "wir" to "presFPP",
            "ihr" to "presSPP",
            "Sie" to "presTPP",
        )

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Übersetzen"
    const val TRANSLATE_PLACEHOLDER = "Wort eingeben"
    const val TRANSLATE_PROMPT = "de -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Konjugieren"
    const val CONJUGATE_PLACEHOLDER = "Verb eingeben"
    const val CONJUGATE_PROMPT = "Konjugieren: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Nomen eingeben"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Schon Plural"
}
