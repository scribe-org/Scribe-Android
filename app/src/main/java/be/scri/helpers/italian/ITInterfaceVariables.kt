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

    const val INVALID_COMMAND_MSG_WIKIDATA = "Non in Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata è un grafo della conoscenza modificabile in modo collaborativo, gestito dalla Wikimedia Foundation. Serve come fonte di dati aperti per progetti come Wikipedia e innumerevoli altri."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe utilizza i dati linguistici di Wikidata per molte delle sue funzionalità principali. Otteniamo informazioni come il genere dei sostantivi, la coniugazione dei verbi e molto altro!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Puoi creare un account su wikidata.org per unirti alla comunità che supporta Scribe e tanti altri progetti. Aiutaci a diffondere informazioni libere in tutto il mondo!"

    const val INVALID_COMMAND_MSG_WIKTIONARY = "Non in Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary è un dizionario modificato in modo collaborativo e gestito dalla Wikimedia Foundation. Serve come fonte di dati linguistici liberi per progetti come Wikipedia e innumerevoli altri."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe utilizza i dati di Wiktionary per fornire traduzioni per il suo comando Traduci. I nostri dati derivano dalle numerose coppie linguistiche create dalla community di Wiktionary!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Puoi creare un account su wiktionary.org per unirti alla community che supporta Scribe e tanti altri progetti. Aiutaci a diffondere informazioni libere in tutto il mondo!"

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
