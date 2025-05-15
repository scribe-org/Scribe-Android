// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputConnection
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles key events for the EnglishKeyboardIME.
 */
@Suppress("TooManyFunctions")
class KeyHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Processes the given key code and performs the corresponding action.
     */
    fun handleKey(code: Int) {
        val inputConnection = ime.currentInputConnection
        if (!isValidState(inputConnection)) return

        resetShiftIfNeeded(code)

        when (code) {
            KeyboardBase.KEYCODE_TAB -> commitTab(inputConnection)
            KeyboardBase.KEYCODE_CAPS_LOCK -> handleCapsLock()
            KeyboardBase.KEYCODE_DELETE -> handleDeleteKey()
            KeyboardBase.KEYCODE_SHIFT -> handleShiftKey()
            KeyboardBase.KEYCODE_ENTER -> handleEnterKey()
            KeyboardBase.KEYCODE_MODE_CHANGE -> handleModeChangeKey()
            KeyboardBase.KEYCODE_SPACE -> handleKeycodeSpace()
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
            -> returnTheConjugateLabels(code , context = ime.applicationContext)
            else -> handleDefaultKey(code)
        }

        updateKeyboardState(code)
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
     * Updates the keyboard state after each key press, including checking for emojis and plural words.
     * Also updates the shift key state if necessary.
     * @param code the key code that was pressed.
     */
    private fun updateKeyboardState(code: Int) {
        ime.lastWord = ime.getLastWordBeforeCursor()
        Log.d("Debug", "${ime.lastWord}")
        ime.autoSuggestEmojis = ime.emojiKeywords?.let { ime.findEmojisForLastWord(it, ime.lastWord) }
        ime.checkIfPluralWord = ime.pluralWords?.let { ime.findWhetherWordIsPlural(it, ime.lastWord) } == true

        Log.i("MY-TAG", "${ime.checkIfPluralWord}")
        Log.d("Debug", "${ime.autoSuggestEmojis}")
        Log.d("MY-TAG", "${ime.nounTypeSuggestion}")
        ime.updateButtonText(ime.emojiAutoSuggestionEnabled, ime.autoSuggestEmojis)
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.updateShiftKeyState()
        }
    }

    /**
     * Handles left/right conjugate key presses.
     *
     * Updates the "conjugate_index" preference to cycle through conjugate options.
     *
     * @param code    The pressed key code (`KeyboardBase.DISPLAY_LEFT`, `KeyboardBase.DISPLAY_RIGHT`, or other).
     * @param context Application context for accessing shared preferences.
     *
     * - `KeyboardBase.DISPLAY_LEFT`: Increments the conjugate index.
     * - `KeyboardBase.DISPLAY_RIGHT`: Decrements the conjugate index.
     * - Other: Index remains unchanged.
     *
     * The function also saves the new index and triggers the IME to update the toolbar.
     */
    private fun handleConjugateKeys(
        code: Int,
        context: Context,
    ) {
        Log.i("ALPHA", "Conjugate key was clicked")
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
        Log.i("ALPHA", "$newValue")
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
        } else if (ime.currentState == ScribeState.CONJUGATE) {
            ime.handleKeycodeEnter(ime.keyboardBinding, false)
            ime.currentState = ScribeState.SELECT_VERB_CONJUNCTION
            ime.updateUI()
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

    private fun returnTheConjugateLabels(code: Int ,   context: Context) {
        if (!ime.returnIsSubsequentRequired()) {
            ime.handleConjugateKeys(code , false)
            ime.currentState = ScribeState.IDLE
            ime.switchToCommandToolBar()
            ime.updateUI()
        }
        else {

            ime.setupConjugateKeysByLanguage(conjugateIndex = 0 , true)
            ime.switchToToolBar(isSubsequentArea = true)
            val word =  ime.handleConjugateKeys(code , true)
            ime.setupConjugateSubView(ime.returnSubsequentData() , word)
        }
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
