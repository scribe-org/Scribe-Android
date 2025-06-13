// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.database.Cursor
import be.scri.helpers.DatabaseFileManager

/**
 * Manages emoji keywords by querying an SQLite database.
 * @param fileManager The central manager for database file access.
 */
class EmojiDataManager(
    private val fileManager: DatabaseFileManager,
) {
    /** The maximum length of any emoji keyword found. */
    var maxKeywordLength = 0
        private set

    /**
     * Retrieves emoji keywords for a specified language.
     * @param language The language code (e.g., "DE", "FR").
     * @return A map of words to their associated emoji keywords.
     */
    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> {
        val emojiMap = HashMap<String, MutableList<String>>()
        val db = fileManager.getLanguageDatabase(language) ?: return emojiMap

        db.use {
            // Get max keyword length
            it.rawQuery("SELECT MAX(LENGTH(word)) FROM emoji_keywords", null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    maxKeywordLength = cursor.getInt(0)
                }
            }
            // Get all emoji keywords
            it.rawQuery("SELECT * FROM emoji_keywords", null)?.use { cursor ->
                processEmojiCursor(cursor, emojiMap)
            }
        }
        return emojiMap
    }

    /**
     * Processes a cursor to populate the emoji keywords map.
     * @param cursor The cursor from the `emoji_keywords` table query.
     * @param emojiMap The map to populate with results.
     */
    private fun processEmojiCursor(
        cursor: Cursor,
        emojiMap: HashMap<String, MutableList<String>>,
    ) {
        val wordIndex = cursor.getColumnIndex("word")
        if (wordIndex == -1 || !cursor.moveToFirst()) return

        val emojiIndices =
            listOf("emoji_keyword_0", "emoji_keyword_1", "emoji_keyword_2")
                .mapNotNull { name -> cursor.getColumnIndex(name).takeIf { it != -1 } }

        do {
            val word = cursor.getString(wordIndex)
            val emojis =
                emojiIndices
                    .mapNotNull { index -> cursor.getString(index)?.takeIf { it.isNotBlank() } }
                    .toMutableList()

            if (emojis.isNotEmpty()) {
                emojiMap[word] = emojis
            }
        } while (cursor.moveToNext())
    }
}
