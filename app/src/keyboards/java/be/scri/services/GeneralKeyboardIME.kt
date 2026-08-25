// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import DataContract
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.InputMethodService.BACK_DISPOSITION_ADJUST_NOTHING
import android.inputmethodservice.InputMethodService.BACK_DISPOSITION_DEFAULT
import android.os.Build
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.extensions.performSoundFeedback
import be.scri.helpers.AnnotationTextUtils.handleColorAndTextForNounType
import be.scri.helpers.AnnotationTextUtils.handleTextForCaseAnnotation
import be.scri.helpers.AutocompletionHandler
import be.scri.helpers.BackspaceHandler
import be.scri.helpers.DatabaseManagers
import be.scri.helpers.EmojiUtils.insertEmoji
import be.scri.helpers.KeyboardBase
import be.scri.helpers.KeyboardDataHandler
import be.scri.helpers.KeyboardLanguageMappingConstants
import be.scri.helpers.KeyboardStateManager
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.NativeSuggestionEngine
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.helpers.PreferencesHelper.getIsEmojiSuggestionsEnabled
import be.scri.helpers.PreferencesHelper.getIsSoundEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import be.scri.helpers.SuggestionHandler
import be.scri.helpers.clipboard.ClipboardMonitor
import be.scri.helpers.clipboard.ClipboardRepository
import be.scri.helpers.data.AutocompletionDataManager
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import be.scri.helpers.recordRecentEmoji
import be.scri.helpers.ui.HintUtils
import be.scri.models.ScribeLanguage
import be.scri.models.ScribeState
import be.scri.ui.compose.IMSLifecycleOwner
import be.scri.ui.compose.KeyboardActionListener
import be.scri.ui.compose.KeyboardViewModel
import be.scri.ui.compose.ScribeKeyboardApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

