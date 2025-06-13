// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.database.Cursor
import android.util.Log
import be.scri.helpers.DatabaseFileManager

/**
 * Manages preposition data and case annotations from the database.
 * @param fileManager The central manager for database file access.
 */
class PrepositionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    /**
     * Gets case annotations for prepositions for a language.
     * This feature is only supported for select languages (e.g., DE, RU).
     * @param language The language code (e.g., "DE", "RU").
     * @return A map where keys are prepositions and values are cases. Returns empty for unsupported languages.
     */
    fun getCaseAnnotations(language: String): HashMap<String, MutableList<String>> {
        if (language.uppercase() !in listOf("DE", "RU")) {
            return hashMapOf()
        }
        return fileManager.getLanguageDatabase(language)?.use { db ->
            db.rawQuery("SELECT preposition, grammaticalCase FROM prepositions", null).use { cursor ->
                processCursor(cursor)
            } // Handle case where cursor is null
        } ?: hashMapOf() // Handle case where database is null
    }

    /**
     * Processes a cursor to extract prepositions and their cases.
     * @param cursor The cursor from the `prepositions` table query.
     * @return A map of prepositions to their case annotations.
     */
    private fun processCursor(cursor: Cursor): HashMap<String, MutableList<String>> {
        val result = HashMap<String, MutableList<String>>()
        val prepIndex = cursor.getColumnIndex("preposition")
        val caseIndex = cursor.getColumnIndex("grammaticalCase")

        if (prepIndex == -1 || caseIndex == -1 || !cursor.moveToFirst()) {
            Log.e("PrepositionDataManager", "Required columns not found in prepositions table.")
            return result
        }

        do {
            val prep = cursor.getString(prepIndex)
            val case = cursor.getString(caseIndex)

            if (prep != null && case != null) {
                result.getOrPut(prep) { mutableListOf() }.add(case)
            }
        } while (cursor.moveToNext())

        return result
    }
}
