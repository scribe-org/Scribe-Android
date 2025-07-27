package be.scri.helpers.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class AutoSuggestionDataManager(
    context: Context,
    private val language: String,
) : SQLiteOpenHelper(context, getDatabaseName(language), null, 1) {
    companion object {
        private const val TABLE_AUTOSUGGESTIONS = "autosuggestions"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_SUGGESTION1 = "autosuggestion_0"
        private const val COLUMN_SUGGESTION2 = "autosuggestion_1"
        private const val COLUMN_SUGGESTION3 = "autosuggestion_2"
        private const val COLUMN_EMOJIS = "emojis"

        private fun getDatabaseName(language: String): String =
            when (language.lowercase()) {
                "en" -> "ENLanguageData.sqlite"
                "fr" -> "FRLanguageData.sqlite"
                "de" -> "DELanguageData.sqlite"
                "es" -> "ESLanguageData.sqlite"
                "it" -> "ITLanguageData.sqlite"
                "pt" -> "PTLanguageData.sqlite"
                "ru" -> "RULanguageData.sqlite"
                "sv" -> "SVLanguageData.sqlite"
                else -> "ENLanguageData.sqlite"
            }
    }

    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
    }

    fun getSuggestions(word: String): List<String>? {
        if (word.isEmpty()) {
            Log.w("AutoSuggestionDataManager", "Empty word provided for suggestion query.")
            return null
        }

        return readableDatabase.use { db ->
            processSuggestionData(db, word)
        }
    }

    private fun processSuggestionData(
        db: SQLiteDatabase,
        word: String,
    ): List<String>? {
        val columnsToSelect = listOf(COLUMN_WORD, COLUMN_SUGGESTION1, COLUMN_SUGGESTION2, COLUMN_SUGGESTION3)

        db.rawQuery("SELECT * FROM $TABLE_AUTOSUGGESTIONS LIMIT 1", null).use { tempCursor ->
            for (column in columnsToSelect) {
                if (tempCursor.getColumnIndex(column) == -1) {
                    Log.e(
                        "AutoSuggestionDataManager",
                        "Column '$column' specified was NOT FOUND in the '$TABLE_AUTOSUGGESTIONS' table. " +
                            "Skipping suggestion processing to prevent a crash.",
                    )
                    return null
                }
            }
        }

        val selection = columnsToSelect.joinToString(", ") { "`$it`" }
        val query = "SELECT $selection FROM $TABLE_AUTOSUGGESTIONS WHERE $COLUMN_WORD = ? COLLATE NOCASE"

        return db.rawQuery(query, arrayOf(word)).use { cursor ->
            if (cursor.moveToFirst()) {
                processSuggestionRow(cursor)
            } else {
                null
            }
        }
    }

    internal fun processSuggestionRow(cursor: Cursor): List<String> {
        val suggestions = mutableListOf<String>()

        val suggestion1Index = cursor.getColumnIndex(COLUMN_SUGGESTION1)
        val suggestion2Index = cursor.getColumnIndex(COLUMN_SUGGESTION2)
        val suggestion3Index = cursor.getColumnIndex(COLUMN_SUGGESTION3)

        cursor.getString(suggestion1Index)?.takeIf { it.isNotEmpty() }?.let { suggestions.add(it) }
        cursor.getString(suggestion2Index)?.takeIf { it.isNotEmpty() }?.let { suggestions.add(it) }
        cursor.getString(suggestion3Index)?.takeIf { it.isNotEmpty() }?.let { suggestions.add(it) }

        return suggestions
    }
}
