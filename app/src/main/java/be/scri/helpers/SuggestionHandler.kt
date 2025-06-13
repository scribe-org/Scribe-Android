// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private var suggestionRunnable: Runnable? = null

    /**
     * Companion object for holding constants related to suggestion handling.
     */
    companion object {
        private const val SUGGESTION_DELAY_MS = 50L
    }

    /**
     * Processes the given word to find and display relevant suggestions.
     * This includes noun gender, plurality, case annotations, and emojis based on the IME's state.
     *
     * @param currentWord The word currently being typed or the word before the cursor.
     */
    fun processWordSuggestions(currentWord: String?) {
        suggestionRunnable?.let { handler.removeCallbacks(it) }

        suggestionRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE && ime.currentState != ScribeState.SELECT_COMMAND) {
                    clearAllSuggestionsAndHideButtonUI()
                    return@Runnable
                }

                ime.lastWord = currentWord

                if (currentWord.isNullOrEmpty()) {
                    clearAllSuggestionsAndHideButtonUI()
                    return@Runnable
                }

                val genderSuggestion = ime.findGenderForLastWord(ime.nounKeywords, currentWord)
                val isPluralByDirectCheck = ime.findWhetherWordIsPlural(ime.pluralWords, currentWord)
                val caseSuggestion = ime.getCaseAnnotationForPreposition(ime.caseAnnotation, currentWord)
                val emojis =
                    if (ime.emojiAutoSuggestionEnabled) {
                        ime.findEmojisForLastWord(ime.emojiKeywords, currentWord)
                    } else {
                        null
                    }

                val hasLinguisticSuggestion =
                    genderSuggestion != null ||
                        isPluralByDirectCheck ||
                        caseSuggestion != null ||
                        ime.isSingularAndPlural

                val hasEmojiSuggestion = !emojis.isNullOrEmpty()

                if (hasLinguisticSuggestion) {
                    ime.nounTypeSuggestion = genderSuggestion
                    ime.checkIfPluralWord = isPluralByDirectCheck
                    ime.caseAnnotationSuggestion = caseSuggestion
                    ime.updateAutoSuggestText(
                        genderSuggestion,
                        isPluralByDirectCheck || ime.isSingularAndPlural,
                        caseSuggestion,
                    )
                } else {
                    ime.disableAutoSuggest()
                }

                if (hasEmojiSuggestion) {
                    ime.autoSuggestEmojis = emojis
                    ime.updateButtonText(true, emojis)
                    ime.updateButtonVisibility(true)
                } else {
                    ime.updateButtonVisibility(false)
                }
            }

        handler.postDelayed(suggestionRunnable!!, SUGGESTION_DELAY_MS)
    }

    /**
     * Clears all suggestion states (noun type, plurality, case, emojis) from the IME
     * and hides suggestion-related UI elements.
     */
    fun clearAllSuggestionsAndHideButtonUI() {
        suggestionRunnable?.let { handler.removeCallbacks(it) }
        ime.disableAutoSuggest()
        ime.updateButtonVisibility(false)
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null
        ime.isSingularAndPlural = false
    }
}
