// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.models

/**
 * Represents the supported languages in Scribe.
 *
 * @property displayName The human-readable name of the language (e.g., "English").
 * @property isoCode The two-letter ISO code for the language (e.g., "EN").
 */
enum class ScribeLanguage(
    val displayName: String,
    val isoCode: String,
) {
    ENGLISH("English", "EN"),
    FRENCH("French", "FR"),
    GERMAN("German", "DE"),
    ITALIAN("Italian", "IT"),
    PORTUGUESE("Portuguese", "PT"),
    RUSSIAN("Russian", "RU"),
    SPANISH("Spanish", "ES"),
    SWEDISH("Swedish", "SV"),
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
