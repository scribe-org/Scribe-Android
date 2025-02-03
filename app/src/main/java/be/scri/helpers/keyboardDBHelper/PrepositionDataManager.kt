package be.scri.helpers.keyboardDBHelper

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class PrepositionDataManager(
    private val context: Context,
) {
    fun openDatabase(language: String): SQLiteDatabase {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun extractPrepositionCase(cursor: Cursor): Pair<String, String> {
        val preposition = cursor.getString(1)
        val caseAnnotation = cursor.getString(2)
        return preposition to caseAnnotation
    }

    fun processCursor(cursor: Cursor): HashMap<String, MutableList<String>> {
        val result = HashMap<String, MutableList<String>>()
        if (cursor.moveToFirst()) {
            do {
                val (preposition, caseAnnotation) = extractPrepositionCase(cursor)
                if (result.containsKey(preposition)) {
                    result[preposition]?.add(caseAnnotation)
                } else {
                    result[preposition] = mutableListOf(caseAnnotation)
                }
            } while (cursor.moveToNext())
        }
        return result
    }

    fun getCaseAnnotations(language: String): HashMap<String, MutableList<String>> {
        val db = openDatabase(language)
        val result = HashMap<String, MutableList<String>>()

        db.use { database ->
            database.rawQuery("SELECT * FROM PREPOSITIONS", null)?.use { cursor ->
                result.putAll(processCursor(cursor))
            }
        }

        return result
    }
}
