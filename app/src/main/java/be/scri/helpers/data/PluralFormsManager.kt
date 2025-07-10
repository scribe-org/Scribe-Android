// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager

/**
 * Manages and queries plural forms of words from the database.
 * @param fileManager The central manager for database file access.
 */
class PluralFormsManager(
    private val fileManager: DatabaseFileManager,
) {
    /**
     * Checks if a word is already in its plural form.
     *
     * @param language The language code (e.g., "DE" for German)
     * @param word The word to check
     * @param jsonData The data contract for database columns
     * @return Boolean indicating if the word is already plural
     */
    fun isAlreadyPlural(
        language: String,
        word: String,
        jsonData: DataContract?
    ): Boolean {
        // First check language-specific patterns
        if (language == "DE" && isGermanPlural(word)) {
            return true
        }
        
        // Then check against database plurals
        return getAllPluralForms(language, jsonData)?.contains(word) == true
    }

    /**
     * German-specific plural check helper
     */
    private fun isGermanPlural(word: String): Boolean {
        return word.endsWith("en") || 
               word.lowercase() in setOf("leben", "daten", "schmerzen")
    }

    /**
     * Retrieves a list of all known plural forms for a given language from the database.
     *
     * @param language The language code (e.g., "EN", "DE") to select the correct database.
     * @param jsonData The data contract, which specifies the names of the columns containing plural forms.
     * @return A [List] of all plural word forms, or `null`
     * if the operation fails or no plural columns are defined.
     */
    fun getAllPluralForms(
        language: String,
        jsonData: DataContract?,
    ): List<String>? =
        jsonData?.numbers?.values?.toList()?.takeIf { it.isNotEmpty() }?.let { pluralForms ->
            fileManager.getLanguageDatabase(language)?.use { db ->
                queryAllPluralForms(db, pluralForms)
            }
        }

    /**
     * Retrieves the specific plural representation for a single noun.
     *
     * @param language The language code to select the correct database.
     * @param jsonData The data contract, which specifies the singular and plural column names.
     * @param noun The singular noun to find the plural for.
     * @return A [Map] containing the singular noun as the key and
     * its plural form as the value, or an empty map if not found.
     */
    fun getPluralRepresentation(
        language: String,
        jsonData: DataContract?,
        noun: String,
    ): Map<String, String?> =
        jsonData?.numbers?.let { numbers ->
            val singularCol = numbers.keys.firstOrNull()
            val pluralCol = numbers.values.firstOrNull()

            if (singularCol != null && pluralCol != null) {
                fileManager.getLanguageDatabase(language)?.use { db ->
                    querySpecificPlural(db, singularCol, pluralCol, noun)
                }
            } else {
                null
            }
        } ?: emptyMap()

    /**
     * Executes a database query to find the plural form for a specific noun.
     *
     * @param db The SQLite database to query.
     * @param singularCol The name of the column containing singular nouns.
     * @param pluralCol The name of the column containing the corresponding plural nouns.
     * @param noun The specific singular noun to search for.
     * @return A map of the singular noun to its plural, or an empty map if not found.
     */
    private fun querySpecificPlural(
        db: SQLiteDatabase,
        singularCol: String,
        pluralCol: String,
        noun: String,
    ): Map<String, String?> {
        val query =
            "SELECT " +
                "`$singularCol`, " +
                "`$pluralCol` " +
                "FROM nouns " +
                "WHERE `$singularCol` = ? " +
                "COLLATE NOCASE"

        return db.rawQuery(query, arrayOf(noun)).use { cursor ->
            if (cursor.moveToFirst()) {
                mapOf(cursor.getString(0) to cursor.getString(1))
            } else {
                emptyMap()
            }
        }
    }

    /**
     * Executes a database query to retrieve all values from all specified plural columns in the `nouns` table.
     *
     * @param db The SQLite database to query.
     * @param pluralColumns A list of column names that contain plural forms.
     * @return A [List] of all plural words found in the specified columns.
     */
    private fun queryAllPluralForms(
        db: SQLiteDatabase,
        pluralColumns: List<String>,
    ): List<String> {
        val result = mutableListOf<String>()
        val columns = pluralColumns.joinToString(", ") { "`$it`" }
        val query = "SELECT $columns FROM nouns"

        db.rawQuery(query, null).use { cursor ->
            if (!cursor.moveToFirst()) return@use

            do {
                addPluralsFromRow(cursor, pluralColumns.indices, result)
            } while (cursor.moveToNext())
        }
        return result
    }

    /**
     * Extracts all non-blank string values from the current cursor row for a given set of columns
     * and adds them to a result list.
     *
     * @param cursor The database cursor, positioned at the desired row.
     * @param columnIndices The range of column indices to read from.
     * @param result The list to which the plural forms will be added.
     */
    private fun addPluralsFromRow(
        cursor: android.database.Cursor,
        columnIndices: IntRange,
        result: MutableList<String>,
    ) {
        for (index in columnIndices) {
            val value = cursor.getString(index)
            if (value?.isNotBlank() == true) {
                result.add(value)
            }
        }
    }
}