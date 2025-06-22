// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.view.inputmethod.InputConnection

/**
 * Utility object for handling emoji-related operations.
 */
object EmojiUtils {
    private const val DATA_SIZE_2 = 2

    /**
     * Checks if the end of a string is likely an emoji.
     * This is a heuristic check based on common emoji Unicode ranges.
     * @param word The string to check.
     * @return `true` if the end of the string contains an emoji character, `false` otherwise.
     */
    fun isEmoji(word: String?): Boolean {
        if (word.isNullOrEmpty() || word.length < DATA_SIZE_2) {
            return false
        }

        val lastTwoChars =
            word.substring(
                word.length - DATA_SIZE_2,
            )

        val emojiRegex =
            Regex(
                "[\\uD83C\\uDF00-\\uD83E\\uDDFF]" +
                    "|[\\u2600-\\u26FF]" +
                    "|[\\u2700-\\u27BF]",
            )

        return emojiRegex.containsMatchIn(lastTwoChars)
    }

    /**
     * Inserts an emoji into the text field, replacing the keyword that triggered it if found.
     * @param emoji The emoji character to insert.
     */
    fun insertEmoji(
        emoji: String,
        ic: InputConnection,
        emojiKeywords: HashMap<String, MutableList<String>>?,
        emojiMaxKeywordLength: Int,
    ) {
        val maxLookBack = emojiMaxKeywordLength.coerceAtLeast(1)
        ic.beginBatchEdit()
        try {
            val prevText = ic.getTextBeforeCursor(maxLookBack, 0)?.toString() ?: ""
            val lastSpace = prevText.lastIndexOf(' ')
            when {
                prevText.isEmpty() ||
                    (lastSpace != -1 && lastSpace == prevText.length - 1) -> {
                    ic.commitText(emoji, 1)
                }

                lastSpace != -1 -> {
                    val lastWord = prevText.substring(lastSpace + 1)

                    if (emojiKeywords?.containsKey(lastWord.lowercase()) == true) {
                        ic.deleteSurroundingText(lastWord.length, 0)
                    }

                    ic.commitText(emoji, 1)
                }

                else -> {
                    if (emojiKeywords?.containsKey(prevText.lowercase()) == true) {
                        ic.deleteSurroundingText(prevText.length, 0)
                    }

                    ic.commitText(emoji, 1)
                }
            }
        } finally {
            ic.endBatchEdit()
        }
    }
}
