/**
 * A helper to facilitate database calls for Scribe keyboard commands.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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

    fun getNounKeywords(language: String): HashMap<String, MutableList<String>> {
        val hashMap = HashMap<String, MutableList<String>>()
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery("SELECT * FROM nouns", null)

        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val key = cursor.getString(0).lowercase()
                    hashMap[key] = getNounKeyMaps(cursor)
                } while (cursor.moveToNext())
            }
        }
        return hashMap
    }

    fun getNounKeyMaps(cursor: Cursor): MutableList<String> {
        val values = mutableListOf<String>()
        values.add(cursor.getString(2))
        return values
    }
}
