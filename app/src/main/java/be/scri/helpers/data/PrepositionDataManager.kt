// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.database.Cursor
import android.util.Log
import be.scri.helpers.DatabaseFileManager

/**
 * Manages preposition data and case annotations from the database.
 *
 * @param fileManager The central manager for database file access.
 */
class PrepositionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    /**
     * Retrieves a map of prepositions to their required grammatical cases for a specific language.
     * This functionality is currently only supported for German ("DE") and Russian ("RU").
     *
     * @param language The language code.
     *
     * @return A [HashMap] where keys are prepositions and values are a list of required cases
     * (e.g., "accusative case").
     * Returns an empty map for unsupported languages or on failure.
     */
    fun getCaseAnnotations(language: String): HashMap<String, MutableList<String>> {
        if (language.uppercase() !in listOf("DE", "RU")) {
            return hashMapOf()
        }
        return fileManager.getLanguageDatabase(language)?.use { db ->
            db.rawQuery("SELECT preposition, grammaticalCase FROM prepositions", null).use { cursor ->
                processCursor(cursor)
            } // handle case where cursor is null
        } ?: hashMapOf() // handle case where database is null
    }

    /**
     * Iterates through a database cursor from the `prepositions` table and populates a map with the results.
     *
     * @param cursor The cursor containing the preposition and grammatical case data.
     *
     * @return A [HashMap] mapping prepositions to a list of their cases.
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
