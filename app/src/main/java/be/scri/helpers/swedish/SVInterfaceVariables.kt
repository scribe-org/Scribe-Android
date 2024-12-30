/**
 * contains the ENInterfaceVariables object, which holds constants and variables for Swedish language interface elements,
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
package be.scri.helpers.swedish

object SVInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "kr"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("kr", "$", "€", "£", "¥")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "mellanslag"
    const val LANGUAGE = "Svenska"
    const val INVALID_COMMAND_MSG = "Inte i Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("jag", "det", "men")
    val NUMERIC_AUTOSUGGESTIONS = listOf("jag", "det", "och")

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Översätt"
    const val TRANSLATE_PLACEHOLDER = "Ange ett ord"
    const val TRANSLATE_PROMPT = "sv -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Konjugera"
    const val CONJUGATE_PLACEHOLDER = "Ange ett verb"
    const val CONJUGATE_PROMPT = "Konjugera: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Ange ett substantiv"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Redan plural"
}
