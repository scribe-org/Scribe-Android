// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

/**
 * Utility object for handling string-related operations.
 */
object StringUtils {
    /**
     * Checks if a word is capitalized (i.e., starts with an uppercase letter).
     *
     * @param word The word to check.
     *
     * @return true if the word is capitalized, false otherwise.
     */
    fun isWordCapitalized(word: String): Boolean {
        if (word.isEmpty()) return false
        return word[0].isUpperCase()
    }
}
