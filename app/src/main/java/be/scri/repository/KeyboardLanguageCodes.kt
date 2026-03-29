// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")

package be.scri.repository

/**
 * Maps settings / UI language names to SQLite asset codes ([DatabaseFileManager] / contract JSON).
 */
object KeyboardLanguageCodes {
    val supportedDisplayLanguages =
        listOf(
            "English",
            "French",
            "German",
            "Italian",
            "Portuguese",
            "Russian",
            "Spanish",
            "Swedish",
        )

    fun toDbAlias(displayLanguage: String): String =
        when (displayLanguage) {
            "English" -> "EN"
            "French" -> "FR"
            "German" -> "DE"
            "Italian" -> "IT"
            "Portuguese" -> "PT"
            "Russian" -> "RU"
            "Spanish" -> "ES"
            "Swedish" -> "SV"
            else -> "EN"
        }
}
