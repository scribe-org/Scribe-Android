// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.data

import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager

class AutoSuggestionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    fun getSuggestions(language: String): HashMap<String, List<String>> {
        val db = fileManager.getLanguageDatabase(language) ?: return hashMapOf()
        return processAllSuggestions(db)
    }

    private fun processAllSuggestions(db: SQLiteDatabase): HashMap<String, List<String>> {
        val suggestionMap = HashMap<String, List<String>>()
        val columnsToSelect = listOf("word", "autosuggestion_0", "autosuggestion_1", "autosuggestion_2")

        db.rawQuery("SELECT * FROM autosuggestions LIMIT 1", null).use { tempCursor ->
            for (column in columnsToSelect) {
                if (tempCursor.getColumnIndex(column) == -1) {
                    return suggestionMap
                }
            }
        }

        val selection = columnsToSelect.joinToString(", ") { "`$it`" }
        db.rawQuery("SELECT $selection FROM autosuggestions", null).use { cursor ->
            val wordIndex = cursor.getColumnIndex("word")
            val suggestionIndices =
                listOf(
                    cursor.getColumnIndex("autosuggestion_0"),
                    cursor.getColumnIndex("autosuggestion_1"),
                    cursor.getColumnIndex("autosuggestion_2"),
                )

            while (cursor.moveToNext()) {
                val word = cursor.getString(wordIndex)?.lowercase()?.takeIf { it.isNotEmpty() } ?: continue
                val suggestions =
                    suggestionIndices.mapNotNull { idx ->
                        cursor.getString(idx)?.takeIf { it.isNotEmpty() }
                    }
                if (suggestions.isNotEmpty()) {
                    suggestionMap[word] = suggestions
                }
            }
        }
        return suggestionMap
    }
}
