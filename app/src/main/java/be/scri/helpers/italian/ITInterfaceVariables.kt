/**
 * contains the ENInterfaceVariables object, which holds constants and variables for Italian language interface elements,.
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
package be.scri.helpers.italian

object ITInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "spazio"
    const val LANGUAGE = "Italiano"
    const val INVALID_COMMAND_MSG = "Non in Wikidata"
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
