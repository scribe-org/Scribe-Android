// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.os.Handler
import android.os.Looper
import be.scri.R
import be.scri.helpers.AnnotationTextUtils.handleColorAndTextForNounType
import be.scri.helpers.AnnotationTextUtils.handleTextForCaseAnnotation
import be.scri.helpers.ui.SuggestionsHelper
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles auto-suggestions such as noun gender, plurality, case, and emojis.
 *
 * @property ime The [GeneralKeyboardIME] instance this handler is associated with.
 */
@Suppress("TooManyFunctions", "LargeClass")
class SuggestionHandler(
    private val ime: GeneralKeyboardIME,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var emojiSuggestionRunnable: Runnable? = null
    private var linguisticSuggestionRunnable: Runnable? = null
    private var wordSuggestionRunnable: Runnable? = null
    private val suggestionHelper = SuggestionsHelper(ime)

    internal var isSingularAndPlural: Boolean = false
    var pluralWords: Set<String>? = null

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

                val genderSuggestion = findGenderForLastWord(ime.nounKeywords, completedWord)
                val isPluralByDirectCheck = findWhetherWordIsPlural(ime.pluralWords, completedWord)
                val caseSuggestion = getCaseAnnotationForPreposition(ime.caseAnnotation, completedWord)

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

    /**
     * Finds associated emojis for the last typed word.
     * @param emojiKeywords The map of keywords to emojis.
     * @param lastWord The word to look up.
     * @return A mutable list of emoji suggestions, or null if none are found.
     */
    fun findEmojisForLastWord(
        emojiKeywords: HashMap<String, MutableList<String>>?,
        lastWord: String?,
    ): MutableList<String>? {
        lastWord?.let { return emojiKeywords?.get(it.lowercase()) }
        return null
    }

    /**
     * Finds the required grammatical case(s) for a preposition.
     * @param caseAnnotation The map of prepositions to their required cases.
     * @param lastWord The word to look up (which should be a preposition).
     * @return A mutable list of case suggestions (e.g., "accusative case"), or null if not found.
     */
    fun getCaseAnnotationForPreposition(
        caseAnnotation: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ): MutableList<String>? {
        lastWord?.let { return caseAnnotation[it.lowercase()] }
        return null
    }

    /**
     * Finds the grammatical gender(s) for the last typed word.
     * @param nounKeywords The map of nouns to their genders.
     * @param lastWord The word to look up.
     * @return A list of gender strings (e.g., "masculine", "neuter"), or null if not a known noun.
     */
    fun findGenderForLastWord(
        nounKeywords: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? {
        lastWord?.let {
            val gender = nounKeywords[it.lowercase()]
            if (gender != null) {
                isSingularAndPlural = pluralWords?.contains(it.lowercase()) == true
                return gender
            }
        }
        return null
    }

    /**
     * Checks if the last word is a known plural form.
     * @param pluralWords The set of all known plural words.
     * @param lastWord The word to check.
     * @return `true` if the word is in the plural set, `false` otherwise.
     */
    fun findWhetherWordIsPlural(
        pluralWords: Set<String>?,
        lastWord: String?,
    ): Boolean = pluralWords?.contains(lastWord?.lowercase()) == true

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

                val nextWordSuggestion = completedWord.lowercase().let { ime.suggestionWords[it] }

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

                val emojis =
                    if (ime.emojiAutoSuggestionEnabled) {
                        findEmojisForLastWord(ime.emojiKeywords, currentWord)
                    } else {
                        null
                    }

                val hasEmojiSuggestion = !emojis.isNullOrEmpty()

                if (hasEmojiSuggestion) {
                    ime.autoSuggestEmojis = emojis
                    suggestionHelper.updateEmojiSuggestion(true, emojis)
                    ime.updateButtonVisibility(true)
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

    /**
     * A helper function to handle displaying a single noun gender suggestion.
     * @param nounTypeSuggestion A list containing a single gender string.
     * @return `true` if a suggestion was displayed, `false` otherwise.
     */
    internal fun handleSingleNounSuggestion(nounTypeSuggestion: List<String>?): Boolean {
        if (nounTypeSuggestion?.size == 1 && !isSingularAndPlural) {
            val (colorRes, text) = handleColorAndTextForNounType(nounTypeSuggestion[0], ime.language, ime.applicationContext)
            if (text != "" || colorRes != R.color.transparent) {
                ime.handleSingleType(nounTypeSuggestion, "noun")
                return true
            }
        }
        return false
    }

    /**
     * A helper function to handle displaying a single preposition case suggestion.
     * @param caseAnnotationSuggestion A list containing a single case annotation string.
     * @return `true` if a suggestion was displayed, `false` otherwise.
     */
    internal fun handleSingleCaseSuggestion(caseAnnotationSuggestion: List<String>?): Boolean {
        if (caseAnnotationSuggestion?.size == 1) {
            val (colorRes, text) =
                handleTextForCaseAnnotation(
                    caseAnnotationSuggestion[0],
                    ime.language,
                    ime.applicationContext,
                )
            if (text != "" || colorRes != R.color.transparent) {
                ime.handleSingleType(caseAnnotationSuggestion, "preposition")
                return true
            }
        }
        return false
    }

    /**
     * A helper function to handle displaying multiple preposition case suggestions.
     * @param caseAnnotationSuggestion A list containing multiple case annotation strings.
     * @return `true` if suggestions were displayed, `false` otherwise.
     */
    internal fun handleMultipleCases(caseAnnotationSuggestion: List<String>?): Boolean {
        if ((caseAnnotationSuggestion?.size ?: 0) > 1) {
            ime.handleMultipleNounFormats(caseAnnotationSuggestion, "preposition")
            return true
        }
        return false
    }

    /**
     * Handles fallback logic when multiple suggestions are available but only one can be shown,
     * or when the primary suggestion type isn't displayable.
     * @param nounTypeSuggestion The list of noun suggestions.
     * @param caseAnnotationSuggestion The list of case suggestions.
     * @return `true` if a fallback suggestion was applied, `false` otherwise.
     */
    internal fun handleFallbackSuggestions(
        nounTypeSuggestion: List<String>?,
        caseAnnotationSuggestion: List<String>?,
    ): Boolean {
        var appliedSomething = false
        nounTypeSuggestion?.let {
            ime.handleSingleType(it, "noun")
            val (_, text) = handleColorAndTextForNounType(it[0], ime.language, ime.applicationContext)
            if (text != "") appliedSomething = true
        }
        if (!appliedSomething) {
            caseAnnotationSuggestion?.let {
                ime.handleSingleType(it, "preposition")
                val (_, text) = handleTextForCaseAnnotation(it[0], ime.language, ime.applicationContext)
                if (text != "") appliedSomething = true
            }
        }
        return appliedSomething
    }
}
