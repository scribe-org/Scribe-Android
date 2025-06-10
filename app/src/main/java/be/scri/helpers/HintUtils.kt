// SPDX-License-Identifier: GPL-3.0-or-later

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

/**
 * Utility object for handling hint-related logic throughout the application.
 *
 * This may include methods for showing, formatting, or validating hints in forms or UI components.
 */
@Suppress("TooManyFunctions")
object HintUtils {
    /**
     * Resets the application hints, marking them as not shown in the shared preferences.
     * This is useful for users who want to see hints again.
     *
     * @param context The context used to access shared preferences.
     */
    fun resetHints(context: Context) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("hint_shown_main", false)
            putBoolean("hint_shown_settings", false)
            putBoolean("hint_shown_about", false)
            apply()
        }
    }

    /**
     * Retrieves the hint for the command bar based on the current state and the selected language.
     *
     * @param currentState The current state of the keyboard.
     * @param language The language code (e.g., "English", "French").
     * @return The appropriate hint message for the given state and language.
     */
    fun getCommandBarHint(
        currentState: GeneralKeyboardIME.ScribeState,
        language: String,
    ): String {
        val hintMessageForState = getHintForState(currentState)
        return hintMessageForState[language] ?: "" // return the placeholder or empty string if not found
    }

    /**
     * Maps the current state of the keyboard to its corresponding hints.
     *
     * @param currentState The current state of the keyboard.
     * @return A map of language codes to hint strings for the current state.
     */
    private fun getHintForState(currentState: GeneralKeyboardIME.ScribeState): Map<String, String> =
        when (currentState) {
            GeneralKeyboardIME.ScribeState.TRANSLATE -> getTranslateHints()
            GeneralKeyboardIME.ScribeState.CONJUGATE -> getConjugateHints()
            GeneralKeyboardIME.ScribeState.PLURAL -> getPluralHints()
            else -> emptyMap()
        }

    /**
     * Provides the translation hints for different languages.
     *
     * @return A map of language codes to their translation hints.
     */
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

    /**
     * Provides the conjugation hints for different languages.
     *
     * @return A map of language codes to their conjugation hints.
     */
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

    /**
     * Provides the plural hints for different languages.
     *
     * @return A map of language codes to their plural hints.
     */
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

    /**
     * Provides invalid hint for selected language.
     *
     * @return A hint string for invalid commands.
     */
    fun getInvalidHint(
        language: String,
        defaultHint: String = ENInterfaceVariables.INVALID_COMMAND_MSG,
    ): String =
        when (language) {
            "English" -> ENInterfaceVariables.INVALID_COMMAND_MSG
            "French" -> FRInterfaceVariables.INVALID_COMMAND_MSG
            "German" -> DEInterfaceVariables.INVALID_COMMAND_MSG
            "Italian" -> ITInterfaceVariables.INVALID_COMMAND_MSG
            "Portuguese" -> PTInterfaceVariables.INVALID_COMMAND_MSG
            "Russian" -> RUInterfaceVariables.INVALID_COMMAND_MSG
            "Spanish" -> ESInterfaceVariables.INVALID_COMMAND_MSG
            "Swedish" -> SVInterfaceVariables.INVALID_COMMAND_MSG
            else -> defaultHint
        }

    /**
     * Retrieves the prompt text for the given state and language.
     * This text provides context to the user based on the current action.
     *
     * @param currentState The current state of the keyboard.
     * @param language The language code (e.g., "English", "French").
     * @return The appropriate prompt text for the given state and language.
     */
    fun getPromptText(
        currentState: GeneralKeyboardIME.ScribeState,
        language: String,
        context: Context,
    ): String =
        when (currentState) {
            GeneralKeyboardIME.ScribeState.TRANSLATE -> getTranslationPrompt(language, context)
            GeneralKeyboardIME.ScribeState.CONJUGATE -> getConjugationPrompt(language)
            GeneralKeyboardIME.ScribeState.PLURAL -> getPluralPrompt(language)
            else -> ""
        }

    /**
     * Retrieves the translation prompt text for the given language.
     *
     * @param language The language code (e.g., "English", "French").
     * @return The translation prompt text for the given language.
     */
    private fun getTranslationPrompt(
        language: String,
        context: Context,
    ): String {
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
        val preferredLanguage =
            PreferencesHelper.getPreferredTranslationLanguage(
                context = context,
                language = language,
            )
        val keyboardLanguage = languageShorthand[preferredLanguage]
        val sourceLanguage = languageShorthand[language] ?: "en" // default fallback to "en"
        return "$keyboardLanguage -> $sourceLanguage"
    }

    /**
     * Retrieves the conjugation prompt text for the given language.
     *
     * @param language The language code (e.g., "English", "French").
     * @return The conjugation prompt text for the given language.
     */
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

    /**
     * Retrieves the plural prompt text for the given language.
     *
     * @param language The language code (e.g., "English", "French").
     * @return The plural prompt text for the given language.
     */
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
