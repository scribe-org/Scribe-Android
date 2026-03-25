// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

/**
 * Object containing constant mappings related to language-specific data.
 * This includes conversions for grammatical annotations.
 * Keyboard-specific UI placeholders are in KeyboardLanguageMappingConstants.
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

    /**
     * Converts a full language name (e.g., "English") to its two-letter ISO alias (e.g., "EN").
     *
     * @param language The full name of the language.
     *
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
