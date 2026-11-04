// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

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

    private val WORD_SEPARATOR = Regex("[\\s-]+")

    /**
     * Checks if a suggestion is or contains a profane word, matching whole words so that
     *
     * @param text The suggestion to check.
     * @param profanity Lowercase profane words for the current language.
     *
     * @return true if the suggestion should not be shown, false otherwise.
     */
    fun containsProfanity(
        text: String,
        profanity: Set<String>,
    ): Boolean {
        if (profanity.isEmpty()) return false
        val lower = text.lowercase().trim()
        if (lower in profanity) return true
        // Only multi-word text (spaces or hyphens) needs the slower per-token check.
        return lower.any { it == '-' || it.isWhitespace() } &&
            lower.split(WORD_SEPARATOR).any { it in profanity }
    }

    /**
     * Loads a string resource and replaces placeholder variables with provided parameters.
     *
     * This helper function works with i18n strings that use {variable_name} placeholder syntax,
     * replacing them in order with the provided parameters.
     *
     * @param id The string resource ID (e.g., R.string.some_string)
     * @param params Variable number of string parameters to replace placeholders with.
     *               Placeholders are replaced in the order they appear in the string.
     *
     * @return The formatted string with all placeholders replaced by the provided parameters.
     */
    @Composable
    fun stringResourceWithParams(
        id: Int,
        vararg params: String,
    ): String {
        var result = stringResource(id)
        params.forEachIndexed { _, param ->
            result = result.replaceFirst(Regex("""\{[^}]+\}"""), param)
        }
        return result
    }

    /**
     * Replaces placeholder variables in a template string with provided parameters.
     *
     * This helper function works with template strings that use {variable_name} placeholder syntax,
     * replacing them in order with the provided parameters.
     *
     * @param template The template string (e.g., "Network error: {error}")
     * @param params Variable number of string parameters to replace placeholders with.
     *               Placeholders are replaced in the order they appear in the string.
     *
     * @return The formatted string with all placeholders replaced by the provided parameters.
     */
    fun formatStringWithParams(
        template: String,
        vararg params: String,
    ): String {
        var result = template
        params.forEach { param ->
            result = result.replaceFirst(Regex("""\{[^}]+\}"""), param)
        }
        return result
    }
}
