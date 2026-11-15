// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint", "detekt.all")

package be.scri.helpers.data

import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager
import be.scri.helpers.StringUtils.containsProfanity

class AutoSuggestionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    fun getSuggestions(
        language: String,
        profanity: Set<String> = emptySet(),
    ): HashMap<String, List<String>> {
        val db = fileManager.getLanguageDatabase(language) ?: return hashMapOf()
        return processAllSuggestions(db, profanity)
    }

    private fun processAllSuggestions(
        db: SQLiteDatabase,
        profanity: Set<String>,
    ): HashMap<String, List<String>> {
        val suggestionMap = HashMap<String, List<String>>()
        val columnsToSelect = listOf("word", "autosuggestion_0", "autosuggestion_1", "autosuggestion_2")

        if (!db.tableExists("autosuggestions")) return suggestionMap
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
                        cursor.getString(idx)?.takeIf { it.isNotEmpty() && !containsProfanity(it, profanity) }
                    }
                if (suggestions.isNotEmpty()) {
                    suggestionMap[word] = suggestions
                }
            }
        }
        return suggestionMap
    }
}
