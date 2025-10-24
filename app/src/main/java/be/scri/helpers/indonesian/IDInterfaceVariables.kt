// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.indonesian

/**
 * Interface variables for Indonesian language keyboards.
 */
object IDInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("₡", "S", "€", "£", "₲", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "spasi"
    const val LANGUAGE = "Bahasa Indonesia"
    const val INVALID_COMMAND_MSG = "Tidak ada di Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("aku", "saya", "itu")
    val NUMERIC_AUTOSUGGESTIONS = listOf("adalah", "hingga", "dan")

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Terjemahkan"
    const val TRANSLATE_PLACEHOLDER = "Masukkan kata"
    const val TRANSLATE_PROMPT = " es -› ${"targetLanguage()"}"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"
}
