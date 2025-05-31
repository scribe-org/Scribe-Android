// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles auto-suggestions such as noun gender, plurality, case, and emojis.
 *
 * @property ime The [GeneralKeyboardIME] instance this handler is associated with.
 */
class SuggestionHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Processes the given word to find and display relevant suggestions.
     * This includes noun gender, plurality, case annotations, and emojis based on the IME's state.
     *
     * @param currentWord The word currently being typed or the word before the cursor.
     */
    fun processWordSuggestions(currentWord: String?) {
        clearAllSuggestionsAndHideButtonUI()

        if (ime.currentState != ScribeState.IDLE && ime.currentState != ScribeState.SELECT_COMMAND) {
            return
        }

        ime.lastWord = currentWord

        if (currentWord.isNullOrEmpty()) return

        val genderSuggestion = ime.findGenderForLastWord(ime.nounKeywords, currentWord)
        val isPluralByDirectCheck = ime.findWhetherWordIsPlural(ime.pluralWords, currentWord)
        val caseSuggestion = ime.getCaseAnnotationForPreposition(ime.caseAnnotation, currentWord)

        ime.nounTypeSuggestion = genderSuggestion
        ime.checkIfPluralWord = isPluralByDirectCheck
        ime.caseAnnotationSuggestion = caseSuggestion

        val showSpecificSuggestion =
            genderSuggestion != null ||
                isPluralByDirectCheck ||
                caseSuggestion != null ||
                ime.isSingularAndPlural

        if (showSpecificSuggestion) {
            ime.updateAutoSuggestText(
                genderSuggestion,
                isPluralByDirectCheck || ime.isSingularAndPlural,
                caseSuggestion,
            )
        } else {
            updateEmojiSuggestionsOnly(currentWord)
        }
    }

    /**
     * Specifically checks for and displays ONLY emoji suggestions for the given word.
     * This is called if no other specific linguistic suggestions (gender, plural, case) are found.
     *
     * @param word The word to check for emoji suggestions.
     */
    private fun updateEmojiSuggestionsOnly(word: String?) {
        if (ime.emojiAutoSuggestionEnabled && !word.isNullOrEmpty()) {
            val emojis = ime.findEmojisForLastWord(ime.emojiKeywords, word)
            if (!emojis.isNullOrEmpty()) {
                ime.autoSuggestEmojis = emojis
                ime.updateButtonText(true, emojis)
                ime.updateButtonVisibility(true)
            }
        }
    }

    /**
     * Clears all suggestion states (noun type, plurality, case, emojis) from the IME
     * and hides suggestion-related UI elements by calling [GeneralKeyboardIME.disableAutoSuggest]
     * and [GeneralKeyboardIME.updateButtonVisibility].
     */
    fun clearAllSuggestionsAndHideButtonUI() {
        ime.disableAutoSuggest()
        ime.updateButtonVisibility(false)
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null
        ime.isSingularAndPlural = false
    }
}
