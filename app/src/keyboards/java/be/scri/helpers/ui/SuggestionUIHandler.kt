// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.view.View
import android.widget.Button
import be.scri.R
import be.scri.helpers.AnnotationTextUtils.handleColorAndTextForNounType
import be.scri.helpers.AnnotationTextUtils.handleTextForCaseAnnotation
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.Companion.NOUN_TYPE_SIZE
import be.scri.services.GeneralKeyboardIME.Companion.SUGGESTION_SIZE

/**
 * Handles auto-suggestion and autocomplete UI layout rendering, button styling,
 * visibility switching, and click listeners for [GeneralKeyboardIME].
 *
 * @property ime The [GeneralKeyboardIME] instance associated with this handler.
 */
@Suppress("TooManyFunctions")
class SuggestionUIHandler(
    private val ime: GeneralKeyboardIME,
) {
    private val uiManager: KeyboardUIManager
        get() = ime.uiManager

    private val themeManager: KeyboardThemeManager
        get() = ime.themeManager

    /**
     * The main dispatcher for displaying linguistic auto-suggestions (gender, case, plurality).
     *
     * @param nounTypeSuggestion The detected gender(s) of the last word.
     * @param isPlural true if the last word is plural.
     * @param caseAnnotationSuggestion The detected case(s) required by the last word.
     * @param wordSuggestions The list of predicted words to display.
     */
    fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
        wordSuggestions: List<String>? = null,
    ) {
        ime.nounTypeSuggestion = nounTypeSuggestion
        ime.checkIfPluralWord = isPlural
        ime.caseAnnotationSuggestion = caseAnnotationSuggestion
        ime.wordSuggestions = wordSuggestions

        if (ime.currentState != ScribeState.IDLE) {
            if (ime.currentState != ScribeState.SELECT_COMMAND) {
                uiManager.disableAutoSuggest(ime.language)
            }
            return
        }
        val hasLinguisticSuggestions = nounTypeSuggestion != null || isPlural || caseAnnotationSuggestion != null || ime.isSingularAndPlural

        val handled =
            when {
                (isPlural && nounTypeSuggestion != null) -> {
                    handleMultipleNounFormats(nounTypeSuggestion, "noun")
                    true
                }

                ((nounTypeSuggestion?.size ?: 0) > 1) -> {
                    handleMultipleNounFormats(nounTypeSuggestion, "noun")
                    true
                }

                handlePluralIfNeeded(isPlural) -> true
                handleSingleNounSuggestion(nounTypeSuggestion) -> true
                handleMultipleCases(caseAnnotationSuggestion) -> true
                handleSingleCaseSuggestion(caseAnnotationSuggestion) -> true
                handleFallbackSuggestions(nounTypeSuggestion, caseAnnotationSuggestion) -> true
                else -> false
            }

        if (!handled) uiManager.disableAutoSuggest(ime.language)
        handleWordSuggestions(wordSuggestions, hasLinguisticSuggestions)
    }

    /**
     * A helper function to specifically trigger the plural suggestion UI if needed.
     *
     * @param isPlural true if the word is plural.
     * @return true if the plural suggestion was handled, false otherwise.
     */
    private fun handlePluralIfNeeded(isPlural: Boolean): Boolean {
        if (isPlural) {
            uiManager.genderSuggestionLeft?.visibility = View.INVISIBLE
            uiManager.genderSuggestionRight?.visibility = View.INVISIBLE
            themeManager.applySingleSuggestionStyle(
                context = ime.applicationContext,
                button = uiManager.binding.translateBtn,
                colorRes = R.color.annotateOrange,
                buttonText = "PL",
                textSizeSp = NOUN_TYPE_SIZE,
            )
            return true
        }
        return false
    }

    /**
     * A helper function to handle displaying a single noun gender suggestion.
     *
     * @param nounTypeSuggestion A list containing a single gender string.
     * @return true if a suggestion was displayed, false otherwise.
     */
    private fun handleSingleNounSuggestion(nounTypeSuggestion: List<String>?): Boolean {
        if (nounTypeSuggestion?.size == 1 && !ime.isSingularAndPlural) {
            val (colorRes, text) = handleColorAndTextForNounType(nounTypeSuggestion[0], ime.language, ime.applicationContext)
            if (text != "" || colorRes != R.color.transparent) {
                handleSingleType(nounTypeSuggestion, "noun")
                return true
            }
        }
        return false
    }

    /**
     * A helper function to handle displaying a single preposition case suggestion.
     *
     * @param caseAnnotationSuggestion A list containing a single case annotation string.
     * @return true if a suggestion was displayed, false otherwise.
     */
    private fun handleSingleCaseSuggestion(caseAnnotationSuggestion: List<String>?): Boolean {
        if (caseAnnotationSuggestion?.size == 1) {
            val (colorRes, text) = handleTextForCaseAnnotation(caseAnnotationSuggestion[0], ime.language, ime.applicationContext)
            if (text != "" || colorRes != R.color.transparent) {
                handleSingleType(caseAnnotationSuggestion, "preposition")
                return true
            }
        }
        return false
    }

    /**
     * A helper function to handle displaying multiple preposition case suggestions.
     *
     * @param caseAnnotationSuggestion A list containing multiple case annotation strings.
     * @return true if suggestions were displayed, false otherwise.
     */
    private fun handleMultipleCases(caseAnnotationSuggestion: List<String>?): Boolean {
        if ((caseAnnotationSuggestion?.size ?: 0) > 1) {
            handleMultipleNounFormats(caseAnnotationSuggestion, "preposition")
            return true
        }
        return false
    }

    /**
     * Handles fallback logic when multiple suggestions are available but only one can be shown,
     * or when the primary suggestion type isn't displayable.
     *
     * @param nounTypeSuggestion The list of noun suggestions.
     * @param caseAnnotationSuggestion The list of case suggestions.
     * @return true if a fallback suggestion was applied, false otherwise.
     */
    private fun handleFallbackSuggestions(
        nounTypeSuggestion: List<String>?,
        caseAnnotationSuggestion: List<String>?,
    ): Boolean {
        var appliedSomething = false
        nounTypeSuggestion?.let {
            handleSingleType(it, "noun")
            val (_, text) = handleColorAndTextForNounType(it[0], ime.language, ime.applicationContext)
            if (text != "") appliedSomething = true
        }
        if (!appliedSomething) {
            caseAnnotationSuggestion?.let {
                handleSingleType(it, "preposition")
                val (_, text) = handleTextForCaseAnnotation(it[0], ime.language, ime.applicationContext)
                if (text != "") appliedSomething = true
            }
        }
        return appliedSomething
    }

    /**
     * Configures a single suggestion button with the appropriate text and color based on the suggestion type.
     *
     * @param singleTypeSuggestion The list containing the single suggestion to display.
     * @param type The type of suggestion, either "noun" or "preposition".
     */
    private fun handleSingleType(
        singleTypeSuggestion: List<String>?,
        type: String? = null,
    ) {
        val suggestionText = singleTypeSuggestion?.getOrNull(0).toString()
        val (colorRes, buttonText) =
            when (type) {
                "noun" -> handleColorAndTextForNounType(suggestionText, ime.language, ime.applicationContext)
                "preposition" -> handleTextForCaseAnnotation(suggestionText, ime.language, ime.applicationContext)
                else -> Pair(R.color.transparent, "")
            }

        uiManager.genderSuggestionLeft?.visibility = View.INVISIBLE
        uiManager.genderSuggestionRight?.visibility = View.INVISIBLE

        themeManager.applySingleSuggestionStyle(
            context = ime.applicationContext,
            button = uiManager.binding.translateBtn,
            colorRes = colorRes,
            buttonText = buttonText,
            textSizeSp = NOUN_TYPE_SIZE,
        )
    }

    /**
     * Applies a specific style to a suggestion button, including text, color, and a custom background.
     *
     * @param button The Button to style.
     * @param colorRes The color resource ID for the background.
     * @param text The text to display on the button.
     * @param backgroundRes The drawable resource ID for the button's background.
     */
    private fun applyInformativeSuggestionStyle(
        button: Button,
        colorRes: Int,
        text: String,
        backgroundRes: Int,
    ) {
        themeManager.applyInformativeSuggestionStyle(
            context = ime.applicationContext,
            button = button,
            colorRes = colorRes,
            text = text,
            backgroundRes = backgroundRes,
        )
    }

    /**
     * Handles the UI logic for displaying multiple suggestions simultaneously,
     * typically for words with multiple genders.
     *
     * @param multipleTypeSuggestion The list of suggestions to display.
     * @param type The type of suggestion, either "noun" or "preposition".
     */
    private fun handleMultipleNounFormats(
        multipleTypeSuggestion: List<String>?,
        type: String? = null,
    ) {
        val suggestionPairs = getSuggestionPairs(type, multipleTypeSuggestion) ?: return
        val (leftSuggestion, rightSuggestion) = suggestionPairs
        val suggestionText = ""
        if (leftSuggestion.second == suggestionText || rightSuggestion.second == suggestionText) {
            handleFallbackOrSingleSuggestion(multipleTypeSuggestion)
            return
        }

        uiManager.genderSuggestionLeft?.visibility = View.VISIBLE
        uiManager.genderSuggestionRight?.visibility = View.VISIBLE
        uiManager.binding.translateBtn.visibility = View.INVISIBLE

        uiManager.genderSuggestionLeft?.let {
            applyInformativeSuggestionStyle(
                it,
                leftSuggestion.first,
                leftSuggestion.second,
                R.drawable.gender_suggestion_button_left_background,
            )
        }

        uiManager.genderSuggestionRight?.let {
            applyInformativeSuggestionStyle(
                it,
                rightSuggestion.first,
                rightSuggestion.second,
                R.drawable.gender_suggestion_button_right_background,
            )
        }
    }

    /**
     * Creates pairs of (color, text) for dual suggestion buttons.
     *
     * @param type The suggestion type ("noun" or "preposition").
     * @param suggestions The list of suggestion strings.
     * @return A pair of pairs, each containing a color resource ID and a text string, or null on failure.
     */
    private fun getSuggestionPairs(
        type: String?,
        suggestions: List<String>?,
    ): Pair<Pair<Int, String>, Pair<Int, String>>? {
        val (leftType, rightType) =
            if (type == "noun" && ime.isSingularAndPlural) {
                "PL" to (suggestions?.getOrNull(0).orEmpty())
            } else {
                (suggestions?.getOrNull(0).orEmpty()) to (suggestions?.getOrNull(1).orEmpty())
            }

        return when (type) {
            "noun" ->
                handleColorAndTextForNounType(leftType, ime.language, ime.applicationContext) to
                    handleColorAndTextForNounType(rightType, ime.language, ime.applicationContext)

            "preposition" ->
                handleTextForCaseAnnotation(leftType, ime.language, ime.applicationContext) to
                    handleTextForCaseAnnotation(rightType, ime.language, ime.applicationContext)

            else -> null
        }
    }

    /**
     * Handles the logic when a word has multiple possible genders or
     * cases but only one suggestion slot is available.
     *
     * @param multipleTypeSuggestion The list of noun suggestions.
     */
    private fun handleFallbackOrSingleSuggestion(multipleTypeSuggestion: List<String>?) {
        val suggestionText = ""
        val validNouns = multipleTypeSuggestion?.filter { handleColorAndTextForNounType(it, ime.language, ime.applicationContext).second != suggestionText }
        val validCases = ime.caseAnnotationSuggestion?.filter { handleTextForCaseAnnotation(it, ime.language, ime.applicationContext).second != suggestionText }
        if (!validNouns.isNullOrEmpty()) {
            handleSingleType(validNouns, "noun")
        } else if (!validCases.isNullOrEmpty()) {
            handleSingleType(validCases, "preposition")
        } else {
            uiManager.disableAutoSuggest(ime.language)
        }
    }

    /**
     * Displays word prediction suggestions on the command buttons.
     *
     * @param wordSuggestions The list of predicted words to display.
     * @param hasLinguisticSuggestions Whether linguistic suggestions are also present.
     */
    private fun handleWordSuggestions(
        wordSuggestions: List<String>?,
        hasLinguisticSuggestions: Boolean,
    ) {
        if (wordSuggestions.isNullOrEmpty()) {
            if (hasLinguisticSuggestions) {
                val baseSuggestions = HintUtils.getBaseAutoSuggestions(ime.language)
                val default1 = baseSuggestions.getOrNull(0).orEmpty()
                val default2 = baseSuggestions.getOrNull(1).orEmpty()
                setSuggestionButton(uiManager.binding.conjugateBtn, default1)
                uiManager.pluralBtn?.let { setSuggestionButton(it, default2) }
            }
            return
        }

        val suggestions = listOfNotNull(wordSuggestions.getOrNull(0), wordSuggestions.getOrNull(1), wordSuggestions.getOrNull(2))
        val suggestion1 = suggestions.getOrNull(0).orEmpty()
        val suggestion2 = suggestions.getOrNull(1).orEmpty()
        val suggestion3 = suggestions.getOrNull(2).orEmpty()

        val emojiCount = ime.autoSuggestEmojis?.size ?: 0
        setSuggestionButton(uiManager.binding.conjugateBtn, suggestion1)

        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
                uiManager.updateButtonVisibility(ime.currentState, true, ime.autoSuggestEmojis)
            }

            hasLinguisticSuggestions && emojiCount == 0 -> {
                uiManager.pluralBtn?.let { setSuggestionButton(it, suggestion2) }
            }

            !hasLinguisticSuggestions && emojiCount != 0 -> {
                setSuggestionButton(uiManager.binding.translateBtn, suggestion2)
                uiManager.updateButtonVisibility(ime.currentState, true, ime.autoSuggestEmojis)
            }

            else -> {
                setSuggestionButton(uiManager.binding.translateBtn, suggestion2)
                uiManager.pluralBtn?.let { setSuggestionButton(it, suggestion3) }
            }
        }
    }

    private fun setSuggestionButton(
        button: Button,
        text: String,
    ) {
        button.text = text
        button.isAllCaps = false
        button.visibility = View.VISIBLE
        button.textSize = SUGGESTION_SIZE
        button.setOnClickListener(null)
        button.background = null
        button.foreground = null
        button.setTextColor(themeManager.getSuggestionTextColor(ime.applicationContext))
        button.setOnClickListener {
            ime.currentInputConnection?.commitText("$text ", 1)
            ime.moveToIdleState()
        }
    }

    /**
     * Pins the word currently being typed into the first (leftmost) suggestion
     * slot, quoted like most mobile keyboards do to mark it as "what you typed"
     * rather than a dictionary suggestion. Called immediately on every keystroke
     * — unlike the completions, it needs no lookup, so it should never lag.
     */
    fun updateTypedWordSuggestion(word: String?) {
        if (ime.currentState != ScribeState.IDLE || word.isNullOrEmpty()) {
            uiManager.disableAutoSuggest(ime.language)
            if (!ime.autoSuggestEmojis.isNullOrEmpty() && ime.emojiAutoSuggestionEnabled) {
                ime.updateEmojiSuggestion(true, ime.autoSuggestEmojis)
                ime.updateButtonVisibility(true)
            }
            return
        }

        setTypedWordButton(uiManager.binding.translateBtn, word)
        setAutocompleteButton(uiManager.binding.conjugateBtn, "")
        if (ime.autoSuggestEmojis.isNullOrEmpty()) {
            uiManager.pluralBtn?.let { setAutocompleteButton(it, "") }
        } else {
            uiManager.updateButtonVisibility(ime.currentState, true, ime.autoSuggestEmojis)
        }

        uiManager.binding.separator1.visibility = View.VISIBLE
        uiManager.binding.separator2.visibility = View.VISIBLE
    }

    /**
     * Fills the remaining suggestion slots with dictionary/engine completions.
     * Clears them (leaving the typed word alone) if not idle.
     */
    fun updateAutocompleteCompletions(completions: List<String>) {
        if (ime.currentState != ScribeState.IDLE) return

        val completion1 = completions.getOrNull(0) ?: ""
        val completion2 = completions.getOrNull(1) ?: ""

        setAutocompleteButton(uiManager.binding.conjugateBtn, completion1)
        if (ime.autoSuggestEmojis.isNullOrEmpty()) {
            uiManager.pluralBtn?.let { setAutocompleteButton(it, completion2) }
        } else {
            uiManager.updateButtonVisibility(ime.currentState, true, ime.autoSuggestEmojis)
        }
    }

    /**
     * Sets up the "what you typed" button: displayed quoted, but tapping it
     * doesn't re-insert the word (it's already in the text field) — it just
     * confirms the word with a space, the same as pressing the space bar
     * would, and moves on to next-word suggestions based on it.
     */
    private fun setTypedWordButton(
        button: Button,
        word: String,
    ) {
        setSuggestionButton(button, "\"$word\"")
        button.setOnClickListener {
            ime.currentInputConnection?.commitText(" ", 1)
            ime.suggestionHandler.processLinguisticSuggestions(word)
            ime.suggestionHandler.processWordSuggestions(word)
            ime.moveToIdleState()
        }
    }

    /**
     * Sets up an autocomplete button with the given suggestion text.
     * When clicked, it replaces the current word with the suggestion.
     */
    private fun setAutocompleteButton(
        button: Button,
        text: String,
    ) {
        setSuggestionButton(button, text)
        if (text.isBlank()) {
            button.setOnClickListener(null)
            return
        }
        button.setOnClickListener {
            val ic = ime.currentInputConnection ?: return@setOnClickListener
            val beforeText = ic.getTextBeforeCursor(50, 0) ?: ""
            val wordStartIndex = beforeText.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '.', ',', '?', '!')) + 1
            val currentWord = beforeText.substring(wordStartIndex)
            ic.deleteSurroundingText(currentWord.length, 0)
            ic.commitText(text, 1)
            ime.moveToIdleState()
        }
    }

    /**
     * Clears autocomplete suggestions by resetting the suggestion strip
     * to the default command buttons via the UI Manager.
     */
    fun clearAutocomplete() {
        if (ime.isUiManagerInitialized) {
            uiManager.disableAutoSuggest(ime.language)
        }
    }
}
