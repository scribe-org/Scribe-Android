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
     * Finds all plural forms for a given language.
     * @param language The language code.
     * @param jsonData The data contract with plural form info.
     * @return A list of all plural forms, or null on failure.
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
     * Gets the plural representation of a given noun.
     * @param language The language code.
     * @param jsonData The data contract with number info.
     * @param noun The noun to find the plural for.
     * @return A map of the singular noun to its plural form, or emptyMap on failure.
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
     * Queries the DB for a specific noun's plural form.
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

        return db.rawQuery(query, arrayOf(noun))?.use { cursor ->
            if (cursor.moveToFirst()) {
                mapOf(cursor.getString(0) to cursor.getString(1))
            } else {
                emptyMap()
            }
        } ?: emptyMap()
    }

    /**
     * Queries the DB for all plural forms from a list of columns.
     */
    private fun queryAllPluralForms(
        db: SQLiteDatabase,
        pluralColumns: List<String>,
    ): List<String> {
        val result = mutableListOf<String>()
        val columns = pluralColumns.joinToString(", ") { "`$it`" }
        val query = "SELECT $columns FROM nouns"

        db.rawQuery(query, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use

            do {
                // The nested logic has been extracted to a helper function.
                addPluralsFromRow(cursor, pluralColumns.indices, result)
            } while (cursor.moveToNext())
        }
        return result
    }

    /**
     * Extracts all non-blank string values from the current cursor row and adds them to a list.
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
