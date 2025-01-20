// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Interface variables for English language keyboards.
 */

package be.scri.helpers.english

object ENInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("$", "€", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "space"
    const val LANGUAGE = "English"
    const val INVALID_COMMAND_MSG = "Not in Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("I", "I'm", "we")
    val NUMERIC_AUTOSUGGESTIONS = listOf("is", "to", "and")

    // MARK: Suggestion Pronouns

    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("have", "be", "can")

    // MARK: Pronoun Conjugation

    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
            "I" to "presSimp",
            "you" to "presSimp",
            "he" to "presTPS",
            "she" to "presTPS",
            "it" to "presTPS",
            "we" to "presSimp",
            "they" to "presSimp",
        )

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Translate"
    const val TRANSLATE_PLACEHOLDER = "Enter a word"
    const val TRANSLATE_PROMPT = " en -› ${"targetLanguage()"}"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Conjugate"
    const val CONJUGATE_PLACEHOLDER = "Enter a verb"
    const val CONJUGATE_PROMPT = "Conjugate: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Enter a noun"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Already plural"
}
