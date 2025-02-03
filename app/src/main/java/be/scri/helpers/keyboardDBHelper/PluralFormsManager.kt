

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class PluralFormsManager(
    private val context: Context,
) {
    fun checkIfWordIsPlural(
        language: String,
        jsonData: DataContract?,
    ): List<String>? {
        if (jsonData?.numbers?.values.isNullOrEmpty()) {
            Log.e("MY-TAG", "JSON data for 'numbers' is null or empty.")
            return null
        }

        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        val pluralForms = jsonData.numbers.values.toList()
        Log.d("MY-TAG", "Plural Forms: $pluralForms")

        return queryPluralForms(dbFile.path, pluralForms)
    }

    private fun queryPluralForms(
        dbPath: String,
        pluralForms: List<String>,
    ): List<String> {
        val result = mutableListOf<String>()
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        db.use { database ->
            database.rawQuery("SELECT * FROM nouns", null)?.use { cursor ->
                processPluralFormsCursor(cursor, pluralForms, result)
            }
        }

        return result
    }

    private fun processPluralFormsCursor(
        cursor: Cursor,
        pluralForms: List<String>,
        result: MutableList<String>,
    ) {
        if (!cursor.moveToFirst()) {
            Log.w("MY-TAG", "Cursor is empty, no data found in 'nouns' table.")
            return
        }

        do {
            addPluralForms(cursor, pluralForms, result)
        } while (cursor.moveToNext())
    }

    private fun addPluralForms(
        cursor: Cursor,
        pluralForms: List<String>,
        result: MutableList<String>,
    ) {
        pluralForms.forEach { pluralForm ->
            val columnIndex = cursor.getColumnIndex(pluralForm)
            if (columnIndex != -1) {
                cursor.getString(columnIndex)?.let { result.add(it) }
            } else {
                Log.e("MY-TAG", "Column '$pluralForm' not found in the database.")
            }
        }
    }
}
