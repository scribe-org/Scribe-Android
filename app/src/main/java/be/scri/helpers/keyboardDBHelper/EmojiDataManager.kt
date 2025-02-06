

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class EmojiDataManager(
    private val context: Context,
) {
    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        return processEmojiKeywords(dbFile.path)
    }

    private fun processEmojiKeywords(dbPath: String): HashMap<String, MutableList<String>> {
        val hashMap = HashMap<String, MutableList<String>>()
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        db.use { database ->
            database.rawQuery("SELECT * FROM emoji_keywords", null).use { cursor ->
                processEmojiCursor(cursor, hashMap)
            }
        }
        return hashMap
    }

    private fun processEmojiCursor(
        cursor: Cursor,
        hashMap: HashMap<String, MutableList<String>>,
    ) {
        if (!cursor.moveToFirst()) return

        do {
            val key = cursor.getString(0)
            hashMap[key] = getEmojiKeyMaps(cursor)
        } while (cursor.moveToNext())
    }

    private fun getEmojiKeyMaps(cursor: Cursor): MutableList<String> =
        MutableList(cursor.columnCount - 1) { index ->
            cursor.getString(index + 1)
        }
}
