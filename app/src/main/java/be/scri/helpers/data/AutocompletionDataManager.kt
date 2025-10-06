// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import be.scri.helpers.DatabaseFileManager

/**
 * This class manages the autocomplete system.
 * It loads words from a language-specific SQLite database,
 * and stores them in a Trie data structure for fast prefix-based lookup.
 */
class AutocompletionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    private val trie = Trie()

    /**
     * Loads all words from the language-specific database into the trie.
     *
     * @param language The language code (e.g. "en", "id") for which to load words.
     */
    fun loadWords(language: String) {
        val db = fileManager.getLanguageDatabase(language)
        db?.rawQuery("SELECT word FROM autocomplete_lexicon", null).use { cursor ->
            val wordIndex = cursor!!.getColumnIndex("word")
            while (cursor.moveToNext()) {
                val word = cursor.getString(wordIndex)?.lowercase()?.trim()
                if (!word.isNullOrEmpty()) {
                    trie.insert(word)
                }
            }
        }
    }

    /**
     * Returns autocomplete suggestions for a given prefix.
     *
     * @param prefix The starting text to search for (e.g. "ap").
     * @param limit  The maximum number of suggestions to return (default: 3).
     * @return A list of matching words that begin with the prefix.
     */
    fun getAutocompletions(
        prefix: String,
        limit: Int = 3,
    ): List<String> = trie.searchPrefix(prefix, limit)
}
