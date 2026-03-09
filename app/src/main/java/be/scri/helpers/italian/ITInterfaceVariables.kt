// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.italian

/**
 * Interface variables for Italian language keyboards.
 */
object ITInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "spazio"
    const val LANGUAGE = "Italiano"
    const val INVALID_COMMAND_MSG = "Non in Wiktionary"
    const val INVALID_COMMAND_MSG_WIKIDATA = "Non in Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata is a collaboratively edited knowledge graph that's maintained by the Wikimedia Foundation. It serves as a source of open data for projects like Wikipedia and countless others."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe uses Wikidata's language data for many of its core features. We get information like noun genders, verb conjugations and much more!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "You can make an account at wikidata.org to join the community that's supporting Scribe and so many other projects. Help us bring free information to the world!"
    const val INVALID_COMMAND_MSG_WIKTIONARY = "Non in Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary is a collaboratively edited dictionary that's maintained by the Wikimedia Foundation. It serves as a source of free linguistic data for projects like Wikipedia and countless others."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe uses Wiktionary's data to provide translations for its Translate command. Our data is derived from the many language pairs that Wiktionary's community has created!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "You can make an account at wiktionary.org to join the community that's supporting Scribe and so many other projects. Help us bring free information to the world!"
    val BASE_AUTOSUGGESTIONS = listOf("ho", "non", "ma")
    val NUMERIC_AUTOSUGGESTIONS = listOf("utenti", "anni", "e")

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Tradurre"
    const val TRANSLATE_PLACEHOLDER = "Inserisci una parola"
    const val TRANSLATE_PROMPT = "it -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Coniugare"
    const val CONJUGATE_PLACEHOLDER = "Inserisci un verbo"
    const val CONJUGATE_PROMPT = "Coniugare: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plurale"
    const val PLURAL_PLACEHOLDER = "Inserisci un nome"
    const val PLURAL_PROMPT = "Plurale: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Già plurale"
}
