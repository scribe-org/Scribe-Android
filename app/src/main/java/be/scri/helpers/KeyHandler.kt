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
     * Handles a key press event.
     * This is the main entry point for processing key codes from the keyboard.
     *
     * @param code The key code of the key that was pressed (e.g., from [KeyboardBase]).
     * @param language The current language.
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
            -> handleConjugateKeys(code, context = ime.applicationContext)
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
            ->
                returnTheConjugateLabels(
                    code,
                    language = language,
                )
            else -> handleDefaultKey(code)
        }

        if (resetWLSAtEnd) {
            wasLastKeySpace = false
        }
    }

    private fun isValidState(inputConnection: InputConnection?): Boolean =
        ime.keyboard != null &&
            inputConnection != null

    private fun resetShiftIfNeeded(code: Int) {
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.lastShiftPressTS = 0
        }
    }

    private fun commitTab(inputConnection: InputConnection) {
        inputConnection.commitText("\t", GeneralKeyboardIME.COMMIT_TEXT_CURSOR_POSITION)
    }

    /**
     * Handles the Caps Lock key event.
     * Toggles the shift state between OFF and LOCKED, and invalidates the keyboard view.
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
     * Handles a default key press (e.g., letters, numbers, symbols).
     * This is called for key codes not specifically handled by other specialized methods.
     * It commits the character to the input connection or the command bar editor
     * and updates suggestions accordingly.
     *
     * @param code The character code of the key pressed.
     */
    private fun handleDefaultKey(code: Int) {
        val isCommandBar =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND
        val binding = if (isCommandBar) ime.keyboardBinding else null

        ime.handleElseCondition(code, ime.keyboardMode, binding, isCommandBar)

        if (!isCommandBar) {
            suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
        } else {
            suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        }
    }

    /**
     * Handles the "Delete" key press.
     * Deletes text from the input connection or the command bar editor,
     * and updates word suggestions if not in command bar mode.
     */
    private fun handleDeleteKey() {
        val isCommandBar =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND
        val binding = if (isCommandBar) ime.keyboardBinding else null
        ime.handleDelete(isCommandBar, binding)

        if (!isCommandBar) {
            suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
        }
    }

    /**
     * Handles the "Shift" key press.
     * Toggles the keyboard's shift state (e.g., off, on for one char, caps lock)
     * by delegating to [GeneralKeyboardIME.handleKeyboardLetters] and
     * invalidates the keyboard view to reflect the change.
     */
    private fun handleShiftKey() {
        ime.handleKeyboardLetters(ime.keyboardMode, ime.keyboardView)
        ime.keyboardView?.invalidateAllKeys()
    }

    /**
     * Handles the "Enter" key press.
     * Depending on the context (input field action, command bar), it either
     * sends an enter key event, performs an editor action, or processes a command.
     */
    private fun handleEnterKey() {
        Log.d(TAG, "handleEnterKey ${ime.currentState}")
        if (ime.currentState == ScribeState.IDLE ||
            ime.currentState == ScribeState.SELECT_COMMAND ||
            ime.currentState == ScribeState.INVALID
        ) {
            suggestionHandler.clearAllSuggestionsAndHideButtonUI()
            ime.handleKeycodeEnter(ime.keyboardBinding, false)
        } else if (ime.currentState == ScribeState.CONJUGATE) {
            ime.handleKeycodeEnter(ime.keyboardBinding, false)
            ime.currentState = ScribeState.SELECT_VERB_CONJUNCTION
            ime.updateUI()
        } else {
            ime.handleKeycodeEnter(ime.keyboardBinding, true)
        }
    }

    /**
     * Handles the "Mode Change" key press (e.g., to switch between letters and symbols).
     * Delegates to [GeneralKeyboardIME.handleModeChange] to switch the keyboard layout
     * and clears all current suggestions as the context changes.
     */
    private fun handleModeChangeKey() {
        ime.handleModeChange(ime.keyboardMode, ime.keyboardView, ime)
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()
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
                } else { // isLeft
                    (currentPos - 1).coerceAtLeast(0)
                }
            ic.setSelection(newPos, newPos)
            suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
        }
    }

    /**
     * Handles left/right conjugate key presses.
     * Updates the "conjugate_index" preference to cycle through conjugate options.
     */
    private fun handleConjugateKeys(
        code: Int,
        context: Context,
    ) {
        Log.i(TAG, "Conjugate key was clicked: $code")
        val sharedPreferences = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentValue = sharedPreferences.getInt("conjugate_index", 0)
        val newValue =
            when (code) {
                KeyboardBase.DISPLAY_LEFT -> currentValue + 1
                KeyboardBase.DISPLAY_RIGHT -> currentValue - 1
                else -> currentValue
            }
        editor.putInt("conjugate_index", newValue)
        editor.apply()
        ime.switchToToolBar()
        Log.i(TAG, "New conjugate_index: $newValue")
    }

    private fun returnTheConjugateLabels(
        code: Int,
        language: String,
    ) {
        if (!ime.returnIsSubsequentRequired()) {
            ime.handleConjugateKeys(code, false)
            ime.currentState = ScribeState.IDLE
            ime.switchToCommandToolBar()
            ime.updateUI()
            ime.saveConjugateModeType(
                language,
                isSubsequentArea = false,
            )
        } else {
            ime.setupConjugateKeysByLanguage(conjugateIndex = 0, true)
            ime.switchToToolBar(isSubsequentArea = false)
            val word = ime.handleConjugateKeys(code, false)
            ime.setupConjugateSubView(ime.returnSubsequentData(), word)
        }
    }
}
