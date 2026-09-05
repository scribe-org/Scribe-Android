// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import DataContract
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_MASK_CLASS
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.view.inputmethod.EditorInfo.IME_MASK_ACTION
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.AnnotationTextUtils.handleColorAndTextForNounType
import be.scri.helpers.AnnotationTextUtils.handleTextForCaseAnnotation
import be.scri.helpers.AutocompletionHandler
import be.scri.helpers.BackspaceHandler
import be.scri.helpers.DatabaseManagers
import be.scri.helpers.EmojiUtils.insertEmoji
import be.scri.helpers.FloatingKeyboardHandler
import be.scri.helpers.KeyHandler
import be.scri.helpers.KeyboardBase
import be.scri.helpers.KeyboardDataHandler
import be.scri.helpers.KeyboardIMEContext
import be.scri.helpers.KeyboardLanguageMappingConstants
import be.scri.helpers.KeyboardStateManager
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.NativeSuggestionEngine
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.getHoldKeyStyle
import be.scri.helpers.PreferencesHelper.getIsEmojiSuggestionsEnabled
import be.scri.helpers.PreferencesHelper.getIsSoundEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.helpers.PreferencesHelper.isShowPopupOnKeypressEnabled
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import be.scri.helpers.SuggestionHandler
import be.scri.helpers.clipboard.ClipboardHandler
import be.scri.helpers.data.AutocompletionDataManager
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import be.scri.helpers.recordRecentEmoji
import be.scri.helpers.ui.KeyboardThemeManager
import be.scri.helpers.ui.KeyboardUIManager
import be.scri.models.ScribeLanguage
import be.scri.models.ScribeState
import be.scri.views.KeyboardView
import java.util.Locale

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

