// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.keyboardDBHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.PreferencesHelper
import java.io.FileOutputStream

/**
 * A helper class to manage translations from a local SQLite database.
 *
 * This class provides methods to fetch translations between different languages
 * by querying the `TranslationData.sqlite` database stored locally. It handles
 * the source and destination language code generation based on user preferences.
 *
 * @property context The application context used to access resources and the database.
 */
class TranslationDataManager(
    private val context: Context,
) {
    /**
     * Retrieves the source and destination language ISO codes based on the provided language.
     *
     * This function fetches the preferred translation language from the preferences
     * and generates the corresponding ISO codes for the source and destination languages.
     *
     * @param language The language name (e.g., "english") for which the source and destination
     *                 ISO codes need to be determined.
     * @return A pair of ISO codes: the source language and the destination language.
     */
    fun getSourceAndDestinationLanguage(language: String): Pair<String?, String?> {
        val sourceLanguage = PreferencesHelper.getPreferredTranslationLanguage(context, language)
        return Pair(generateISOCodeForLanguage(sourceLanguage.toString()), generateISOCodeForLanguage(language))
    }

    /**
     * Generates the ISO code for a given language name.
     *
     * This function maps the full name of a language (e.g., "english") to its corresponding
     * ISO 639-1 code (e.g., "en").
     *
     * @param languageName The name of the language (e.g., "english").
     * @return The ISO 639-1 code for the given language, or "en" if the language is unrecognized.
     */
    private fun generateISOCodeForLanguage(languageName: String): String? =
        when (languageName.lowercase()) {
            "english" -> "en"
            "french" -> "fr"
            "german" -> "de"
            "spanish" -> "es"
            "italian" -> "it"
            "portuguese" -> "pt"
            "russian" -> "ru"
            else -> "en"
        }

    /**
     * Generates the full language name for a given ISO code.
     *
     * This function maps an ISO 639-1 language code (e.g., "en") to the corresponding full
     * language name (e.g., "english").
     *
     * @param isoCode The ISO 639-1 code for the language (e.g., "en").
     * @return The full name of the language, or "english" if the ISO code is unrecognized.
     */
    private fun generateLanguageNameForISOCode(isoCode: String): String? =
        when (isoCode.lowercase()) {
            "en" -> "english"
            "fr" -> "french"
            "de" -> "german"
            "es" -> "spanish"
            "it" -> "italian"
            "pt" -> "portuguese"
            "ru" -> "russian"
            else -> "english"
        }

    /**
     * Retrieves the translation data for a given word from the database.
     *
     * This function queries the database for the translation of the given word from the source
     * language to the destination language, using the source and destination language ISO codes.
     *
     * @param sourceAndDestination A pair of ISO codes representing the source and destination languages.
     * @param word The word for which the translation is to be fetched.
     * @return The translation of the word in the destination language, or an empty string if not found.
     */
    fun getTranslationDataForAWord(
        sourceAndDestination: Pair<String?, String?>,
        word: String,
    ): String {
        val sourceLanguage = generateLanguageNameForISOCode(sourceAndDestination.first!!)
        val destinationColumn = sourceAndDestination.second!!
        val dbPath = context.getDatabasePath("TranslationData.sqlite")

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open("data/TranslationData.sqlite").use { inputStream ->
                FileOutputStream(dbPath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        val db = SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
        var result = ""

        db.use { database ->
            val query = "SELECT $destinationColumn FROM `$sourceLanguage` WHERE word = ?"
            val cursor = database.rawQuery(query, arrayOf(word))

            cursor.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(destinationColumn))
                }
            }
        }

        return result
    }
}
