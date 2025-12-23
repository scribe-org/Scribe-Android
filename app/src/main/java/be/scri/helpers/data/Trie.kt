// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

/**
 * A node in the Trie structure.
 *
 * @property children Map of characters to child TrieNodes.
 * @property isWord Indicates if this node marks the end of a valid word.
 */
class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isWord = false
}

/**
 * Trie (prefix tree) implementation for storing and retrieving words efficiently.
 *
 * Supports word insertion and prefix-based autocompletion search.
 */
class Trie {
    private val root = TrieNode()

    /**
     * Inserts a word into the trie.
     *
     * @param word The word to insert.
     *
     * Example:
     * ```
     * val trie = Trie()
     * trie.insert("apple")
     * ```
     */
    fun insert(word: String) {
        var node = root
        for (char in word.lowercase()) {
            node = node.children.getOrPut(char) { TrieNode() }
        }
        node.isWord = true
    }

    /**
     * Finds all words in the trie that start with the given prefix.
     *
     * @param prefix The prefix to search for.
     * @param limit The maximum number of results to return (default = 10).
     *
     * @return A list of words that begin with the prefix, up to [limit].
     *
     * Example:
     * ```
     * trie.searchPrefix("ap", 5) // ["apple", "application", "apply"]
     * ```
     */
    fun searchPrefix(
        prefix: String,
        limit: Int = 10,
    ): List<String> {
        var node = root
        for (char in prefix.lowercase()) {
            node = node.children[char] ?: return emptyList()
        }

        val results = mutableListOf<String>()
        collectWords(node, prefix, results, limit)
        return results
    }

    /**
     * Recursively collects words starting from the given node.
     *
     * @param node The current TrieNode.
     * @param prefix The word prefix built so far.
     * @param results The list of collected results.
     * @param limit The maximum number of results to collect.
     */
    private fun collectWords(
        node: TrieNode,
        prefix: String,
        results: MutableList<String>,
        limit: Int,
    ) {
        if (results.size >= limit) return
        if (node.isWord) results.add(prefix)
        for ((char, child) in node.children) {
            collectWords(child, prefix + char, results, limit)
            if (results.size >= limit) return
        }
    }
}