@Suppress("TooManyFunctions", "LargeClass")
abstract class GeneralKeyboardIME(
    override val scribeLanguage: ScribeLanguage,
) : InputMethodService(),
    KeyboardView.OnKeyboardActionListener,
    KeyboardUIManager.KeyboardUIListener,
    KeyboardBase.KeyboardContextProvider,
    KeyboardIMEContext {
    constructor(languageName: String) : this(ScribeLanguage.fromDisplayName(languageName))

    override val imeContext: Context
        get() = applicationContext

    override fun getInputConnection(): InputConnection? = getCurrentInputConnection()

    override fun getImeResources(): Resources = resources

    override fun getImeWindow(): Dialog? = getWindow()

    override val language: String
        get() = scribeLanguage.displayName

    // Abstract members required by subclasses (like EnglishKeyboardIME).
    abstract override fun getKeyboardLayoutXML(): Int

    abstract override val keyboardLetters: Int
    abstract override val keyboardSymbols: Int
    abstract override val keyboardSymbolShift: Int

    override var keyboard: KeyboardBase? = null
    override var keyboardView: KeyboardView? = null

    // UI Manager instance.
    override lateinit var uiManager: KeyboardUIManager

    abstract override var lastShiftPressTS: Long
    abstract override var keyboardMode: Int
    abstract var inputTypeClass: Int
    abstract var enterKeyType: Int
    abstract var switchToLetters: Boolean

    // Language-specific layout and behavior configurations (decoupled from base class).
    open val defaultConjugateModeType: String = "3x2"
    open val defaultConjugateLayoutXML: Int = R.xml.conjugate_view_3x2
    open val isPluralCapitalized: Boolean = false

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

    // Delegate backspace handling to a separate class.
    private val backspaceHandler = BackspaceHandler(this)

    // Bridge for BackspaceHandler to access binding through UI Manager.
    override val binding: InputMethodViewBinding
        get() = uiManager.binding

    internal val clipboardHandler by lazy { ClipboardHandler(this) }
    internal var hasNewClip: Boolean
        get() = clipboardHandler.hasNewClip
        set(value) {
            clipboardHandler.hasNewClip = value
        }
    internal var latestClipText: String?
        get() = clipboardHandler.latestClipText
        set(value) {
            clipboardHandler.latestClipText = value
        }

    // MARK: State Variables

    override var isSingularAndPlural: Boolean = false
    private var subsequentAreaRequired: Boolean = false
    private var subsequentData: MutableList<List<String>> = mutableListOf()

    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    internal val dataHandler = KeyboardDataHandler()

    internal val dbManagers: DatabaseManagers
        get() = dataHandler.dbManagers

    internal val autocompletionManager: AutocompletionDataManager
        get() = dataHandler.autocompletionManager

    private lateinit var nativeSuggestionEngine: NativeSuggestionEngine
    override lateinit var suggestionHandler: SuggestionHandler
    override lateinit var autocompletionHandler: AutocompletionHandler
    internal lateinit var keyHandler: KeyHandler
    internal val floatingKeyboardHandler by lazy { FloatingKeyboardHandler(this) }

    internal var dataContract: DataContract?
        get() = dataHandler.dataContract
        set(value) {
            dataHandler.dataContract = value
        }

    override val isUiManagerInitialized: Boolean get() = this::uiManager.isInitialized

    override var emojiKeywords: HashMap<String, MutableList<String>>?
        get() = dataHandler.emojiKeywords
        set(value) {
            dataHandler.emojiKeywords = value
        }

    private var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>?
        get() = dataHandler.conjugateOutput
        set(value) {
            dataHandler.conjugateOutput = value
        }

    private var conjugateLabels: Set<String>
        get() = dataHandler.conjugateLabels
        set(value) {
            dataHandler.conjugateLabels = value
        }

    private var emojiMaxKeywordLength: Int
        get() = dataHandler.emojiMaxKeywordLength
        set(value) {
            dataHandler.emojiMaxKeywordLength = value
        }

    override var nounKeywords: HashMap<String, List<String>>
        get() = dataHandler.nounKeywords
        set(value) {
            dataHandler.nounKeywords = value
        }

    override var suggestionWords: HashMap<String, List<String>>
        get() = dataHandler.suggestionWords
        set(value) {
            dataHandler.suggestionWords = value
        }

    override var pluralWords: Set<String>?
        get() = dataHandler.pluralWords
        set(value) {
            dataHandler.pluralWords = value
        }

    override var caseAnnotation: HashMap<String, MutableList<String>>
        get() = dataHandler.caseAnnotation
        set(value) {
            dataHandler.caseAnnotation = value
        }

    override var emojiAutoSuggestionEnabled: Boolean = false
    override var lastWord: String? = null
    override var autoSuggestEmojis: MutableList<String>? = null
    override var caseAnnotationSuggestion: MutableList<String>? = null
    override var nounTypeSuggestion: List<String>? = null
    override var wordSuggestions: List<String>? = null
    override var checkIfPluralWord: Boolean = false
    private var currentEnterKeyType: Int? = null
    private var isNumericKeyboardActive: Boolean = false

    internal val stateManager = KeyboardStateManager()
    internal val themeManager = KeyboardThemeManager()

    override var currentState: ScribeState
        get() = stateManager.currentState
        set(value) {
            stateManager.currentState = value
        }

    internal var invalidCommandSource: ScribeState
        get() = stateManager.invalidCommandSource
        set(value) {
            stateManager.invalidCommandSource = value
        }

    // Properties used by BackspaceHandler, delegated to UI Manager.
    override var currentCommandBarHint: String
        get() = uiManager.currentCommandBarHint
        set(value) {
            uiManager.currentCommandBarHint = value
        }

    override var commandBarHintColor: Int
        get() = uiManager.commandBarHintColor
        set(value) {
            uiManager.commandBarHintColor = value
        }

    // MARK: Conjugation State

    private var currentVerbForConjugation: String? = null
    private var selectedConjugationSubCategory: String? = null

    protected open fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    internal companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
        const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500
        const val TEXT_LENGTH = 20
        const val NOUN_TYPE_SIZE = 20f
        const val SUGGESTION_SIZE = 15f
        const val DARK_THEME = "#aeb3be"
        const val LIGHT_THEME = "#4b4b4b"
        internal const val MAX_TEXT_LENGTH = 1000
        const val COMMIT_TEXT_CURSOR_POSITION = 1
        internal const val CUSTOM_CURSOR = "│" // special tall cursor character

        internal fun shouldUseNumericKeyboard(inputType: Int): Boolean =
            when (inputType and TYPE_MASK_CLASS) {
                TYPE_CLASS_NUMBER, TYPE_CLASS_DATETIME, TYPE_CLASS_PHONE -> true
                else -> false
            }

        internal fun getKeyboardLayoutXMLForInputType(
            inputType: Int,
            letterKeyboardLayoutXML: Int,
        ): Int =
            if (shouldUseNumericKeyboard(inputType)) {
                R.xml.keys_numeric
            } else {
                letterKeyboardLayoutXML
            }
    }

    // MARK: Lifecycle Methods

    /**
     * Called when the service is first created. Initializes database and suggestion handlers.
     */
    override fun onCreate() {
        super.onCreate()
        dataHandler.initialize(this)
        nativeSuggestionEngine = NativeSuggestionEngine(this)
        suggestionHandler = SuggestionHandler(this)
        autocompletionHandler = AutocompletionHandler(this)
        keyHandler = KeyHandler(this)
        clipboardHandler.initClipboardMonitor()
    }

    override fun onDestroy() {
        if (this::nativeSuggestionEngine.isInitialized) {
            nativeSuggestionEngine.close()
        }
        super.onDestroy()
    }

    /**
     * Creates the main view for the input method, inflating it from XML and setting up the keyboard.
     *
     * @return The root View of the input method.
     */
    override fun onCreateInputView(): View {
        // Initialize UI manager.
        val viewBinding = InputMethodViewBinding.inflate(layoutInflater)
        uiManager = KeyboardUIManager(viewBinding, this, this)
        keyboardView = uiManager.keyboardView

        // Initial keyboard setup.
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType, getKeyboardWidth())

        keyboardView?.apply {
            setVibrate = getIsVibrateEnabled(applicationContext, language)
            setSound = getIsSoundEnabled(applicationContext, language)
            setHoldForAltCharacters = getHoldKeyStyle(applicationContext, language)
            setKeyboard(this@GeneralKeyboardIME.keyboard!!)
            mOnKeyboardActionListener = this@GeneralKeyboardIME
        }

        currentState = ScribeState.IDLE
        saveConjugateModeType("none")

        viewBinding.root.post {
            disableParentClipping(viewBinding.root)
        }
        initFloatingMode()
        setupFloatingDragListener()

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
        if (this::uiManager.isInitialized) {
            val inputView = uiManager.binding.root
            if (inputView.visibility == View.VISIBLE && inputView.height > 0) {
                val location = IntArray(2)
                inputView.getLocationInWindow(location)

                if (isFloatingMode) {
                    // In floating mode, report zero insets so Android doesn't
                    // push app content up or render IME chrome (∨ / 🌐 buttons)
                    // below the card. The touchable region is restricted to the
                    // card bounds so taps outside reach the underlying app.
                    outInsets.visibleTopInsets = inputView.height
                    outInsets.contentTopInsets = inputView.height
                    outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION

                    val card = binding.keyboardCard
                    val density = resources.displayMetrics.density
                    if (card.width > 0 && card.height > 0) {
                        val centerX = card.left + card.width / 2f + card.translationX
                        val centerY = card.top + card.height / 2f + card.translationY
                        val visualW = card.width * card.scaleX
                        val visualH = card.height * card.scaleY
                        val left = (centerX - visualW / 2f).toInt()
                        val top = (centerY - visualH / 2f).toInt()
                        val right = (centerX + visualW / 2f).toInt()
                        val bottom = (centerY + visualH / 2f).toInt()

                        val rect = Rect(left, top, right, bottom)
                        // Expand touchable region slightly to allow resizing handles to be clickable
                        val margin = (25 * density).toInt()
                        rect.inset(-margin, -margin)
                        outInsets.touchableRegion.set(rect)
                    } else {
                        outInsets.touchableRegion.setEmpty()
                    }
                } else {
                    outInsets.visibleTopInsets = location[1]
                    outInsets.contentTopInsets = location[1]
                    outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE
                }
            }
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        applyFloatingModeState()
        applyNavBarColor()
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

        // This setter triggers the logic in the property override if not shadowed.
        hasTextBeforeCursor = currentInputConnection?.getTextBeforeCursor(1, 0)?.isNotEmpty() == true

        isNumericKeyboardActive = shouldUseNumericKeyboard(attribute.inputType)
        keyboardMode = if (isNumericKeyboardActive) keyboardSymbols else keyboardLetters
        val keyboardXml = getKeyboardLayoutXMLForInputType(attribute.inputType, getKeyboardLayoutXML())

        loadLanguageData()

        keyboard = KeyboardBase(this, keyboardXml, enterKeyType, getKeyboardWidth())
        keyboardView?.setKeyboard(keyboard!!)

        if (this::uiManager.isInitialized && keyboardXml == R.xml.keys_symbols) {
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
        clipboardHandler.startMonitoring()
        emojiAutoSuggestionEnabled = getIsEmojiSuggestionsEnabled(applicationContext, language)
        autoSuggestEmojis = null
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()

        moveToIdleState()

        val languageAlias = getLanguageAlias(language)
        val dbFile = applicationContext.getDatabasePath("${languageAlias}LanguageData.sqlite")
        val hasData = dbFile.exists()
        val bannerContainer = binding.root.findViewById<View>(R.id.empty_state_banner_container)
        val banner = binding.root.findViewById<TextView>(R.id.empty_state_banner)
        val downloadDataText =
            KeyboardLanguageMappingConstants.downloadDataPlaceholder[languageAlias]
                ?: "Please download language data"
        banner.text = downloadDataText
        bannerContainer.visibility =
            if (hasData) View.GONE else View.VISIBLE
        binding.commandOptionsBar.visibility =
            if (hasData && !isNumericKeyboardActive) View.VISIBLE else View.GONE
        themeManager.applyBannerTheme(
            context = applicationContext,
            banner = banner,
            bannerContainer = bannerContainer,
        )

        bannerContainer.setOnClickListener {
            val intent =
                Intent(applicationContext, MainActivity::class.java)
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("navigate_to", "download_data")
                    }

            startActivity(intent)
        }

        applyNavBarColor()

        // Set initial shift state for empty text fields.
        if (keyboardMode == keyboardLetters) {
            val textBefore = currentInputConnection?.getTextBeforeCursor(1, 0)?.toString().orEmpty()
            if (textBefore.isEmpty()) {
                keyboardView?.mKeyboard?.mShiftState = SHIFT_ON_ONE_CHAR
            }
            keyboardView?.invalidateAllKeys()
        }
    }

    /**
     * Called when the input view is finished. Resets the keyboard state to idle.
     *
     * @param finishingInput true if we are finishing for good,
     * `false` if just switching to another app.
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        clipboardHandler.stopMonitoring()
        moveToIdleState()
    }

    // MARK: OnKeyboardActionListener

    /**
     * Interface method called by KeyboardView.
     * Delegates to the property 'hasTextBeforeCursor' which subclasses may override.
     */
    override fun hasTextBeforeCursor(): Boolean = hasTextBeforeCursor

    override fun commitPeriodAfterSpace() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            currentInputConnection?.apply {
                deleteSurroundingText(1, 0)
                commitText(". ", 1)
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
            keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType, getKeyboardWidth())
            val editorInfo = currentInputEditorInfo
            val inputConnection = currentInputConnection
            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL && keyboard?.mShiftState != SHIFT_ON_PERMANENT) {
                if (inputConnection != null && inputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
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
        keyHandler.handleKey(code, language)
    }

    // MARK: Helper Methods

    override fun openEmojiKeyboard() {
        uiManager.showEmojiPalette(language)
    }

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
    override fun isSearchBar(): Boolean {
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
        dataHandler.loadLanguageData(language)
    }

    override fun applyNavBarColor() {
        themeManager.applyNavBarColor(
            service = this,
            window = window?.window,
            isFloatingMode = isFloatingMode,
            uiManager = if (this::uiManager.isInitialized) uiManager else null,
        )
    }

    /**
     * Saves the type of conjugation layout being used (e.g., "2x2", "3x2") to shared preferences.
     *
     * @param language The current keyboard language.
     * @param isSubsequentArea true if this is for a secondary view.
     */
    override fun saveConjugateModeType(
        language: String,
        isSubsequentArea: Boolean,
    ) {
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        val mode =
            if (!isSubsequentArea) {
                when (language) {
                    "English", "Russian", "Swedish" -> "2x2"
                    "German", "French", "Italian", "Portuguese", "Spanish" -> "2x2"
                    else -> "none"
                }
            } else {
                "none"
            }
        sharedPref.edit { putString("conjugate_mode_type", mode) }
    }

    // MARK: UI Update Delegation

    /**
     * The main dispatcher for updating the entire keyboard UI. It calls the appropriate setup function
     * based on the current [ScribeState].
     */
    override fun updateUI() = refreshUI()

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
            invalidCommandSource = invalidCommandSource,
        )
    }

    /**
     * Transitions the keyboard to the `IDLE` state and updates the UI.
     */
    override fun moveToIdleState() {
        clearSuggestionData()
        stateManager.moveToIdle()
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

    // MARK: KeyboardUIListener

    override fun onScribeKeyOptionsClicked() {
        if (stateManager.isIdle) {
            clearSuggestionData()
            stateManager.moveToState(ScribeState.SELECT_COMMAND)
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
        stateManager.moveToState(ScribeState.TRANSLATE)
        saveConjugateModeType("none")
        refreshUI()
    }

    override fun onConjugateClicked() {
        if (stateManager.currentState != ScribeState.SELECT_VERB_CONJUNCTION) {
            stateManager.moveToState(ScribeState.CONJUGATE)
        }
        refreshUI()
    }

    override fun onPluralClicked() {
        stateManager.moveToState(ScribeState.PLURAL)
        saveConjugateModeType("none")
        if (isPluralCapitalized) keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
        refreshUI()
    }

    override fun onCloseClicked() {
        moveToIdleState()
    }

    override fun onFloatClicked() {
        toggleFloatingMode()
    }

    override fun isFloatingModeActive(): Boolean = isFloatingMode

    override fun onEmojiSelected(emoji: String) {
        if (emoji.isNotEmpty()) {
            recordRecentEmoji(this, emoji)
            currentInputConnection?.let { ic ->
                insertEmoji(emoji, ic, emojiKeywords, emojiMaxKeywordLength)
            }
        }
    }

    override fun onSuggestionClicked(suggestion: String) {
        currentInputConnection?.commitText("$suggestion ", 1)
        moveToIdleState()
    }

    override fun getCurrentEnterKeyType(): Int = enterKeyType

    override fun isNumericKeyboardActive(): Boolean = isNumericKeyboardActive

    override fun getCurrentKeyboardLayoutXML(): Int =
        when (keyboardMode) {
            keyboardSymbols -> getPrimarySymbolKeyboardLayoutXML()
            keyboardSymbolShift -> R.xml.keys_symbols_shift
            else -> getKeyboardLayoutXML()
        }

    private fun getPrimarySymbolKeyboardLayoutXML(): Int =
        if (isNumericKeyboardActive) {
            R.xml.keys_numeric
        } else {
            R.xml.keys_symbols
        }

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

    // MARK: Input Logic

    /**
     * Handles the logic for the Enter key press. This can either perform an editor action,
     * commit a newline, or execute a Scribe command depending on the current state.
     */
    override fun handleKeycodeEnter() {
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
            stateManager.setInvalidState(currentState)
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

        if (conjugateOutput == null) {
            stateManager.setInvalidState(ScribeState.CONJUGATE)
        } else {
            saveConjugateModeType(language)
            stateManager.moveToState(ScribeState.SELECT_VERB_CONJUNCTION)
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
        val wordBeforeEnter = getLastWordBeforeCursor()
        val imeOptionsActionId = getImeOptionsActionId()
        if (imeOptionsActionId != IME_ACTION_NONE) {
            inputConnection.performEditorAction(imeOptionsActionId)
        } else {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        moveToIdleState()
        if (!wordBeforeEnter.isNullOrEmpty()) {
            suggestionHandler.processLinguisticSuggestions(wordBeforeEnter)
        } else {
            suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        }
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
    override fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
        commandBarState: Boolean,
    ) {
        val currentShiftState = keyboardView?.mKeyboard?.mShiftState ?: SHIFT_OFF
        if (commandBarState) {
            val codeChar =
                if (Character.isLetter(code.toChar()) && currentShiftState > SHIFT_OFF) {
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
            if (Character.isLetter(codeChar) && currentShiftState > SHIFT_OFF) {
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

        if (currentShiftState == SHIFT_ON_ONE_CHAR && keyboardMode == keyboardLetters) {
            keyboardView?.mKeyboard?.mShiftState = SHIFT_OFF
            keyboardView?.invalidateAllKeys()
        }
    }

    // MARK: Deletion Logic

    /**
     * Handles the logic for the Delete key. It deletes characters from either
     * the main input field or the command bar, depending on the context.
     * Delegated to BackspaceHandler.
     *
     * @param isCommandBar true` if the deletion should happen in the command bar.
     * @param isLongPress true` if this is a long press/repeat action, false for single tap.
     */
    override fun handleDelete(isLongPress: Boolean) {
        val inputConnection = currentInputConnection ?: return
        val effectiveIsCommandBar =
            currentState != ScribeState.IDLE &&
                currentState != ScribeState.SELECT_COMMAND

        if (!effectiveIsCommandBar) {
            val selectedText = inputConnection.getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
                // Use BreakIterator to delete full emoji characters.
                val prevText = inputConnection.getTextBeforeCursor(8, 0)
                if (!prevText.isNullOrEmpty()) {
                    val breakIterator =
                        android.icu.text.BreakIterator
                            .getCharacterInstance()
                    breakIterator.setText(prevText.toString())
                    val end = breakIterator.last()
                    val start = breakIterator.previous()
                    val count =
                        if (start == android.icu.text.BreakIterator.DONE) {
                            1
                        } else {
                            (end - start).coerceAtLeast(1)
                        }
                    inputConnection.deleteSurroundingText(count, 0)
                    return
                }
            }
        }

        backspaceHandler.handleBackspace(effectiveIsCommandBar, isLongPress)
    }

    /**
     * Returns whether the delete key is currently repeating (long press).
     * Delegated to BackspaceHandler.
     */
    override fun isDeleteRepeating() = backspaceHandler.isDeleteRepeating

    /**
     * Sets the flag to indicate that the delete key is currently repeating (long press).
     * Delegated to BackspaceHandler.
     */
    override fun setDeleteRepeating(repeating: Boolean) {
        backspaceHandler.isDeleteRepeating = repeating
    }

    // MARK: State & Logic Helpers

    /**
     * Safely fetches autocomplete suggestions for the given prefix.
     * Returns an empty list if a database or state error occurs.
     */
    override fun getAutocompletions(
        word: String,
        previousWord: String?,
        limit: Int,
    ): List<String> {
        if (this::nativeSuggestionEngine.isInitialized) {
            val nativeCompletions = nativeSuggestionEngine.getAutocompletions(language, word, previousWord, limit)
            if (nativeCompletions.isNotEmpty()) {
                return nativeCompletions.map { it.substringBefore("-") }
            }
        }
        return dataHandler.getAutocompletions(word, limit)
    }

    /**
     * Gets the current text in the command bar without the cursor.
     *
     * @return The text content without the trailing cursor character.
     */
    override fun getCommandBarTextWithoutCursor() = uiManager.getCommandBarTextWithoutCursor()

    /**
     * Sets the command bar text and ensures it ends with the custom cursor.
     *
     * @param text The text to set (without cursor).
     * @param cursorAtStart The flag to check if the text in the EditText is empty to determine the position of the cursor
     */
    override fun setCommandBarTextWithCursor(
        text: String,
        cursorAtStart: Boolean,
    ) = uiManager.setCommandBarTextWithCursor(text, cursorAtStart)

    /**
     * Extracts the last word from the text immediately preceding the cursor.
     *
     * @return The last word as a [String], or null if no word is found.
     */
    override fun getLastWordBeforeCursor(): String? = getText()?.trim()?.split("\\s+".toRegex())?.lastOrNull()

    /**
     * Extracts the word immediately before the one currently being composed, i.e. the last
     * completed word preceding the in-progress word at the cursor. Used to give the autocomplete
     * engine sentence context so it can bias completions instead of scoring the prefix in isolation.
     *
     * @return The previous completed word as a [String], or null if there isn't one.
     */
    override fun getPreviousWordBeforeCursor(): String? {
        val words = getText()?.trim()?.split("\\s+".toRegex()) ?: return null
        return words.getOrNull(words.size - 2)
    }

    /**
     * Retrieves the text immediately preceding the cursor.
     *
     * @return The text before the cursor, up to a defined maximum length.
     */
    fun getText(): String? = currentInputConnection?.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()

    // MARK: Misc Private Helpers

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
    private fun getPluralRepresentation(word: String?): String? = dataHandler.getPluralRepresentation(language, word)

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
    ): String = dataHandler.getTranslation(language, commandBarInput)

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
     * @param keyboardMode The current keyboard mode.
     * @param keyboardView The instance of the keyboard view.
     */
    override fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
    ) {
        if (keyboardMode == keyboardLetters) {
            val shiftState = keyboardView?.mKeyboard?.mShiftState ?: SHIFT_OFF
            when {
                shiftState == SHIFT_ON_PERMANENT -> keyboardView?.setShifted(SHIFT_OFF)
                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> keyboardView?.setShifted(SHIFT_ON_PERMANENT)
                shiftState == SHIFT_ON_ONE_CHAR -> keyboardView?.setShifted(SHIFT_OFF)
                shiftState == SHIFT_OFF -> keyboardView?.setShifted(SHIFT_ON_ONE_CHAR)
            }
            lastShiftPressTS = System.currentTimeMillis()
        } else {
            val keyboardXml =
                if (keyboardMode == keyboardSymbols) {
                    this.keyboardMode = keyboardSymbolShift
                    R.xml.keys_symbols_shift
                } else {
                    this.keyboardMode = keyboardSymbols
                    getPrimarySymbolKeyboardLayoutXML()
                }
            keyboard = KeyboardBase(this, keyboardXml, enterKeyType, getKeyboardWidth())
            keyboardView!!.setKeyboard(keyboard!!)
            if (keyboardXml == R.xml.keys_symbols) {
                handleModeChange(keyboardMode, keyboardView, this)
            }
        }
    }

    /**
     * Handles switching between the letter and symbol keyboards.
     *
     * @param keyboardMode The current keyboard mode (letters or symbols).
     * @param keyboardView The instance of the keyboard view.
     * @param context The application context.
     */
    override fun handleModeChange(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
        context: Context,
    ) {
        val keyboardXml =
            if (keyboardMode == keyboardLetters) {
                this.keyboardMode = keyboardSymbols
                getPrimarySymbolKeyboardLayoutXML()
            } else {
                this.keyboardMode = keyboardLetters
                getKeyboardLayoutXML()
            }
        keyboard = KeyboardBase(context, keyboardXml, enterKeyType, getKeyboardWidth())
        if (this.keyboardMode == keyboardLetters) {
            val wasShifted = keyboard?.mShiftState == SHIFT_ON_ONE_CHAR || keyboard?.mShiftState == SHIFT_ON_PERMANENT
            if (wasShifted) {
                keyboard?.setShifted(keyboard?.mShiftState ?: SHIFT_OFF)
            }
        }
        keyboardView?.setKeyboard(keyboard!!)
        keyboardView?.invalidateAllKeys()
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
    override fun findEmojisForLastWord(
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
    override fun findGenderForLastWord(
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
    override fun findWhetherWordIsPlural(
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
    override fun getNextWordSuggestions(
        wordSuggestions: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? {
        if (lastWord == null) return null
        if (this::nativeSuggestionEngine.isInitialized) {
            val nativeSuggestions = nativeSuggestionEngine.getNextWordSuggestions(language, lastWord)
            if (nativeSuggestions.isNotEmpty()) {
                return nativeSuggestions.map { it.substringBefore("-") }
            }
        }
        return wordSuggestions[lastWord.lowercase()]?.map { it.substringBefore("-") }
    }

    /**
     * Finds the required grammatical case(s) for a preposition.
     *
     * @param caseAnnotation The map of prepositions to their required cases.
     * @param lastWord The word to look up (which should be a preposition).
     *
     * @return A mutable list of case suggestions (e.g., "accusative case"), or null if not found.
     */
    override fun getCaseAnnotationForPreposition(
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
    override fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>?,
        isPlural: Boolean,
        caseAnnotationSuggestion: MutableList<String>?,
        wordSuggestions: List<String>?,
    ) {
        this.nounTypeSuggestion = nounTypeSuggestion
        this.checkIfPluralWord = isPlural
        this.caseAnnotationSuggestion = caseAnnotationSuggestion
        this.wordSuggestions = wordSuggestions

        if (currentState != ScribeState.IDLE) {
            if (currentState != ScribeState.SELECT_COMMAND) {
                uiManager.disableAutoSuggest(language)
            }
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

    // MARK: Linguistic Logic

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
            themeManager.applySingleSuggestionStyle(
                context = applicationContext,
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

        themeManager.applySingleSuggestionStyle(
            context = applicationContext,
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
            context = applicationContext,
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
        if (wordSuggestions.isNullOrEmpty()) {
            if (hasLinguisticSuggestions) {
                val baseSuggestions =
                    be.scri.helpers.ui.HintUtils
                        .getBaseAutoSuggestions(language)
                val default1 = baseSuggestions.getOrNull(0) ?: ""
                val default2 = baseSuggestions.getOrNull(1) ?: ""
                setSuggestionButton(uiManager.binding.conjugateBtn, default1)
                if (autoSuggestEmojis.isNullOrEmpty()) {
                    uiManager.pluralBtn?.let { setSuggestionButton(it, default2) }
                } else {
                    uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
                }
            }
            return
        }

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
            !hasLinguisticSuggestions && emojiCount != 0 -> {
                setSuggestionButton(uiManager.binding.translateBtn, suggestion2)
                uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
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
        button.text = text
        button.isAllCaps = false
        button.visibility = View.VISIBLE
        button.textSize = SUGGESTION_SIZE
        button.setOnClickListener(null)
        button.background = null
        button.foreground = null
        button.setTextColor(themeManager.getSuggestionTextColor(applicationContext))
        button.setOnClickListener {
            currentInputConnection?.commitText("$text ", 1)
            moveToIdleState()
        }
    }

    // MARK: Autocomplete

    /**
     * Pins the word currently being typed into the first (leftmost) suggestion
     * slot, quoted like most mobile keyboards do to mark it as "what you typed"
     * rather than a dictionary suggestion. Called immediately on every keystroke
     * — unlike the completions, it needs no lookup, so it should never lag.
     */
    override fun updateTypedWordSuggestion(word: String?) {
        if (currentState != ScribeState.IDLE || word.isNullOrEmpty()) {
            uiManager.disableAutoSuggest(language)
            if (!autoSuggestEmojis.isNullOrEmpty() && emojiAutoSuggestionEnabled) {
                updateEmojiSuggestion(true, autoSuggestEmojis)
                updateButtonVisibility(true)
            }
            return
        }

        setTypedWordButton(uiManager.binding.translateBtn, word)
        setAutocompleteButton(uiManager.binding.conjugateBtn, "")
        if (autoSuggestEmojis.isNullOrEmpty()) {
            uiManager.pluralBtn?.let { setAutocompleteButton(it, "") }
        } else {
            uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
        }

        uiManager.binding.separator1.visibility = View.VISIBLE
        uiManager.binding.separator2.visibility = View.VISIBLE
    }

    /**
     * Fills the remaining suggestion slots with dictionary/engine completions.
     * Clears them (leaving the typed word alone) if not idle.
     */
    override fun updateAutocompleteCompletions(completions: List<String>) {
        if (currentState != ScribeState.IDLE) return

        val completion1 = completions.getOrNull(0) ?: ""
        val completion2 = completions.getOrNull(1) ?: ""

        setAutocompleteButton(uiManager.binding.conjugateBtn, completion1)
        if (autoSuggestEmojis.isNullOrEmpty()) {
            uiManager.pluralBtn?.let { setAutocompleteButton(it, completion2) }
        } else {
            uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
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
            currentInputConnection?.commitText(" ", 1)
            suggestionHandler.processLinguisticSuggestions(word)
            suggestionHandler.processWordSuggestions(word)
            moveToIdleState()
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
    override fun clearAutocomplete() {
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
    override fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    override fun returnSubsequentData(): List<List<String>> = subsequentData

    /**
     * Handles a key press on one of the special conjugation keys.
     * It either commits the text directly or prepares for a subsequent selection view.
     *
     * @param code The key code of the pressed key.
     * @param isSubsequentRequired true if a sub-view is needed for more options.
     *
     * @return The label of the key that was pressed.
     */
    override fun handleConjugateKeys(
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
    override fun setupConjugateSubView(
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
        // Re-initialize keyboard via UI manager helper which calls 'initializeKeyboard(xml)'.
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
        // Binding access via uiManager.
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
                    defaultConjugateLayoutXML
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
    override fun updateButtonVisibility(enabled: Boolean) = uiManager.updateButtonVisibility(currentState, enabled, autoSuggestEmojis)

    override fun updateEmojiSuggestion(
        enabled: Boolean,
        emojis: MutableList<String>?,
    ) = uiManager.updateEmojiSuggestion(currentState, enabled, emojis)

    override fun disableAutoSuggest() = uiManager.disableAutoSuggest(language)

    // MARK: Floating Keyboard Integration

    override fun getKeyboardWidth(): Int =
        if (isFloatingMode) {
            val density = resources.displayMetrics.density
            val screenWidth = resources.displayMetrics.widthPixels
            val floatWidth = (320f * density).toInt()
            Math.min(floatWidth, (screenWidth * 0.85f).toInt())
        } else {
            resources.displayMetrics.widthPixels
        }

    override fun recreateKeyboard() {
        if (!this::uiManager.isInitialized) return
        val xmlId = getCurrentKeyboardLayoutXML()
        val currentShiftState = keyboard?.mShiftState ?: SHIFT_OFF
        keyboard = KeyboardBase(this, xmlId, enterKeyType, getKeyboardWidth())
        keyboard?.setShifted(currentShiftState)
        keyboardView?.setKeyboard(keyboard!!)

        if (xmlId == R.xml.keys_symbols) {
            uiManager.setupCurrencySymbol(language)
        }
        keyboardView?.invalidateAllKeys()
    }

    val isFloatingMode: Boolean
        get() = floatingKeyboardHandler.isFloatingMode

    fun initFloatingMode() {
        floatingKeyboardHandler.initFloatingMode()
    }

    override fun toggleFloatingMode() {
        floatingKeyboardHandler.toggleFloatingMode()
    }

    fun disableFloatingMode() {
        floatingKeyboardHandler.disableFloatingMode()
    }

    fun applyFloatingModeState() {
        floatingKeyboardHandler.applyFloatingModeState()
    }

    fun setupFloatingDragListener() {
        floatingKeyboardHandler.setupFloatingDragListener()
    }

    fun disableParentClipping(view: View) {
        floatingKeyboardHandler.disableParentClipping(view)
    }

    override fun onClipboardSuggestionClicked() {
        clipboardHandler.onClipboardSuggestionClicked()
    }

    override fun hideClipboardSuggestionChip() {
        clipboardHandler.hideClipboardSuggestionChip()
    }

    override fun openClipboardPanel() {
        clipboardHandler.openClipboardPanel()
    }

    fun closeClipboardPanel() {
        clipboardHandler.closeClipboardPanel()
    }
}

private fun Float.coerceInSafe(
    bound1: Float,
    bound2: Float,
): Float {
    val minVal = if (bound1 < bound2) bound1 else bound2
    val maxVal = if (bound1 > bound2) bound1 else bound2
    return this.coerceIn(minVal, maxVal)
}
