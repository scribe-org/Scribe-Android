// SPDX-License-Identifier: AGPL-3.0-or-later

/**
 * A helper to facilitate resetting application hints if the user would like to see them again.
 */

package be.scri.helpers

import android.content.Context
import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portuguese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables
import be.scri.services.GeneralKeyboardIME

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
        currentState: GeneralKeyboardIME.ScribeState,
        language: String,
    ): String {
        val hintMessageForState = getHintForState(currentState)
        return hintMessageForState[language] ?: "" // return the placeholder or empty string if not found
    }

    private fun getHintForState(currentState: GeneralKeyboardIME.ScribeState): Map<String, String> =
        when (currentState) {
            GeneralKeyboardIME.ScribeState.TRANSLATE -> getTranslateHints()
            GeneralKeyboardIME.ScribeState.CONJUGATE -> getConjugateHints()
            GeneralKeyboardIME.ScribeState.PLURAL -> getPluralHints()
            else -> emptyMap()
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
        currentState: GeneralKeyboardIME.ScribeState,
        language: String,
    ): String =
        when (currentState) {
            GeneralKeyboardIME.ScribeState.TRANSLATE -> getTranslationPrompt(language)
            GeneralKeyboardIME.ScribeState.CONJUGATE -> getConjugationPrompt(language)
            GeneralKeyboardIME.ScribeState.PLURAL -> getPluralPrompt(language)
            else -> ""
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
        val shorthand = languageShorthand[language] ?: "en" // default fallback to "en"
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
            else -> "Conjugate :"
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
            else -> "Plural :"
        }
}
