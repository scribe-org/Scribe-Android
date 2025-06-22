// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portuguese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables

/**
 * Object containing constant mappings related to language-specific data and UI elements.
 * This includes conversions for grammatical annotations and placeholders for UI text.
 */
object LanguageMappingConstants {
    val prepAnnotationConversionDict =
        mapOf(
            "German" to mapOf("Acc" to "Akk"),
            "Russian" to
                mapOf(
                    "Acc" to "Вин",
                    "Dat" to "Дат",
                    "Gen" to "Род",
                    "Loc" to "Мес",
                    "Pre" to "Пре",
                    "Ins" to "Инс",
                ),
        )

    val nounAnnotationConversionDict =
        mapOf(
            "Swedish" to mapOf("C" to "U"),
            "Russian" to mapOf("F" to "Ж", "M" to "М", "N" to "Н"),
        )

    val translatePlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.TRANSLATE_KEY_LBL,
            "ES" to ESInterfaceVariables.TRANSLATE_KEY_LBL,
            "DE" to DEInterfaceVariables.TRANSLATE_KEY_LBL,
            "IT" to ITInterfaceVariables.TRANSLATE_KEY_LBL,
            "FR" to FRInterfaceVariables.TRANSLATE_KEY_LBL,
            "PT" to PTInterfaceVariables.TRANSLATE_KEY_LBL,
            "RU" to RUInterfaceVariables.TRANSLATE_KEY_LBL,
            "SV" to SVInterfaceVariables.TRANSLATE_KEY_LBL,
        )

    val conjugatePlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.CONJUGATE_KEY_LBL,
            "ES" to ESInterfaceVariables.CONJUGATE_KEY_LBL,
            "DE" to DEInterfaceVariables.CONJUGATE_KEY_LBL,
            "IT" to ITInterfaceVariables.CONJUGATE_KEY_LBL,
            "FR" to FRInterfaceVariables.CONJUGATE_KEY_LBL,
            "PT" to PTInterfaceVariables.CONJUGATE_KEY_LBL,
            "RU" to RUInterfaceVariables.CONJUGATE_KEY_LBL,
            "SV" to SVInterfaceVariables.CONJUGATE_KEY_LBL,
        )

    val pluralPlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.PLURAL_KEY_LBL,
            "ES" to ESInterfaceVariables.PLURAL_KEY_LBL,
            "DE" to DEInterfaceVariables.PLURAL_KEY_LBL,
            "IT" to ITInterfaceVariables.PLURAL_KEY_LBL,
            "FR" to FRInterfaceVariables.PLURAL_KEY_LBL,
            "PT" to PTInterfaceVariables.PLURAL_KEY_LBL,
            "RU" to RUInterfaceVariables.PLURAL_KEY_LBL,
            "SV" to SVInterfaceVariables.PLURAL_KEY_LBL,
        )

    /**
     * Converts a full language name (e.g., "English") to its two-letter ISO alias (e.g., "EN").
     * @param language The full name of the language.
     * @return The two-letter alias.
     */
    fun getLanguageAlias(language: String): String =
        when (language) {
            "English" -> "EN"
            "French" -> "FR"
            "German" -> "DE"
            "Italian" -> "IT"
            "Portuguese" -> "PT"
            "Russian" -> "RU"
            "Spanish" -> "ES"
            "Swedish" -> "SV"
            else -> ""
        }
}
