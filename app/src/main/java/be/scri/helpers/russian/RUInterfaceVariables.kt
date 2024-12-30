/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/**
 * Class of methods to manage Scribe's behaviors.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.scri.helpers.russian

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
