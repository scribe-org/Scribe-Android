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
}
