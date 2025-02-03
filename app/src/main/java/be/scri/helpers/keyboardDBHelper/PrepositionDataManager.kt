package be.scri.helpers.keyboardDBHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class PrepositionDataManager(
    private val context: Context,
) {
    fun getCaseAnnotations(language: String): HashMap<String, MutableList<String>> {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        val result = HashMap<String, MutableList<String>>()
        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        if (language.lowercase() == "de" || language.lowercase() == "ru") {
            db.use { database ->
                database.rawQuery("SELECT * FROM PREPOSITIONS", null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        do {
                            val preposition = cursor.getString(1)
                            val caseAnnotation = cursor.getString(2)

                            if (result.containsKey(preposition)) {
                                result[preposition]?.add(caseAnnotation)
                            } else {
                                result[preposition] = mutableListOf<String>(caseAnnotation)
                            }
                        } while (cursor.moveToNext())
                    }
                }
            }
        }
        return result
    }
}
