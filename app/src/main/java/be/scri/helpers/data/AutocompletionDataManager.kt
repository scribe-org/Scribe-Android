// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import be.scri.helpers.DatabaseFileManager
import be.scri.helpers.StringUtils.isWordCapitalized

/**
 * This class manages the autocomplete system.
 * It loads words from a language-specific SQLite database,
 * and stores them in a Trie data structure for fast prefix-based lookup.
 * If the `autocomplete_lexicon` table/Trie is not available, it falls back to caching all noun words.
 */
class AutocompletionDataManager(
    private val fileManager: DatabaseFileManager,
) {
    private var trie = Trie()
    private var trieLoaded = false
    private val nounWords = mutableListOf<String>()
    private var isGerman = false
    private val germanNouns = mutableSetOf<String>()

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
        trie = Trie()
        trieLoaded = false
        nounWords.clear()
        germanNouns.clear()
        isGerman = language.equals("DE", ignoreCase = true)

        val db = fileManager.getLanguageDatabase(language) ?: return

        db.use { database ->
            val hasLexicon = database.tableExists("autocomplete_lexicon")
            val hasNouns = database.tableExists("nouns")

            if (hasLexicon) {
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
            }

            if (hasNouns && numbersColumns.isNotEmpty()) {
                val unionQuery =
                    numbersColumns.joinToString(" UNION ") { column ->
                        "SELECT DISTINCT $column AS word FROM nouns WHERE $column IS NOT NULL AND $column != ''"
                    } + " ORDER BY word ASC"

                database.rawQuery(unionQuery, null).use { cursor ->
                    val wordIndex = cursor.getColumnIndex("word")
                    while (cursor.moveToNext()) {
                        val word = cursor.getString(wordIndex)?.lowercase()?.trim()
                        if (!word.isNullOrEmpty()) {
                            if (isGerman) {
                                germanNouns.add(word)
                            }
                            if (!hasLexicon) {
                                nounWords.add(word)
                            }
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
    ): List<String> {
        val isCapitalized = isWordCapitalized(prefix)
        val normalizedPrefix = prefix.lowercase().trim()

        val results =
            if (trieLoaded) {
                trie.searchPrefix(normalizedPrefix, limit)
            } else {
                getAutocompletionsFromNouns(normalizedPrefix, limit)
            }

        return results.map { word ->
            val isGermanNoun = isGerman && germanNouns.contains(word)
            if (isCapitalized || isGermanNoun) {
                word.replaceFirstChar { it.uppercaseChar() }
            } else {
                word
            }
        }
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
