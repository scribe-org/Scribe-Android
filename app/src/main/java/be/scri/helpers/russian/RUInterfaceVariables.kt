// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.russian

/**
 * Interface variables for Russian language keyboards.
 */
object RUInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "₽"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("₽", "$", "€", "£", "¥")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "Пробел"
    const val LANGUAGE = "Pусский"
    const val INVALID_COMMAND_MSG = "Нет в Викиданных"
    val BASE_AUTOSUGGESTIONS = listOf("я", "а", "в")
    val NUMERIC_AUTOSUGGESTIONS = listOf("в", "и", "я")

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Перевести"
    const val TRANSLATE_PLACEHOLDER = "Введите слово"
    const val TRANSLATE_PROMPT = "ru -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Спрягать"
    const val CONJUGATE_PLACEHOLDER = "Введите глагол"
    const val CONJUGATE_PROMPT = "Спрягать: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Множ-ое"
    const val PLURAL_PLACEHOLDER = "Введите существительное"
    const val PLURAL_PROMPT = "Множ-ое: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Уже во множ-ом"
}
