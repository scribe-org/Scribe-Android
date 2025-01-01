package be.scri.helpers

import android.content.Context
import android.util.Log
import be.scri.services.SimpleKeyboardIME
import be.scri.helpers.english.ENInterfaceVariables // Import for English variables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portugese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables


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
        language: String
    ): String {
        // Determine the hint message
        val hintMessage = when (currentState) {
            SimpleKeyboardIME.ScribeState.TRANSLATE -> {
                when (language) {
                    "English" -> ENInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "French" -> FRInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "German" -> DEInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "Italian" -> ITInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "Portuguese" -> PTInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "Russian" -> RUInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "Spanish" -> ESInterfaceVariables.TRANSLATE_PLACEHOLDER
                    "Swedish" -> SVInterfaceVariables.TRANSLATE_PLACEHOLDER
                    else -> "" // Default fallback
                }
            }
            SimpleKeyboardIME.ScribeState.CONJUGATE -> {
                when (language) {
                    "English" -> ENInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "French" -> FRInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "German" -> DEInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "Italian" -> ITInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "Portuguese" -> PTInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "Russian" -> RUInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "Spanish" -> ESInterfaceVariables.CONJUGATE_PLACEHOLDER
                    "Swedish" -> SVInterfaceVariables.CONJUGATE_PLACEHOLDER
                    else -> "" // Default fallback
                }
            }
            SimpleKeyboardIME.ScribeState.PLURAL -> {
                when (language) {
                    "English" -> ENInterfaceVariables.PLURAL_PLACEHOLDER
                    "French" -> FRInterfaceVariables.PLURAL_PLACEHOLDER
                    "German" -> DEInterfaceVariables.PLURAL_PLACEHOLDER
                    "Italian" -> ITInterfaceVariables.PLURAL_PLACEHOLDER
                    "Portuguese" -> PTInterfaceVariables.PLURAL_PLACEHOLDER
                    "Russian" -> RUInterfaceVariables.PLURAL_PLACEHOLDER
                    "Spanish" -> ESInterfaceVariables.PLURAL_PLACEHOLDER
                    "Swedish" -> SVInterfaceVariables.PLURAL_PLACEHOLDER
                    else -> "" // Default fallback
                }
            }
            else -> "" // Fallback for unknown states
        }

        return hintMessage
    }


    fun getPromptText(
        currentState: SimpleKeyboardIME.ScribeState,
        language: String
    ): String {
        return when (currentState) {

            SimpleKeyboardIME.ScribeState.TRANSLATE -> {
                val languageShorthand = mapOf(
                    "English" to "en",
                    "French" to "fr",
                    "German" to "de",
                    "Italian" to "it",
                    "Portuguese" to "pt",
                    "Russian" to "ru",
                    "Spanish" to "es",
                    "Swedish" to "sv"
                )

                val shorthand = languageShorthand[language] ?: "en" // Default fallback to "en"
                "en -> $shorthand"
            }

            // CONJUGATE State
            SimpleKeyboardIME.ScribeState.CONJUGATE -> {
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
            }

            // PLURAL State
            SimpleKeyboardIME.ScribeState.PLURAL -> {
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

            // Default fallback for unknown states
            else -> ""
        }
    }





}
