// SPDX-License-Identifier: GPL-3.0-or-later

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * A helper class to manage emoji keywords by querying an SQLite database based on the specified language.
 */
class EmojiDataManager(
    private val context: Context,
) {
    // Track max keyword length.
    var maxKeywordLength = 0

    /**
     * Retrieves emoji keywords for the specified language from a corresponding SQLite database file.
     *
     * @param language the language code (e.g., "en", "es") used to locate the correct database file.
     * @return a [HashMap] mapping emoji characters to a list of associated keywords in the given language.
     */
    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        return processEmojiKeywords(dbFile.path)
    }

    /**
     * Processes an SQLite database to extract emoji keywords and stores them in a map.
     *
     * @param dbPath the path to the SQLite database file containing emoji keyword mappings.
     * @return a [HashMap] mapping emoji characters to a list of associated keywords.
     *
     * The function also determines the maximum keyword length from the database and stores it in [maxKeywordLength].
     */
    private fun processEmojiKeywords(dbPath: String): HashMap<String, MutableList<String>> {
        val hashMap = HashMap<String, MutableList<String>>()

        SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            // Get max keyword length.
            db.rawQuery("SELECT MAX(LENGTH(word)) FROM emoji_keywords", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    maxKeywordLength = cursor.getInt(0)
                }
            }

            // Keyword processing.
            db.rawQuery("SELECT * FROM emoji_keywords", null).use { cursor ->
                processEmojiCursor(cursor, hashMap)
            }
        }
        return hashMap
    }

    /**
     * Processes a database cursor containing emoji keyword data and populates a map with the results.
     *
     * @param cursor the [Cursor] object returned from querying the `emoji_keywords` table.
     * @param map a [HashMap] that maps emoji characters (as keys) to their associated keywords (as a list of strings).
     *
     * Each row in the cursor is expected to contain a column for the emoji and a column for the keyword.
     * This function adds each keyword to the list corresponding to its emoji in the map.
     */
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

    /**
     * Extracts emoji keyword mappings from a database cursor row.
     *
     * @param cursor the [Cursor] positioned at a row in the `emoji_keywords` table.
     * @return a [MutableList] of [String] containing all column values except the first (typically the emoji itself).
     *
     * This function assumes the first column contains the emoji character,
     * and the remaining columns contain associated keywords.
     */
    private fun getEmojiKeyMaps(cursor: Cursor): MutableList<String> =
        MutableList(cursor.columnCount - 1) { index ->
            cursor.getString(index + 1)
        }
}
