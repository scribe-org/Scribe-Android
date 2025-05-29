// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.util.Log
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles key events for the EnglishKeyboardIME.
 */
class KeyHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Processes the given key code and performs the corresponding action.
     */
    fun handleKey(code: Int) {
        val inputConnection = ime.currentInputConnection
        if (ime.keyboard == null || inputConnection == null) {
            return
        }
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.lastShiftPressTS = 0
        }

        when (code) {
            KeyboardBase.KEYCODE_TAB -> inputConnection.commitText("\t", GeneralKeyboardIME.COMMIT_TEXT_CURSOR_POSITION)
            KeyboardBase.KEYCODE_CAPS_LOCK -> handleCapsLock()
            KeyboardBase.KEYCODE_DELETE -> handleDeleteKey()
            KeyboardBase.KEYCODE_SHIFT -> handleShiftKey()
            KeyboardBase.KEYCODE_ENTER -> handleEnterKey()
            KeyboardBase.KEYCODE_MODE_CHANGE -> handleModeChangeKey()
            KeyboardBase.KEYCODE_SPACE -> handleKeycodeSpace()
            KeyboardBase.KEYCODE_LEFT_ARROW -> handleArrowKey(false)
            KeyboardBase.KEYCODE_RIGHT_ARROW -> handleArrowKey(true)
            else -> handleDefaultKey(code)
        }
        updateKeyboardState(code)
    }

    /**
     * Updates the keyboard state after each key press, including checking for emojis and plural words.
     * Also updates the shift key state if necessary.
     * @param code the key code that was pressed.
     */
    private fun updateKeyboardState(code: Int) {
        ime.lastWord = ime.getLastWordBeforeCursor()
        Log.d("Debug", "${ime.lastWord}")
        ime.autoSuggestEmojis = ime.findEmojisForLastWord(ime.emojiKeywords, ime.lastWord)
        ime.checkIfPluralWord = ime.findWhetherWordIsPlural(ime.pluralWords, ime.lastWord)

        Log.i("MY-TAG", "${ime.checkIfPluralWord}")
        Log.d("Debug", "${ime.autoSuggestEmojis}")
        Log.d("MY-TAG", "${ime.nounTypeSuggestion}")
        ime.updateButtonText(ime.emojiAutoSuggestionEnabled, ime.autoSuggestEmojis)

        if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
            ime.updateAutoSuggestText(isPlural = ime.checkIfPluralWord)
        }

        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.updateShiftKeyState()
        }
    }

    /**
     * Handles the Caps Lock key event.
     * Toggles the shift state between OFF and LOCKED, and invalidates the keyboard view.
     */
    private fun handleCapsLock() {
        ime.keyboard?.let {
            val newState =
                when (it.mShiftState) {
                    KeyboardBase.SHIFT_OFF -> KeyboardBase.SHIFT_LOCKED
                    else -> KeyboardBase.SHIFT_OFF
                }
            if (it.setShifted(newState)) {
                ime.keyboardView?.invalidateAllKeys()
            }
        }
    }

    /**
     * Handles a default key event (for keys not explicitly defined in other methods).
     * Executes the necessary actions based on the current state and keyboard mode.
     * @param code the key code of the pressed key.
     */
    private fun handleDefaultKey(code: Int) {
        if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
            ime.handleElseCondition(code, ime.keyboardMode, binding = null)
        } else {
            ime.handleElseCondition(code, ime.keyboardMode, ime.keyboardBinding, commandBarState = true)
        }
        ime.disableAutoSuggest()
    }

    /**
     * Handles the Delete key event.
     * Deletes the previous character if in an appropriate state and updates the keyboard view.
     */
    private fun handleDeleteKey() {
        val shouldDelete =
            when (ime.currentState) {
                ScribeState.IDLE, ScribeState.SELECT_COMMAND -> false
                else -> true
            }
        ime.handleDelete(shouldDelete, ime.keyboardBinding)
        ime.keyboardView!!.invalidateAllKeys()
        ime.disableAutoSuggest()
    }

    /**
     * Handles the Shift key event.
     * Updates the keyboard to reflect the new letter case and invalidates the keyboard view.
     */
    private fun handleShiftKey() {
        ime.handleKeyboardLetters(ime.keyboardMode, ime.keyboardView)
        ime.keyboardView!!.invalidateAllKeys()
        ime.disableAutoSuggest()
    }

    /**
     * Handles the Enter key event.
     * Executes different actions depending on the current state of the keyboard.
     */
    private fun handleEnterKey() {
        if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
            ime.handleKeycodeEnter(ime.keyboardBinding, false)
        } else {
            ime.handleKeycodeEnter(ime.keyboardBinding, true)
            ime.currentState = ScribeState.IDLE
            ime.switchToCommandToolBar()
            ime.updateUI()
        }
        ime.disableAutoSuggest()
    }

    /**
     * Handles the Mode Change key event.
     * Switches the keyboard mode and updates the state accordingly.
     */
    private fun handleModeChangeKey() {
        ime.handleModeChange(ime.keyboardMode, ime.keyboardView, ime)
        ime.disableAutoSuggest()
    }

    /**
     * Handles the Arrow key event (either left or right).
     * Moves the cursor in the appropriate direction based on the pressed key.
     * @param isRight true if the right arrow key is pressed, false if the left arrow key is pressed.
     */
    private fun handleArrowKey(isRight: Boolean) {
        ime.currentInputConnection?.let { ic ->
            val currentPos = ic.getTextBeforeCursor(GeneralKeyboardIME.MAX_TEXT_LENGTH, 0)?.length ?: 0
            val newPos =
                if (isRight) {
                    val textAfter = ic.getTextAfterCursor(GeneralKeyboardIME.MAX_TEXT_LENGTH, 0)?.toString() ?: ""
                    (currentPos + 1).coerceAtMost(currentPos + textAfter.length)
                } else {
                    (currentPos - 1).coerceAtLeast(0)
                }
            ic.setSelection(newPos, newPos)
        }
    }

    /**
     * Handles the Space key event.
     * Inserts a space character and performs any necessary updates based on the current state.
     */
    private fun handleKeycodeSpace() {
        val code = KeyboardBase.KEYCODE_SPACE
        if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
            ime.handleElseCondition(code, ime.keyboardMode, binding = null)
            ime.updateAutoSuggestText(isPlural = ime.checkIfPluralWord)
        } else {
            ime.handleElseCondition(code, ime.keyboardMode, ime.keyboardBinding, commandBarState = true)
            ime.disableAutoSuggest()
        }
    }
}
