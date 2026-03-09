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
    const val INVALID_COMMAND_MSG = "Não está no Wiktionary"
    const val INVALID_COMMAND_MSG_WIKIDATA = "Não está no Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "A Wikidata é um grafo de conhecimento editado colaborativamente e mantivo pela Fundação Wikimedia. A Wikidata é uma fonte de dados públicos para projetos como a Wikipédia e muitos outros."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "O Scribe usa dados linguísticos da Wikidata para muitas de suas funcionalidades. Temos informações sobre gêneros de substantivos, conjugações de verbos, e muito mais!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Você pode criar uma conta em wikidata.org e se juntar à comunidade que apoia o Scribe e muitos outros projetos. Ajude-nos a fornecer dados gratuitos para o mundo!"
    const val INVALID_COMMAND_MSG_WIKTIONARY = "Não está no Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary is a collaboratively edited dictionary that's maintained by the Wikimedia Foundation. It serves as a source of free linguistic data for projects like Wikipedia and countless others."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe uses Wiktionary's data to provide translations for its Translate command. Our data is derived from the many language pairs that Wiktionary's community has created!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "You can make an account at wiktionary.org to join the community that's supporting Scribe and so many other projects. Help us bring free information to the world!"
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
