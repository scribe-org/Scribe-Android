// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputConnection
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles key events for the [GeneralKeyboardIME].
 * This class processes raw key codes, determines the appropriate action based on the
 * current keyboard state and the key pressed, and delegates to specific handlers
 * or directly interacts with the [GeneralKeyboardIME] instance.
 *
 * @property ime The [GeneralKeyboardIME] instance this handler is associated with.
 */
@Suppress("TooManyFunctions")
class KeyHandler(
    private val ime: GeneralKeyboardIME,
) {
    private val suggestionHandler = SuggestionHandler(ime)
    private val spaceKeyProcessor = SpaceKeyProcessor(ime, suggestionHandler)

    /** Tracks if the last key pressed was a space, used for "period on double space" logic. */
    private var wasLastKeySpace: Boolean = false

    private companion object {
        private const val TAG = "KeyHandler"
    }

    /**
     * Handles a key press event. This is the main entry point for processing key codes from the keyboard.
     * It routes the key code to the appropriate handler method based on the code and the current IME state.
     *
     * @param code The integer code of the key that was pressed (e.g., from [KeyboardBase]).
     * @param language The current keyboard language.
     */
    fun handleKey(
        code: Int,
        language: String,
    ) {
        val inputConnection = ime.currentInputConnection
        if (!isValidState(inputConnection)) {
            wasLastKeySpace = false
            return
        }

        resetShiftIfNeeded(code)

        val previousWasLastKeySpace = wasLastKeySpace
        var resetWLSAtEnd = true

        when (code) {
            KeyboardBase.KEYCODE_TAB -> commitTab(inputConnection)
            KeyboardBase.KEYCODE_CAPS_LOCK -> handleCapsLock()
            KeyboardBase.KEYCODE_DELETE -> handleDeleteKey()
            KeyboardBase.KEYCODE_SHIFT -> {
                handleShiftKey()
                wasLastKeySpace = previousWasLastKeySpace
                resetWLSAtEnd = false
            }
            KeyboardBase.KEYCODE_ENTER -> handleEnterKey()
            KeyboardBase.KEYCODE_MODE_CHANGE -> handleModeChangeKey()
            KeyboardBase.KEYCODE_SPACE -> {
                wasLastKeySpace = spaceKeyProcessor.processKeycodeSpace(previousWasLastKeySpace)
                resetWLSAtEnd = false
            }
            KeyboardBase.KEYCODE_LEFT_ARROW,
            KeyboardBase.KEYCODE_RIGHT_ARROW,
            -> handleArrowKey(code == KeyboardBase.KEYCODE_RIGHT_ARROW)
            KeyboardBase.DISPLAY_LEFT,
            KeyboardBase.DISPLAY_RIGHT,
            -> handleConjugateCycleKeys(code, context = ime.applicationContext)
            KeyboardBase.CODE_FPS,
            KeyboardBase.CODE_FPP,
            KeyboardBase.CODE_SPS,
            KeyboardBase.CODE_SPP,
            KeyboardBase.CODE_TPS,
            KeyboardBase.CODE_TPP,
            KeyboardBase.CODE_TR,
            KeyboardBase.CODE_TL,
            KeyboardBase.CODE_BR,
            KeyboardBase.CODE_BL,
            KeyboardBase.CODE_1X1,
            KeyboardBase.CODE_1X3_LEFT,
            KeyboardBase.CODE_1X3_CENTER,
            KeyboardBase.CODE_1X3_RIGHT,
            KeyboardBase.CODE_2X1_TOP,
            KeyboardBase.CODE_2X1_BOTTOM,
            -> handleConjugateSelectionKey(code, language)
            else -> handleDefaultKey(code)
        }

        if (resetWLSAtEnd) {
            wasLastKeySpace = false
        }
    }

    /**
     * Checks if the IME is in a valid state to process key events.
     * A valid state requires a non-null keyboard instance and an active input connection.
     *
     * @param inputConnection The current input connection.
     * @return `true` if the state is valid, `false` otherwise.
     */
    private fun isValidState(inputConnection: InputConnection?): Boolean =
        ime.keyboard != null &&
            inputConnection != null

    /**
     * Resets the shift key's double-tap timestamp if the pressed key is not the shift key itself.
     * This is used to manage the "shift lock on double-tap" feature.
     *
     * @param code The integer code of the key that was pressed.
     */
    private fun resetShiftIfNeeded(code: Int) {
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.lastShiftPressTS = 0
        }
    }

    /**
     * Commits a tab character to the current input connection.
     *
     * @param inputConnection The active input connection.
     */
    private fun commitTab(inputConnection: InputConnection) {
        inputConnection.commitText("\t", GeneralKeyboardIME.COMMIT_TEXT_CURSOR_POSITION)
    }

    /**
     * Toggles the state of the caps lock on the keyboard.
     * If the shift state is off, it changes to locked; otherwise, it turns it off.
     */
    private fun handleCapsLock() {
        ime.keyboard?.let { kb ->
            val newState =
                if (kb.mShiftState == KeyboardBase.SHIFT_OFF) {
                    KeyboardBase.SHIFT_LOCKED
                } else {
                    KeyboardBase.SHIFT_OFF
                }
            if (kb.setShifted(newState)) {
                ime.keyboardView?.invalidateAllKeys()
            }
        }
    }

    /**
     * Handles a non-special character key press. It delegates the character insertion to the IME
     * and then triggers a re-evaluation of word suggestions.
     *
     * @param code The character code of the key pressed.
     */
    private fun handleDefaultKey(code: Int) {
        val isCommandBarActive = ime.currentState != ScribeState.IDLE && ime.currentState != ScribeState.SELECT_COMMAND
        ime.handleElseCondition(code, ime.keyboardMode, isCommandBarActive)

        if (!isCommandBarActive) {
            suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
        } else {
            suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        }
    }

    /**
     * Handles the delete/backspace key press. It delegates the deletion logic to the IME
     * and then triggers a re-evaluation of word suggestions based on the new text.
     */
    private fun handleDeleteKey() {
        val isCommandBarActive = ime.currentState != ScribeState.IDLE && ime.currentState != ScribeState.SELECT_COMMAND
        ime.handleDelete(isCommandBarActive)

        if (!isCommandBarActive) {
            suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
        }
    }

    /**
     * Handles the shift key press. It delegates the logic for changing the shift state to the IME
     * and then invalidates the keyboard view to reflect the change.
     */
    private fun handleShiftKey() {
        ime.handleKeyboardLetters(ime.keyboardMode, ime.keyboardView)
        ime.keyboardView?.invalidateAllKeys()
    }

    /**
     * Handles the enter key press by delegating the complex logic (e.g., command execution,
     * editor action) to the main IME class.
     */
    private fun handleEnterKey() {
        ime.handleKeycodeEnter()
    }

    /**
     * Handles the mode change key press (e.g., switching to the symbol keyboard).
     * It delegates the logic to the IME and clears any active suggestions.
     */
    private fun handleModeChangeKey() {
        ime.handleModeChange(ime.keyboardMode, ime.keyboardView, ime)
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()
    }

    /**
     * Handles left and right arrow key presses to move the cursor within the input field
     * and then updates suggestions based on the new cursor position.
     *
     * @param isRight `true` to move right, `false` to move left.
     */
    private fun handleArrowKey(isRight: Boolean) {
        ime.currentInputConnection?.let { ic ->
            val currentPos = ic.getTextBeforeCursor(GeneralKeyboardIME.MAX_TEXT_LENGTH, 0)?.length ?: 0
            val newPos =
                if (isRight) {
                    val textAfter = ic.getTextAfterCursor(GeneralKeyboardIME.MAX_TEXT_LENGTH, 0)?.toString() ?: ""
                    (currentPos + 1).coerceAtMost(currentPos + textAfter.length)
                } else { // isLeft
                    (currentPos - 1).coerceAtLeast(0)
                }
            ic.setSelection(newPos, newPos)
            suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
        }
    }

    /**
     * Handles the cycle keys (< >) in the conjugation view, which move between different
     * tenses or moods (e.g., Present, Past, Future). It updates a shared preference to
     * track the current index and refreshes the UI.
     *
     * @param code The key code, used to determine direction (left or right).
     * @param context The application context to access SharedPreferences.
     */
    private fun handleConjugateCycleKeys(
        code: Int,
        context: Context,
    ) {
        val sharedPreferences = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var currentValue = sharedPreferences.getInt("conjugate_index", 0)

        // Increment or decrement based on the key pressed
        if (code == KeyboardBase.DISPLAY_LEFT) {
            currentValue--
        } else if (code == KeyboardBase.DISPLAY_RIGHT) {
            currentValue++
        }

        editor.putInt("conjugate_index", currentValue)
        editor.apply()

        ime.updateUI()
        Log.i(TAG, "New conjugate_index: $currentValue")
    }

    /**
     * Handles a key press on a specific conjugation key (e.g., "1st Person Singular").
     * If the conjugation has multiple forms, it triggers a sub-view; otherwise, it commits
     * the selected form to the input field and returns to the idle state.
     *
     * @param code The key code of the selected conjugation.
     * @param language The current keyboard language.
     */
    private fun handleConjugateSelectionKey(
        code: Int,
        language: String,
    ) {
        if (!ime.returnIsSubsequentRequired()) {
            ime.handleConjugateKeys(code, false)
            ime.moveToIdleState()
            ime.saveConjugateModeType(language, isSubsequentArea = false)
        } else {
            val word = ime.handleConjugateKeys(code, true)
            ime.setupConjugateSubView(ime.returnSubsequentData(), word)
        }
    }
}
