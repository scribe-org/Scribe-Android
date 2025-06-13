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
     * Gets source and destination language ISO codes from preferences.
     * @param language The current keyboard language name (e.g., "english").
     * @return A pair of (source, destination) ISO codes.
     */
    fun getSourceAndDestinationLanguage(language: String): Pair<String?, String?> {
        val sourceLanguage = PreferencesHelper.getPreferredTranslationLanguage(context, language)
        return Pair(generateISOCodeForLanguage(sourceLanguage.toString()), generateISOCodeForLanguage(language))
    }

    /**
     * Gets translation data for a given word.
     * @param sourceAndDestination A pair of (source, dest) ISO codes.
     * @param word The word to translate.
     * @return The translated word, or the original word if no translation is needed/found.
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
     * Executes the database query to find a translation for a word.
     *
     * @param db The SQLite database instance.
     * @param sourceTable The name of the table to query (e.g., "english").
     * @param destColumn The name of the column containing the translation.
     * @param word The word to search for.
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
                cursor.getString(cursor.getColumnIndexOrThrow(destColumn)) ?: ""
            } else {
                ""
            }
        }
    }

    /**
     * Generates an ISO code from a full language name.
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
     * Generates a full language name from an ISO code.
     */
    private fun generateLanguageNameForISOCode(isoCode: String): String =
        when (isoCode.lowercase()) {
            "en" -> "english"
            "fr" -> "french"
            "de" -> "german"
            "es" -> "spanish"
            "it" -> "italian"
            "pt" -> "portuguese"
            "ru" -> "russian"
            "sv" -> "swedish"
            else -> "english"
        }
}
