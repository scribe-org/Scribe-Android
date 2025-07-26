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
        if (code != KeyboardBase.KEYCODE_SPACE) {
            suggestionHandler.clearLinguisticSuggestions()
        }

        val resetWLSAtEnd = processKeyCode(code, language, inputConnection, previousWasLastKeySpace)

        if (resetWLSAtEnd) {
            wasLastKeySpace = false
        }
    }

    /**
     * Processes the key code and returns whether to reset wasLastKeySpace at the end.
     *
     * @param code The key code to process.
     * @param language The current keyboard language.
     * @param inputConnection The current input connection.
     * @param previousWasLastKeySpace The previous state of wasLastKeySpace.
     * @return True to reset wasLastKeySpace, false to preserve it.
     */
    private fun processKeyCode(
        code: Int,
        language: String,
        inputConnection: InputConnection,
        previousWasLastKeySpace: Boolean,
    ): Boolean =
        when (code) {
            KeyboardBase.KEYCODE_TAB -> {
                commitTab(inputConnection)
                true
            }
            KeyboardBase.KEYCODE_CAPS_LOCK -> {
                handleCapsLock()
                true
            }
            KeyboardBase.KEYCODE_DELETE -> {
                handleDeleteKey()
                true
            }
            KeyboardBase.KEYCODE_SHIFT -> handleShiftKeyPress(previousWasLastKeySpace)
            KeyboardBase.KEYCODE_ENTER -> {
                handleEnterKey()
                true
            }
            KeyboardBase.KEYCODE_MODE_CHANGE -> {
                handleModeChangeKey()
                true
            }
            KeyboardBase.KEYCODE_SPACE -> handleSpaceKeyPress(previousWasLastKeySpace)
            in KeyboardBase.NAVIGATION_KEYS -> {
                handleNavigationKey(code)
                true
            }
            in KeyboardBase.SCRIBE_VIEW_KEYS -> {
                handleScribeViewKey(code, language)
                true
            }
            KeyboardBase.CODE_CURRENCY -> {
                handleCurrencyKey(language)
                true
            }
            else -> {
                handleDefaultKey(code)
                true
            }
        }

    /**
     * Handles the shift key press and returns whether to reset wasLastKeySpace at the end.
     *
     * @param previousWasLastKeySpace The previous state of wasLastKeySpace.
     * @return False to preserve wasLastKeySpace state, true to reset it.
     */
    private fun handleShiftKeyPress(previousWasLastKeySpace: Boolean): Boolean {
        handleShiftKey()
        wasLastKeySpace = previousWasLastKeySpace
        return false
    }

    /**
     * Handles the space key press and returns whether to reset wasLastKeySpace at the end.
     *
     * @param previousWasLastKeySpace The previous state of wasLastKeySpace.
     * @return False to preserve wasLastKeySpace state, true to reset it.
     */
    private fun handleSpaceKeyPress(previousWasLastKeySpace: Boolean): Boolean {
        wasLastKeySpace = spaceKeyProcessor.processKeycodeSpace(previousWasLastKeySpace)
        return false
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
        val isCommandBarActive =
            ime.currentState == ScribeState.TRANSLATE ||
                ime.currentState == ScribeState.CONJUGATE ||
                ime.currentState == ScribeState.PLURAL

        ime.handleElseCondition(code, ime.keyboardMode, isCommandBarActive)

        if (ime.currentState == ScribeState.IDLE) {
            suggestionHandler.processEmojiSuggestions(ime.getLastWordBeforeCursor())
        } else if (isCommandBarActive) {
            suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        }
    }
    /**
     * Handles the delete/backspace key press. It delegates the deletion logic to the IME
     * and then triggers a re-evaluation of word suggestions based on the new text.
     */

    private fun handleDeleteKey() {
        val isCommandBarActive =
            ime.currentState == ScribeState.TRANSLATE ||
                ime.currentState == ScribeState.CONJUGATE ||
                ime.currentState == ScribeState.PLURAL

        ime.handleDelete(isCommandBarActive)
        ime.handleDelete(isCommandBarActive, ime.isDeleteRepeating())

        if (ime.currentState == ScribeState.IDLE) {
            suggestionHandler.processEmojiSuggestions(ime.getLastWordBeforeCursor())
        }
    }

    /**
     * Handles the currency symbol key press. It outputs the user's selected currency symbol for the current language.
     */
    private fun handleCurrencyKey(language: String) {
        val currencySymbol = PreferencesHelper.getDefaultCurrencySymbol(ime.applicationContext, language)
        ime.currentInputConnection?.commitText(currencySymbol, 1)

        // Process emoji suggestions if in idle state
        if (ime.currentState == ScribeState.IDLE) {
            suggestionHandler.processEmojiSuggestions(ime.getLastWordBeforeCursor())
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
     * Handles navigation keys (left/right arrows).
     * @param code The key code, used to determine direction.
     */
    private fun handleNavigationKey(code: Int) {
        val isRight = code == KeyboardBase.KEYCODE_RIGHT_ARROW
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
            suggestionHandler.processEmojiSuggestions(ime.getLastWordBeforeCursor())
        }
    }

    /**
     * Handles all special keys related to the Scribe command views (conjugation, etc.).
     * @param code The key code of the pressed key.
     * @param language The current keyboard language.
     */
    private fun handleScribeViewKey(
        code: Int,
        language: String,
    ) {
        when (code) {
            KeyboardBase.DISPLAY_LEFT, KeyboardBase.DISPLAY_RIGHT ->
                handleConjugateCycleKeys(code, ime.applicationContext)
            else ->
                handleConjugateSelectionKey(code, language)
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
