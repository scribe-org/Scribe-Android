// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME

/**
 * Processes key events specifically related to the space key.
 * This includes handling "period on double tap" logic, committing spaces
 * in normal input mode or command bar mode, and interacting with suggestions.
 *
 * @property ime The [GeneralKeyboardIME] instance this processor is associated with.
 * @property suggestionHandler The [SuggestionHandler] to manage suggestions.
 */
class SpaceKeyProcessor(
    private val ime: GeneralKeyboardIME,
    private val suggestionHandler: SuggestionHandler,
) {
    /**
     * Handles the "Space" key press.
     * If not in command bar mode, it implements "period on double tap" logic or commits a normal space.
     * If in command bar mode, it treats space as a regular character input.
     *
     * @param currentWasLastKeySpace The state of whether the previous key was a space.
     *
     * @return The new state for `wasLastKeySpace` after processing the space key.
     */
    fun processKeycodeSpace(currentWasLastKeySpace: Boolean): Boolean {
        val isCommandBar =
            ime.currentState != ScribeState.IDLE &&
                ime.currentState != ScribeState.SELECT_COMMAND

        return if (isCommandBar) {
            handleSpaceInCommandBar()
            false
        } else {
            handleSpaceOutsideCommandBar(currentWasLastKeySpace)
            true
        }
    }

    /**
     * Handles space key press when in command bar mode.
     * It commits the space to the command bar editor and clears suggestions.
     */
    private fun handleSpaceInCommandBar() {
        ime.handleElseCondition(
            code = KeyboardBase.KEYCODE_SPACE,
            keyboardMode = ime.keyboardMode,
            commandBarState = true,
        )
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()
    }

    /**
     * Handles space key press when not in command bar mode.
     * This includes the "period on double tap" logic if enabled and applicable,
     * otherwise commits a normal space. Updates word suggestions.
     *
     * @param wasLastKeySpace true if the previous key pressed was a space.
     */
    private fun handleSpaceOutsideCommandBar(wasLastKeySpace: Boolean) {
        val periodOnDoubleTapEnabled = PreferencesHelper.getEnablePeriodOnSpaceBarDoubleTap(context = ime, ime.language)
        val ic = ime.currentInputConnection ?: return
        val wordBeforeSpace = ime.getLastWordBeforeCursor()

        val textBefore = ic.getTextBeforeCursor(2, 0)?.toString()
        val charBeforeSpace = if (textBefore != null && textBefore.length == 2) textBefore[0] else null
        val isPunctuationOrSpaceBefore =
            charBeforeSpace == null ||
                charBeforeSpace.isWhitespace() ||
                charBeforeSpace in listOf('.', '?', '!', ',')

        var shouldEnableAutoCapitalization = false

        if (periodOnDoubleTapEnabled &&
            wasLastKeySpace &&
            textBefore != null &&
            textBefore.endsWith(" ") &&
            !isPunctuationOrSpaceBefore
        ) {
            ime.commitPeriodAfterSpace()
            shouldEnableAutoCapitalization = true
        } else {
            insertSpace()

            val textAfterSpace = ic.getTextBeforeCursor(2, 0)?.toString()
            if (textAfterSpace?.length == 2) {
                val punctuationChar = textAfterSpace[0]
                val spaceChar = textAfterSpace[1]
                if (spaceChar == ' ' && punctuationChar in listOf('.', '?', '!')) {
                    shouldEnableAutoCapitalization = true
                }
            }
        }

        if (shouldEnableAutoCapitalization) {
            ime.keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
            ime.keyboardView?.invalidateAllKeys()
        }

        suggestionHandler.processLinguisticSuggestions(wordBeforeSpace)
        suggestionHandler.processWordSuggestions(wordBeforeSpace)
    }

    /**
     * Commits a single space character to the input connection.
     * This is used when "period on double tap" conditions are not met, the feature is disabled,
     * or a simple space is intended.
     */
    private fun insertSpace() {
        ime.handleElseCondition(
            code = KeyboardBase.KEYCODE_SPACE,
            keyboardMode = ime.keyboardMode,
            commandBarState = false,
        )
    }
}
