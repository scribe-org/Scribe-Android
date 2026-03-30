// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.portuguese

/**
 * Interface variables for Portuguese language keyboards.
 */
object PTInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("$", "€", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "espaço"
    const val LANGUAGE = "Português"

    const val INVALID_COMMAND_MSG_WIKIDATA = "Não está no Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "A Wikidata é um grafo de conhecimento editado colaborativamente e mantivo pela Fundação Wikimedia. A Wikidata é uma fonte de dados públicos para projetos como a Wikipédia e muitos outros."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "O Scribe usa dados linguísticos da Wikidata para muitas de suas funcionalidades. Temos informações sobre gêneros de substantivos, conjugações de verbos, e muito mais!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Você pode criar uma conta em wikidata.org e se juntar à comunidade que apoia o Scribe e muitos outros projetos. Ajude-nos a fornecer dados gratuitos para o mundo!"

    const val INVALID_COMMAND_MSG_WIKTIONARY = "Não está no Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "O Wikcionário é um dicionário editado colaborativamente e mantido pela Fundação Wikimedia. Ele serve como fonte de dados linguísticos gratuitos para projetos como a Wikipédia e inúmeros outros."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "O Scribe utiliza os dados do Wikcionário para fornecer traduções para o seu comando Traduzir. Nossos dados são derivados dos diversos pares de idiomas criados pela comunidade do Wikcionário!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Você pode criar uma conta em wiktionary.org para se juntar à comunidade que apoia o Scribe e muitos outros projetos. Ajude-nos a levar informação gratuita para o mundo!"
    const val DOWNLOAD_DATA_MSG = "Por favor, descarregue os dados do idioma"

    val BASE_AUTOSUGGESTIONS = listOf("o", "a", "eu")
    val NUMERIC_AUTOSUGGESTIONS = listOf("de", "que", "a")

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Traduzir"
    const val TRANSLATE_PLACEHOLDER = "Digite uma palavra"
    const val TRANSLATE_PROMPT = "pt -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Conjugar"
    const val CONJUGATE_PLACEHOLDER = "Digite um verbo"
    const val CONJUGATE_PROMPT = "Conjugar: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Digite um substantivo"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Já plural"
}
