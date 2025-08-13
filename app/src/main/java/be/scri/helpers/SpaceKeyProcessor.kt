// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

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
            handleNormalSpaceInput(currentWasLastKeySpace)
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
     * @param wasLastKeySpace True if the previous key pressed was a space.
     */
    private fun handleNormalSpaceInput(wasLastKeySpace: Boolean) {
        val periodOnDoubleTapEnabled = PreferencesHelper.getEnablePeriodOnSpaceBarDoubleTap(context = ime, ime.language)
        val ic = ime.currentInputConnection ?: return
        val wordBeforeSpace = ime.getLastWordBeforeCursor()
        // Get char before space
        val twoCharsBeforeCursor = ic.getTextBeforeCursor(2, 0)?.toString()
        val charBeforeSpace = if (twoCharsBeforeCursor?.length == 2) twoCharsBeforeCursor[0] else null
        val isPunctuationBeforeSpace = charBeforeSpace == '.' || charBeforeSpace == '?' || charBeforeSpace == '!'

        if (periodOnDoubleTapEnabled && wasLastKeySpace && ime.hasTextBeforeCursor()) {
            val textBeforeTwoChars = ic.getTextBeforeCursor(2, 0)?.toString()

            if (meetsTwoCharDoubleSpacePeriodCondition(textBeforeTwoChars)) {
                val oneCharBefore = ic.getTextBeforeCursor(1, 0)?.toString()
                if (oneCharBefore == " " && !isPunctuationBeforeSpace) {
                    ime.commitPeriodAfterSpace()
                } else {
                    commitNormalSpace()
                }
            } else {
                val textBeforeOneChar = ic.getTextBeforeCursor(1, 0)?.toString()
                if (textBeforeOneChar == " " && !isPunctuationBeforeSpace) {
                    ime.commitPeriodAfterSpace()
                } else {
                    commitNormalSpace()
                }
            }
        } else {
            commitNormalSpace()
        }

        suggestionHandler.processLinguisticSuggestions(wordBeforeSpace)
    }


    /**
     * Commits a single space character to the input connection.
     * This is used when "period on double tap" conditions are not met, the feature is disabled,
     * or a simple space is intended.
     */
    private fun commitNormalSpace() {
        ime.handleElseCondition(
            code = KeyboardBase.KEYCODE_SPACE,
            keyboardMode = ime.keyboardMode,
            commandBarState = false,
        )
    }

    /**
     * Checks if the text before the cursor meets the specific criteria for inserting a period
     * on a double space when the text before is two characters long.
     * Criteria: not null, length is 2, starts with a space, and does not end with " .".
     * This typically matches patterns like " X" (where X is not '.') or "  ".
     * @param textBefore The two characters of text immediately before the cursor.
     * @return True if the conditions are met, false otherwise.
     */
    private fun meetsTwoCharDoubleSpacePeriodCondition(textBefore: String?): Boolean =
        textBefore != null &&
            textBefore.length == 2 &&
            textBefore.startsWith(" ") &&
            !textBefore.endsWith(" .")
}
