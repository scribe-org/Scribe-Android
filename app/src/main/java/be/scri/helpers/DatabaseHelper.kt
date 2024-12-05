package be.scri.helpers

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DatabaseHelper(
    private val context: Context,
) : SQLiteOpenHelper(context, null, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // No operation for now
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        // No operation for now
    }

    fun loadDatabase(language: String) {
        val databaseName = "${language}LanguageData.sqlite"
        val dbFile = context.getDatabasePath(databaseName)
        if (!dbFile.exists()) {
            val inputStream: InputStream = context.assets.open("data/$databaseName")
            val outputStream: OutputStream = FileOutputStream(dbFile)

            inputStream.copyTo(outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        }
    }

    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> {
        val hashMap = HashMap<String, MutableList<String>>()
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery("SELECT * FROM emoji_keywords", null)

        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val key = cursor.getString(0)
                    hashMap[key] = getEmojiKeyMaps(cursor)
                } while (cursor.moveToNext())
            }
        }
        return hashMap
    }

    fun getEmojiKeyMaps(cursor: Cursor): MutableList<String> {
        val values = mutableListOf<String>()

        for (i in 1 until cursor.columnCount) {
            values.add(cursor.getString(i))
        }
        return values
    }
}
