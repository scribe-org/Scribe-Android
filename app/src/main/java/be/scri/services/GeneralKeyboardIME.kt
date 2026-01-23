// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import DataContract
import android.R.color.white
import android.content.Context
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_MASK_CLASS
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.view.inputmethod.EditorInfo.IME_MASK_ACTION
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import be.scri.R
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.AnnotationTextUtils.handleColorAndTextForNounType
import be.scri.helpers.AnnotationTextUtils.handleTextForCaseAnnotation
import be.scri.helpers.AutocompletionHandler
import be.scri.helpers.BackspaceHandler
import be.scri.helpers.DatabaseManagers
import be.scri.helpers.EmojiUtils.insertEmoji
import be.scri.helpers.KeyboardBase
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.getHoldKeyStyle
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.helpers.PreferencesHelper.getIsEmojiSuggestionsEnabled
import be.scri.helpers.PreferencesHelper.getIsSoundEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.helpers.PreferencesHelper.isShowPopupOnKeypressEnabled
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import be.scri.helpers.SuggestionHandler
import be.scri.helpers.data.AutocompletionDataManager
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import be.scri.helpers.ui.KeyboardUIManager
import be.scri.views.KeyboardView
import java.util.Locale

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

@Suppress("TooManyFunctions", "LargeClass")
abstract class GeneralKeyboardIME(
    var language: String,
) : InputMethodService(),
    KeyboardView.OnKeyboardActionListener,
    KeyboardUIManager.KeyboardUIListener {
    // Abstract members required by subclasses (like EnglishKeyboardIME)
    abstract override fun getKeyboardLayoutXML(): Int

    abstract val keyboardLetters: Int
    abstract val keyboardSymbols: Int
    abstract val keyboardSymbolShift: Int

    open var keyboard: KeyboardBase? = null
    var keyboardView: KeyboardView? = null

    // UI Manager instance
    lateinit var uiManager: KeyboardUIManager

    abstract var lastShiftPressTS: Long
    abstract var keyboardMode: Int
    abstract var inputTypeClass: Int
    abstract var enterKeyType: Int
    abstract var switchToLetters: Boolean

    /**
     * Property used by EnglishKeyboardIME override.
     * We define a custom getter here for the base logic, but subclasses can override the field.
     */
    open var hasTextBeforeCursor: Boolean = false
        get() {
            val ic = currentInputConnection ?: return false
            val text = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.trim() ?: ""
            return text.isNotEmpty() && text.lastOrNull() != '.'
        }
        set(value) {
            field = value
        }

    // Delegate backspace handling to a separate class
    private val backspaceHandler = BackspaceHandler(this)

    // Bridge for BackspaceHandler to access binding through UI Manager
    internal val binding: InputMethodViewBinding
        get() = uiManager.binding

    // State Variables
    internal var isSingularAndPlural: Boolean = false
    private var subsequentAreaRequired: Boolean = false
    private var subsequentData: MutableList<List<String>> = mutableListOf()

    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    private lateinit var dbManagers: DatabaseManagers
    private lateinit var suggestionHandler: SuggestionHandler
    private lateinit var autocompletionHandler: AutocompletionHandler
    private lateinit var autocompletionManager: AutocompletionDataManager
    private var dataContract: DataContract? = null

    var emojiKeywords: HashMap<String, MutableList<String>>? = null
    private var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>? = null
    private var conjugateLabels: Set<String> = emptySet()

    private var emojiMaxKeywordLength: Int = 0
    internal lateinit var nounKeywords: HashMap<String, List<String>>
    internal lateinit var suggestionWords: HashMap<String, List<String>>
    var pluralWords: Set<String>? = null
    internal lateinit var caseAnnotation: HashMap<String, MutableList<String>>

    var emojiAutoSuggestionEnabled: Boolean = false
    var lastWord: String? = null
    var autoSuggestEmojis: MutableList<String>? = null
    var caseAnnotationSuggestion: MutableList<String>? = null
    var nounTypeSuggestion: List<String>? = null
    var wordSuggestions: List<String>? = null
    var checkIfPluralWord: Boolean = false
    private var currentEnterKeyType: Int? = null

    internal var currentState: ScribeState = ScribeState.IDLE

    // Properties used by BackspaceHandler, delegated to UI Manager
    internal var currentCommandBarHint: String
        get() = uiManager.currentCommandBarHint
        set(value) {
            uiManager.currentCommandBarHint = value
        }

    internal var commandBarHintColor: Int
        get() = uiManager.commandBarHintColor
        set(value) {
            uiManager.commandBarHintColor = value
        }

    // Conjugation State
    private var currentVerbForConjugation: String? = null
    private var selectedConjugationSubCategory: String? = null

    internal companion object {
        const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500
        const val TEXT_LENGTH = 20
        const val NOUN_TYPE_SIZE = 20f
        const val SUGGESTION_SIZE = 15f
        const val DARK_THEME = "#aeb3be"
        const val LIGHT_THEME = "#4b4b4b"
        internal const val MAX_TEXT_LENGTH = 1000
        const val COMMIT_TEXT_CURSOR_POSITION = 1
        internal const val CUSTOM_CURSOR = "│" // special tall cursor character
    }

    enum class ScribeState { IDLE, SELECT_COMMAND, TRANSLATE, CONJUGATE, PLURAL, SELECT_VERB_CONJUNCTION, INVALID, ALREADY_PLURAL }

    // --- Lifecycle Methods ---

    /**
     * Called when the service is first created. Initializes database and suggestion handlers.
     */
    override fun onCreate() {
        super.onCreate()
        dbManagers = DatabaseManagers(this)
        suggestionHandler = SuggestionHandler(this)
        autocompletionManager = dbManagers.autocompletionManager
        autocompletionHandler = AutocompletionHandler(this)
    }

    /**
     * Creates the main view for the input method, inflating it from XML and setting up the keyboard.
     *
     * @return The root View of the input method.
     */
    override fun onCreateInputView(): View {
        // Initialize UI Manager
        val viewBinding = InputMethodViewBinding.inflate(layoutInflater)
        uiManager = KeyboardUIManager(viewBinding, this, this)
        keyboardView = uiManager.keyboardView

        // Initial Keyboard Setup
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)

        keyboardView?.apply {
            setVibrate = getIsVibrateEnabled(applicationContext, language)
            setSound = getIsSoundEnabled(applicationContext, language)
            setHoldForAltCharacters = getHoldKeyStyle(applicationContext, language)
            setKeyboard(this@GeneralKeyboardIME.keyboard!!)
            mOnKeyboardActionListener = this@GeneralKeyboardIME
        }

        currentState = ScribeState.IDLE
        saveConjugateModeType("none")

        refreshUI()

        return viewBinding.root
    }

    /**
     * Always show the input view. Required for API 36 onwards as edge-to-edge
     * enforcement can cause the keyboard to not display if this returns false.
     */
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    /**
     * Disable fullscreen mode to ensure the keyboard displays correctly on API 36 onwards.
     * Fullscreen mode can interfere with edge-to-edge layouts.
     */
    override fun onEvaluateFullscreenMode(): Boolean = false

    /**
     * Compute the insets for the keyboard view. This is essential for API 36+
     * where the system needs to know the exact size of the keyboard to properly
     * handle edge-to-edge display and window insets.
     */
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        // Access root view via UI manager if initialized
        if (this::uiManager.isInitialized) {
            val inputView = uiManager.binding.root
            if (inputView.visibility == View.VISIBLE && inputView.height > 0) {
                val location = IntArray(2)
                inputView.getLocationInWindow(location)
                outInsets.visibleTopInsets = location[1]
                outInsets.contentTopInsets = location[1]
                outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE
            }
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        keyboardView?.setPreview = isShowPopupOnKeypressEnabled(applicationContext, language)
        keyboardView?.setVibrate = getIsVibrateEnabled(applicationContext, language)
        keyboardView?.setSound = getIsSoundEnabled(applicationContext, language)
        keyboardView?.setHoldForAltCharacters = getHoldKeyStyle(applicationContext, language)
    }

    /**
     * Called when the IME is starting to interact with a new input field.
     * It initializes the keyboard based on the input type and loads all language-specific data.
     *
     * @param attribute The editor information for the new input field.
     * @param restarting true if we are restarting the input with the same editor.
     */
    override fun onStartInput(
        attribute: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)
        inputTypeClass = attribute!!.inputType and TYPE_MASK_CLASS
        enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)
        currentEnterKeyType = enterKeyType

        // This setter triggers the logic in the property override if not shadowed
        hasTextBeforeCursor = currentInputConnection?.getTextBeforeCursor(1, 0)?.isNotEmpty() == true

        val keyboardXml =
            when (inputTypeClass) {
                TYPE_CLASS_NUMBER, TYPE_CLASS_DATETIME, TYPE_CLASS_PHONE -> {
                    keyboardMode = keyboardSymbols
                    R.xml.keys_symbols
                }
                else -> {
                    keyboardMode = keyboardLetters
                    getKeyboardLayoutXML()
                }
            }

        loadLanguageData()

        keyboard = KeyboardBase(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)

        if (keyboardXml == R.xml.keys_symbols) {
            uiManager.setupCurrencySymbol(language)
        }
    }

    /**
     * Called when the input view is starting. It sets up the UI theme, emoji settings,
     * and initial keyboard state.
     *
     * @param editorInfo The editor information for the input field.
     * @param restarting true if we are restarting the input with the same editor.
     */
    override fun onStartInputView(
        editorInfo: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInputView(editorInfo, restarting)
        emojiAutoSuggestionEnabled = getIsEmojiSuggestionsEnabled(applicationContext, language)
        autoSuggestEmojis = null
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()

        moveToIdleState()

        val window = window?.window ?: return
        val isDarkMode = getIsDarkModeOrNot(applicationContext)
        val color = if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color

        window.navigationBarColor = ContextCompat.getColor(this, color)

        // Handle Edge-to-Edge Navigation Bar icons color
        val decorView = window.decorView
        var flags = decorView.systemUiVisibility
        flags =
            if (isLightColor(window.navigationBarColor)) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        decorView.systemUiVisibility = flags

        val textBefore = currentInputConnection?.getTextBeforeCursor(1, 0)?.toString().orEmpty()
        if (textBefore.isEmpty()) keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
    }

    /**
     * Called when the input view is finished. Resets the keyboard state to idle.
     *
     * @param finishingInput true if we are finishing for good,
     * `false` if just switching to another app.
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        moveToIdleState()
    }

    // --- OnKeyboardActionListener Interface Implementation ---

    /**
     * Interface method called by KeyboardView.
     * Delegates to the property 'hasTextBeforeCursor' which subclasses may override.
     */
    override fun hasTextBeforeCursor(): Boolean = hasTextBeforeCursor

    /**
     * Handles the "period on double tap" feature. If enabled, it replaces the two spaces with a period and a space.
     */
    override fun commitPeriodAfterSpace() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            val isPeriodOnDoubleTapEnabled = PreferencesHelper.getEnablePeriodOnSpaceBarDoubleTap(this, language)
            if (isPeriodOnDoubleTapEnabled) {
                currentInputConnection?.apply {
                    deleteSurroundingText(1, 0)
                    commitText(". ", 1)
                }
            } else {
                currentInputConnection?.apply {
                    deleteSurroundingText(1, 0)
                    commitText("  ", 1)
                }
            }
        }
    }

    /**
     * Called when a key is pressed down. Triggers haptic feedback if enabled.
     *
     * @param primaryCode The integer code of the key that was pressed.
     */
    override fun onPress(primaryCode: Int) {
        if (primaryCode != 0) keyboardView?.vibrateIfNeeded()
        if (primaryCode != 0) keyboardView?.soundIfNeeded()
    }

    /**
     * Called when a key is released. Handles the logic
     * to switch back to the letter keyboard
     * after typing a character from the symbol keyboard.
     */
    override fun onActionUp() {
        if (switchToLetters) {
            keyboardMode = keyboardLetters
            keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
            val editorInfo = currentInputEditorInfo
            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL && keyboard?.mShiftState != SHIFT_ON_PERMANENT) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
                }
            }
            keyboardView!!.setKeyboard(keyboard!!)
            switchToLetters = false
        }
    }

    override fun moveCursorLeft() = moveCursor(false)

    override fun moveCursorRight() = moveCursor(true)

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 0)
    }

    /**
     * Handles key input from the keyboard. Delegates to specific handlers based on the key code.
     */
    override fun onKey(code: Int) {
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            when (code) {
                KeyboardBase.KEYCODE_DELETE -> handleDelete()
                KeyboardBase.KEYCODE_SHIFT -> {
                    keyboard?.let {
                        if (keyboardMode == keyboardLetters) {
                            when {
                                it.mShiftState == SHIFT_ON_PERMANENT -> it.mShiftState = SHIFT_OFF
                                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> it.mShiftState = SHIFT_ON_PERMANENT
                                it.mShiftState == SHIFT_ON_ONE_CHAR -> it.mShiftState = SHIFT_OFF
                                it.mShiftState == SHIFT_OFF -> it.mShiftState = SHIFT_ON_ONE_CHAR
                            }
                            lastShiftPressTS = System.currentTimeMillis()
                        } else {
                            handleModeChange(keyboardMode, keyboardView, this)
                        }
                    }
                    keyboardView?.invalidateAllKeys()
                }
                KeyboardBase.KEYCODE_ENTER -> handleKeycodeEnter()
                KeyboardBase.KEYCODE_MODE_CHANGE -> handleModeChange(keyboardMode, keyboardView, this)
                else -> {
                    if (KeyboardBase.SCRIBE_VIEW_KEYS.contains(code)) {
                        val keyLabel = keyboardView?.getKeyLabel(code)
                        if (!keyLabel.isNullOrEmpty()) {
                            commitText("$keyLabel ")
                        }
                    } else {
                        val commandBarState = currentState != ScribeState.IDLE && currentState != ScribeState.SELECT_COMMAND
                        handleElseCondition(code, keyboardMode, commandBarState)
                    }
                }
            }
        }
    }

    // --- Helper Methods ---

    protected fun isPeriodAndCommaEnabled(): Boolean {
        val isPreferenceEnabled = PreferencesHelper.getEnablePeriodAndCommaABC(this, language)
        val isInSearchBar = isSearchBar()
        return isPreferenceEnabled || isInSearchBar
    }

    /**
     * This function is updated to reliably detect search bars in various apps,
     * including browsers like Chrome and Firefox, not just fields with IME_ACTION_SEARCH.
     * The logic is combined into a single return statement to satisfy the `detekt` ReturnCount rule.
     * It checks multiple signals:
     * 1. The explicit IME action for search.
     * 2. The input type variation for URIs (common in address bars).
     * 3. The hint text for keywords like "search" or "address".
     *
     * @return true if the current input field is likely a search or address bar, false otherwise.
     */
    fun isSearchBar(): Boolean {
        val editorInfo = currentInputEditorInfo
        val isActionSearch = (enterKeyType == EditorInfo.IME_ACTION_SEARCH)
        val isUriType = editorInfo?.let { (it.inputType and InputType.TYPE_TEXT_VARIATION_URI) != 0 } == true
        val hasSearchHint =
            editorInfo?.hintText?.toString()?.lowercase(Locale.ROOT)?.let {
                it.contains("search") || it.contains("address")
            } == true
        return isActionSearch || isUriType || hasSearchHint
    }

    private fun loadLanguageData() {
        val languageAlias = getLanguageAlias(language)
        dataContract = dbManagers.getLanguageContract(languageAlias)
        emojiKeywords = dbManagers.emojiManager.getEmojiKeywords(languageAlias)
        emojiMaxKeywordLength = dbManagers.emojiManager.maxKeywordLength
        pluralWords =
            dbManagers.pluralManager
                .getAllPluralForms(languageAlias, dataContract)
                ?.map { it.lowercase() }
                ?.toSet()
        nounKeywords = dbManagers.genderManager.findGenderOfWord(languageAlias, dataContract)
        suggestionWords = dbManagers.suggestionManager.getSuggestions(languageAlias)
        autocompletionManager.loadWords(languageAlias)
        caseAnnotation = dbManagers.prepositionManager.getCaseAnnotations(languageAlias)

        val tempConjugateOutput = dbManagers.conjugateDataManager.getTheConjugateLabels(languageAlias, dataContract, "describe")
        conjugateOutput = if (tempConjugateOutput?.isEmpty() == true) null else tempConjugateOutput
        conjugateLabels = dbManagers.conjugateDataManager.extractConjugateHeadings(dataContract, "coacha")
    }

    private fun isLightColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    /**
     * Saves the type of conjugation layout being used (e.g., "2x2", "3x2") to shared preferences.
     *
     * @param language The current keyboard language.
     * @param isSubsequentArea true if this is for a secondary view.
     */
    internal fun saveConjugateModeType(
        language: String,
        isSubsequentArea: Boolean = false,
    ) {
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        val mode =
            if (!isSubsequentArea) {
                when (language) {
                    "English", "Russian", "Swedish" -> "2x2"
                    "German", "French", "Italian", "Portuguese", "Spanish" -> "3x2"
                    else -> "none"
                }
            } else {
                "none"
            }
        sharedPref.edit { putString("conjugate_mode_type", mode) }
    }

    // --- UI Update Delegation ---

    /**
     * The main dispatcher for updating the entire keyboard UI. It calls the appropriate setup function
     * based on the current [ScribeState].
     */
    internal fun updateUI() = refreshUI()

    private fun refreshUI() {
        if (!this::uiManager.isInitialized) return

        uiManager.updateUI(
            currentState = currentState,
            language = language,
            emojiAutoSuggestionEnabled = emojiAutoSuggestionEnabled,
            autoSuggestEmojis = autoSuggestEmojis,
            conjugateOutput = conjugateOutput,
            conjugateLabels = conjugateLabels,
            selectedConjugationSubCategory = selectedConjugationSubCategory,
            currentVerbForConjugation = currentVerbForConjugation,
        )
    }

    /**
     * Transitions the keyboard to the `IDLE` state and updates the UI.
     */
    internal fun moveToIdleState() {
        clearSuggestionData()
        currentState = ScribeState.IDLE
        saveConjugateModeType("none")
        currentVerbForConjugation = null
        selectedConjugationSubCategory = null
        if (this::uiManager.isInitialized) refreshUI()
    }

    /**
     * Clears all cached suggestion data.
     */
    private fun clearSuggestionData() {
        autoSuggestEmojis = null
        nounTypeSuggestion = null
        caseAnnotationSuggestion = null
        isSingularAndPlural = false
    }

    // --- KeyboardUIListener Implementation ---

    override fun onScribeKeyOptionsClicked() {
        if (currentState == ScribeState.IDLE) {
            clearSuggestionData()
            currentState = ScribeState.SELECT_COMMAND
            saveConjugateModeType("none")
            currentVerbForConjugation = null
        } else {
            moveToIdleState()
        }
        refreshUI()
    }

    override fun onScribeKeyToolbarClicked() {
        moveToIdleState()
    }

    override fun onTranslateClicked() {
        currentState = ScribeState.TRANSLATE
        saveConjugateModeType("none")
        refreshUI()
    }

    override fun onConjugateClicked() {
        currentState = ScribeState.CONJUGATE
        refreshUI()
    }

    override fun onPluralClicked() {
        currentState = ScribeState.PLURAL
        saveConjugateModeType("none")
        if (language == "German") keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
        refreshUI()
    }

    override fun onCloseClicked() {
        moveToIdleState()
    }

    override fun onEmojiSelected(emoji: String) {
        if (emoji.isNotEmpty()) {
            insertEmoji(emoji, currentInputConnection, emojiKeywords, emojiMaxKeywordLength)
        }
    }

    override fun onSuggestionClicked(suggestion: String) {
        currentInputConnection?.commitText("$suggestion ", 1)
        moveToIdleState()
    }

    override fun getCurrentEnterKeyType(): Int = enterKeyType

    override fun onKeyboardActionListener(): KeyboardView.OnKeyboardActionListener = this

    override fun processLinguisticSuggestions(word: String) {
        suggestionHandler.processLinguisticSuggestions(word)
    }

    override fun commitText(text: String) {
        if (currentState == ScribeState.SELECT_VERB_CONJUNCTION) {
            val label = text.trim()
            val conjugateIndex = getValidatedConjugateIndex()
            val title = conjugateOutput?.keys?.elementAtOrNull(conjugateIndex)
            val languageOutput = title?.let { conjugateOutput!![it] }

            val matchingEntry =
                languageOutput?.entries?.find { (_, values) ->
                    if (values.size == 1) values.first() == label else values.joinToString(" / ") == label
                }

            if (matchingEntry != null) {
                val (key, values) = matchingEntry
                if (values.size > 1) {
                    selectedConjugationSubCategory = key
                    refreshUI()
                    return
                }
            }
        }

        currentInputConnection?.commitText(text, 1)
        suggestionHandler.processLinguisticSuggestions(text.trim())

        if (currentState == ScribeState.SELECT_VERB_CONJUNCTION) {
            selectedConjugationSubCategory = null
            moveToIdleState()
        }
    }

    // --- Input Logic ---

    /**
     * Handles the logic for the Enter key press. This can either perform an editor action,
     * commit a newline, or execute a Scribe command depending on the current state.
     */
    fun handleKeycodeEnter() {
        val inputConnection = currentInputConnection ?: return

        if (currentState == ScribeState.INVALID || currentState == ScribeState.ALREADY_PLURAL) {
            moveToIdleState()
            return
        }

        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleDefaultEnter(inputConnection)
            return
        }

        val rawInput = uiManager.getCommandBarTextWithoutCursor().trim().takeIf { it.isNotEmpty() }

        if (rawInput == null) {
            moveToIdleState()
        } else {
            when (currentState) {
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
    private fun handlePluralOrTranslateState(
        rawInput: String,
        inputConnection: InputConnection,
    ) {
        val isAllCaps = rawInput.isNotEmpty() && rawInput.all { !it.isLetter() || it.isUpperCase() }

        val commandModeOutput =
            when (currentState) {
                ScribeState.PLURAL -> {
                    when (val pluralResult = getPluralRepresentation(rawInput)) {
                        ALREADY_PLURAL_MSG -> {
                            currentState = ScribeState.ALREADY_PLURAL
                            refreshUI()
                            return
                        }
                        null -> ""
                        else -> if (isAllCaps) pluralResult.uppercase() else pluralResult
                    }
                }
                ScribeState.TRANSLATE -> {
                    val translation = getTranslation(language, rawInput)
                    if (isAllCaps) translation.uppercase() else translation
                }
                else -> ""
            }

        if (commandModeOutput.isEmpty()) {
            currentState = ScribeState.INVALID
            refreshUI()
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
    private fun handleConjugateState(rawInput: String) {
        val searchInput = rawInput.lowercase()
        currentVerbForConjugation = rawInput
        val languageAlias = getLanguageAlias(language)

        val tempOutput = dbManagers.conjugateDataManager.getTheConjugateLabels(languageAlias, dataContract, searchInput)

        val isAllCaps = rawInput.isNotEmpty() && rawInput.all { !it.isLetter() || it.isUpperCase() }
        val isCapitalized = !isAllCaps && rawInput.firstOrNull()?.isUpperCase() == true

        conjugateOutput =
            if (tempOutput?.isEmpty() == true || tempOutput?.values?.all { it.isEmpty() } == true) {
                null
            } else if ((isAllCaps || isCapitalized) && tempOutput != null) {
                applyCapitalizationToConjugations(tempOutput, isAllCaps)
            } else {
                tempOutput
            }

        conjugateLabels = dbManagers.conjugateDataManager.extractConjugateHeadings(dataContract, searchInput)

        currentState =
            if (conjugateOutput == null) {
                ScribeState.INVALID
            } else {
                saveConjugateModeType(language)
                ScribeState.SELECT_VERB_CONJUNCTION
            }
        refreshUI()
    }

    /**
     * Handles the default behavior of the Enter key when not in a special Scribe command mode.
     *
     * It performs the editor action or sends a standard Enter key event.
     *
     * @param inputConnection The current input connection.
     */
    private fun handleDefaultEnter(inputConnection: InputConnection) {
        val imeOptionsActionId = getImeOptionsActionId()
        if (imeOptionsActionId != IME_ACTION_NONE) {
            inputConnection.performEditorAction(imeOptionsActionId)
        } else {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        moveToIdleState()
    }

    /**
     * Commits the output of a Scribe command (like translation or pluralization) to the input field.
     *
     * @param commandModeOutput The string result of the command.
     * @param inputConnection The current input connection.
     */
    private fun applyCommandOutput(
        commandModeOutput: String,
        inputConnection: InputConnection,
    ) {
        if (commandModeOutput.isNotEmpty()) {
            val output = if (!commandModeOutput.endsWith(" ")) "$commandModeOutput " else commandModeOutput
            inputConnection.commitText(output, COMMIT_TEXT_CURSOR_POSITION)
            suggestionHandler.processLinguisticSuggestions(output.trim())
        }
        uiManager.binding.commandBar.setText("")
        moveToIdleState()
    }

    /**
     * Handles the input of any non-special character key (e.g., letters, numbers, punctuation).
     * It commits the character to the main input field or the command bar.
     *
     * @param code The character code of the key.
     * @param keyboardMode The current keyboard mode.
     * @param commandBarState true if input should go to the command bar.
     */
    fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
        commandBarState: Boolean = false,
    ) {
        if (commandBarState) {
            val codeChar =
                if (Character.isLetter(code.toChar()) && keyboard!!.mShiftState > SHIFT_OFF) {
                    Character.toUpperCase(code.toChar())
                } else {
                    code.toChar()
                }
            val currentTextWithoutCursor = uiManager.getCommandBarTextWithoutCursor()

            if (currentTextWithoutCursor == currentCommandBarHint) {
                uiManager.binding.commandBar.setTextColor(uiManager.commandBarTextColor)
                uiManager.setCommandBarTextWithCursor(codeChar.toString())
            } else {
                val newText = currentTextWithoutCursor + codeChar
                uiManager.setCommandBarTextWithCursor(newText)
            }
        } else {
            val inputConnection = currentInputConnection ?: return
            var codeChar = code.toChar()
            if (Character.isLetter(codeChar) && keyboard!!.mShiftState > SHIFT_OFF) {
                codeChar = Character.toUpperCase(codeChar)
            }

            if (keyboardMode != keyboardLetters && code == KeyboardBase.KEYCODE_SPACE) {
                val originalText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                inputConnection.commitText(codeChar.toString(), 1)
                val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                switchToLetters = originalText != newText
            } else {
                inputConnection.commitText(codeChar.toString(), 1)
            }
        }

        if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR && keyboardMode == keyboardLetters) {
            keyboard!!.mShiftState = SHIFT_OFF
            keyboardView!!.invalidateAllKeys()
        }
    }

    // --- Deletion Logic ---

    /**
     * Handles the logic for the Delete/Backspace key. It deletes characters from either
     * the main input field or the command bar, depending on the context.
     * Delegated to BackspaceHandler.
     *
     * @param isCommandBar true` if the deletion should happen in the command bar.
     * @param isLongPress true` if this is a long press/repeat action, false for single tap.
     */
    fun handleDelete(isLongPress: Boolean = false) {
        val effectiveIsCommandBar = currentState != ScribeState.IDLE && currentState != ScribeState.SELECT_COMMAND
        backspaceHandler.handleBackspace(effectiveIsCommandBar, isLongPress)
    }

    /**
     * Returns whether the delete key is currently repeating (long press).
     * Delegated to BackspaceHandler.
     */
    fun isDeleteRepeating() = backspaceHandler.isDeleteRepeating

    /**
     * Sets the flag to indicate that the delete key is currently repeating (long press).
     * Delegated to BackspaceHandler.
     */
    fun setDeleteRepeating(repeating: Boolean) {
        backspaceHandler.isDeleteRepeating = repeating
    }

    // --- State & Logic Helpers ---

    /**
     * Safely fetches autocomplete suggestions for the given prefix.
     * Returns an empty list if a database or state error occurs.
     */
    fun getAutocompletions(
        prefix: String,
        limit: Int = 3,
    ): List<String> =
        try {
            dbManagers.autocompletionManager.getAutocompletions(prefix, limit)
        } catch (e: SQLiteException) {
            Log.e("GeneralKeyboardIME", "Database error in autocompletion", e)
            emptyList()
        } catch (e: IllegalStateException) {
            Log.e("GeneralKeyboardIME", "Illegal state in autocompletion", e)
            emptyList()
        }

    /**
     * Gets the current text in the command bar without the cursor.
     *
     * @return The text content without the trailing cursor character.
     */
    fun getCommandBarTextWithoutCursor() = uiManager.getCommandBarTextWithoutCursor()

    /**
     * Sets the command bar text and ensures it ends with the custom cursor.
     *
     * @param text The text to set (without cursor).
     * @param cursorAtStart The flag to check if the text in the EditText is empty to determine the position of the cursor
     */
    fun setCommandBarTextWithCursor(
        text: String,
        cursorAtStart: Boolean = false,
    ) = uiManager.setCommandBarTextWithCursor(text, cursorAtStart)

    /**
     * Extracts the last word from the text immediately preceding the cursor.
     *
     * @return The last word as a [String], or null if no word is found.
     */
    fun getLastWordBeforeCursor(): String? = getText()?.trim()?.split("\\s+".toRegex())?.lastOrNull()

    /**
     * Retrieves the text immediately preceding the cursor.
     *
     * @return The text before the cursor, up to a defined maximum length.
     */
    fun getText(): String? = currentInputConnection?.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()

    // --- Misc Private Helpers ---

    /**
     * Gets the IME action ID (e.g., Go, Search, Done) from the current editor info.
     *
     * @return The IME action ID, or `IME_ACTION_NONE`.
     */
    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }

    /**
     * Retrieves the plural form of a word from the database.
     *
     * @param word The singular word to find the plural for.
     *
     * @return The plural form as a string, or null if not found.
     */
    private fun getPluralRepresentation(word: String?): String? {
        if (word.isNullOrEmpty()) return null
        val langAlias = getLanguageAlias(language)
        val lowercaseWord = word.lowercase()
        if (pluralWords?.contains(lowercaseWord) == true) return ALREADY_PLURAL_MSG
        return dbManagers.pluralManager
            .getPluralRepresentation(langAlias, dataContract, word)
            .values
            .firstOrNull()
    }

    /**
     * Retrieves the translation for a given word.
     *
     * @param language The current keyboard language (destination language).
     * @param commandBarInput The word to be translated (source word).
     *
     * @return The translated word as a string.
     */
    private fun getTranslation(
        language: String,
        commandBarInput: String,
    ): String {
        val sourceDest = dbManagers.translationDataManager.getSourceAndDestinationLanguage(language)
        return dbManagers.translationDataManager.getTranslationDataForAWord(sourceDest, commandBarInput)
    }

    /**
     * Applies capitalization to all conjugated forms in the output map.
     * Supports both standard capitalization (first letter) and all capital letters formatting.
     *
     * @param conjugations The original map of conjugations from the database.
     * @param isAllCaps If true, applies all capital letters; if false, capitalizes only first letter.
     *
     * @return A new map with properly formatted conjugations.
     */
    private fun applyCapitalizationToConjugations(
        conjugations: MutableMap<String, MutableMap<String, Collection<String>>>,
        isAllCaps: Boolean = false,
    ): MutableMap<String, MutableMap<String, Collection<String>>> {
        val formattedOutput: MutableMap<String, MutableMap<String, Collection<String>>> = mutableMapOf()
        conjugations.forEach { (tenseKey, conjugationMap) ->
            val formattedConjugations: MutableMap<String, Collection<String>> = mutableMapOf()
            conjugationMap.forEach { (categoryKey, forms) ->
                val formattedForms =
                    forms.map { form ->
                        when {
                            form.isEmpty() -> form
                            isAllCaps -> form.uppercase()
                            else -> form.replaceFirstChar { it.uppercase() }
                        }
                    }
                formattedConjugations[categoryKey] = formattedForms
            }
            formattedOutput[tenseKey] = formattedConjugations
        }
        return formattedOutput
    }

    /**
     * Retrieves and validates the stored index for the current conjugation view.
     * Ensures the index is within the bounds of available conjugation types.
     *
     * @return A valid, zero-based index for the conjugation type.
     */
    private fun getValidatedConjugateIndex(): Int {
        val prefs = getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        var index = prefs.getInt("conjugate_index", 0)
        val maxIndex = conjugateOutput?.keys?.count()?.minus(1) ?: -1
        index = if (maxIndex >= 0) index.coerceIn(0, maxIndex) else 0
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }

    /**
     * Handles the logic for the Shift key. It cycles through shift states (off, on-for-one-char, caps lock)
     * on the letter keyboard, and toggles between symbol pages on the symbol keyboard.
     *
     * @param keyboardMode The current keyboard mode.
     * @param keyboardView The instance of the keyboard view.
     */
    fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
    ) {
        if (keyboardMode == keyboardLetters) {
            when {
                keyboard!!.mShiftState == SHIFT_ON_PERMANENT -> keyboard!!.mShiftState = SHIFT_OFF
                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> keyboard!!.mShiftState = SHIFT_ON_PERMANENT
                keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR -> keyboard!!.mShiftState = SHIFT_OFF
                keyboard!!.mShiftState == SHIFT_OFF -> keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
            }
            lastShiftPressTS = System.currentTimeMillis()
        } else {
            handleModeChange(keyboardMode, keyboardView, this)
        }
    }

    /**
     * Handles switching between the letter and symbol keyboards.
     *
     * @param keyboardMode The current keyboard mode (letters or symbols).
     * @param keyboardView The instance of the keyboard view.
     * @param context The application context.
     */
    fun handleModeChange(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
        context: Context,
    ) {
        val keyboardXml =
            if (keyboardMode == keyboardLetters) {
                this.keyboardMode = keyboardSymbols
                R.xml.keys_symbols
            } else {
                this.keyboardMode = keyboardLetters
                getKeyboardLayoutXML()
            }
        keyboard = KeyboardBase(context, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)
        if (keyboardXml == R.xml.keys_symbols) {
            uiManager.setupCurrencySymbol(language)
        }
    }

    /**
     * Moves the cursor in the input field.
     *
     * @param moveRight true to move right, false to move left.
     */
    private fun moveCursor(moveRight: Boolean) {
        val extractedText = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val newPos = extractedText.selectionStart + if (moveRight) 1 else -1
        currentInputConnection?.setSelection(newPos, newPos)
    }

    /**
     * Finds associated emojis for the last typed word.
     *
     * @param emojiKeywords The map of keywords to emojis.
     * @param lastWord The word to look up.
     *
     * @return A mutable list of emoji suggestions, or null if none are found.
     */
    fun findEmojisForLastWord(
        emojiKeywords: HashMap<String, MutableList<String>>?,
        lastWord: String?,
    ) = lastWord?.let { emojiKeywords?.get(it.lowercase()) }

    /**
     * Finds the grammatical gender(s) for the last typed word.
     *
     * @param nounKeywords The map of nouns to their genders.
     * @param lastWord The word to look up.
     *
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
     *
     * @param pluralWords The set of all known plural words.
     * @param lastWord The word to check.
     *
     * @return true if the word is in the plural set, false otherwise.
     */
    fun findWhetherWordIsPlural(
        pluralWords: Set<String>?,
        lastWord: String?,
    ): Boolean = pluralWords?.contains(lastWord?.lowercase()) == true

    /**
     * Finds the next suggestions for the last typed word.
     *
     * @param wordSuggestions The map of words to their suggestions.
     * @param lastWord The word to look up.
     *
     * @return A list of gender strings (e.g., "masculine", "neuter"), or null if not a known noun.
     */
    fun getNextWordSuggestions(
        wordSuggestions: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? = lastWord?.let { wordSuggestions[it.lowercase()] }

    /**
     * Finds the required grammatical case(s) for a preposition.
     *
     * @param caseAnnotation The map of prepositions to their required cases.
     * @param lastWord The word to look up (which should be a preposition).
     *
     * @return A mutable list of case suggestions (e.g., "accusative case"), or null if not found.
     */
    fun getCaseAnnotationForPreposition(
        caseAnnotation: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ) = lastWord?.let { caseAnnotation[it.lowercase()] }

    // Logic for updating auto-suggest text and buttons.
    // Since KeyboardUIManager doesn't have linguistic logic, we manipulate views here.

    /**
     * The main dispatcher for displaying linguistic auto-suggestions (gender, case, plurality).
     *
     * @param nounTypeSuggestion The detected gender(s) of the last word.
     * @param isPlural true if the last word is plural.
     * @param caseAnnotationSuggestion The detected case(s) required by the last word.
     */
    fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
        wordSuggestions: List<String>? = null,
    ) {
        this.nounTypeSuggestion = nounTypeSuggestion
        this.checkIfPluralWord = isPlural
        this.caseAnnotationSuggestion = caseAnnotationSuggestion
        this.wordSuggestions = wordSuggestions

        if (currentState != ScribeState.IDLE) {
            uiManager.disableAutoSuggest(language)
            return
        }
        val hasLinguisticSuggestions = nounTypeSuggestion != null || isPlural || caseAnnotationSuggestion != null || isSingularAndPlural

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

        if (!handled) uiManager.disableAutoSuggest(language)
        handleWordSuggestions(wordSuggestions, hasLinguisticSuggestions)
    }

    // --- Linguistic Logic here to manipulate exposed views ---

    /**
     * A helper function to specifically trigger the plural suggestion UI if needed.
     *
     * @param isPlural true if the word is plural.
     *
     * @return true if the plural suggestion was handled, false otherwise.
     */
    private fun handlePluralIfNeeded(isPlural: Boolean): Boolean {
        if (isPlural) {
            uiManager.genderSuggestionLeft?.visibility = View.INVISIBLE
            uiManager.genderSuggestionRight?.visibility = View.INVISIBLE
            uiManager.binding.translateBtn.apply {
                visibility = View.VISIBLE
                text = "PL"
                textSize = NOUN_TYPE_SIZE
                background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.annotateOrange)
                setTextColor(ContextCompat.getColor(context, white))
                isClickable = false
                setOnClickListener(null)
            }
            return true
        }
        return false
    }

    /**
     * A helper function to handle displaying a single noun gender suggestion.
     *
     * @param nounTypeSuggestion A list containing a single gender string.
     *
     * @return true if a suggestion was displayed, false otherwise.
     */
    private fun handleSingleNounSuggestion(nounTypeSuggestion: List<String>?): Boolean {
        if (nounTypeSuggestion?.size == 1 && !isSingularAndPlural) {
            val (colorRes, text) = handleColorAndTextForNounType(nounTypeSuggestion[0], language, applicationContext)
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
     *
     * @return true if a suggestion was displayed, false otherwise.
     */
    private fun handleSingleCaseSuggestion(caseAnnotationSuggestion: List<String>?): Boolean {
        if (caseAnnotationSuggestion?.size == 1) {
            val (colorRes, text) = handleTextForCaseAnnotation(caseAnnotationSuggestion[0], language, applicationContext)
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
     *
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
     *
     * @return true if a fallback suggestion was applied, false otherwise.
     */
    private fun handleFallbackSuggestions(
        nounTypeSuggestion: List<String>?,
        caseAnnotationSuggestion: List<String>?,
    ): Boolean {
        var appliedSomething = false
        nounTypeSuggestion?.let {
            handleSingleType(it, "noun")
            val (_, text) = handleColorAndTextForNounType(it[0], language, applicationContext)
            if (text != "") appliedSomething = true
        }
        if (!appliedSomething) {
            caseAnnotationSuggestion?.let {
                handleSingleType(it, "preposition")
                val (_, text) = handleTextForCaseAnnotation(it[0], language, applicationContext)
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
                "noun" -> handleColorAndTextForNounType(suggestionText, language, applicationContext)
                "preposition" -> handleTextForCaseAnnotation(suggestionText, language, applicationContext)
                else -> Pair(R.color.transparent, "")
            }

        uiManager.genderSuggestionLeft?.visibility = View.INVISIBLE
        uiManager.genderSuggestionRight?.visibility = View.INVISIBLE
        uiManager.binding.translateBtn.textSize = NOUN_TYPE_SIZE

        uiManager.binding.translateBtn.apply {
            visibility = View.VISIBLE
            text = buttonText
            isClickable = false
            setOnClickListener(null)

            if (colorRes != R.color.transparent) {
                background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
                backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
                setTextColor(ContextCompat.getColor(context, white))
            } else {
                background = null
                val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.transparent)
                setTextColor(ContextCompat.getColor(context, if (isUserDarkMode) white else android.R.color.black))
            }
        }
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
        button.text = text
        button.setTextColor(ContextCompat.getColor(applicationContext, be.scri.R.color.white))
        button.isClickable = false
        button.setOnClickListener(null)

        val background = ContextCompat.getDrawable(applicationContext, backgroundRes)?.mutate()

        if (background is RippleDrawable) {
            val contentDrawable = background.getDrawable(0)

            if (contentDrawable is LayerDrawable) {
                val shapeDrawable =
                    contentDrawable.findDrawableByLayerId(
                        be.scri.R.id.button_background_shape,
                    ) as? GradientDrawable

                shapeDrawable?.setColor(
                    ContextCompat.getColor(
                        applicationContext,
                        colorRes,
                    ),
                )
            }
        }
        button.background = background
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
                be.scri.R.drawable.gender_suggestion_button_left_background,
            )
        }

        uiManager.genderSuggestionRight?.let {
            applyInformativeSuggestionStyle(
                it,
                rightSuggestion.first,
                rightSuggestion.second,
                be.scri.R.drawable.gender_suggestion_button_right_background,
            )
        }
    }

    /**
     * Creates pairs of (color, text) for dual suggestion buttons.
     *
     * @param type The suggestion type ("noun" or "preposition").
     * @param suggestions The list of suggestion strings.
     *
     * @return A pair of pairs, each containing a color resource ID and a text string, or null on failure.
     */
    private fun getSuggestionPairs(
        type: String?,
        suggestions: List<String>?,
    ): Pair<Pair<Int, String>, Pair<Int, String>>? {
        val (leftType, rightType) =
            if (type == "noun" && isSingularAndPlural) {
                "PL" to (suggestions?.getOrNull(0) ?: "")
            } else {
                (suggestions?.getOrNull(0) ?: "") to (suggestions?.getOrNull(1) ?: "")
            }

        return when (type) {
            "noun" ->
                handleColorAndTextForNounType(leftType, language, applicationContext) to
                    handleColorAndTextForNounType(rightType, language, applicationContext)
            "preposition" ->
                handleTextForCaseAnnotation(leftType, language, applicationContext) to
                    handleTextForCaseAnnotation(rightType, language, applicationContext)
            else -> null
        }
    }

    /**
     * Handles the logic when a word has multiple possible genders or
     * cases but only one suggestion slot is available.
     *
     * It picks the first valid suggestion to display.
     * @param multipleTypeSuggestion The list of noun suggestions.
     */
    private fun handleFallbackOrSingleSuggestion(multipleTypeSuggestion: List<String>?) {
        val suggestionText = ""
        val validNouns = multipleTypeSuggestion?.filter { handleColorAndTextForNounType(it, language, applicationContext).second != suggestionText }
        val validCases = caseAnnotationSuggestion?.filter { handleTextForCaseAnnotation(it, language, applicationContext).second != suggestionText }
        if (!validNouns.isNullOrEmpty()) {
            handleSingleType(validNouns, "noun")
        } else if (!validCases.isNullOrEmpty()) {
            handleSingleType(validCases, "preposition")
        } else {
            uiManager.disableAutoSuggest(language)
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
        if (wordSuggestions.isNullOrEmpty()) return

        val suggestions = listOfNotNull(wordSuggestions.getOrNull(0), wordSuggestions.getOrNull(1), wordSuggestions.getOrNull(2))
        val suggestion1 = suggestions.getOrNull(0) ?: ""
        val suggestion2 = suggestions.getOrNull(1) ?: ""
        val suggestion3 = suggestions.getOrNull(2) ?: ""

        val emojiCount = autoSuggestEmojis?.size ?: 0
        setSuggestionButton(uiManager.binding.conjugateBtn, suggestion1)

        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
                uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
            }
            hasLinguisticSuggestions && emojiCount == 0 -> {
                setSuggestionButton(uiManager.pluralBtn!!, suggestion2)
            }
            else -> {
                setSuggestionButton(uiManager.binding.translateBtn, suggestion2)
                setSuggestionButton(uiManager.pluralBtn!!, suggestion3)
            }
        }
    }

    private fun setSuggestionButton(
        button: Button,
        text: String,
    ) {
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        val textColor = if (isUserDarkMode) Color.WHITE else "#1E1E1E".toColorInt()
        button.text = text
        button.isAllCaps = false
        button.visibility = View.VISIBLE
        button.textSize = SUGGESTION_SIZE
        button.setOnClickListener(null)
        button.background = null
        button.setTextColor(textColor)
        button.setOnClickListener {
            currentInputConnection?.commitText("$text ", 1)
            moveToIdleState()
        }
    }

    // --- Autocomplete ---

    /**
     * Updates autocomplete UI with a new list of suggestions.
     * Clears it if not idle or no completions.
     */
    fun updateAutocompleteSuggestions(completions: List<String>?) {
        if (currentState != ScribeState.IDLE) {
            uiManager.disableAutoSuggest(language)
            return
        }
        if (completions.isNullOrEmpty()) {
            uiManager.disableAutoSuggest(language)
            return
        }

        val completion1 = completions.getOrNull(0) ?: ""
        val completion2 = completions.getOrNull(1) ?: ""
        val completion3 = completions.getOrNull(2) ?: ""

        setAutocompleteButton(uiManager.binding.conjugateBtn, completion1)
        setAutocompleteButton(uiManager.binding.translateBtn, completion2)
        setAutocompleteButton(uiManager.pluralBtn!!, completion3)

        uiManager.binding.separator1.visibility = View.VISIBLE
        uiManager.binding.separator2.visibility = View.VISIBLE
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
            val ic = currentInputConnection ?: return@setOnClickListener
            val beforeText = ic.getTextBeforeCursor(50, 0) ?: ""
            val wordStartIndex = beforeText.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '.', ',', '?', '!')) + 1
            val currentWord = beforeText.substring(wordStartIndex)
            ic.deleteSurroundingText(currentWord.length, 0)
            ic.commitText(text, 1)
            moveToIdleState()
        }
    }

    /**
     * Clears autocomplete suggestions by resetting the suggestion strip
     * to the default command buttons via the UI Manager.
     */
    fun clearAutocomplete() {
        if (this::uiManager.isInitialized) {
            uiManager.disableAutoSuggest(language)
        }
    }

    /**
     * Returns whether the current conjugation state requires a subsequent selection view.
     * This is used, for example, when a conjugation form has multiple options (e.g., "am/is/are" in English).
     *
     * @return true if a subsequent selection screen is needed, false otherwise.
     */
    fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    fun returnSubsequentData(): List<List<String>> = subsequentData

    /**
     * Handles a key press on one of the special conjugation keys.
     * It either commits the text directly or prepares for a subsequent selection view.
     *
     * @param code The key code of the pressed key.
     * @param isSubsequentRequired true if a sub-view is needed for more options.
     *
     * @return The label of the key that was pressed.
     */
    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean,
    ): String? {
        val keyLabel = keyboardView?.getKeyLabel(code)
        if (!isSubsequentRequired) {
            if (!keyLabel.isNullOrEmpty()) {
                currentInputConnection?.commitText("$keyLabel ", 1)
                suggestionHandler.processLinguisticSuggestions(keyLabel)
            }
        }
        return keyLabel
    }

    /**
     * Sets up a secondary "sub-view" for conjugation when a single key has multiple options.
     *
     * @param data The full dataset of subsequent options.
     * @param word The specific word selected from the primary view, used to filter the data.
     */
    fun setupConjugateSubView(
        data: List<List<String>>,
        word: String?,
    ) {
        val uniqueData = data.distinct()
        val filteredData = uniqueData.filter { sublist -> sublist.contains(word) }
        val flattenList = filteredData.flatten()
        saveConjugateModeType(language = language, true)
        val prefs = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        val keyboardXmlId = getKeyboardLayoutForState(currentState, true, flattenList.size)
        // Re-initialize keyboard via UI manager helper which calls 'initializeKeyboard(xml)'
        uiManager.initializeKeyboard(keyboardXmlId)
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        when (flattenList.size) {
            DATA_SIZE_2 -> {
                keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_2X1_TOP)
                keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_2X1_BOTTOM)
                subsequentAreaRequired = false
            }
            DATA_CONSTANT_3 -> {
                keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_1X3_RIGHT)
                keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_1X3_CENTER)
                keyboardView?.setKeyLabel(flattenList[DATA_SIZE_2], "HI", KeyboardBase.CODE_1X3_RIGHT)
                subsequentAreaRequired = false
            }
        }
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        // binding access via uiManager
        uiManager.binding.ivInfo.visibility = View.GONE
    }

    /**
     * Determines which keyboard layout XML to use based on the current [ScribeState].
     *
     * @param state The current state of the Scribe keyboard.
     * @param isSubsequentArea true if this is for a secondary conjugation view.
     * @param dataSize The number of items to display, used to select an appropriate layout.
     *
     * @return The resource ID of the keyboard layout XML.
     */
    private fun getKeyboardLayoutForState(
        state: ScribeState,
        isSubsequentArea: Boolean = false,
        dataSize: Int = 0,
    ): Int =
        when (state) {
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                saveConjugateModeType(language)
                if (!isSubsequentArea && dataSize == 0) {
                    when (language) {
                        "English", "Russian", "Swedish" -> R.xml.conjugate_view_2x2
                        else -> R.xml.conjugate_view_3x2
                    }
                } else {
                    when (dataSize) {
                        DATA_SIZE_2 -> R.xml.conjugate_view_2x1
                        DATA_CONSTANT_3 -> R.xml.conjugate_view_1x3
                        else -> R.xml.conjugate_view_2x2
                    }
                }
            }
            else -> {
                getKeyboardLayoutXML()
            }
        }

    /**
     * Updates the visibility of the suggestion buttons based on device type (phone/tablet)
     * and whether auto-suggestions are currently active.
     *
     * @param enabled true if suggestions are available.
     */
    fun updateButtonVisibility(enabled: Boolean) = uiManager.updateButtonVisibility(currentState, enabled, autoSuggestEmojis)

    /**
     * Updates the text of the suggestion buttons, primarily for displaying emoji suggestions.
     *
     * @param enabled true if suggestions are active.
     * @param emojis The list of emojis to display.
     */
    fun updateEmojiSuggestion(
        enabled: Boolean,
        emojis: MutableList<String>?,
    ) = uiManager.updateEmojiSuggestion(currentState, enabled, emojis)

    /**
     * Disables all auto-suggestions and resets the suggestion buttons to their default, inactive state.
     */
    fun disableAutoSuggest() = uiManager.disableAutoSuggest(language)
}