@Suppress("TooManyFunctions", "LargeClass")
abstract class GeneralKeyboardIME(
    val scribeLanguage: ScribeLanguage,
) : InputMethodService(),
    KeyboardActionListener,
    KeyboardBase.KeyboardContextProvider {
    constructor(languageName: String) : this(ScribeLanguage.fromDisplayName(languageName))

    override val language: String
        get() = scribeLanguage.displayName

    abstract fun getKeyboardLayoutXML(): Int

    abstract override val keyboardLetters: Int
    abstract val keyboardSymbols: Int
    abstract val keyboardSymbolShift: Int

    open var keyboard: KeyboardBase? = null
        set(value) {
            field = value
            keyboardViewModel.updateKeyboard(value)
        }

    internal val keyboardViewModel = KeyboardViewModel()
    private var imsLifecycleOwner: IMSLifecycleOwner? = null
    private var composeInputView: View? = null

    internal fun setShifted(shiftState: Int) {
        keyboard?.setShifted(shiftState)
        keyboardViewModel.setShiftState(keyboard?.mShiftState ?: SHIFT_OFF)
    }

    fun getKeyLabel(code: Int): String? =
        keyboard
            ?.mKeys
            ?.find { it?.code == code }
            ?.label
            ?.toString()

    abstract var lastShiftPressTS: Long
    abstract override var keyboardMode: Int
    abstract var inputTypeClass: Int
    abstract var enterKeyType: Int
    abstract var switchToLetters: Boolean

    open val defaultConjugateModeType: String = "3x2"
    open val defaultConjugateLayoutXML: Int = R.xml.conjugate_view_3x2
    open val isPluralCapitalized: Boolean = false

    open var hasTextBeforeCursor: Boolean = false
        get() {
            val ic = currentInputConnection ?: return false
            val text = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.trim() ?: ""
            return text.isNotEmpty() && text.lastOrNull() != '.'
        }
        set(value) {
            field = value
        }

    private val backspaceHandler = BackspaceHandler(this)

    internal var hasNewClip: Boolean = false
    internal var latestClipText: String? = null
    private lateinit var clipboardMonitor: ClipboardMonitor

    internal var isSingularAndPlural: Boolean = false
    private var subsequentAreaRequired: Boolean = false
    private var subsequentData: MutableList<List<String>> = mutableListOf()

    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    internal val dataHandler = KeyboardDataHandler()

    internal val dbManagers: DatabaseManagers
        get() = dataHandler.dbManagers

    internal val autocompletionManager: AutocompletionDataManager
        get() = dataHandler.autocompletionManager

    private lateinit var nativeSuggestionEngine: NativeSuggestionEngine
    internal lateinit var suggestionHandler: SuggestionHandler
    internal lateinit var autocompletionHandler: AutocompletionHandler
    internal var dataContract: DataContract?
        get() = dataHandler.dataContract
        set(value) {
            dataHandler.dataContract = value
        }

    var emojiKeywords: HashMap<String, MutableList<String>>?
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

    internal var nounKeywords: HashMap<String, List<String>>
        get() = dataHandler.nounKeywords
        set(value) {
            dataHandler.nounKeywords = value
        }

    internal var suggestionWords: HashMap<String, List<String>>
        get() = dataHandler.suggestionWords
        set(value) {
            dataHandler.suggestionWords = value
        }

    var pluralWords: Set<String>?
        get() = dataHandler.pluralWords
        set(value) {
            dataHandler.pluralWords = value
        }

    internal var caseAnnotation: HashMap<String, MutableList<String>>
        get() = dataHandler.caseAnnotation
        set(value) {
            dataHandler.caseAnnotation = value
        }

    var emojiAutoSuggestionEnabled: Boolean = false
    var lastWord: String? = null
    var autoSuggestEmojis: MutableList<String>? = null
    var caseAnnotationSuggestion: MutableList<String>? = null
    var nounTypeSuggestion: List<String>? = null
    var wordSuggestions: List<String>? = null
    var checkIfPluralWord: Boolean = false
    private var currentEnterKeyType: Int? = null
    private var isNumericKeyboardActive: Boolean = false

    private var highlightedAutocompleteSuggestion: String? = null

    var emojiColonModeOn: Boolean = false
        set(value) {
            field = value
            keyboardViewModel.setEmojiColonMode(value)
        }

    internal val stateManager = KeyboardStateManager()

    internal var currentState: ScribeState
        get() = stateManager.currentState
        set(value) {
            stateManager.currentState = value
            keyboardViewModel.updateState(value)
        }

    internal var invalidCommandSource: ScribeState
        get() = stateManager.invalidCommandSource
        set(value) {
            stateManager.invalidCommandSource = value
            keyboardViewModel.setInvalidCommandSource(value)
        }

    var commandBarHint: String
        get() = keyboardViewModel.commandBarHint.value ?: ""
        set(value) {
            keyboardViewModel.setCommandBarHint(value)
        }

    var commandBarHintColor: Int
        get() = keyboardViewModel.commandBarHintColor.value ?: Color.TRANSPARENT
        set(value) {
            keyboardViewModel.setCommandBarHintColor(value)
        }

    private var currentVerbForConjugation: String? = null
    private var selectedConjugationSubCategory: String? = null

    protected open fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    internal companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
        const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500
        const val TEXT_LENGTH = 20
        const val WORD_LOOKBACK_LENGTH = 50
        const val MAX_COLON_EMOJI_SUGGESTIONS = 9
        const val NOUN_TYPE_SIZE = 20f
        const val SUGGESTION_SIZE = 15f
        const val DARK_THEME = "#aeb3be"
        const val LIGHT_THEME = "#4b4b4b"
        internal const val MAX_TEXT_LENGTH = 1000
        const val COMMIT_TEXT_CURSOR_POSITION = 1
        internal const val CUSTOM_CURSOR = "│"
        internal const val FLOATING_TOUCH_MARGIN_DP = 16
        internal const val COMMAND_LABEL_LANGUAGE = "EN"
        internal const val COMMAND_LABEL_LANGUAGE_NAME = "English"

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

    override fun onCreate() {
        super.onCreate()
        dataHandler.initialize(this)
        nativeSuggestionEngine = NativeSuggestionEngine(this)
        suggestionHandler = SuggestionHandler(this)
        autocompletionHandler = AutocompletionHandler(this)
        clipboardMonitor =
            ClipboardMonitor(this) { text ->
                latestClipText = text
                hasNewClip = true
                keyboardViewModel.showClipboardSuggestion(text)
            }
    }

    override fun onDestroy() {
        imeScope.cancel()
        imsLifecycleOwner?.onDestroy()
        imsLifecycleOwner = null
        if (this::nativeSuggestionEngine.isInitialized) {
            nativeSuggestionEngine.close()
        }
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        keyboardViewModel.updateLanguage(language)

        keyboardViewModel.updateCommandLabels(
            KeyboardLanguageMappingConstants.translatePlaceholder[COMMAND_LABEL_LANGUAGE] ?: "Translate",
            KeyboardLanguageMappingConstants.conjugatePlaceholder[COMMAND_LABEL_LANGUAGE] ?: "Conjugate",
            KeyboardLanguageMappingConstants.pluralPlaceholder[COMMAND_LABEL_LANGUAGE] ?: "Plural",
        )

        saveConjugateModeType("none")
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType, getKeyboardWidth())

        currentState = ScribeState.IDLE
        initFloatingMode()

        val lifecycleOwner =
            imsLifecycleOwner ?: IMSLifecycleOwner().also {
                it.onCreate()
                imsLifecycleOwner = it
            }

        window?.window?.decorView?.let { decor ->
            decor.setViewTreeLifecycleOwner(lifecycleOwner)
            decor.setViewTreeViewModelStoreOwner(lifecycleOwner)
            decor.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        return ComposeView(this)
            .apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                    val navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    keyboardViewModel.setBottomInset(if (isFloatingMode) 0 else navBarBottom)
                    insets
                }
                androidx.core.view.ViewCompat
                    .requestApplyInsets(this)
                setContent {
                    ScribeKeyboardApp(
                        viewModel = keyboardViewModel,
                        actionListener = this@GeneralKeyboardIME,
                    )
                }
            }.also { composeInputView = it }
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val inputView = composeInputView ?: return
        if (inputView.visibility == View.VISIBLE && inputView.height > 0) {
            val location = IntArray(2)
            inputView.getLocationInWindow(location)

            if (isFloatingMode) {
                outInsets.visibleTopInsets = inputView.height
                outInsets.contentTopInsets = inputView.height
                outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION

                val density = resources.displayMetrics.density
                val card = keyboardViewModel.floatingCardBounds.value
                if (card.width > 0f && card.height > 0f) {
                    val offsetX = keyboardViewModel.floatingOffsetX.value
                    val offsetY = keyboardViewModel.floatingOffsetY.value
                    val scaleX = keyboardViewModel.floatingScaleX.value
                    val scaleY = keyboardViewModel.floatingScaleY.value

                    val centerX = card.left + card.width / 2f + offsetX
                    val centerY = card.top + card.height / 2f + offsetY
                    val visualW = card.width * scaleX
                    val visualH = card.height * scaleY
                    val left = (centerX - visualW / 2f).toInt()
                    val top = (centerY - visualH / 2f).toInt()
                    val right = (centerX + visualW / 2f).toInt()
                    val bottom = (centerY + visualH / 2f).toInt()

                    val rect = Rect(left, top, right, bottom)
                    val margin = (FLOATING_TOUCH_MARGIN_DP * density).toInt()
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

    override fun onWindowShown() {
        super.onWindowShown()
        imsLifecycleOwner?.let { lifecycleOwner ->
            window?.window?.decorView?.let { decor ->
                decor.setViewTreeLifecycleOwner(lifecycleOwner)
                decor.setViewTreeViewModelStoreOwner(lifecycleOwner)
                decor.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            }
        }
        applyFloatingModeState()
        applyNavBarColor()
    }

    override fun onStartInput(
        attribute: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)
        inputTypeClass = attribute!!.inputType and TYPE_MASK_CLASS
        enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)
        currentEnterKeyType = enterKeyType

        hasTextBeforeCursor = currentInputConnection?.getTextBeforeCursor(1, 0)?.isNotEmpty() == true

        isNumericKeyboardActive = shouldUseNumericKeyboard(attribute.inputType)
        keyboardMode = if (isNumericKeyboardActive) keyboardSymbols else keyboardLetters
        val keyboardXml = getKeyboardLayoutXMLForInputType(attribute.inputType, getKeyboardLayoutXML())

        loadLanguageData()

        keyboard = KeyboardBase(this, keyboardXml, enterKeyType, getKeyboardWidth())
    }

    override fun onStartInputView(
        editorInfo: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInputView(editorInfo, restarting)
        imsLifecycleOwner?.onResume()
        if (this::clipboardMonitor.isInitialized) {
            clipboardMonitor.startMonitoring()
        }
        emojiAutoSuggestionEnabled = getIsEmojiSuggestionsEnabled(applicationContext, language)
        autoSuggestEmojis = null
        emojiColonModeOn = false
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()

        moveToIdleState()

        val languageAlias = getLanguageAlias(language)
        val dbFile = applicationContext.getDatabasePath("${languageAlias}LanguageData.sqlite")
        val hasData = dbFile.exists()
        keyboardViewModel.setHasData(hasData)
        keyboardViewModel.setHasLanguageData(hasData)

        applyNavBarColor()

        if (keyboardMode == keyboardLetters) {
            val textBefore = currentInputConnection?.getTextBeforeCursor(1, 0)?.toString().orEmpty()
            if (textBefore.isEmpty()) {
                setShifted(SHIFT_ON_ONE_CHAR)
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        imsLifecycleOwner?.onPause()
        if (this::clipboardMonitor.isInitialized) {
            clipboardMonitor.stopMonitoring()
        }
        moveToIdleState()
    }

    override fun hasTextBeforeCursor(): Boolean = hasTextBeforeCursor

    override fun commitPeriodAfterSpace() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            currentInputConnection?.apply {
                deleteSurroundingText(1, 0)
                commitText(". ", 1)
            }
        }
    }

    override fun onPress(primaryCode: Int) {
        keyboardViewModel.showClipboardSuggestion(null)
        if (primaryCode != 0) {
            val view = window?.window?.decorView
            if (view != null) {
                if (getIsVibrateEnabled(applicationContext, language)) {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                if (getIsSoundEnabled(applicationContext, language)) {
                    view.performSoundFeedback()
                }
            }
        }
    }

    override fun onActionUp() {
        if (switchToLetters) {
            keyboardMode = keyboardLetters
            keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType, getKeyboardWidth())
            val editorInfo = currentInputEditorInfo
            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL && keyboard?.mShiftState != SHIFT_ON_PERMANENT) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    setShifted(SHIFT_ON_ONE_CHAR)
                }
            }
            switchToLetters = false
        }
    }

    override fun moveCursorLeft() = moveCursor(false)

    override fun moveCursorRight() = moveCursor(true)

    override fun onText(text: String) {
        keyboardViewModel.showClipboardSuggestion(null)
        currentInputConnection?.commitText(text, 0)
    }

    override fun onKey(code: Int) {
        keyboardViewModel.showClipboardSuggestion(null)
        when (code) {
            KeyboardBase.KEYCODE_EMOJI -> {
                openEmojiKeyboard()
                return
            }
            KeyboardBase.KEYCODE_FLOAT_TOGGLE -> {
                toggleFloatingMode()
                return
            }
            KeyboardBase.KEYCODE_CLIPBOARD -> {
                openClipboardPanel()
                return
            }
        }
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            when (code) {
                KeyboardBase.KEYCODE_DELETE -> handleDelete()
                KeyboardBase.KEYCODE_SHIFT -> {
                    if (keyboardMode == keyboardLetters) {
                        val shiftState = keyboard?.mShiftState ?: SHIFT_OFF
                        when {
                            shiftState == SHIFT_ON_PERMANENT -> setShifted(SHIFT_OFF)
                            System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> setShifted(SHIFT_ON_PERMANENT)
                            shiftState == SHIFT_ON_ONE_CHAR -> setShifted(SHIFT_OFF)
                            shiftState == SHIFT_OFF -> setShifted(SHIFT_ON_ONE_CHAR)
                        }
                        lastShiftPressTS = System.currentTimeMillis()
                    } else {
                        handleModeChange(keyboardMode, this)
                    }
                }

                KeyboardBase.KEYCODE_ENTER -> handleKeycodeEnter()
                KeyboardBase.KEYCODE_MODE_CHANGE -> handleModeChange(keyboardMode, this)
                KeyboardBase.KEYCODE_CLIPBOARD -> openClipboardPanel()
                else -> {
                    if (KeyboardBase.SCRIBE_VIEW_KEYS.contains(code)) {
                        val keyLabel = getKeyLabel(code)
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

    fun openEmojiKeyboard() {
        keyboardViewModel.setEmojiKeyboardVisible(true)
    }

    protected fun isPeriodAndCommaEnabled(): Boolean {
        val isPreferenceEnabled = PreferencesHelper.getEnablePeriodAndCommaABC(this, language)
        val isInSearchBar = isSearchBar()
        return isPreferenceEnabled || isInSearchBar
    }

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

    private val imeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun loadLanguageData() {
        val currentLanguage = language
        imeScope.launch {
            withContext(Dispatchers.IO) {
                dataHandler.loadLanguageData(currentLanguage)
            }
        }
    }

    private fun isLightColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    private fun applyNavBarColor() {
        val window = window?.window ?: return
        window.decorView.post {
            val isDarkMode = getIsDarkModeOrNot(applicationContext)
            val colorRes = if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color
            val color = ContextCompat.getColor(this, colorRes)

            WindowCompat.setDecorFitsSystemWindows(window, false)

            if (Build.VERSION.SDK_INT < 35) {
                window.navigationBarColor = Color.TRANSPARENT
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            if (isFloatingMode) {
                window.decorView.setBackgroundColor(Color.TRANSPARENT)
            } else {
                window.decorView.setBackgroundColor(color)
            }
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightNavigationBars = isLightColor(color)

            if (isFloatingMode) {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            } else {
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = 0
            }

            composeInputView?.let { inputView ->
                if (isFloatingMode) {
                    inputView.setBackgroundColor(Color.TRANSPARENT)
                } else {
                    inputView.setBackgroundColor(color)
                }
                ViewCompat.requestApplyInsets(inputView)
            }
        }
    }

    internal fun saveConjugateModeType(
        language: String = this.language,
        isSubsequentArea: Boolean = false,
    ) {
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        val mode = if (!isSubsequentArea) defaultConjugateModeType else "none"
        sharedPref.edit { putString("conjugate_mode_type", mode) }
    }

    private fun enterInvalidState(source: ScribeState) {
        invalidCommandSource = source
        currentState = ScribeState.INVALID
    }

    internal fun moveToIdleState() {
        clearSuggestionData()
        currentState = ScribeState.IDLE
        saveConjugateModeType("none")
        currentVerbForConjugation = null
        selectedConjugationSubCategory = null
        keyboardViewModel.setPromptText("")
        keyboardViewModel.setCommandBarText("")
        keyboardViewModel.updateConjugateData(null, null, null)
    }

    private fun clearSuggestionData() {
        emojiColonModeOn = false
        highlightedAutocompleteSuggestion = null
        keyboardViewModel.setHighlightedSuggestion(null)
        keyboardViewModel.setAutocompleteActive(false)
        autoSuggestEmojis = null
        nounTypeSuggestion = null
        caseAnnotationSuggestion = null
        isSingularAndPlural = false
        keyboardViewModel.setSuggestions(null, null, null)
        keyboardViewModel.setGenderSuggestions(null, null)
        keyboardViewModel.updateEmojiSuggestions(emptyList())
    }

    override fun onScribeKeyOptionsClicked() {
        keyboardViewModel.showClipboardSuggestion(null)
        if (stateManager.isIdle) {
            clearSuggestionData()
            currentState = ScribeState.SELECT_COMMAND
            saveConjugateModeType("none")
            currentVerbForConjugation = null
        } else {
            moveToIdleState()
        }
    }

    override fun onScribeKeyToolbarClicked() {
        moveToIdleState()
    }

    override fun onTranslateClicked() {
        currentState = ScribeState.TRANSLATE
        saveConjugateModeType("none")
        keyboardViewModel.setPromptText(
            HintUtils.getPromptText(ScribeState.TRANSLATE, language, applicationContext, null),
        )
        keyboardViewModel.setCommandBarHint(
            HintUtils.getCommandBarHint(ScribeState.TRANSLATE, COMMAND_LABEL_LANGUAGE_NAME, null),
        )
    }

    override fun onConjugateClicked() {
        if (currentState != ScribeState.SELECT_VERB_CONJUNCTION) {
            currentState = ScribeState.CONJUGATE
            keyboardViewModel.setPromptText(
                HintUtils.getPromptText(
                    ScribeState.CONJUGATE,
                    COMMAND_LABEL_LANGUAGE_NAME,
                    applicationContext,
                    null,
                ),
            )
            keyboardViewModel.setCommandBarHint(
                HintUtils.getCommandBarHint(ScribeState.CONJUGATE, COMMAND_LABEL_LANGUAGE_NAME, null),
            )
        }
    }

    override fun onPluralClicked() {
        currentState = ScribeState.PLURAL
        saveConjugateModeType("none")
        keyboardViewModel.setPromptText(
            HintUtils.getPromptText(
                ScribeState.PLURAL,
                COMMAND_LABEL_LANGUAGE_NAME,
                applicationContext,
                null,
            ),
        )
        keyboardViewModel.setCommandBarHint(
            HintUtils.getCommandBarHint(ScribeState.PLURAL, COMMAND_LABEL_LANGUAGE_NAME, null),
        )
        if (language == "German" || isPluralCapitalized) setShifted(SHIFT_ON_ONE_CHAR)
    }

    override fun onCloseClicked() {
        moveToIdleState()
    }

    override fun isFloatingModeActive(): Boolean = isFloatingMode

    override fun onDownloadDataBannerClicked() {
        val intent =
            Intent(applicationContext, MainActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("navigate_to", "download_data")
                }
        startActivity(intent)
    }

    override fun onEmojiSelected(emoji: String) {
        if (emoji.isNotEmpty()) {
            recordRecentEmoji(this, emoji)
            insertEmoji(emoji, currentInputConnection, emojiKeywords, emojiMaxKeywordLength, emojiColonModeOn)
            if (emojiColonModeOn) {
                emojiColonModeOn = false
                clearAutocomplete()
            }
        }
    }

    override fun onAutocompleteSuggestionClicked(suggestion: String) {
        replaceCurrentWordWithSuggestion(suggestion)
        moveToIdleState()
    }

    override fun onSuggestionClicked(suggestion: String) {
        if (currentState == ScribeState.SELECT_VERB_CONJUNCTION) {
            commitText(suggestion)
        } else {
            currentInputConnection?.commitText("$suggestion ", 1)
            moveToIdleState()
        }
    }

    fun getCurrentEnterKeyType(): Int = enterKeyType

    fun isNumericKeyboardActive(): Boolean = isNumericKeyboardActive

    fun getCurrentKeyboardLayoutXML(): Int =
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

    fun processLinguisticSuggestions(word: String) {
        suggestionHandler.processLinguisticSuggestions(word)
    }

    fun commitText(text: String) {
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
                    keyboardViewModel.updateConjugateData(conjugateOutput, selectedConjugationSubCategory, currentVerbForConjugation)
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

        val rawInput =
            keyboardViewModel.commandBarText.value
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

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
            enterInvalidState(currentState)
        } else {
            applyCommandOutput(commandModeOutput, inputConnection)
        }
    }

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
            enterInvalidState(ScribeState.CONJUGATE)
        } else {
            saveConjugateModeType(language)
            currentState = ScribeState.SELECT_VERB_CONJUNCTION
        }
        keyboardViewModel.updateConjugateData(conjugateOutput, selectedConjugationSubCategory, currentVerbForConjugation)
    }

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

    private fun applyCommandOutput(
        commandModeOutput: String,
        inputConnection: InputConnection,
    ) {
        if (commandModeOutput.isNotEmpty()) {
            val output = if (!commandModeOutput.endsWith(" ")) "$commandModeOutput " else commandModeOutput
            inputConnection.commitText(output, COMMIT_TEXT_CURSOR_POSITION)
            suggestionHandler.processLinguisticSuggestions(output.trim())
        }
        keyboardViewModel.setCommandBarText("")
        moveToIdleState()
    }

    fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
        commandBarState: Boolean = false,
    ) {
        val currentShiftState = keyboard?.mShiftState ?: SHIFT_OFF
        if (commandBarState) {
            val codeChar =
                if (Character.isLetter(code.toChar()) && currentShiftState > SHIFT_OFF) {
                    Character.toUpperCase(code.toChar())
                } else {
                    code.toChar()
                }
            val currentTextWithoutCursor = keyboardViewModel.commandBarText.value ?: ""

            if (currentTextWithoutCursor == commandBarHint) {
                keyboardViewModel.setCommandBarHintColor(commandBarHintColor)
                keyboardViewModel.setCommandBarText(codeChar.toString())
            } else {
                val newText = currentTextWithoutCursor + codeChar
                keyboardViewModel.setCommandBarText(newText)
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
            setShifted(SHIFT_OFF)
        }
    }

    fun handleDelete(isLongPress: Boolean = false) {
        val inputConnection = currentInputConnection ?: return
        val effectiveIsCommandBar =
            currentState != ScribeState.IDLE &&
                currentState != ScribeState.SELECT_COMMAND

        if (!effectiveIsCommandBar) {
            val selectedText = inputConnection.getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
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

    fun isDeleteRepeating() = backspaceHandler.isDeleteRepeating

    override fun setDeleteRepeating(repeating: Boolean) {
        backspaceHandler.isDeleteRepeating = repeating
    }

    data class AutocompleteResult(
        val completions: List<String>,
        val highlightedSuggestion: String?,
    )

    fun getAutocompletions(
        prefix: String,
        limit: Int = 3,
    ): AutocompleteResult {
        val completions =
            dataHandler.getAutocompletions(prefix, limit).ifEmpty {
                if (this::nativeSuggestionEngine.isInitialized) {
                    nativeSuggestionEngine.getAutocompletions(language, prefix, limit)
                } else {
                    emptyList()
                }
            }

        val isPrefixItselfAValidWord =
            this::nativeSuggestionEngine.isInitialized && nativeSuggestionEngine.isValidWord(language, prefix)

        if (isPrefixItselfAValidWord && completions.none { it.equals(prefix, ignoreCase = true) }) {
            return AutocompleteResult((listOf(prefix) + completions).take(limit), highlightedSuggestion = prefix)
        }

        if (!isPrefixItselfAValidWord && completions.size == 1) {
            val onlyCompletion = completions.first()
            return AutocompleteResult(listOf(onlyCompletion, prefix).take(limit), highlightedSuggestion = onlyCompletion)
        }

        return AutocompleteResult(completions, highlightedSuggestion = null)
    }

    fun getCommandBarTextWithoutCursor() = keyboardViewModel.commandBarText.value ?: ""

    fun setCommandBarTextWithCursor(
        text: String,
        cursorAtStart: Boolean = false,
    ) = keyboardViewModel.setCommandBarText(text)

    fun getLastWordBeforeCursor(): String? = getText()?.trim()?.split("\\s+".toRegex())?.lastOrNull()

    fun getText(): String? = currentInputConnection?.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()

    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }

    private fun getPluralRepresentation(word: String?): String? = dataHandler.getPluralRepresentation(language, word)

    private fun getTranslation(
        language: String,
        commandBarInput: String,
    ): String = dataHandler.getTranslation(language, commandBarInput)

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

    private fun getValidatedConjugateIndex(): Int {
        val prefs = getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        var index = prefs.getInt("conjugate_index", 0)
        val maxIndex = conjugateOutput?.keys?.count()?.minus(1) ?: -1
        index = if (maxIndex >= 0) index.coerceIn(0, maxIndex) else 0
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }

    fun handleKeyboardLetters(keyboardMode: Int) {
        if (keyboardMode == keyboardLetters) {
            val shiftState = keyboard?.mShiftState ?: SHIFT_OFF
            when {
                shiftState == SHIFT_ON_PERMANENT -> setShifted(SHIFT_OFF)
                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> setShifted(SHIFT_ON_PERMANENT)
                shiftState == SHIFT_ON_ONE_CHAR -> setShifted(SHIFT_OFF)
                shiftState == SHIFT_OFF -> setShifted(SHIFT_ON_ONE_CHAR)
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
            if (keyboardXml == R.xml.keys_symbols) {
                handleModeChange(keyboardMode, this)
            }
        }
    }

    fun handleModeChange(
        keyboardMode: Int,
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
                setShifted(keyboard?.mShiftState ?: SHIFT_OFF)
            }
        }
    }

    private fun moveCursor(moveRight: Boolean) {
        val extractedText = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val newPos = extractedText.selectionStart + if (moveRight) 1 else -1
        currentInputConnection?.setSelection(newPos, newPos)
    }

    fun findEmojisForLastWord(
        emojiKeywords: HashMap<String, MutableList<String>>?,
        lastWord: String?,
    ) = lastWord?.let { emojiKeywords?.get(it.lowercase()) }

    fun findEmojisForPrefix(
        emojiKeywords: HashMap<String, MutableList<String>>?,
        prefix: String,
    ): MutableList<String> {
        if (emojiKeywords.isNullOrEmpty() || prefix.isEmpty()) return mutableListOf()
        val needle = prefix.lowercase()
        return emojiKeywords.keys
            .asSequence()
            .filter { it.startsWith(needle) }
            .sortedWith(compareBy({ it.length }, { it }))
            .flatMap { emojiKeywords.getValue(it).asSequence() }
            .distinct()
            .take(MAX_COLON_EMOJI_SUGGESTIONS)
            .toMutableList()
    }

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

    fun findWhetherWordIsPlural(
        pluralWords: Set<String>?,
        lastWord: String?,
    ): Boolean = pluralWords?.contains(lastWord?.lowercase()) == true

    fun getNextWordSuggestions(
        wordSuggestions: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? {
        if (lastWord == null) return null
        if (this::nativeSuggestionEngine.isInitialized) {
            val nativeSuggestions = nativeSuggestionEngine.getNextWordSuggestions(language, lastWord)
            if (nativeSuggestions.isNotEmpty()) {
                return nativeSuggestions
            }
        }
        return wordSuggestions[lastWord.lowercase()]
    }

    fun getCaseAnnotationForPreposition(
        caseAnnotation: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ) = lastWord?.let { caseAnnotation[it.lowercase()] }

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
            if (currentState != ScribeState.SELECT_COMMAND) {
                disableAutoSuggest(language)
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

        if (!handled) disableAutoSuggest(language)
        handleWordSuggestions(wordSuggestions, hasLinguisticSuggestions)
    }

    private fun handlePluralIfNeeded(isPlural: Boolean): Boolean {
        if (isPlural) {
            keyboardViewModel.setGenderSuggestions(null, null)
            return true
        }
        return false
    }

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

    private fun handleMultipleCases(caseAnnotationSuggestion: List<String>?): Boolean {
        if ((caseAnnotationSuggestion?.size ?: 0) > 1) {
            handleMultipleNounFormats(caseAnnotationSuggestion, "preposition")
            return true
        }
        return false
    }

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

        keyboardViewModel.setGenderSuggestions(buttonText, null)
    }

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

        keyboardViewModel.setGenderSuggestions(leftSuggestion.second, rightSuggestion.second)
    }

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

    private fun handleFallbackOrSingleSuggestion(multipleTypeSuggestion: List<String>?) {
        val suggestionText = ""
        val validNouns = multipleTypeSuggestion?.filter { handleColorAndTextForNounType(it, language, applicationContext).second != suggestionText }
        val validCases = caseAnnotationSuggestion?.filter { handleTextForCaseAnnotation(it, language, applicationContext).second != suggestionText }
        if (!validNouns.isNullOrEmpty()) {
            handleSingleType(validNouns, "noun")
        } else if (!validCases.isNullOrEmpty()) {
            handleSingleType(validCases, "preposition")
        } else {
            disableAutoSuggest(language)
        }
    }

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
                keyboardViewModel.setSuggestions(null, default1, default2)
            }
            return
        }

        keyboardViewModel.setAutocompleteActive(false)
        val suggestions = listOfNotNull(wordSuggestions.getOrNull(0), wordSuggestions.getOrNull(1), wordSuggestions.getOrNull(2))
        val suggestion1 = suggestions.getOrNull(0) ?: ""
        val suggestion2 = suggestions.getOrNull(1) ?: ""
        val suggestion3 = suggestions.getOrNull(2) ?: ""
        val emojiCount = autoSuggestEmojis?.size ?: 0
        var sTranslate: String? = null
        var sConjugate: String? = suggestion1
        var sPlural: String? = null

        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
            }
            hasLinguisticSuggestions && emojiCount == 0 -> {
                sPlural = suggestion2
            }
            !hasLinguisticSuggestions && emojiCount != 0 -> {
                sTranslate = suggestion2
            }
            else -> {
                sTranslate = suggestion2
                sPlural = suggestion3
            }
        }

        keyboardViewModel.setSuggestions(sTranslate, sConjugate, sPlural)
    }

    fun updateAutocompleteSuggestions(
        completions: List<String>?,
        highlightedSuggestion: String? = null,
    ) {
        if (currentState != ScribeState.IDLE || completions.isNullOrEmpty()) {
            highlightedAutocompleteSuggestion = null
            keyboardViewModel.setHighlightedSuggestion(null)
            keyboardViewModel.setAutocompleteActive(false)
            disableAutoSuggest(language)
            return
        }

        val completion1 = completions.getOrNull(0) ?: ""
        val completion2 = completions.getOrNull(1) ?: ""
        val completion3 = completions.getOrNull(2) ?: ""

        highlightedAutocompleteSuggestion = highlightedSuggestion
        keyboardViewModel.setHighlightedSuggestion(highlightedSuggestion)
        keyboardViewModel.setAutocompleteActive(true)
        keyboardViewModel.setSuggestions(completion1, completion2, completion3)
    }

    private fun replaceCurrentWordWithSuggestion(text: String) {
        val ic = currentInputConnection ?: return
        val beforeText = ic.getTextBeforeCursor(WORD_LOOKBACK_LENGTH, 0) ?: ""
        val wordStartIndex = beforeText.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '.', ',', '?', '!')) + 1
        val currentWord = beforeText.substring(wordStartIndex)
        ic.deleteSurroundingText(currentWord.length, 0)
        ic.commitText(text, 1)
    }

    fun tryInsertHighlightedAutocompleteSuggestion(): Boolean {
        val suggestion = highlightedAutocompleteSuggestion ?: return false
        highlightedAutocompleteSuggestion = null
        keyboardViewModel.setHighlightedSuggestion(null)
        replaceCurrentWordWithSuggestion(suggestion)
        currentInputConnection?.commitText(" ", 1)
        moveToIdleState()
        return true
    }

    fun clearAutocomplete() {
        highlightedAutocompleteSuggestion = null
        keyboardViewModel.setHighlightedSuggestion(null)
        disableAutoSuggest(language)
    }

    fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    fun returnSubsequentData(): List<List<String>> = subsequentData

    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean,
    ): String? {
        val keyLabel = getKeyLabel(code)
        if (!isSubsequentRequired) {
            if (!keyLabel.isNullOrEmpty()) {
                currentInputConnection?.commitText("$keyLabel ", 1)
                suggestionHandler.processLinguisticSuggestions(keyLabel)
            }
        }
        return keyLabel
    }

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
        subsequentAreaRequired = false
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
    }

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

    fun updateButtonVisibility(enabled: Boolean) {
    }

    fun updateEmojiSuggestion(
        enabled: Boolean,
        emojis: MutableList<String>?,
    ) {
        if (enabled && emojis != null) {
            keyboardViewModel.updateEmojiSuggestions(emojis)
        } else {
            keyboardViewModel.updateEmojiSuggestions(emptyList())
        }
    }

    fun disableAutoSuggest() = disableAutoSuggest(language)

    private fun disableAutoSuggest(language: String) {
        keyboardViewModel.setAutocompleteActive(false)
        val suggestions =
            be.scri.helpers.ui.HintUtils
                .getBaseAutoSuggestions(language)
        keyboardViewModel.setGenderSuggestions(null, null)

        if (isNumericKeyboardActive) {
            keyboardViewModel.setSuggestions(suggestions.getOrNull(0), null, null)
        } else {
            keyboardViewModel.setSuggestions(
                suggestions.getOrNull(0),
                suggestions.getOrNull(1),
                suggestions.getOrNull(2),
            )
        }
    }

    fun onClipboardSuggestionClicked() {
        latestClipText?.let { text ->
            currentInputConnection?.commitText(text, 1)
        }
        hasNewClip = false
        latestClipText = null
    }

    fun openClipboardPanel() {
        keyboardViewModel.setClipboardPanelVisible(true)
        refreshClipboardItems()
    }

    fun closeClipboardPanel() {
        keyboardViewModel.setClipboardPanelVisible(false)
    }

    private val clipboardRepository by lazy {
        be.scri.helpers.clipboard
            .ClipboardRepository(this)
    }

    private fun refreshClipboardItems() {
        imeScope.launch {
            val items = clipboardRepository.getAllItems()
            keyboardViewModel.updateClipboardItems(items)
        }
    }

    override fun onClipboardItemClicked(item: be.scri.helpers.clipboard.ClipboardItem) {
        currentInputConnection?.commitText(item.text, 1)
        closeClipboardPanel()
    }

    override fun onClipboardItemDelete(item: be.scri.helpers.clipboard.ClipboardItem) {
        imeScope.launch {
            clipboardRepository.deleteItem(item.id)
            refreshClipboardItems()
        }
    }

    override fun onClipboardItemPinToggle(item: be.scri.helpers.clipboard.ClipboardItem) {
        imeScope.launch {
            clipboardRepository.togglePin(item.id, item.isPinned)
            refreshClipboardItems()
        }
    }

    override fun onClipboardClearAll() {
        imeScope.launch {
            clipboardRepository.clearAll()
            refreshClipboardItems()
        }
    }

    override fun onClipboardPanelClose() {
        closeClipboardPanel()
    }

    fun getKeyboardWidth(): Int =
        if (isFloatingMode) {
            val density = resources.displayMetrics.density
            val screenWidth = resources.displayMetrics.widthPixels
            val floatWidth = (320f * density).toInt()
            minOf(floatWidth, (screenWidth * 0.85f).toInt())
        } else {
            resources.displayMetrics.widthPixels
        }

    private fun recreateKeyboard() {
        val xmlId = getCurrentKeyboardLayoutXML()
        val currentShiftState = keyboard?.mShiftState ?: SHIFT_OFF
        keyboard = KeyboardBase(this, xmlId, enterKeyType, getKeyboardWidth())
        keyboard?.setShifted(currentShiftState)

        if (xmlId == R.xml.keys_symbols) {
            keyboardViewModel.setCurrencySymbol(PreferencesHelper.getDefaultCurrencySymbol(this, language))
        }
    }

    val isFloatingMode: Boolean
        get() = keyboardViewModel.isFloatingMode.value

    private fun loadFloatingTransform() {
        keyboardViewModel.setFloatingTransform(
            PreferencesHelper.getFloatingX(this, language),
            PreferencesHelper.getFloatingY(this, language),
            PreferencesHelper.getFloatingScaleX(this, language),
            PreferencesHelper.getFloatingScaleY(this, language),
        )
    }

    fun initFloatingMode() {
        keyboardViewModel.setFloatingMode(PreferencesHelper.getIsFloatingModeEnabled(this, language))
        loadFloatingTransform()
        applyFloatingModeState()
    }

    fun toggleFloatingMode() {
        val enabled = !isFloatingMode
        PreferencesHelper.setIsFloatingModeEnabled(this, language, enabled)
        keyboardViewModel.setFloatingMode(enabled)
        loadFloatingTransform()
        applyFloatingModeState()
    }

    fun disableFloatingMode() {
        if (!isFloatingMode) return
        PreferencesHelper.setIsFloatingModeEnabled(this, language, false)
        keyboardViewModel.setFloatingMode(false)
        applyFloatingModeState()
    }

    fun applyFloatingModeState() {
        setBackDisposition(
            if (isFloatingMode) {
                BACK_DISPOSITION_ADJUST_NOTHING
            } else {
                BACK_DISPOSITION_DEFAULT
            },
        )
        recreateKeyboard()
        applyNavBarColor()
    }

    override fun onFloatingGestureEnded(
        offsetX: Float,
        offsetY: Float,
        scaleX: Float,
        scaleY: Float,
        dockToBottom: Boolean,
    ) {
        if (dockToBottom) {
            disableFloatingMode()
            return
        }
        PreferencesHelper.setFloatingX(this, language, offsetX)
        PreferencesHelper.setFloatingY(this, language, offsetY)
        PreferencesHelper.setFloatingScaleX(this, language, scaleX)
        PreferencesHelper.setFloatingScaleY(this, language, scaleY)
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
