// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles key events for the GeneralKeyboardIME.
 */
class KeyHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Processes the given key code and performs the corresponding action.
     */
    fun handleKey(code: Int) {
        val inputConnection = ime.currentInputConnection
        if (ime.keyboard == null || inputConnection == null) return

        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.lastShiftPressTS = 0
        }

        if (code == KeyboardBase.KEYCODE_TAB) {
            inputConnection.commitText("\t", GeneralKeyboardIME.COMMIT_TEXT_CURSOR_POSITION)
        } else if (code == KeyboardBase.KEYCODE_CAPS_LOCK) {
            handleCapsLockInternal()
        } else if (code == KeyboardBase.KEYCODE_DELETE) {
            handleDeleteKey()
        } else if (code == KeyboardBase.KEYCODE_SHIFT) {
            ime.handleKeyboardLetters(ime.keyboardMode, ime.keyboardView)
            ime.keyboardView?.invalidateAllKeys()
        } else if (code == KeyboardBase.KEYCODE_ENTER) {
            handleEnterKey()
        } else if (code == KeyboardBase.KEYCODE_MODE_CHANGE) {
            ime.handleModeChange(ime.keyboardMode, ime.keyboardView, ime)
            ime.disableAutoSuggest()
            ime.updateButtonVisibility(false)
            ime.nounTypeSuggestion = null
            ime.checkIfPluralWord = false
            ime.caseAnnotationSuggestion = null
            ime.autoSuggestEmojis = null
        } else if (code == KeyboardBase.KEYCODE_SPACE) {
            handleKeycodeSpace()
        } else if (code == KeyboardBase.KEYCODE_LEFT_ARROW || code == KeyboardBase.KEYCODE_RIGHT_ARROW) {
            handleArrowKey(isRightArrow = (code == KeyboardBase.KEYCODE_RIGHT_ARROW))
        } else {
            handleDefaultKey(code)
        }

        if (code != KeyboardBase.KEYCODE_SPACE) {
            updateKeyboardStateAfterKeyPress(code)
        }
    }

    /**
     * Handles the internal logic for toggling CAPS LOCK state.
     */
    private fun handleCapsLockInternal() {
        ime.keyboard?.let { kb ->
            val newState =
                when (kb.mShiftState) {
                    KeyboardBase.SHIFT_OFF -> KeyboardBase.SHIFT_LOCKED
                    else -> KeyboardBase.SHIFT_OFF
                }
            if (kb.setShifted(newState)) {
                ime.keyboardView?.invalidateAllKeys()
            }
        }
    }

    /**
     * Updates general keyboard state, like emoji suggestions (if applicable) and shift state.
     * Called after most key presses, except for SPACE.
     */
    private fun updateKeyboardStateAfterKeyPress(code: Int) {
        ime.lastWord = ime.getLastWordBeforeCursor()

        val isIdleAndNoSpecificSuggestions =
            ime.currentState == ScribeState.IDLE &&
                ime.nounTypeSuggestion == null &&
                !ime.checkIfPluralWord &&
                ime.caseAnnotationSuggestion == null

        if (isIdleAndNoSpecificSuggestions) {
            updateEmojiSuggestions(ime.lastWord)
        }

        if (code != KeyboardBase.KEYCODE_SHIFT) {
            ime.updateShiftKeyState()
        }
    }

    private fun handleDefaultKey(code: Int) {
        val isForToolbarCommandBar =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND
        val currentBindingForCommandBar = if (isForToolbarCommandBar) ime.keyboardBinding else null

        ime.handleElseCondition(
            code,
            ime.keyboardMode,
            binding = currentBindingForCommandBar,
            commandBarState = isForToolbarCommandBar,
        )
        // Inlined clearAllSuggestionStatesAndHideButton()
        ime.disableAutoSuggest()
        ime.updateButtonVisibility(false)
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null
        // End Inlined
    }

    private fun handleDeleteKey() {
        val isDeleteForCommandBar =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND
        val bindingForDelete = if (isDeleteForCommandBar) ime.keyboardBinding else null

        ime.handleDelete(
            currentState = isDeleteForCommandBar,
            binding = bindingForDelete,
        )

        if (!isDeleteForCommandBar) {
            val newLastWord = ime.getLastWordBeforeCursor()
            processWordSuggestions(newLastWord)
        }
    }

    private fun handleEnterKey() {
        val isCommandBarState =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND
        val bindingForEnter = if (isCommandBarState) ime.keyboardBinding else null

        ime.handleKeycodeEnter(
            binding = bindingForEnter,
            commandBarState = isCommandBarState,
        )

        if (isCommandBarState) {
            ime.currentState = ScribeState.IDLE
            ime.switchToCommandToolBar()
            ime.updateUI()
        }
        // Inlined clearAllSuggestionStatesAndHideButton()
        ime.disableAutoSuggest()
        ime.updateButtonVisibility(false)
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null
        // End Inlined
    }

    private fun handleArrowKey(isRightArrow: Boolean) {
        ime.currentInputConnection?.let { ic ->
            val currentPos = ic.getTextBeforeCursor(GeneralKeyboardIME.MAX_TEXT_LENGTH, 0)?.length ?: 0
            val newPos =
                if (isRightArrow) {
                    val textAfter = ic.getTextAfterCursor(GeneralKeyboardIME.MAX_TEXT_LENGTH, 0)?.toString() ?: ""
                    (currentPos + 1).coerceAtMost(currentPos + textAfter.length)
                } else {
                    (currentPos - 1).coerceAtLeast(0)
                }
            ic.setSelection(newPos, newPos)
        }
        // Inlined clearAllSuggestionStatesAndHideButton()
        ime.disableAutoSuggest()
        ime.updateButtonVisibility(false)
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null
        // End Inlined
    }

    private fun handleKeycodeSpace() {
        val isCommandBarContext =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND

        if (!isCommandBarContext) {
            ime.handleElseCondition(
                code = KeyboardBase.KEYCODE_SPACE,
                keyboardMode = ime.keyboardMode,
                binding = null,
                commandBarState = false,
            )
            val wordBeforeSpace = ime.getLastWordBeforeCursor()
            processWordSuggestions(wordBeforeSpace)
        } else {
            // Handle space in command bar
            ime.handleElseCondition(
                code = KeyboardBase.KEYCODE_SPACE,
                keyboardMode = ime.keyboardMode,
                binding = ime.keyboardBinding,
                commandBarState = true,
            )
            // Inlined clearAllSuggestionStatesAndHideButton()
            ime.disableAutoSuggest()
            ime.updateButtonVisibility(false)
            ime.nounTypeSuggestion = null
            ime.checkIfPluralWord = false
            ime.caseAnnotationSuggestion = null
            ime.autoSuggestEmojis = null
            // End Inlined
        }
    }

    /**
     * Processes the given word to find and display relevant suggestions (noun type, plural, case, or emojis).
     */
    private fun processWordSuggestions(currentWord: String?) {
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null

        if (!currentWord.isNullOrEmpty()) {
            ime.lastWord = currentWord

            val genderSuggestion = ime.findGenderForLastWord(ime.nounKeywords, currentWord)
            val isPlural = ime.findWhetherWordIsPlural(ime.pluralWords, currentWord)
            val caseSuggestion = ime.getCaseAnnotationForPreposition(ime.caseAnnotation, currentWord)

            // Update IME state with new findings
            ime.nounTypeSuggestion = genderSuggestion
            ime.checkIfPluralWord = isPlural
            ime.caseAnnotationSuggestion = caseSuggestion

            if (genderSuggestion != null || isPlural || caseSuggestion != null) {
                ime.updateAutoSuggestText(genderSuggestion, isPlural, caseSuggestion)
                ime.updateButtonVisibility(false)
            } else {
                ime.disableAutoSuggest()
                updateEmojiSuggestions(currentWord)
            }
        } else {
            // Word is null or empty, clear all suggestions and hide button
            ime.lastWord = currentWord

            ime.disableAutoSuggest()
            ime.updateButtonVisibility(false)
            ime.nounTypeSuggestion = null
            ime.checkIfPluralWord = false
            ime.caseAnnotationSuggestion = null
            ime.autoSuggestEmojis = null
        }
    }

    /**
     * Checks for and displays emoji suggestions for the given word.
     * This is used proactively or as a fallback.
     */
    private fun updateEmojiSuggestions(word: String?) {
        if (ime.emojiAutoSuggestionEnabled && !word.isNullOrEmpty()) {
            val emojis = ime.findEmojisForLastWord(ime.emojiKeywords, word)
            ime.autoSuggestEmojis = emojis
            if (emojis?.isNotEmpty() == true) {
                ime.updateButtonText(true, emojis)
                ime.updateButtonVisibility(true)
            } else {
                ime.updateButtonVisibility(false)
            }
        } else {
            // Emoji suggestions disabled or word is null/empty
            ime.updateButtonVisibility(false)
        }
    }
}
