// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager
import be.scri.helpers.PreferencesHelper

/**
 * Manages translations from a local SQLite database.
 * @param context The application context.
 * @param fileManager The central manager for database file access.
 */
class TranslationDataManager(
    private val context: Context,
    private val fileManager: DatabaseFileManager,
) {
    /**
     * Determines the source and destination language ISO codes for a translation operation.
     * The source is derived from user preferences, and the destination is the current keyboard language.
     *
     * @param language The current keyboard language name (e.g., "English").
     * @return A [Pair] containing the source and destination ISO codes (e.g., "en" to "fr").
     */
    fun getSourceAndDestinationLanguage(language: String): Pair<String?, String?> {
        val sourceLanguage = PreferencesHelper.getPreferredTranslationLanguage(context, language)
        return Pair(generateISOCodeForLanguage(sourceLanguage.toString()), generateISOCodeForLanguage(language))
    }

    /**
     * Retrieves the translation for a given word from the local translation database.
     * If the source and destination languages are the same, it returns the original word.
     *
     * @param sourceAndDestination A [Pair] of source and destination ISO language codes.
     * @param word The word to be translated.
     * @return The translated word as a [String], or an empty string if no translation is found.
     */
    fun getTranslationDataForAWord(
        sourceAndDestination: Pair<String?, String?>,
        word: String,
    ): String {
        val (sourceCode, destCode) = sourceAndDestination

        if (sourceCode == destCode || sourceCode == null || destCode == null) {
            return word
        }

        val sourceTable = generateLanguageNameForISOCode(sourceCode)

        return fileManager.getTranslationDatabase()?.use { db ->
            queryForTranslation(db, sourceTable, destCode, word)
        } ?: ""
    }

    /**
     * Executes the raw SQL query to find a translation for a given word in the database.
     *
     * @param db The SQLite database instance.
     * @param sourceTable The name of the table to query (derived from the source language, e.g., "english").
     * @param destColumn The name of the column containing the translation
     * (derived from the destination language ISO code, e.g., "fr").
     * @param word The word to search for in the 'word' column of the source table.
     * @return The translated word, or an empty string if not found.
     */
    private fun queryForTranslation(
        db: SQLiteDatabase,
        sourceTable: String,
        destColumn: String,
        word: String,
    ): String {
        val query =
            """
            SELECT `$destColumn`
            FROM `$sourceTable`
            WHERE word = ?
            """.trimIndent()

        return db.rawQuery(query, arrayOf(word)).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(destColumn))?.trimEnd() ?: ""
            } else {
                ""
            }
        }
    }

    /**
     * Converts a full language name (e.g., "english") to its corresponding two-letter ISO 639-1 code.
     *
     * @param languageName The full name of the language.
     * @return The two-letter ISO code as a [String]. Defaults to "en".
     */
    private fun generateISOCodeForLanguage(languageName: String): String =
        when (languageName.lowercase()) {
            "english" -> "en"
            "french" -> "fr"
            "german" -> "de"
            "spanish" -> "es"
            "italian" -> "it"
            "portuguese" -> "pt"
            "russian" -> "ru"
            "swedish" -> "sv"
            else -> "en"
        }

    /**
     * Converts a two-letter ISO 639-1 code to its corresponding full language name used for table lookups.
     *
     * @param isoCode The two-letter ISO code.
     * @return The full language name as a [String]. Defaults to "english".
     */
    private fun generateLanguageNameForISOCode(isoCode: String): String =
        when (isoCode.lowercase()) {
            "en" -> "english"
            "de" -> "german"
            "es" -> "spanish"
            "fr" -> "french"
            "it" -> "italian"
            "pt" -> "portuguese"
            "ru" -> "russian"
            "sv" -> "swedish"
            else -> "english"
        }
}
