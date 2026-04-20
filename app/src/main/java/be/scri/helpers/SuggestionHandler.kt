// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.os.Handler
import android.os.Looper
import android.util.Log
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
    private var emojiSuggestionRunnable: Runnable? = null
    private var linguisticSuggestionRunnable: Runnable? = null
    private var wordSuggestionRunnable: Runnable? = null

    /**
     * Companion object for holding constants related to suggestion handling.
     */
    companion object {
        private const val SUGGESTION_DELAY_MS = 50L
    }

    /**
     * Processes the given word to find and display relevant LINGUISTIC suggestions.
     * This includes noun gender, plurality, and case annotations.
     * This is intended to be called AFTER a word is completed (e.g., after space).
     *
     * @param completedWord The word that was just completed.
     */
    fun processLinguisticSuggestions(completedWord: String?) {
        linguisticSuggestionRunnable?.let { handler.removeCallbacks(it) }

        linguisticSuggestionRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE) {
                    clearAllSuggestionsAndHideButtonUI()
                    return@Runnable
                }

                if (completedWord.isNullOrEmpty()) {
                    clearLinguisticSuggestions()
                    return@Runnable
                }

                val genderSuggestion = ime.findGenderForLastWord(ime.nounKeywords, completedWord)
                val isPluralByDirectCheck = ime.findWhetherWordIsPlural(ime.pluralWords, completedWord)
                val caseSuggestion = ime.getCaseAnnotationForPreposition(ime.caseAnnotation, completedWord)

                val hasLinguisticSuggestion =
                    genderSuggestion != null ||
                        isPluralByDirectCheck ||
                        caseSuggestion != null ||
                        ime.isSingularAndPlural

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
            }
        handler.postDelayed(linguisticSuggestionRunnable!!, SUGGESTION_DELAY_MS)
    }

    fun processWordSuggestions(completedWord: String?) {
        wordSuggestionRunnable?.let { handler.removeCallbacks(it) }

        wordSuggestionRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE) {
                    clearAllSuggestionsAndHideButtonUI()
                    return@Runnable
                }

                if (completedWord.isNullOrEmpty()) {
                    clearLinguisticSuggestions()
                    return@Runnable
                }

                val nextWordSuggestion = ime.getNextWordSuggestions(ime.suggestionWords, completedWord)

                if (nextWordSuggestion != null) {
                    ime.wordSuggestions = nextWordSuggestion
                    ime.updateAutoSuggestText(
                        ime.nounTypeSuggestion,
                        ime.checkIfPluralWord || ime.isSingularAndPlural,
                        ime.caseAnnotationSuggestion,
                        nextWordSuggestion,
                    )
                } else {
                    ime.disableAutoSuggest()
                }
            }

        handler.postDelayed(wordSuggestionRunnable!!, SUGGESTION_DELAY_MS)
    }

    /**
     * Processes the given word to find and display relevant EMOJI suggestions.
     * This is intended to be called AS the user types.
     *
     * @param currentWord The word currently being typed.
     */
    fun processEmojiSuggestions(currentWord: String?) {
        emojiSuggestionRunnable?.let { handler.removeCallbacks(it) }
        emojiSuggestionRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE) {
                    clearAllSuggestionsAndHideButtonUI()
                    return@Runnable
                }

                ime.lastWord = currentWord

                if (currentWord.isNullOrEmpty()) {
                    ime.updateButtonVisibility(false)
                    return@Runnable
                }

                var emojis: MutableList<String>?
                if (ime.emojiColonModeOn) {
                    val currentWordCleaned = currentWord.removePrefix(":") // Drop colon that triggered emojiColonMode
                    emojis = if (currentWordCleaned.isEmpty()) { // For no word typed yet, show common emojis
                        EmojiUtils.COMMON_EMOJIS.toMutableList()
                    } else {
                        ime.findEmojisForPrefix(ime.emojiKeywords, currentWordCleaned)
                    }
                } else {
                    emojis = null
                }

                val hasEmojiSuggestion = !emojis.isNullOrEmpty()

                if (hasEmojiSuggestion) {
                    ime.autoSuggestEmojis = emojis
                    ime.updateButtonVisibility(true)
                    ime.updateEmojiSuggestion(true, emojis)
                } else if (ime.emojiColonModeOn) { // Show blank buttons when there are no matches
                    ime.autoSuggestEmojis = mutableListOf()
                    ime.updateEmojiSuggestion(true, mutableListOf())
                } else {
                    ime.updateButtonVisibility(false)
                }
            }

        handler.postDelayed(emojiSuggestionRunnable!!, SUGGESTION_DELAY_MS)
    }

    /**
     * Clears only the linguistic suggestions (gender, case, plural) from the UI.
     * Leaves emoji suggestions untouched.
     */
    fun clearLinguisticSuggestions() {
        linguisticSuggestionRunnable?.let { handler.removeCallbacks(it) }
        ime.disableAutoSuggest()
        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.isSingularAndPlural = false
    }

    /**
     * Clears all suggestion states (noun type, plurality, case, emojis) from the IME
     * and hides suggestion-related UI elements.
     */
    fun clearAllSuggestionsAndHideButtonUI() {
        emojiSuggestionRunnable?.let { handler.removeCallbacks(it) }
        linguisticSuggestionRunnable?.let { handler.removeCallbacks(it) }
        wordSuggestionRunnable?.let { handler.removeCallbacks(it) }

        ime.disableAutoSuggest()

        if (ime.currentState != ScribeState.SELECT_COMMAND) {
            ime.updateButtonVisibility(false)
        }

        ime.nounTypeSuggestion = null
        ime.checkIfPluralWord = false
        ime.caseAnnotationSuggestion = null
        ime.autoSuggestEmojis = null
        ime.isSingularAndPlural = false
    }
}
