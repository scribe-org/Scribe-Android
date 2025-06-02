// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

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
class KeyHandler(
    private val ime: GeneralKeyboardIME,
) {
    private val suggestionHandler = SuggestionHandler(ime)
    private val spaceKeyProcessor = SpaceKeyProcessor(ime, suggestionHandler)

    /** Tracks if the last key pressed was a space, used for "period on double space" logic. */
    private var wasLastKeySpace: Boolean = false

    /**
     * Handles a key press event.
     * This is the main entry point for processing key codes from the keyboard.
     *
     * @param code The key code of the key that was pressed (e.g., from [KeyboardBase]).
     */
    fun handleKey(code: Int) {
        val inputConnection = ime.currentInputConnection
        if (ime.keyboard == null || inputConnection == null) {
            wasLastKeySpace = false
            return
        }

        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.lastShiftPressTS = 0
        }

        dispatchKeyPress(code, inputConnection)

        if (shouldUpdateShiftKeyState(code)) {
            ime.updateShiftKeyState()
        }
    }

    /**
     * Dispatches the key press to the appropriate handler based on the key code.
     * Manages the [wasLastKeySpace] flag based on the key pressed.
     *
     * @param code The key code of the pressed key.
     * @param inputConnection The current input connection.
     */
    private fun dispatchKeyPress(
        code: Int,
        inputConnection: InputConnection,
    ) {
        val previousWasLastKeySpace = wasLastKeySpace
        var resetWasLastKeySpaceByDefault = true

        when (code) {
            KeyboardBase.KEYCODE_TAB -> {
                inputConnection.commitText("\t", GeneralKeyboardIME.COMMIT_TEXT_CURSOR_POSITION)
            }
            KeyboardBase.KEYCODE_CAPS_LOCK -> {
                handleCapsLockInternal()
            }
            KeyboardBase.KEYCODE_DELETE -> {
                handleDeleteKey()
            }
            KeyboardBase.KEYCODE_SHIFT -> {
                handleShiftKey()
                resetWasLastKeySpaceByDefault = false
                wasLastKeySpace = previousWasLastKeySpace
            }
            KeyboardBase.KEYCODE_ENTER -> {
                handleEnterKey()
            }
            KeyboardBase.KEYCODE_MODE_CHANGE -> {
                handleModeChangeKey()
            }
            KeyboardBase.KEYCODE_SPACE -> {
                wasLastKeySpace = spaceKeyProcessor.processKeycodeSpace(previousWasLastKeySpace)
                resetWasLastKeySpaceByDefault = false
            }
            KeyboardBase.KEYCODE_LEFT_ARROW, KeyboardBase.KEYCODE_RIGHT_ARROW -> {
                handleArrowKey(code == KeyboardBase.KEYCODE_RIGHT_ARROW)
            }
            else -> {
                handleDefaultKey(code)
            }
        }

        if (resetWasLastKeySpaceByDefault) {
            wasLastKeySpace = false
        }
    }

    /**
     * Determines if the shift key state should be updated after a key press.
     * @param code The key code of the pressed key.
     * @return True if the shift key state should be updated, false otherwise.
     */
    private fun shouldUpdateShiftKeyState(code: Int): Boolean =
        code != KeyboardBase.KEYCODE_SHIFT &&
            code != KeyboardBase.KEYCODE_CAPS_LOCK &&
            (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND)

    /**
     * Toggles the Caps Lock state of the keyboard.
     * If Caps Lock is off, it's turned on (locked). If it's on (locked), it's turned off.
     * Invalidates the keyboard view to reflect the change in key appearance.
     */
    private fun handleCapsLockInternal() {
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
     * Updates suggestions based on the resulting state of the IME.
     */
    private fun handleEnterKey() {
        val originalState = ime.currentState
        val isCommandBar =
            originalState != ScribeState.IDLE &&
                originalState != ScribeState.SELECT_COMMAND
        val binding = if (isCommandBar) ime.keyboardBinding else null

        ime.handleKeycodeEnter(binding, isCommandBar)

        when (ime.currentState) {
            ScribeState.IDLE, ScribeState.SELECT_COMMAND -> {
                if (isCommandBar) {
                    suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
                } else {
                    suggestionHandler.clearAllSuggestionsAndHideButtonUI()
                }
            }
            ScribeState.INVALID -> {
                suggestionHandler.clearAllSuggestionsAndHideButtonUI()
            }
            else -> { }
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
     * Handles "Left" or "Right" arrow key presses.
     * Moves the cursor in the input field using IME methods and updates word suggestions
     * based on the new cursor position.
     *
     * @param isRightArrow True if the right arrow key was pressed, false if the left arrow key.
     */
    private fun handleArrowKey(isRightArrow: Boolean) {
        if (isRightArrow) ime.moveCursorRight() else ime.moveCursorLeft()
        suggestionHandler.processWordSuggestions(ime.getLastWordBeforeCursor())
    }
}
