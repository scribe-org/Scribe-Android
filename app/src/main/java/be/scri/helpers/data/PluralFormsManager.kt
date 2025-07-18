// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.database.sqlite.SQLiteException
import android.util.Log
import be.scri.helpers.DatabaseFileManager

/**
 * Manages and queries plural forms for supported languages.
 * Provides methods to check for already-plural words, fetch plural forms, and more.
 */
class PluralFormsManager(
    private val fileManager: DatabaseFileManager,
) {
    private val pluralCache = mutableMapOf<String, List<String>>()
    private val germanPlurals = setOf("leben", "daten", "schmerzen")
    private val englishSingulars = setOf("news", "physics", "mathematics")

    /**
     * Checks if the given word is already plural for the specified language.
     *
     * @param language The language code (e.g., "EN", "DE").
     * @param word The word to check.
     * @param jsonData Optional language metadata.
     * @return True if the word is already plural, false otherwise.
     */
    fun isAlreadyPlural(
        language: String,
        word: String,
        jsonData: DataContract?,
    ): Boolean {
        if (word.isEmpty()) return false

        return try {
            when (language.uppercase()) {
                "DE" -> word.endsWith("en", true) || word.lowercase() in germanPlurals
                "EN" -> word.endsWith("s", true) && word.lowercase() !in englishSingulars
                "ES" -> word.endsWith("s", true) || word.endsWith("es", true)
                "FR" -> word.endsWith("x", true) || word.endsWith("s", true)
                else -> false
            } ||
                getCachedPluralForms(language, jsonData)?.any {
                    it.equals(word, true)
                } == true
        } catch (e: SQLiteException) {
            Log.e("PluralForms", "Error checking plural for $word", e)
            false
        }
    }

    /**
     * Retrieves the plural representation for a given noun.
     *
     * @param language The language code.
     * @param jsonData Optional language metadata.
     * @param noun The word to pluralize.
     * @return A map of singular to plural forms (or empty if not found).
     */
    fun getPluralRepresentation(
        language: String,
        jsonData: DataContract?,
        noun: String,
    ): Map<String, String?> =
        jsonData?.numbers?.let { numbers ->
            getPluralFromNumbers(language, numbers, noun)
        } ?: emptyMap()

    /**
     * Helper to extract plural representation to avoid deep nesting and return-count detekt complaints.
     *
     * @param language The language code.
     * @param numbers The mapping of singular to plural columns.
     * @param noun The word to pluralize.
     * @return A map with singular as key and plural as value (or empty if not found).
     */
    private fun getPluralFromNumbers(
    language: String,
    numbers: Map<String, String>,
    noun: String,
): Map<String, String?> {
    val singularCol = numbers.keys.firstOrNull() ?: return emptyMap()
    val pluralCol = numbers.values.firstOrNull() ?: return emptyMap()
    val db = fileManager.getLanguageDatabase(language) ?: return emptyMap()
    return queryPluralFromDB(db, singularCol, pluralCol, noun)
}

/**
 * Actually runs the DB query (in its own function to avoid nesting!).
 */
private fun queryPluralFromDB(
    db: android.database.sqlite.SQLiteDatabase,
    singularCol: String,
    pluralCol: String,
    noun: String,
): Map<String, String?> {
    var result: Map<String, String?> = emptyMap()
    db.use { database ->
        database.rawQuery(
            "SELECT `$singularCol`, `$pluralCol` FROM nouns WHERE `$singularCol` = ? COLLATE NOCASE",
            arrayOf(noun),
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                result = mapOf(cursor.getString(0) to cursor.getString(1))
            }
        }
    }
    return result
}

    /**
     * Gets a cached list of plural forms for a given language and contract.
     *
     * @param language The language code.
     * @param jsonData Optional language metadata.
     * @return A list of plural form strings, or null if none are found.
     */
    private fun getCachedPluralForms(
        language: String,
        jsonData: DataContract?,
    ): List<String>? {
        val cacheKey = "$language-${jsonData?.numbers?.values?.hashCode()}"
        return pluralCache
            .getOrPut(cacheKey) {
                jsonData?.numbers?.values?.toList()?.takeIf { it.isNotEmpty() }?.let { forms ->
                    fileManager.getLanguageDatabase(language)?.use { db ->
                        val result = mutableListOf<String>()
                        val query = "SELECT ${forms.joinToString(", ") { "`$it`" }} FROM nouns"

                        db.rawQuery(query, null).use { cursor ->
                            if (cursor.moveToFirst()) {
                                do {
                                    forms.indices.forEach { i ->
                                        cursor.getString(i)?.takeIf { it.isNotBlank() }?.let {
                                            result.add(it)
                                        }
                                    }
                                } while (cursor.moveToNext())
                            }
                        }
                        result
                    }
                } ?: emptyList()
            }.takeIf { it.isNotEmpty() }
    }
}
