package be.scri.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ENLanguageData.sqlite"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun loadDatabase() {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) {
            val inputStream: InputStream = context.assets.open("data/$DATABASE_NAME")
            val outputStream: OutputStream = FileOutputStream(dbFile)

            inputStream.copyTo(outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        }
    }

    fun getEmojiKeywords(): HashMap<String, MutableList<String>> {
        val hashMap = HashMap<String, MutableList<String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM emoji_keywords", null)

        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val key = cursor.getString(0)
                    val values = mutableListOf<String>()

                    for (i in 1 until cursor.columnCount) {
                        values.add(cursor.getString(i))
                    }

                    hashMap[key] = values
                } while (cursor.moveToNext())
            }
        }
        return hashMap
    }
}
