/**
 * A helper to facilitate resetting application hints if the user would like to see them again.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package be.scri.helpers

import android.content.Context
import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portugese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables
import be.scri.services.SimpleKeyboardIME

object HintUtils {
    fun resetHints(context: Context) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("hint_shown_main", false)
            putBoolean("hint_shown_settings", false)
            putBoolean("hint_shown_about", false)
            apply()
        }
    }

    fun getCommandBarHint(
        currentState: SimpleKeyboardIME.ScribeState,
        language: String,
    ): String {
        val hintMessageForState = getHintForState(currentState)
        return hintMessageForState[language] ?: "" // Return the placeholder or empty string if not found
    }

    private fun getHintForState(currentState: SimpleKeyboardIME.ScribeState): Map<String, String> =
        when (currentState) {
            SimpleKeyboardIME.ScribeState.TRANSLATE -> getTranslateHints()
            SimpleKeyboardIME.ScribeState.CONJUGATE -> getConjugateHints()
            SimpleKeyboardIME.ScribeState.PLURAL -> getPluralHints()
            else -> emptyMap() // Fallback for unknown states
        }

    private fun getTranslateHints(): Map<String, String> =
        mapOf(
            "English" to ENInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "French" to FRInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "German" to DEInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "Italian" to ITInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "Portuguese" to PTInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "Russian" to RUInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "Spanish" to ESInterfaceVariables.TRANSLATE_PLACEHOLDER,
            "Swedish" to SVInterfaceVariables.TRANSLATE_PLACEHOLDER,
        )

    private fun getConjugateHints(): Map<String, String> =
        mapOf(
            "English" to ENInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "French" to FRInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "German" to DEInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "Italian" to ITInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "Portuguese" to PTInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "Russian" to RUInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "Spanish" to ESInterfaceVariables.CONJUGATE_PLACEHOLDER,
            "Swedish" to SVInterfaceVariables.CONJUGATE_PLACEHOLDER,
        )

    private fun getPluralHints(): Map<String, String> =
        mapOf(
            "English" to ENInterfaceVariables.PLURAL_PLACEHOLDER,
            "French" to FRInterfaceVariables.PLURAL_PLACEHOLDER,
            "German" to DEInterfaceVariables.PLURAL_PLACEHOLDER,
            "Italian" to ITInterfaceVariables.PLURAL_PLACEHOLDER,
            "Portuguese" to PTInterfaceVariables.PLURAL_PLACEHOLDER,
            "Russian" to RUInterfaceVariables.PLURAL_PLACEHOLDER,
            "Spanish" to ESInterfaceVariables.PLURAL_PLACEHOLDER,
            "Swedish" to SVInterfaceVariables.PLURAL_PLACEHOLDER,
        )

    fun getPromptText(
        currentState: SimpleKeyboardIME.ScribeState,
        language: String,
    ): String =
        when (currentState) {
            SimpleKeyboardIME.ScribeState.TRANSLATE -> getTranslationPrompt(language)
            SimpleKeyboardIME.ScribeState.CONJUGATE -> getConjugationPrompt(language)
            SimpleKeyboardIME.ScribeState.PLURAL -> getPluralPrompt(language)
            else -> "" // Default fallback for unknown states
        }

    private fun getTranslationPrompt(language: String): String {
        val languageShorthand =
            mapOf(
                "English" to "en",
                "French" to "fr",
                "German" to "de",
                "Italian" to "it",
                "Portuguese" to "pt",
                "Russian" to "ru",
                "Spanish" to "es",
                "Swedish" to "sv",
            )
        val shorthand = languageShorthand[language] ?: "en" // Default fallback to "en"
        return "en -> $shorthand"
    }

    private fun getConjugationPrompt(language: String): String =
        when (language) {
            "English" -> ENInterfaceVariables.CONJUGATE_PROMPT
            "French" -> FRInterfaceVariables.CONJUGATE_PROMPT
            "German" -> DEInterfaceVariables.CONJUGATE_PROMPT
            "Italian" -> ITInterfaceVariables.CONJUGATE_PROMPT
            "Portuguese" -> PTInterfaceVariables.CONJUGATE_PROMPT
            "Russian" -> RUInterfaceVariables.CONJUGATE_PROMPT
            "Spanish" -> ESInterfaceVariables.CONJUGATE_PROMPT
            "Swedish" -> SVInterfaceVariables.CONJUGATE_PROMPT
            else -> "Conjugate :" // Default fallback
        }

    private fun getPluralPrompt(language: String): String =
        when (language) {
            "English" -> ENInterfaceVariables.PLURAL_PROMPT
            "French" -> FRInterfaceVariables.PLURAL_PROMPT
            "German" -> DEInterfaceVariables.PLURAL_PROMPT
            "Italian" -> ITInterfaceVariables.PLURAL_PROMPT
            "Portuguese" -> PTInterfaceVariables.PLURAL_PROMPT
            "Russian" -> RUInterfaceVariables.PLURAL_PROMPT
            "Spanish" -> ESInterfaceVariables.PLURAL_PROMPT
            "Swedish" -> SVInterfaceVariables.PLURAL_PROMPT
            else -> "Plural :" // Default fallback
        }
}
