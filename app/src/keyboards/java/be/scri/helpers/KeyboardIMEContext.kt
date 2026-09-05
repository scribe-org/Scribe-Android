// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.view.inputmethod.InputConnection
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.ui.KeyboardUIManager
import be.scri.models.ScribeLanguage
import be.scri.models.ScribeState
import be.scri.views.KeyboardView

/**
 * Interface defining the contract between [be.scri.services.GeneralKeyboardIME]
 * and its helper handlers.
 *
 * This explicit abstraction decouples helper handlers from the concrete IME service,
 * restricts access to only required APIs, and enables clean unit testing.
 */
@Suppress("TooManyFunctions")
interface KeyboardIMEContext {
    companion object {
        const val COMMIT_TEXT_CURSOR_POSITION = 1
        const val MAX_TEXT_LENGTH = 1000
    }

    val imeContext: Context
    val language: String
    val scribeLanguage: ScribeLanguage

    fun getInputConnection(): InputConnection?

    fun getImeResources(): Resources

    fun getImeWindow(): Dialog?

    var keyboard: KeyboardBase?
    var keyboardView: KeyboardView?

    val binding: InputMethodViewBinding
    val uiManager: KeyboardUIManager
    val isUiManagerInitialized: Boolean

    val currentState: ScribeState
    var keyboardMode: Int
    val keyboardLetters: Int
    val keyboardSymbols: Int
    val keyboardSymbolShift: Int

    var lastShiftPressTS: Long

    val currentCommandBarHint: String
    val commandBarHintColor: Int

    val suggestionHandler: SuggestionHandler
    val autocompletionHandler: AutocompletionHandler

    var isSingularAndPlural: Boolean
    var checkIfPluralWord: Boolean
    var nounTypeSuggestion: List<String>?
    var caseAnnotationSuggestion: MutableList<String>?
    var wordSuggestions: List<String>?
    var autoSuggestEmojis: MutableList<String>?
    var lastWord: String?
    val emojiAutoSuggestionEnabled: Boolean

    val nounKeywords: HashMap<String, List<String>>
    val pluralWords: Set<String>?
    val caseAnnotation: HashMap<String, MutableList<String>>
    val suggestionWords: HashMap<String, List<String>>
    val emojiKeywords: HashMap<String, MutableList<String>>?

    fun handleDelete(isLongPress: Boolean = false)

    fun isDeleteRepeating(): Boolean

    fun clearAutocomplete()

    fun disableAutoSuggest()

    fun updateButtonVisibility(enabled: Boolean)

    fun updateEmojiSuggestion(
        enabled: Boolean,
        emojis: MutableList<String>?,
    )

    fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
        wordSuggestions: List<String>? = null,
    )

    fun updateTypedWordSuggestion(word: String?)

    fun updateAutocompleteCompletions(completions: List<String>)

    fun getPreviousWordBeforeCursor(): String?

    fun getLastWordBeforeCursor(): String?

    fun getAutocompletions(
        word: String,
        previousWord: String?,
        limit: Int = 3,
    ): List<String>

    fun findGenderForLastWord(
        nounKeywords: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>?

    fun findWhetherWordIsPlural(
        pluralWords: Set<String>?,
        lastWord: String?,
    ): Boolean

    fun getCaseAnnotationForPreposition(
        caseAnnotation: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ): MutableList<String>?

    fun getNextWordSuggestions(
        wordSuggestions: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>?

    fun findEmojisForLastWord(
        emojiKeywords: HashMap<String, MutableList<String>>?,
        lastWord: String?,
    ): MutableList<String>?

    fun getCommandBarTextWithoutCursor(): String

    fun setCommandBarTextWithCursor(
        text: String,
        cursorAtStart: Boolean = false,
    )

    fun commitPeriodAfterSpace()

    fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
        commandBarState: Boolean = false,
    )

    fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
    )

    fun handleKeycodeEnter()

    fun handleModeChange(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
        context: Context,
    )

    fun hideClipboardSuggestionChip()

    fun openClipboardPanel()

    fun openEmojiKeyboard()

    fun toggleFloatingMode()

    fun updateUI()

    fun returnIsSubsequentRequired(): Boolean

    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean = false,
    ): String?

    fun moveToIdleState()

    fun saveConjugateModeType(
        language: String = this.language,
        isSubsequentArea: Boolean = false,
    )

    fun setupConjugateSubView(
        data: List<List<String>>,
        word: String?,
    )

    fun returnSubsequentData(): List<List<String>>

    fun getKeyboardWidth(): Int

    fun recreateKeyboard()

    fun setBackDisposition(disposition: Int)

    fun applyNavBarColor()
}
