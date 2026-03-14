// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.english

/**
 * Interface variables for English language keyboards.
 */
object ENInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("$", "€", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "space"
    const val LANGUAGE = "English"
    const val INVALID_COMMAND_MSG = "Not in Wiktionary"
    const val INVALID_COMMAND_MSG_WIKIDATA = "Not in Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata is a collaboratively edited knowledge graph that's maintained by the Wikimedia Foundation. It serves as a source of open data for projects like Wikipedia and countless others."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe uses Wikidata's language data for many of its core features. We get information like noun genders, verb conjugations and much more!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "You can make an account at wikidata.org to join the community that's supporting Scribe and so many other projects. Help us bring free information to the world!"
    const val INVALID_COMMAND_MSG_WIKTIONARY = "Not in Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary is a collaboratively edited dictionary that's maintained by the Wikimedia Foundation. It serves as a source of free linguistic data for projects like Wikipedia and countless others."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe uses Wiktionary's data to provide translations for its Translate command. Our data is derived from the many language pairs that Wiktionary's community has created!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "You can make an account at wiktionary.org to join the community that's supporting Scribe and so many other projects. Help us bring free information to the world!"
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
