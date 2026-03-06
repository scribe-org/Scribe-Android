// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import be.scri.helpers.DatabaseFileManager

/**
 * This class manages the autocomplete system.
 * It loads words from a language-specific SQLite database,
 * and stores them in a Trie data structure for fast prefix-based lookup.
 * If the `autocomplete_lexicon` table/Trie is not available, it falls back to caching all noun words.
 */
class AutocompletionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    private val trie = Trie()
    private var trieLoaded = false
    private val nounWords = mutableListOf<String>()

    /**
     * Loads all words from the language-specific database into the trie.
     * If the `autocomplete_lexicon` table/Trie is not present, it loads noun words from the specified columns instead.
     *
     * @param language The language code (e.g. "en", "id") for which to load words.
     * @param numbersColumns Column names from the contract's `numbers` map.
     */
    fun loadWords(
        language: String,
        numbersColumns: List<String> = emptyList(),
    ) {
        val db = fileManager.getLanguageDatabase(language) ?: return

        db.use { database ->
            if (database.tableExists("autocomplete_lexicon")) {
                database.rawQuery("SELECT word FROM autocomplete_lexicon", null).use { cursor ->
                    val wordIndex = cursor.getColumnIndex("word")
                    while (cursor.moveToNext()) {
                        val word = cursor.getString(wordIndex)?.lowercase()?.trim()
                        if (!word.isNullOrEmpty()) {
                            trie.insert(word)
                        }
                    }
                }
                trieLoaded = true
            } else if (database.tableExists("nouns") && numbersColumns.isNotEmpty()) {
                val unionQuery =
                    numbersColumns.joinToString(" UNION ") { column ->
                        "SELECT DISTINCT $column AS word FROM nouns WHERE $column IS NOT NULL AND $column != ''"
                    } + " ORDER BY word ASC"

                database.rawQuery(unionQuery, null).use { cursor ->
                    val wordIndex = cursor.getColumnIndex("word")
                    while (cursor.moveToNext()) {
                        val word = cursor.getString(wordIndex)?.lowercase()?.trim()
                        if (!word.isNullOrEmpty()) {
                            nounWords.add(word)
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns autocomplete suggestions for a given prefix.
     * Uses the Trie if loaded, otherwise filters the cached noun word list.
     *
     * @param prefix The starting text to search for (e.g. "ap").
     * @param limit  The maximum number of suggestions to return (default: 3).
     *
     * @return A list of matching words that begin with the prefix.
     */
    fun getAutocompletions(
        prefix: String,
        limit: Int = 3,
    ): List<String> =
        if (trieLoaded) {
            trie.searchPrefix(prefix, limit)
        } else {
            getAutocompletionsFromNouns(prefix, limit)
        }

    /**
     * Filters the cached noun word list to find matches that start with the given prefix.
     */
    private fun getAutocompletionsFromNouns(
        prefix: String,
        limit: Int,
    ): List<String> {
        if (nounWords.isEmpty() || prefix.isBlank()) return emptyList()
        val normalizedPrefix = prefix.lowercase().trim()
        return nounWords
            .filter { it.startsWith(normalizedPrefix) }
            .take(limit)
    }
}
