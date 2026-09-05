// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.InputConnection
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.Companion.COMMIT_TEXT_CURSOR_POSITION

/**
 * Encapsulates command execution logic, Enter key dispatching,
 * lookup state machine transitions, and command output formatting.
 */
class CommandHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Handles the logic for the Enter key press. This can either perform an editor action,
     * commit a newline, or execute a Scribe command depending on the current state.
     */
    fun handleKeycodeEnter() {
        val inputConnection = ime.currentInputConnection ?: return

        if (ime.currentState == ScribeState.INVALID || ime.currentState == ScribeState.ALREADY_PLURAL) {
            ime.moveToIdleState()
            return
        }

        if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
            handleDefaultEnter(inputConnection)
            return
        }

        val rawInput =
            ime.uiManager
                .getCommandBarTextWithoutCursor()
                .trim()
                .takeIf { it.isNotEmpty() }

        if (rawInput == null) {
            ime.moveToIdleState()
        } else {
            when (ime.currentState) {
                ScribeState.PLURAL, ScribeState.TRANSLATE -> handlePluralOrTranslateState(rawInput, inputConnection)
                ScribeState.CONJUGATE -> handleConjugateState(rawInput)
                else -> handleDefaultEnter(inputConnection)
            }
        }
    }

    /**
     * Handles the Enter key press when in the plural or translate state.
     *
     * @param rawInput The text from the command bar.
     * @param inputConnection The current input connection.
     */
    fun handlePluralOrTranslateState(
        rawInput: String,
        inputConnection: InputConnection,
    ) {
        val isAllCaps = rawInput.isNotEmpty() && rawInput.all { !it.isLetter() || it.isUpperCase() }

        val commandModeOutput =
            when (ime.currentState) {
                ScribeState.PLURAL -> {
                    when (val pluralResult = ime.getPluralRepresentation(rawInput)) {
                        ALREADY_PLURAL_MSG -> {
                            ime.currentState = ScribeState.ALREADY_PLURAL
                            ime.refreshUI()
                            return
                        }

                        null -> ""
                        else -> if (isAllCaps) pluralResult.uppercase() else pluralResult
                    }
                }

                ScribeState.TRANSLATE -> {
                    val translation = ime.getTranslation(ime.language, rawInput)
                    if (isAllCaps) translation.uppercase() else translation
                }

                else -> ""
            }

        if (commandModeOutput.isEmpty()) {
            ime.stateManager.setInvalidState(ime.currentState)
            ime.refreshUI()
        } else {
            applyCommandOutput(commandModeOutput, inputConnection)
        }
    }

    /**
     * Handles the Enter key press when in the `CONJUGATE` state. It fetches the
     * conjugation data for the entered verb and transitions to the selection view.
     *
     * @param rawInput The verb entered in the command bar.
     */
    fun handleConjugateState(rawInput: String) {
        val searchInput = rawInput.lowercase()
        ime.currentVerbForConjugation = rawInput
        val languageAlias = getLanguageAlias(ime.language)

        val tempOutput = ime.dbManagers.conjugateDataManager.getTheConjugateLabels(languageAlias, ime.dataContract, searchInput)

        val isAllCaps = rawInput.isNotEmpty() && rawInput.all { !it.isLetter() || it.isUpperCase() }
        val isCapitalized = !isAllCaps && rawInput.firstOrNull()?.isUpperCase() == true

        ime.conjugateOutput =
            if (tempOutput?.isEmpty() == true || tempOutput?.values?.all { it.isEmpty() } == true) {
                null
            } else if ((isAllCaps || isCapitalized) && tempOutput != null) {
                ime.applyCapitalizationToConjugations(tempOutput, isAllCaps)
            } else {
                tempOutput
            }

        ime.conjugateLabels = ime.dbManagers.conjugateDataManager.extractConjugateHeadings(ime.dataContract, searchInput)

        if (ime.conjugateOutput == null) {
            ime.stateManager.setInvalidState(ScribeState.CONJUGATE)
        } else {
            ime.saveConjugateModeType(ime.language)
            ime.stateManager.moveToState(ScribeState.SELECT_VERB_CONJUNCTION)
        }
        ime.refreshUI()
    }

    /**
     * Handles the default behavior of the Enter key when not in a special Scribe command mode.
     *
     * It performs the editor action or sends a standard Enter key event.
     *
     * @param inputConnection The current input connection.
     */
    fun handleDefaultEnter(inputConnection: InputConnection) {
        val wordBeforeEnter = ime.getLastWordBeforeCursor()
        val imeOptionsActionId = ime.getImeOptionsActionId()
        if (imeOptionsActionId != IME_ACTION_NONE) {
            inputConnection.performEditorAction(imeOptionsActionId)
        } else {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        ime.moveToIdleState()
        if (!wordBeforeEnter.isNullOrEmpty()) {
            ime.suggestionHandler.processLinguisticSuggestions(wordBeforeEnter)
        } else {
            ime.suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        }
    }

    /**
     * Commits the output of a Scribe command (like translation or pluralization) to the input field.
     *
     * @param commandModeOutput The string result of the command.
     * @param inputConnection The current input connection.
     */
    fun applyCommandOutput(
        commandModeOutput: String,
        inputConnection: InputConnection,
    ) {
        if (commandModeOutput.isNotEmpty()) {
            val output = if (!commandModeOutput.endsWith(" ")) "$commandModeOutput " else commandModeOutput
            inputConnection.commitText(output, COMMIT_TEXT_CURSOR_POSITION)
            ime.suggestionHandler.processLinguisticSuggestions(output.trim())
        }
        runCatching {
            ime.uiManager.binding.commandBar
                .setText("")
        }

        ime.moveToIdleState()
    }
}
