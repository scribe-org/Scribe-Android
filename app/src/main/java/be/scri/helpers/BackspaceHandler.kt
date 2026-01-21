// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.text.TextUtils
import android.view.inputmethod.InputConnection
import be.scri.helpers.PreferencesHelper.getIsWordByWordDeletionEnabled
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.Companion.MAX_TEXT_LENGTH

/**
 * Handles backspace/delete events for the [GeneralKeyboardIME].
 * Encapsulates logic for single character deletion, word-by-word deletion,
 * command bar deletion, and repeating delete state.
 *
 * @property ime The [GeneralKeyboardIME] instance this handler is associated with.
 */
class BackspaceHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Track if the delete key is currently being repeated (long press).
     */
    var isDeleteRepeating: Boolean = false

    /**
     * Handles the logic for the Delete/Backspace key. It deletes characters from either
     * the main input field or the command bar, depending on the context.
     *
     * @param isCommandBar true if the deletion should happen in the command bar.
     * @param isLongPress true if this is a long press/repeat action, false for single tap.
     */
    fun handleBackspace(
        isCommandBar: Boolean = false,
        isLongPress: Boolean = false,
    ) {
        val keyboard = ime.keyboard ?: return
        val keyboardView = ime.keyboardView ?: return

        if (keyboard.mShiftState == SHIFT_ON_ONE_CHAR) {
            keyboard.mShiftState = SHIFT_OFF
        }

        if (isCommandBar) {
            handleCommandBarDelete()
        } else {
            val inputConnection = ime.currentInputConnection ?: return
            if (TextUtils.isEmpty(inputConnection.getSelectedText(0))) {
                val isWordByWordEnabled = getIsWordByWordDeletionEnabled(ime.applicationContext, ime.language)
                // Only use word-by-word deletion on long press when the feature is enabled.
                if (isWordByWordEnabled && isLongPress) {
                    deleteWordByWord(inputConnection)
                } else {
                    deleteSingleCharacter(inputConnection)
                }
            } else {
                inputConnection.commitText("", 1)
            }

            // Auto-shift if text is empty
            if (inputConnection.getTextBeforeCursor(1, 0)?.isEmpty() != false) {
                keyboard.mShiftState = SHIFT_ON_ONE_CHAR
                keyboardView.invalidateAllKeys()
            }
        }
    }

    /**
     * Handles the delete key press specifically for the command bar text field.
     */
    private fun handleCommandBarDelete() {
        val currentTextWithoutCursor = ime.getCommandBarTextWithoutCursor()
        // If we're already showing the hint, do nothing on delete.
        if (currentTextWithoutCursor == ime.currentCommandBarHint) {
            return
        }

        if (currentTextWithoutCursor.isNotEmpty()) {
            val newText = currentTextWithoutCursor.dropLast(1)
            if (newText.isEmpty()) {
                // All real text has been deleted, so restore the hint.
                ime.setCommandBarTextWithCursor(ime.currentCommandBarHint, cursorAtStart = true)
                ime.binding.commandBar.setTextColor(ime.commandBarHintColor)
            } else {
                // There's still text left, so just update it.
                ime.setCommandBarTextWithCursor(newText)
            }
        }

        // Handle German plural mode shift state.
        val finalCommandBarText = ime.getCommandBarTextWithoutCursor()
        val isEmptyOrAHint = finalCommandBarText.isEmpty() || finalCommandBarText == ime.currentCommandBarHint
        val isGerman = ime.language == "German"
        val isPluralState = ime.currentState == GeneralKeyboardIME.ScribeState.PLURAL

        if (isEmptyOrAHint && isGerman && isPluralState) {
            ime.keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
        }
    }

    /**
     * Deletes an entire word, including any trailing whitespace.
     *
     * @param inputConnection The current input connection.
     */
    private fun deleteWordByWord(inputConnection: InputConnection) {
        val textBeforeCursor = inputConnection.getTextBeforeCursor(MAX_TEXT_LENGTH, 0)?.toString() ?: ""

        if (textBeforeCursor.isEmpty()) {
            return
        }

        var deletionLength = 0
        var index = textBeforeCursor.length - 1

        // Skip any whitespace.
        while (index >= 0 && textBeforeCursor[index].isWhitespace()) {
            deletionLength++
            index--
        }

        // If we only had whitespace, delete it.
        if (index < 0) {
            if (deletionLength > 0) {
                inputConnection.deleteSurroundingText(deletionLength, 0)
            }
            return
        }

        // Now delete the word characters.
        if (isWordCharacter(textBeforeCursor[index])) {
            // Delete regular word characters (letters, numbers, some punctuation).
            while (index >= 0 && isWordCharacter(textBeforeCursor[index])) {
                deletionLength++
                index--
            }
        } else {
            // If the character at cursor is not a word character (e.g., special punctuation),
            // delete just that single character instead of trying to delete a whole word.
            deletionLength++
        }

        if (deletionLength > 0) {
            inputConnection.deleteSurroundingText(deletionLength, 0)
        }
    }

    /**
     * Deletes a single character.
     */
    private fun deleteSingleCharacter(inputConnection: InputConnection) {
        inputConnection.deleteSurroundingText(1, 0)
    }

    /**
     * Determines if a character is considered part of a word for deletion purposes.
     */
    private fun isWordCharacter(char: Char): Boolean {
        // Letters and digits are always word characters.
        if (char.isLetterOrDigit()) {
            return true
        }

        // Check if special characters are considered word.
        return when (Character.getType(char).toByte()) {
            // Connector punctuation.
            Character.CONNECTOR_PUNCTUATION -> true
            Character.DASH_PUNCTUATION -> true
            Character.OTHER_PUNCTUATION -> {
                char in "'\".,@#$%&*+=~`|\\/:;?!^"
            }
            Character.CURRENCY_SYMBOL -> true
            Character.MATH_SYMBOL -> char in "+=<>~^"
            Character.OTHER_SYMBOL -> char in "@#$%&*+=~`|\\/:;?!^"
            else -> false
        }
    }
}
