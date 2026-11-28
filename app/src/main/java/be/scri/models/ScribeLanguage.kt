// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.models

/**
 * Represents the supported languages in Scribe.
 *
 * @property displayName The human-readable name of the language (e.g., "English").
 * @property isoCode The two-letter ISO code for the language (e.g., "EN").
 * @property defaultLayout The default keyboard layout (e.g., "QWERTY", "AZERTY", "QWERTZ", "ЙЦУКЕН").
 */
enum class ScribeLanguage(
    val displayName: String,
    val isoCode: String,
    val defaultLayout: String,
) {
    ENGLISH("English", "EN", "QWERTY"),
    FRENCH("French", "FR", "AZERTY"),
    GERMAN("German", "DE", "QWERTZ"),
    ITALIAN("Italian", "IT", "QWERTY"),
    PORTUGUESE("Portuguese", "PT", "QWERTY"),
    RUSSIAN("Russian", "RU", "ЙЦУКЕН"),
    SPANISH("Spanish", "ES", "QWERTY"),
    SWEDISH("Swedish", "SV", "QWERTY"),
    ;

    companion object {
        /**
         * Resolves a [ScribeLanguage] from a display name, ISO code, or string representation.
         * Case-insensitive matching. Defaults to [ENGLISH] if not found.
         *
         * @param value The string name or code to parse.
         * @return The matching [ScribeLanguage], or [ENGLISH] as fallback.
         */
        fun fromString(value: String?): ScribeLanguage {
            if (value.isNullOrBlank()) return ENGLISH
            val trimmed = value.trim()
            return entries.firstOrNull { lang ->
                lang.displayName.equals(trimmed, ignoreCase = true) ||
                    lang.isoCode.equals(trimmed, ignoreCase = true) ||
                    lang.name.equals(trimmed, ignoreCase = true)
            } ?: ENGLISH
        }

        /**
         * Resolves a [ScribeLanguage] from a display name (e.g., "English", "French").
         */
        fun fromDisplayName(displayName: String?): ScribeLanguage = fromString(displayName)

        /**
         * Resolves a [ScribeLanguage] from a two-letter ISO code (e.g., "EN", "FR").
         */
        fun fromIsoCode(isoCode: String?): ScribeLanguage = fromString(isoCode)
    }
}
