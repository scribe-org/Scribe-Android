// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.german

/**
 * Interface variables for German language keyboards.
 */
object DEInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "Leerzeichen"
    const val LANGUAGE = "Deutsch"

    const val INVALID_COMMAND_MSG_WIKIDATA = "Nicht in Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata ist ein kollaborativ gestalteter, mehrsprachiger Wissensgraf, der von der Wikimedia Foundation gehostet wird. Sie dient als Quelle für offene Daten für unzählige Projekte, beispielsweise Wikipedia."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe nutzt Sprachdaten von Wikidata für viele Kernfunktionen. Von dort erhalten wir Informationen wie Genera, Verbkonjugationen und viele mehr!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Du kannst auf wikidata.org einen Account erstellen, um der Community, die Scribe und viele andere Projekte unterstützt, beizutreten. Hilf uns dabei, der Welt freie Informationen zu geben!"

    const val INVALID_COMMAND_MSG_WIKTIONARY = "Nicht in Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary ist ein gemeinschaftlich bearbeitetes Wörterbuch, das von der Wikimedia Foundation gepflegt wird. Es dient als Quelle freier Sprachdaten für Projekte wie Wikipedia und unzählige andere."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe verwendet die Daten von Wiktionary, um Übersetzungen für den Befehl „Übersetzen“ bereitzustellen. Unsere Daten stammen aus den vielen Sprachpaaren, die die Wiktionary-Community erstellt hat!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Erstellen Sie ein Konto auf wiktionary.org, um der Community beizutreten, die Scribe und viele andere Projekte unterstützt. Helfen Sie uns, freie Informationen in die Welt zu bringen!"
    const val DOWNLOAD_DATA_MSG = "Bitte Sprachdaten herunterladen"

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
