// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.spanish

/**
 * Interface variables for Spanish language keyboards.
 */
object ESInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("₡", "S", "€", "£", "₲", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "espacio"
    const val LANGUAGE = "Español"

    const val INVALID_COMMAND_MSG_WIKIDATA = "No en Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata es un gráfico de conocimiento editado de forma colaborativa y mantenido por la Fundación Wikimedia. Sirve como fuente de datos abiertos para proyectos como Wikipedia y muchos otros."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe utiliza los datos lingüísticos de Wikidata para muchas de sus funciones principales. ¡Obtenemos información como géneros de sustantivos, conjugaciones de verbos y mucho más!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Puedes crear una cuenta en wikidata.org para unirte a la comunidad que apoya a Scribe y a muchos otros proyectos. ¡Ayúdanos a llevar información gratuita al mundo!"

    const val INVALID_COMMAND_MSG_WIKTIONARY = "No en Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wikcionario es un diccionario editado de forma colaborativa y mantenido por la Fundación Wikimedia. Sirve como fuente de datos lingüísticos gratuitos para proyectos como Wikipedia y muchos otros."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe utiliza los datos de Wikcionario para proporcionar traducciones para su comando Traducir. ¡Nuestros datos provienen de los numerosos pares de idiomas que la comunidad de Wikcionario ha creado!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Puedes crear una cuenta en wiktionary.org para unirte a la comunidad que apoya a Scribe y a muchos otros proyectos. ¡Ayúdanos a llevar información gratuita al mundo!"

    val BASE_AUTOSUGGESTIONS = listOf("el", "la", "no")
    val NUMERIC_AUTOSUGGESTIONS = listOf("que", "de", "en")

    // MARK: Suggestion Pronouns

    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("ser", "REFLEXIVE_PRONOUN", "no")

    // MARK: Pronoun Conjugation

    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
            "yo" to "presFPS",
            "tú" to "presSPS",
            "él" to "presTPS",
            "ella" to "presTPS",
            "nosotros" to "presFPP",
            "nosotras" to "presFPP",
            "vosotros" to "presSPP",
            "vosotras" to "presSPP",
            "ellos" to "presTPP",
            "ellas" to "presTPP",
            "ustedes" to "presTPP",
        )

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Traducir"
    const val TRANSLATE_PLACEHOLDER = "Ingrese una palabra"
    const val TRANSLATE_PROMPT = " es -› ${"targetLanguage()"}"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Conjugar"
    const val CONJUGATE_PLACEHOLDER = "Ingrese un verbo"
    const val CONJUGATE_PROMPT = "Conjugar: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Ingrese un sustantivo"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Ya en plural"
}
