// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import DataContract
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
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
import android.text.TextUtils
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import be.scri.R
import be.scri.R.color.md_grey_black_dark
import be.scri.R.color.white
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.AnnotationTextUtils.handleColorAndTextForNounType
import be.scri.helpers.AnnotationTextUtils.handleTextForCaseAnnotation
import be.scri.helpers.DatabaseManagers
import be.scri.helpers.EmojiUtils.insertEmoji
import be.scri.helpers.KeyboardBase
import be.scri.helpers.LanguageMappingConstants.conjugatePlaceholder
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.LanguageMappingConstants.pluralPlaceholder
import be.scri.helpers.LanguageMappingConstants.translatePlaceholder
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
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import be.scri.helpers.ui.HintUtils
import be.scri.views.KeyboardView
import java.util.Locale

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

@Suppress("TooManyFunctions", "LargeClass")
abstract class GeneralKeyboardIME(
    var language: String,
) : InputMethodService(),
    KeyboardView.OnKeyboardActionListener {
    abstract fun getKeyboardLayoutXML(): Int

    abstract val keyboardLetters: Int
    abstract val keyboardSymbols: Int
    abstract val keyboardSymbolShift: Int

    open var keyboard: KeyboardBase? = null
    var keyboardView: KeyboardView? = null
    abstract var lastShiftPressTS: Long
    abstract var keyboardMode: Int
    abstract var inputTypeClass: Int
    abstract var enterKeyType: Int
    abstract var switchToLetters: Boolean
    abstract var hasTextBeforeCursor: Boolean

    // Track if the delete key is currently being repeated (long press).
    private var isDeleteRepeating: Boolean = false

    internal lateinit var binding: InputMethodViewBinding

    private var pluralBtn: Button? = null
    private var emojiBtnPhone1: Button? = null
    private var emojiSpacePhone: View? = null
    private var emojiBtnPhone2: Button? = null
    private var emojiBtnTablet1: Button? = null
    private var emojiSpaceTablet1: View? = null
    private var emojiBtnTablet2: Button? = null
    private var emojiSpaceTablet2: View? = null
    private var emojiBtnTablet3: Button? = null
    private var genderSuggestionLeft: Button? = null
    private var genderSuggestionRight: Button? = null

    internal var isSingularAndPlural: Boolean = false
    private var subsequentAreaRequired: Boolean = false
    private var subsequentData: MutableList<List<String>> = mutableListOf()

    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    private lateinit var dbManagers: DatabaseManagers
    private lateinit var suggestionHandler: SuggestionHandler
    private var dataContract: DataContract? = null
    var emojiKeywords: HashMap<String, MutableList<String>>? = null
    private lateinit var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>
    private lateinit var conjugateLabels: Set<String>
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
    private var earlierValue: Int? = keyboardView?.setEnterKeyIcon(ScribeState.IDLE)

    private var currentCommandBarHint: String = ""
    private var commandBarHintColor: Int = Color.GRAY
    private var commandBarTextColor: Int = Color.BLACK

    /**
     * This function is updated to reliably detect search bars in various apps,
     * including browsers like Chrome and Firefox, not just fields with IME_ACTION_SEARCH.
     * The logic is combined into a single return statement to satisfy the `detekt` ReturnCount rule.
     * It checks multiple signals:
     * 1. The explicit IME action for search.
     * 2. The input type variation for URIs (common in address bars).
     * 3. The hint text for keywords like "search" or "address".
     *
     * @return `true` if the current input field is likely a search or address bar, `false` otherwise.
     */
    fun isSearchBar(): Boolean {
        val editorInfo = currentInputEditorInfo

        val isActionSearch = (enterKeyType == EditorInfo.IME_ACTION_SEARCH)

        val isUriType =
            editorInfo?.let {
                (it.inputType and InputType.TYPE_TEXT_VARIATION_URI) != 0
            } == true

        val hasSearchHint =
            editorInfo?.hintText?.toString()?.lowercase(Locale.ROOT)?.let {
                it.contains("search") || it.contains("address")
            } == true

        return isActionSearch || isUriType || hasSearchHint
    }

    protected fun isPeriodAndCommaEnabled(): Boolean {
        val isPreferenceEnabled = PreferencesHelper.getEnablePeriodAndCommaABC(this, language)
        val isInSearchBar = isSearchBar()

        return isPreferenceEnabled || isInSearchBar
    }

    enum class ScribeState { IDLE, SELECT_COMMAND, TRANSLATE, CONJUGATE, PLURAL, SELECT_VERB_CONJUNCTION, INVALID, ALREADY_PLURAL }

    /**
     * Returns whether the current conjugation state requires a subsequent selection view.
     * This is used, for example, when a conjugation form has multiple options (e.g., "am/is/are" in English).
     * @return `true` if a subsequent selection screen is needed, `false` otherwise.
     */
    internal fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    internal fun returnSubsequentData(): List<List<String>> = subsequentData

    /**
     * Called when the service is first created. Initializes database and suggestion handlers.
     */
    override fun onCreate() {
        super.onCreate()
        dbManagers = DatabaseManagers(this)
        suggestionHandler = SuggestionHandler(this)
    }

    /**
     * Creates the main view for the input method, inflating it from XML and setting up the keyboard.
     * @return The root View of the input method.
     */
    override fun onCreateInputView(): View {
        binding = InputMethodViewBinding.inflate(layoutInflater)
        val inputView = binding.root
        keyboardView = binding.keyboardView
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        keyboardView?.setVibrate = getIsVibrateEnabled(applicationContext, language)
        keyboardView?.setSound = getIsSoundEnabled(applicationContext, language)
        keyboardView?.setHoldForAltCharacters = getHoldKeyStyle(applicationContext, language)
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.mOnKeyboardActionListener = this
        initializeUiElements()
        setupClickListeners()
        currentState = ScribeState.IDLE
        saveConjugateModeType("none")
        updateUI()
        return inputView
    }

    override fun onWindowShown() {
        super.onWindowShown()
        keyboardView?.setPreview = isShowPopupOnKeypressEnabled(applicationContext, language)
        keyboardView?.setVibrate = getIsVibrateEnabled(applicationContext, language)
        keyboardView?.setSound = getIsSoundEnabled(applicationContext, language)
        keyboardView?.setHoldForAltCharacters = getHoldKeyStyle(applicationContext, language)
    }

    /**
     * Finds and initializes UI elements from the inflated view binding.
     */
    private fun initializeUiElements() {
        pluralBtn = binding.pluralBtn
        emojiBtnPhone1 = binding.emojiBtnPhone1
        emojiSpacePhone = binding.emojiSpacePhone
        emojiBtnPhone2 = binding.emojiBtnPhone2
        emojiBtnTablet1 = binding.emojiBtnTablet1
        emojiSpaceTablet1 = binding.emojiSpaceTablet1
        emojiBtnTablet2 = binding.emojiBtnTablet2
        emojiSpaceTablet2 = binding.emojiSpaceTablet2
        emojiBtnTablet3 = binding.emojiBtnTablet3
        genderSuggestionLeft = binding.translateBtnLeft
        genderSuggestionRight = binding.translateBtnRight
    }

    /**
     * Sets up the OnClickListeners for the main interactive elements of the Scribe key and toolbar.
     */
    private fun setupClickListeners() {
        binding.scribeKeyOptions.setOnClickListener {
            if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                if (currentState == ScribeState.IDLE) {
                    moveToSelectCommandState()
                } else {
                    moveToIdleState()
                }
            }
        }

        setCommandButtonListeners()
        binding.scribeKeyToolbar.setOnClickListener { moveToIdleState() }
    }

    /**
     * Attaches OnClickListeners to the command buttons (Translate, Conjugate, Plural).
     */
    private fun setCommandButtonListeners() {
        binding.translateBtn.setOnClickListener {
            currentState = ScribeState.TRANSLATE
            saveConjugateModeType("none")
            updateUI()
        }
        binding.conjugateBtn.setOnClickListener {
            currentState = ScribeState.CONJUGATE
            updateUI()
        }
        binding.pluralBtn.setOnClickListener {
            currentState = ScribeState.PLURAL
            saveConjugateModeType("none")
            if (language == "German") keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
            updateUI()
        }
    }

    /**
     * Called when the input view is finished. Resets the keyboard state to IDLE.
     * @param finishingInput `true` if we are finishing for good,
     * `false` if just switching to another app.
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        moveToIdleState()
    }

    /**
     * Called by the system when the service is first initialized, before the input view is created.
     * Initializes the base keyboard and updates the UI if the view is already bound.
     */
    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        if (this::binding.isInitialized) updateUI()
    }

    /**
     * Overrides the default implementation to check if there is any
     * non-whitespace text before the cursor.
     * @return `true` if there is meaningful text before the cursor, `false` otherwise.
     */
    override fun hasTextBeforeCursor(): Boolean {
        val ic = currentInputConnection ?: return false
        val text = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.trim() ?: ""
        return text.isNotEmpty() && text.lastOrNull() != '.'
    }

    /**
     * Called when a key is pressed down. Triggers haptic feedback if enabled.
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
            if (
                editorInfo != null &&
                editorInfo.inputType != InputType.TYPE_NULL &&
                keyboard?.mShiftState != SHIFT_ON_PERMANENT
            ) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
                }
            }
            keyboardView!!.setKeyboard(keyboard!!)
            switchToLetters = false
        }
    }

    /**
     * Sets the flag to indicate that the delete key is currently repeating (long press).
     */
    fun setDeleteRepeating(isRepeating: Boolean) {
        isDeleteRepeating = isRepeating
    }

    /**
     * Returns whether the delete key is currently repeating (long press).
     */
    fun isDeleteRepeating(): Boolean = isDeleteRepeating

    override fun moveCursorLeft() {
        moveCursor(false)
    }

    override fun moveCursorRight() {
        moveCursor(true)
    }

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 0)
    }

    /**
     * Called when the IME is starting to interact with a new input field.
     * It initializes the keyboard based on the input type and loads all language-specific data.
     * @param attribute The editor information for the new input field.
     * @param restarting `true` if we are restarting the input with the same editor.
     */
    override fun onStartInput(
        attribute: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)
        inputTypeClass = attribute!!.inputType and TYPE_MASK_CLASS
        enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)
        currentEnterKeyType = enterKeyType
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
        caseAnnotation = dbManagers.prepositionManager.getCaseAnnotations(languageAlias)
        conjugateOutput = dbManagers.conjugateDataManager.getTheConjugateLabels(languageAlias, dataContract, "coacha")
        conjugateLabels = dbManagers.conjugateDataManager.extractConjugateHeadings(dataContract, "coacha")
        keyboard = KeyboardBase(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)

        // Set up the currency symbol if we're using the symbols keyboard layout.
        if (keyboardXml == R.xml.keys_symbols) {
            setupCurrencySymbol()
        }
    }

    /**
     * Called when the input view is starting. It sets up the UI theme, emoji settings,
     * and initial keyboard state.
     * @param editorInfo The editor information for the input field.
     * @param restarting `true` if we are restarting the input with the same editor.
     */
    override fun onStartInputView(
        editorInfo: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInputView(editorInfo, restarting)
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        updateEnterKeyColor(isUserDarkMode)
        emojiAutoSuggestionEnabled = getIsEmojiSuggestionsEnabled(applicationContext, language)

        autoSuggestEmojis = null
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()

        moveToIdleState()
        val window = window?.window ?: return
        var color = R.color.dark_keyboard_bg_color
        val isDarkMode = getIsDarkModeOrNot(applicationContext)
        color =
            if (isDarkMode) {
                R.color.dark_keyboard_bg_color
            } else {
                R.color.light_keyboard_bg_color
            }

        window.navigationBarColor = ContextCompat.getColor(this, color)

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

    private fun isLightColor(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

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
     * Updates the color of the Enter key based on the current Scribe state and theme (dark/light mode).
     * @param isDarkMode The current dark mode status. If null, it will be determined from context.
     */
    private fun updateEnterKeyColor(isDarkMode: Boolean?) {
        val resolvedIsDarkMode = isDarkMode ?: getIsDarkModeOrNot(applicationContext)
        when (currentState) {
            ScribeState.IDLE, ScribeState.SELECT_COMMAND -> {
                keyboardView?.setEnterKeyIcon(ScribeState.IDLE, earlierValue)
                keyboardView?.setEnterKeyColor(null, isDarkMode = resolvedIsDarkMode)
            }
            else -> {
                keyboardView?.setEnterKeyColor(getColor(R.color.color_primary))
                keyboardView?.setEnterKeyIcon(ScribeState.PLURAL, earlierValue)
            }
        }
        val scribeKeyTint = if (resolvedIsDarkMode) R.color.light_key_color else R.color.light_key_text_color
        binding.scribeKeyOptions.foregroundTintList = ContextCompat.getColorStateList(this, scribeKeyTint)
        binding.scribeKeyToolbar.foregroundTintList = ContextCompat.getColorStateList(this, scribeKeyTint)
    }

    /**
     * Updates the hint and prompt text displayed in the command bar area based on the current state.
     * @param isUserDarkMode The current dark mode status. If null, it will be determined from context.
     * @param text A specific text to be displayed in the prompt, often used for conjugation titles.
     * @param word A word to be included in the hint text.
     */
    private fun updateCommandBarHintAndPrompt(
        isUserDarkMode: Boolean? = null,
        text: String? = null,
        word: String? = null,
    ) {
        val resolvedIsDarkMode = isUserDarkMode ?: getIsDarkModeOrNot(applicationContext)
        val commandBarEditText = binding.commandBar
        val promptTextView = binding.promptText

        // 1. Get the hint message and prompt text
        currentCommandBarHint = HintUtils.getCommandBarHint(currentState, language, word)
        val promptText = HintUtils.getPromptText(currentState, language, context = this, text)
        promptTextView.text = promptText

        // 2. Set the appropriate colors based on the theme
        commandBarHintColor = if (resolvedIsDarkMode) getColor(R.color.hint_white) else getColor(R.color.hint_black)
        commandBarTextColor = if (resolvedIsDarkMode) getColor(white) else Color.BLACK

        val backgroundColor = if (resolvedIsDarkMode) R.color.command_bar_color_dark else white
        binding.commandBarLayout.backgroundTintList = ContextCompat.getColorStateList(this, backgroundColor)
        promptTextView.setTextColor(if (resolvedIsDarkMode) getColor(white) else Color.BLACK)
        promptTextView.setBackgroundColor(getColor(backgroundColor))

        // 3. Set the initial state of the command bar to show the hint
        commandBarEditText.setTextColor(commandBarHintColor)
        setCommandBarTextWithCursor(currentCommandBarHint, cursorAtStart = true)
        commandBarEditText.requestFocus()
    }

    /**
     * The main dispatcher for updating the entire keyboard UI. It calls the appropriate setup function
     * based on the current [ScribeState].
     */
    internal fun updateUI() {
        if (!this::binding.isInitialized) return
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)

        when (currentState) {
            ScribeState.IDLE -> {
                setupIdleView()
            }
            ScribeState.SELECT_COMMAND -> {
                setupSelectCommandView()
            }

            ScribeState.INVALID -> setupInvalidView()
            ScribeState.TRANSLATE -> {
                setupToolbarView()
                // Add specific handling here to maintain translate button.
                binding.translateBtn.text = translatePlaceholder[getLanguageAlias(language)] ?: "Translate"
                binding.translateBtn.visibility = View.VISIBLE
            }
            ScribeState.ALREADY_PLURAL -> setupAlreadyPluralView()
            else -> setupToolbarView()
        }

        updateEnterKeyColor(isUserDarkMode)
    }

    /**
     * Configures the UI for the `IDLE` state, showing default suggestions or emoji suggestions.
     */
    private fun setupIdleView() {
        binding.commandOptionsBar.visibility = View.VISIBLE
        binding.toolbarBar.visibility = View.GONE

        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)

        binding.commandOptionsBar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isUserDarkMode) {
                    R.color.dark_keyboard_bg_color
                } else {
                    R.color.light_keyboard_bg_color
                },
            ),
        )

        val textColor = if (isUserDarkMode) Color.WHITE else "#1E1E1E".toColorInt()

        listOf(binding.translateBtn, binding.conjugateBtn, binding.pluralBtn).forEachIndexed { index, button ->
            button.visibility = View.VISIBLE
            button.background = null
            button.setTextColor(textColor)
            button.text = HintUtils.getBaseAutoSuggestions(language).getOrNull(index)
            button.isAllCaps = false
            button.textSize = SUGGESTION_SIZE
            button.setOnClickListener(null)
        }

        listOf(binding.separator2, binding.separator3).forEach { separator ->
            separator.setBackgroundColor(ContextCompat.getColor(this, R.color.special_key_light))
            val params = separator.layoutParams
            // Convert 0.5dp to pixels. coerceAtLeast(1) ensures it's never zero.
            params.width = (SEPARATOR_WIDTH * resources.displayMetrics.density).toInt().coerceAtLeast(1)
            separator.layoutParams = params

            separator.visibility = View.VISIBLE
        }

        binding.separator1.visibility = View.GONE

        binding.scribeKeyOptions.foreground = AppCompatResources.getDrawable(this, R.drawable.ic_scribe_icon_vector)
        initializeKeyboard(getKeyboardLayoutXML())
        updateButtonVisibility(emojiAutoSuggestionEnabled)
        updateEmojiSuggestion(emojiAutoSuggestionEnabled, autoSuggestEmojis)
        binding.commandBar.setText("")
        disableAutoSuggest()
    }

    /**
     * Configures the UI for the `SELECT_COMMAND` state, showing the main command buttons
     * (Translate, Conjugate, Plural).
     */
    private fun setupSelectCommandView() {
        binding.commandOptionsBar.visibility = View.VISIBLE
        binding.toolbarBar.visibility = View.GONE

        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)

        binding.commandOptionsBar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isUserDarkMode) {
                    R.color.dark_keyboard_bg_color
                } else {
                    R.color.light_keyboard_bg_color
                },
            ),
        )

        val langAlias = getLanguageAlias(language)

        updateButtonVisibility(isAutoSuggestEnabled = false)
        setCommandButtonListeners()

        val buttonTextColor = if (isUserDarkMode) Color.WHITE else Color.BLACK

        listOf(binding.translateBtn, binding.conjugateBtn, binding.pluralBtn).forEach { button ->
            button.visibility = View.VISIBLE
            button.background = ContextCompat.getDrawable(this, R.drawable.button_background_rounded)
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.theme_scribe_blue)
            button.setTextColor(buttonTextColor)
            button.textSize = SUGGESTION_SIZE
        }

        binding.translateBtn.text = translatePlaceholder[langAlias] ?: "Translate"
        binding.conjugateBtn.text = conjugatePlaceholder[langAlias] ?: "Conjugate"
        binding.pluralBtn.text = pluralPlaceholder[langAlias] ?: "Plural"

        val separatorColor = (if (isUserDarkMode) DARK_THEME else LIGHT_THEME).toColorInt()
        binding.separator2.setBackgroundColor(separatorColor)
        binding.separator3.setBackgroundColor(separatorColor)

        val spaceInDp = COMMAND_BUTTON_SPACING_DP
        val spaceInPx = (spaceInDp * resources.displayMetrics.density).toInt()
        listOf(binding.separator2, binding.separator3).forEach { separator ->
            separator.setBackgroundColor(Color.TRANSPARENT)
            val params = separator.layoutParams
            params.width = spaceInPx
            separator.layoutParams = params
        }

        binding.separator1.visibility = View.GONE
        binding.separator2.visibility = View.VISIBLE
        binding.separator3.visibility = View.VISIBLE
        binding.separator4.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE

        binding.scribeKeyOptions.foreground = AppCompatResources.getDrawable(this, R.drawable.close)
    }

    /**
     * Configures the UI for command modes (`TRANSLATE`, `CONJUGATE`, etc.), showing the command bar and toolbar.
     */
    private fun setupToolbarView() {
        binding.commandOptionsBar.visibility = View.GONE
        binding.toolbarBar.visibility = View.VISIBLE
        val isDarkMode = getIsDarkModeOrNot(applicationContext)
        binding.toolbarBar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isDarkMode) {
                    R.color.dark_keyboard_bg_color
                } else {
                    R.color.light_keyboard_bg_color
                },
            ),
        )

        binding.scribeKeyToolbar.foreground =
            AppCompatResources.getDrawable(
                this,
                R.drawable.close,
            )

        var hintWord: String? = null
        var promptText: String? = null

        if (currentState == ScribeState.SELECT_VERB_CONJUNCTION) {
            val keyboardXmlId = getKeyboardLayoutForState(currentState)
            initializeKeyboard(keyboardXmlId)

            val conjugateIndex = getValidatedConjugateIndex()
            setupConjugateKeysByLanguage(conjugateIndex)
            promptText = conjugateOutput.keys.elementAtOrNull(conjugateIndex)
            hintWord = conjugateLabels.lastOrNull()
        }

        updateCommandBarHintAndPrompt(text = promptText, isUserDarkMode = isDarkMode, word = hintWord)
    }

    /**
     * Configures the UI for the `INVALID` state, which is shown when a command (e.g., translation) fails.
     */
    @SuppressLint("SetTextI18n")
    private fun setupInvalidView() {
        binding.commandOptionsBar.visibility = View.GONE
        binding.toolbarBar.visibility = View.VISIBLE
        val isDarkMode = getIsDarkModeOrNot(applicationContext)
        binding.toolbarBar.setBackgroundColor(if (isDarkMode) "#1E1E1E".toColorInt() else "#d2d4da".toColorInt())
        binding.ivInfo.visibility = View.VISIBLE
        binding.promptText.text = HintUtils.getInvalidHint(language = language) + ": "
        binding.commandBar.hint = ""
        binding.scribeKeyToolbar.foreground = AppCompatResources.getDrawable(this, R.drawable.ic_scribe_icon_vector)
        binding.scribeKeyToolbar.setOnClickListener { moveToSelectCommandState() }
    }

    /**
     * Configures the UI for the `ALREADY_PLURAL` state, which is shown when the user
     * attempts to pluralize a word that is already plural.
     */
    @SuppressLint("SetTextI18n")
    private fun setupAlreadyPluralView() {
        binding.commandOptionsBar.visibility = View.GONE
        binding.toolbarBar.visibility = View.VISIBLE
        val isDarkMode = getIsDarkModeOrNot(applicationContext)
        binding.toolbarBar.setBackgroundColor(
            if (isDarkMode) "#1E1E1E".toColorInt() else "#d2d4da".toColorInt(),
        )
        binding.ivInfo.visibility = View.VISIBLE
        binding.promptText.text = "$ALREADY_PLURAL_MSG: "
        binding.commandBar.hint = ""
        binding.scribeKeyToolbar.foreground =
            AppCompatResources.getDrawable(
                this,
                R.drawable.ic_scribe_icon_vector,
            )
        binding.scribeKeyToolbar.setOnClickListener { moveToIdleState() }
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

    /**
     * Transitions the keyboard to the `SELECT_COMMAND` state and updates the UI.
     */
    private fun moveToSelectCommandState() {
        clearSuggestionData()
        currentState = ScribeState.SELECT_COMMAND
        saveConjugateModeType("none")
        updateUI()
    }

    /**
     * Transitions the keyboard to the `IDLE` state and updates the UI.
     */
    internal fun moveToIdleState() {
        clearSuggestionData()
        currentState = ScribeState.IDLE
        saveConjugateModeType("none")
        if (this::binding.isInitialized) updateUI()
    }

    /**
     * Determines which keyboard layout XML to use based on the current [ScribeState].
     * @param state The current state of the Scribe keyboard.
     * @param isSubsequentArea `true` if this is for a secondary conjugation view.
     * @param dataSize The number of items to display, used to select an appropriate layout.
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
     * Initializes or re-initializes the keyboard with a new layout.
     * @param xmlId The resource ID of the keyboard layout XML.
     */
    private fun initializeKeyboard(xmlId: Int) {
        keyboard = KeyboardBase(this, xmlId, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)
        keyboardView?.requestLayout()

        // Set up the currency symbol if we're on the symbols keyboard.
        if (keyboardMode == keyboardSymbols) {
            setupCurrencySymbol()
        }
    }

    /**
     * Sets up the currency symbol on the keyboard based on user preferences.
     */
    private fun setupCurrencySymbol() {
        val currencySymbol = PreferencesHelper.getDefaultCurrencySymbol(this, language)
        keyboardView?.setKeyLabel(currencySymbol, "", KeyboardBase.CODE_CURRENCY)
    }

    /**
     * Retrieves and validates the stored index for the current conjugation view.
     * Ensures the index is within the bounds of available conjugation types.
     * @return A valid, zero-based index for the conjugation type.
     */
    private fun getValidatedConjugateIndex(): Int {
        val prefs = getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        var index = prefs.getInt("conjugate_index", 0)
        val maxIndex = if (this::conjugateOutput.isInitialized) conjugateOutput.keys.count() - 1 else -1
        index = if (maxIndex >= 0) index.coerceIn(0, maxIndex) else 0
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }

    /**
     * A wrapper to set up the conjugation key labels for the current language and index.
     * @param conjugateIndex The index of the conjugation tense/mood to display.
     * @param isSubsequentArea `true` if setting up a secondary view.
     */
    internal fun setupConjugateKeysByLanguage(
        conjugateIndex: Int,
        isSubsequentArea: Boolean = false,
    ) {
        setUpConjugateKeys(
            startIndex = conjugateIndex,
            isSubsequentArea = isSubsequentArea,
        )
    }

    /**
     * Sets the labels for the special conjugation keys based on the selected tense/mood.
     * @param startIndex The index of the conjugation tense/mood from the loaded data.
     * @param isSubsequentArea `true` if this is for a secondary conjugation view.
     */
    private fun setUpConjugateKeys(
        startIndex: Int,
        isSubsequentArea: Boolean,
    ) {
        if (!this::conjugateOutput.isInitialized || !this::conjugateLabels.isInitialized) {
            return
        }

        val title = conjugateOutput.keys.elementAtOrNull(startIndex)
        val languageOutput = title?.let { conjugateOutput[it] }

        if (conjugateLabels.isEmpty() || title == null || languageOutput == null) {
            return
        }

        Log.i("HELLO", "The output from the languageOutput is $languageOutput")
        if (language != "English") {
            setUpNonEnglishConjugateKeys(languageOutput, conjugateLabels.toList(), title)
        } else {
            setUpEnglishConjugateKeys(languageOutput, isSubsequentArea)
        }

        if (isSubsequentArea) {
            keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_FPS)
        }
    }

    /**
     * Sets up conjugation key labels for non-English languages, which typically follow a 3x2 grid layout.
     * @param languageOutput The map of conjugation forms for the selected tense.
     * @param conjugateLabel The list of labels for each person/number (e.g., "1ps", "2ps").
     * @param title The title of the current tense/mood.
     */
    private fun setUpNonEnglishConjugateKeys(
        languageOutput: Map<String, Collection<String>>,
        conjugateLabel: List<String>,
        title: String,
    ) {
        val keyCodes =
            when (language) {
                "Swedish" -> {
                    listOf(
                        KeyboardBase.CODE_TR,
                        KeyboardBase.CODE_TL,
                        KeyboardBase.CODE_BR,
                        KeyboardBase.CODE_BL,
                    )
                }

                else -> {
                    listOf(
                        KeyboardBase.CODE_FPS,
                        KeyboardBase.CODE_FPP,
                        KeyboardBase.CODE_SPS,
                        KeyboardBase.CODE_SPP,
                        KeyboardBase.CODE_TPS,
                        KeyboardBase.CODE_TPP,
                    )
                }
            }

        keyCodes.forEachIndexed { index, code ->
            val value = languageOutput[title]?.elementAtOrNull(index) ?: ""
            keyboardView?.setKeyLabel(value, conjugateLabel.getOrNull(index) ?: "", code)
        }
    }

    /**
     * Sets up conjugation key labels for English, which has a more complex structure,
     * potentially requiring a subsequent selection view.
     * @param languageOutput The map of conjugation forms for the selected tense.
     * @param isSubsequentArea `true` if this is for a secondary view.
     */
    private fun setUpEnglishConjugateKeys(
        languageOutput: Map<String, Collection<String>>,
        isSubsequentArea: Boolean,
    ) {
        val keys = languageOutput.keys.toList()
        val sharedPreferences = this.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)

        val keyMapping =
            listOf(
                Triple(0, KeyboardBase.CODE_TL, "CODE_TL"),
                Triple(1, KeyboardBase.CODE_TR, "CODE_TR"),
                Triple(DATA_SIZE_2, KeyboardBase.CODE_BL, "CODE_BL"),
                Triple(DATA_CONSTANT_3, KeyboardBase.CODE_BR, "CODE_BR"),
            )

        if (!isSubsequentArea) {
            keyMapping.forEach { (_, code, _) -> keyboardView?.setKeyLabel("HI", "HI", code) }
        }

        subsequentAreaRequired = false
        keyMapping.forEach { (index, code, prefKey) ->
            val outputKey = keys.getOrNull(index)
            val output = outputKey?.let { languageOutput[it] }

            if (output != null) {
                if (output.size > 1) {
                    subsequentAreaRequired = true
                    subsequentData.add(output.toList())
                    sharedPreferences.edit { putString("1", prefKey) }
                } else {
                    sharedPreferences.edit { putString("0", prefKey) }
                }
                keyboardView?.setKeyLabel(output.firstOrNull().toString(), "HI", code)
            }
        }
    }

    /**
     * Sets up a secondary "sub-view" for conjugation when a single key has multiple options.
     * @param data The full dataset of subsequent options.
     * @param word The specific word selected from the primary view, used to filter the data.
     */
    internal fun setupConjugateSubView(
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
        initializeKeyboard(keyboardXmlId)
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
        binding.ivInfo.visibility = View.GONE
    }

    /**
     * Saves the type of conjugation layout being used (e.g., "2x2", "3x2") to shared preferences.
     * @param language The current keyboard language.
     * @param isSubsequentArea `true` if this is for a secondary view.
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

    /**
     * Updates the visibility of the suggestion buttons based on device type (phone/tablet)
     * and whether auto-suggestions are currently active.
     * @param isAutoSuggestEnabled `true` if emoji or linguistic suggestions are available.
     */
    internal fun updateButtonVisibility(isAutoSuggestEnabled: Boolean) {
        if (currentState != ScribeState.IDLE) {
            setupDefaultButtonVisibility()
            return
        }

        val isTablet =
            (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE

        val emojiCount = if (isAutoSuggestEnabled) autoSuggestEmojis?.size ?: 0 else 0

        if (isTablet) {
            updateTabletButtonVisibility(emojiCount)
        } else {
            updatePhoneButtonVisibility(emojiCount)
        }
    }

    /**
     * Sets the default visibility for buttons when not in the `IDLE` state.
     * Hides all suggestion-related buttons.
     */
    private fun setupDefaultButtonVisibility() {
        pluralBtn?.visibility = View.VISIBLE
        emojiBtnPhone1?.visibility = View.GONE
        emojiBtnPhone2?.visibility = View.GONE
        emojiBtnTablet1?.visibility = View.GONE
        emojiBtnTablet2?.visibility = View.GONE
        emojiBtnTablet3?.visibility = View.GONE
        binding.separator4.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
    }

    /**
     * Handles the logic for showing/hiding suggestion buttons specifically on tablet layouts.
     * @param emojiCount The number of available emoji suggestions.
     */
    private fun updateTabletButtonVisibility(emojiCount: Int) {
        pluralBtn?.visibility = if (emojiCount > 0) View.INVISIBLE else View.VISIBLE

        when (emojiCount) {
            0 -> {
                emojiBtnTablet1?.visibility = View.GONE
                emojiSpaceTablet1?.visibility = View.GONE
                emojiBtnTablet2?.visibility = View.GONE
                emojiSpaceTablet2?.visibility = View.GONE
                emojiBtnTablet3?.visibility = View.GONE
            }
            1 -> {
                // Single emoji case: Show button 1, hide others to allow centering.
                emojiBtnTablet1?.visibility = View.VISIBLE
                emojiSpaceTablet1?.visibility = View.GONE
                emojiBtnTablet2?.visibility = View.GONE
                emojiSpaceTablet2?.visibility = View.GONE
                emojiBtnTablet3?.visibility = View.GONE
            }
            2 -> {
                // Two emoji case: Show first two buttons and first separator.
                emojiBtnTablet1?.visibility = View.VISIBLE
                emojiSpaceTablet1?.visibility = View.VISIBLE
                emojiBtnTablet2?.visibility = View.VISIBLE
                emojiSpaceTablet2?.visibility = View.GONE
                emojiBtnTablet3?.visibility = View.GONE
            }
            else -> { // 3 or more emojis
                // Full display case.
                emojiBtnTablet1?.visibility = View.VISIBLE
                emojiSpaceTablet1?.visibility = View.VISIBLE
                emojiBtnTablet2?.visibility = View.VISIBLE
                emojiSpaceTablet2?.visibility = View.VISIBLE
                emojiBtnTablet3?.visibility = View.VISIBLE
            }
        }

        // Hide other separators and phone-specific views.
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
        emojiBtnPhone1?.visibility = View.GONE
        emojiSpacePhone?.visibility = View.GONE
        emojiBtnPhone2?.visibility = View.GONE
        binding.separator4.visibility = View.GONE
    }

    /**
     * Handles the logic for showing/hiding suggestion buttons specifically on phone layouts.
     * @param emojiCount The number of available emoji suggestions.
     */
    private fun updatePhoneButtonVisibility(emojiCount: Int) {
        pluralBtn?.visibility = if (emojiCount > 0) View.INVISIBLE else View.VISIBLE

        when {
            emojiCount == 1 -> {
                // Single emoji case: Show button 1, hide separator and button 2.
                // Assuming parent layout centers the single visible item.
                emojiBtnPhone1?.visibility = View.VISIBLE
                emojiSpacePhone?.visibility = View.GONE
                emojiBtnPhone2?.visibility = View.GONE
            }
            emojiCount >= 2 -> {
                // Multiple emoji case: Show button 1, separator, and button 2.
                emojiBtnPhone1?.visibility = View.VISIBLE
                emojiSpacePhone?.visibility = View.VISIBLE
                emojiBtnPhone2?.visibility = View.VISIBLE
            }
            else -> { // emojiCount is 0 or less
                // No emoji case: Hide everything related to phone emojis.
                emojiBtnPhone1?.visibility = View.GONE
                emojiSpacePhone?.visibility = View.GONE
                emojiBtnPhone2?.visibility = View.GONE
            }
        }

        binding.separator4.visibility = if (emojiCount > 1) View.VISIBLE else View.GONE

        // Hide tablet-specific views.
        emojiBtnTablet1?.visibility = View.GONE
        emojiSpaceTablet1?.visibility = View.GONE
        emojiBtnTablet2?.visibility = View.GONE
        emojiSpaceTablet2?.visibility = View.GONE
        emojiBtnTablet3?.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
    }

    /**
     * Retrieves the text immediately preceding the cursor.
     * @return The text before the cursor, up to a defined maximum length.
     */
    fun getText(): String? = currentInputConnection?.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()

    /**
     * Extracts the last word from the text immediately preceding the cursor.
     * @return The last word as a [String], or null if no word is found.
     */
    fun getLastWordBeforeCursor(): String? = getText()?.trim()?.split("\\s+".toRegex())?.lastOrNull()

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
     * Finds the next suggestions for the last typed word.
     * @param wordSuggestions The map of words to their suggestions.
     * @param lastWord The word to look up.
     * @return A list of gender strings (e.g., "masculine", "neuter"), or null if not a known noun.
     */
    fun getNextWordSuggestions(
        wordSuggestions: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? {
        lastWord?.let {
            val suggestions = wordSuggestions[it.lowercase()]
            if (suggestions != null) {
                return suggestions
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
     * Updates the text of the suggestion buttons, primarily for displaying emoji suggestions.
     * @param isAutoSuggestEnabled `true` if suggestions are active.
     * @param autoSuggestEmojis The list of emojis to display.
     */
    fun updateEmojiSuggestion(
        isAutoSuggestEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        if (currentState != ScribeState.IDLE) return

        val tabletButtons = listOf(binding.emojiBtnTablet1, binding.emojiBtnTablet2, binding.emojiBtnTablet3)
        val phoneButtons = listOf(binding.emojiBtnPhone1, binding.emojiBtnPhone2)

        if (isAutoSuggestEnabled && autoSuggestEmojis != null) {
            tabletButtons.forEachIndexed { index, button ->
                val emoji = autoSuggestEmojis.getOrNull(index) ?: ""
                button.text = emoji
                button.setOnClickListener {
                    if (emoji.isNotEmpty()) {
                        insertEmoji(
                            emoji,
                            currentInputConnection,
                            emojiKeywords,
                            emojiMaxKeywordLength,
                        )
                    }
                }
            }

            phoneButtons.forEachIndexed { index, button ->
                val emoji = autoSuggestEmojis.getOrNull(index) ?: ""
                button.text = emoji
                button.setOnClickListener {
                    if (emoji.isNotEmpty()) {
                        insertEmoji(
                            emoji,
                            currentInputConnection,
                            emojiKeywords,
                            emojiMaxKeywordLength,
                        )
                    }
                }
            }
        } else {
            (tabletButtons + phoneButtons).forEach { button ->
                button.text = ""
                button.setOnClickListener(null)
            }
        }
    }

    /**
     * The main dispatcher for displaying linguistic auto-suggestions (gender, case, plurality).
     * @param nounTypeSuggestion The detected gender(s) of the last word.
     * @param isPlural `true` if the last word is plural.
     * @param caseAnnotationSuggestion The detected case(s) required by the last word.
     */
    fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
        wordSuggestions: List<String>? = null,
    ) {
        if (currentState != ScribeState.IDLE) {
            disableAutoSuggest()
            return
        }
        val hasLinguisticSuggestions =
            nounTypeSuggestion != null ||
                isPlural ||
                caseAnnotationSuggestion != null ||
                isSingularAndPlural
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
        if (!handled) disableAutoSuggest()
        handleWordSuggestions(
            wordSuggestions = wordSuggestions,
            hasLinguisticSuggestions = hasLinguisticSuggestions,
        )
    }

    /**
     * A helper function to specifically trigger the plural suggestion UI if needed.
     * @param isPlural `true` if the word is plural.
     * @return `true` if the plural suggestion was handled, `false` otherwise.
     */
    private fun handlePluralIfNeeded(isPlural: Boolean): Boolean {
        if (isPlural) {
            Log.d("PluralDebug", "isplural")
            handlePluralAutoSuggest()
            return true
        }
        Log.d("PluralDebug", "notplural")

        return false
    }

    /**
     * A helper function to handle displaying a single noun gender suggestion.
     * @param nounTypeSuggestion A list containing a single gender string.
     * @return `true` if a suggestion was displayed, `false` otherwise.
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
     * @param caseAnnotationSuggestion A list containing a single case annotation string.
     * @return `true` if a suggestion was displayed, `false` otherwise.
     */
    private fun handleSingleCaseSuggestion(caseAnnotationSuggestion: List<String>?): Boolean {
        if (caseAnnotationSuggestion?.size == 1) {
            val (colorRes, text) =
                handleTextForCaseAnnotation(
                    caseAnnotationSuggestion[0],
                    language,
                    applicationContext,
                )
            if (text != "" || colorRes != R.color.transparent) {
                handleSingleType(caseAnnotationSuggestion, "preposition")
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
     * @param nounTypeSuggestion The list of noun suggestions.
     * @param caseAnnotationSuggestion The list of case suggestions.
     * @return `true` if a fallback suggestion was applied, `false` otherwise.
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
     * Configures the UI to show a "PL" (Plural) suggestion.
     */
    private fun handlePluralAutoSuggest() {
        Log.d("PluralDebug", "Plural suggestions")
        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtnRight.visibility = View.INVISIBLE

        binding.translateBtn.apply {
            visibility = View.VISIBLE
            text = "PL"
            textSize = NOUN_TYPE_SIZE
            background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.annotateOrange)
            setTextColor(getColor(white))
            isClickable = false
            setOnClickListener(null)
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

    private fun handleWordSuggestions(
        hasLinguisticSuggestions: Boolean,
        wordSuggestions: List<String>? = null,
    ): Boolean {
        if (wordSuggestions.isNullOrEmpty()) {
            return false
        }
        val suggestions =
            listOfNotNull(
                wordSuggestions.getOrNull(0),
                wordSuggestions.getOrNull(1),
                wordSuggestions.getOrNull(2),
            )
        val suggestion1 = suggestions.getOrNull(0) ?: ""
        val suggestion2 = suggestions.getOrNull(1) ?: ""
        val suggestion3 = suggestions.getOrNull(2) ?: ""

        val emojiCount = autoSuggestEmojis?.size ?: 0
        setSuggestionButton(binding.conjugateBtn, suggestion1)
        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
                updateButtonVisibility(true)
            }

            hasLinguisticSuggestions && emojiCount == 0 -> {
                setSuggestionButton(binding.pluralBtn, suggestion2)
            }
            else -> {
                setSuggestionButton(binding.translateBtn, suggestion2)
                setSuggestionButton(binding.pluralBtn, suggestion3)
            }
        }
        return true
    }

    /**
     * Configures a single suggestion button with the appropriate text and color based on the suggestion type.
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

        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtnRight.visibility = View.INVISIBLE
        binding.translateBtn.textSize = NOUN_TYPE_SIZE

        binding.translateBtn.apply {
            visibility = View.VISIBLE
            text = buttonText
            isClickable = false
            setOnClickListener(null)

            if (colorRes != R.color.transparent) {
                background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
                backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
                setTextColor(getColor(white))
            } else {
                background = null
                val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.transparent)
                setTextColor(getColor(if (isUserDarkMode) white else md_grey_black_dark))
            }
        }
    }

    /**
     * Determines the left and right suggestion types to display for dual suggestions.
     * @param type The suggestion type ("noun" or "preposition").
     * @param suggestions The list of suggestion strings.
     * @return A pair of strings representing the left and right suggestion.
     */
    private fun getSuggestionTypes(
        type: String?,
        suggestions: List<String>?,
    ): Pair<String, String> =
        if (type == "noun" && isSingularAndPlural) {
            "PL" to (suggestions?.getOrNull(0) ?: "")
        } else {
            (suggestions?.getOrNull(0) ?: "") to (suggestions?.getOrNull(1) ?: "")
        }

    /**
     * Creates pairs of (color, text) for dual suggestion buttons.
     * @param type The suggestion type ("noun" or "preposition").
     * @param suggestions The list of suggestion strings.
     * @return A pair of pairs, each containing a color resource ID and a text string, or null on failure.
     */
    private fun getSuggestionPairs(
        type: String?,
        suggestions: List<String>?,
    ): Pair<Pair<Int, String>, Pair<Int, String>>? {
        val (leftType, rightType) = getSuggestionTypes(type, suggestions)
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
     * Applies a specific style to a suggestion button, including text, color, and a custom background.
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
        button.setTextColor(getColor(white))
        button.isClickable = false
        button.setOnClickListener(null)

        val background = ContextCompat.getDrawable(applicationContext, backgroundRes)?.mutate()

        if (background is RippleDrawable) {
            val contentDrawable = background.getDrawable(0)

            if (contentDrawable is LayerDrawable) {
                val shapeDrawable =
                    contentDrawable.findDrawableByLayerId(
                        R.id.button_background_shape,
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
     * Sets up the UI for two side-by-side suggestion buttons.
     * @param leftSuggestion A pair containing the color and text for the left button.
     * @param rightSuggestion A pair containing the color and text for the right button.
     */
    private fun setupDualSuggestionButtons(
        leftSuggestion: Pair<Int, String>,
        rightSuggestion: Pair<Int, String>,
    ) {
        binding.apply {
            translateBtnLeft.visibility = View.VISIBLE
            translateBtnRight.visibility = View.VISIBLE
            translateBtn.visibility = View.INVISIBLE

            applyInformativeSuggestionStyle(
                translateBtnLeft,
                leftSuggestion.first,
                leftSuggestion.second,
                R.drawable.gender_suggestion_button_left_background,
            )

            applyInformativeSuggestionStyle(
                translateBtnRight,
                rightSuggestion.first,
                rightSuggestion.second,
                R.drawable.gender_suggestion_button_right_background,
            )
        }
    }

    /**
     * Handles the logic when a word has multiple possible genders or
     * cases but only one suggestion slot is available.
     * It picks the first valid suggestion to display.
     * @param multipleTypeSuggestion The list of noun suggestions.
     */
    private fun handleFallbackOrSingleSuggestion(multipleTypeSuggestion: List<String>?) {
        val suggestionText = ""
        val validNouns =
            multipleTypeSuggestion?.filter {
                handleColorAndTextForNounType(
                    it,
                    language,
                    applicationContext,
                ).second != suggestionText
            }
        val validCases =
            caseAnnotationSuggestion?.filter {
                handleTextForCaseAnnotation(
                    it,
                    language,
                    applicationContext,
                ).second != suggestionText
            }
        if (!validNouns.isNullOrEmpty()) {
            handleSingleType(validNouns, "noun")
        } else if (!validCases.isNullOrEmpty()) {
            handleSingleType(validCases, "preposition")
        } else {
            disableAutoSuggest()
        }
    }

    /**
     * Handles the UI logic for displaying multiple suggestions simultaneously,
     * typically for words with multiple genders.
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
        setupDualSuggestionButtons(leftSuggestion, rightSuggestion)
    }

    /**
     * Disables all auto-suggestions and resets the suggestion buttons to their default, inactive state.
     */
    fun disableAutoSuggest() {
        binding.translateBtnRight.visibility = View.INVISIBLE
        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtn.visibility = View.VISIBLE

        // Don't change button text if we're in TRANSLATE or SELECT_COMMAND state.
        if (currentState != ScribeState.TRANSLATE && currentState != ScribeState.SELECT_COMMAND) {
            // A helper function to create the click listener.
            val createSuggestionClickListener = { suggestion: String ->
                View.OnClickListener {
                    currentInputConnection?.commitText("$suggestion ", 1)
                }
            }

            val suggestions = HintUtils.getBaseAutoSuggestions(language)

            val suggestion1 = suggestions.getOrNull(0) ?: ""
            binding.translateBtn.text = suggestion1
            binding.translateBtn.background = null
            binding.translateBtn.setOnClickListener(createSuggestionClickListener(suggestion1))

            val suggestion2 = suggestions.getOrNull(1) ?: ""
            binding.conjugateBtn.text = suggestion2
            binding.conjugateBtn.setOnClickListener(createSuggestionClickListener(suggestion2))

            val suggestion3 = suggestions.getOrNull(2) ?: ""
            binding.pluralBtn.text = suggestion3
            binding.pluralBtn.setOnClickListener(createSuggestionClickListener(suggestion3))
        }

        handleTextSizeForSuggestion(binding.translateBtn)
    }

    /**
     * Sets the text size and color for a default, non-active suggestion button.
     * @param button The button to style.
     */

    private fun handleTextSizeForSuggestion(button: Button) {
        button.textSize = SUGGESTION_SIZE
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        button.setTextColor(if (isUserDarkMode) getColor(white) else getColor(md_grey_black_dark))
    }

    /**
     * Retrieves the plural form of a word from the database.
     * @param word The singular word to find the plural for.
     * @return The plural form as a string, or null if not found.
     */
    private fun getPluralRepresentation(word: String?): String? {
        if (word.isNullOrEmpty()) return null
        val langAlias = getLanguageAlias(language)
        val lowercaseWord = word.lowercase()

        // Check if the word is already plural.
        val isAlreadyPlural = pluralWords?.contains(lowercaseWord) == true
        if (isAlreadyPlural) {
            return ALREADY_PLURAL_MSG
        }

        // If not plural, try to find the plural form in singular column.
        val pluralMap = dbManagers.pluralManager.getPluralRepresentation(langAlias, dataContract, word)
        val pluralResult = pluralMap.values.firstOrNull()
        return pluralResult
    }

    /**
     * Moves the cursor in the input field.
     * @param moveRight `true` to move right, `false` to move left.
     */
    private fun moveCursor(moveRight: Boolean) {
        val extractedText = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val newPos = extractedText.selectionStart + if (moveRight) 1 else -1
        currentInputConnection?.setSelection(newPos, newPos)
    }

    /**
     * Retrieves the translation for a given word.
     * @param language The current keyboard language (destination language).
     * @param commandBarInput The word to be translated (source word).
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
     * Gets the IME action ID (e.g., Go, Search, Done) from the current editor info.
     * @return The IME action ID, or `IME_ACTION_NONE`.
     */
    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }

    /**
     * Handles the logic for the Enter key press. This can either perform an editor action,
     * commit a newline, or execute a Scribe command depending on the current state.
     */
    fun handleKeycodeEnter() {
        val inputConnection = currentInputConnection ?: return

        // Handle states that should return to idle instead of performing Enter action.
        if (currentState == ScribeState.INVALID || currentState == ScribeState.ALREADY_PLURAL) {
            moveToIdleState()
            return
        }

        // Handle states that should perform normal Enter action.
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleDefaultEnter(inputConnection)
            return
        }

        val rawInput = getCommandBarTextWithoutCursor().trim().takeIf { it.isNotEmpty() }

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
     * Handles the Enter key press when in the `PLURAL` or `TRANSLATE` state.
     * @param rawInput The text from the command bar.
     * @param inputConnection The current input connection.
     */
    private fun handlePluralOrTranslateState(
        rawInput: String,
        inputConnection: InputConnection,
    ) {
        val commandModeOutput =
            when (currentState) {
                ScribeState.PLURAL -> {
                    when (val pluralResult = getPluralRepresentation(rawInput)) {
                        ALREADY_PLURAL_MSG -> {
                            currentState = ScribeState.ALREADY_PLURAL
                            updateUI()
                            return
                        }
                        null -> ""
                        else -> pluralResult
                    }
                }
                ScribeState.TRANSLATE -> getTranslation(language, rawInput)
                else -> ""
            }

        if (commandModeOutput.isEmpty()) {
            currentState = ScribeState.INVALID
            updateUI()
        } else {
            applyCommandOutput(commandModeOutput, inputConnection)
        }
    }

    /**
     * Handles the Enter key press when in the `CONJUGATE` state. It fetches the
     * conjugation data for the entered verb and transitions to the selection view.
     * @param rawInput The verb entered in the command bar.
     */
    private fun handleConjugateState(rawInput: String) {
        val languageAlias = getLanguageAlias(language)

        conjugateOutput =
            dbManagers.conjugateDataManager.getTheConjugateLabels(
                languageAlias,
                dataContract,
                rawInput.lowercase(),
            )

        conjugateLabels =
            dbManagers.conjugateDataManager.extractConjugateHeadings(
                dataContract,
                rawInput.lowercase(),
            )

        currentState =
            if (
                conjugateOutput.isEmpty() ||
                conjugateOutput.values.all { it.isEmpty() }
            ) {
                ScribeState.INVALID
            } else {
                saveConjugateModeType(language)
                ScribeState.SELECT_VERB_CONJUNCTION
            }

        updateUI()
    }

    /**
     * Handles the default behavior of the Enter key when not in a special Scribe command mode.
     * It performs the editor action or sends a standard Enter key event.
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
        binding.commandBar.setText("")
        moveToIdleState()
    }

    /**
     * Handles switching between the letter and symbol keyboards.
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
        keyboardView?.requestLayout()

        // Set up the currency symbol if we're using the symbols keyboard layout.
        if (keyboardXml == R.xml.keys_symbols) {
            setupCurrencySymbol()
        }
    }

    /**
     * Handles the logic for the Shift key. It cycles through shift states (off, on-for-one-char, caps lock)
     * on the letter keyboard, and toggles between symbol pages on the symbol keyboard.
     * @param keyboardMode The current keyboard mode.
     * @param keyboardView The instance of the keyboard view.
     */
    fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
    ) {
        if (keyboardMode == keyboardLetters) {
            when {
                keyboard!!.mShiftState == SHIFT_ON_PERMANENT -> {
                    keyboard!!.mShiftState = SHIFT_OFF
                }

                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> {
                    keyboard!!.mShiftState = SHIFT_ON_PERMANENT
                }

                keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR -> {
                    keyboard!!.mShiftState = SHIFT_OFF
                }

                keyboard!!.mShiftState == SHIFT_OFF -> {
                    keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
                }
            }
            lastShiftPressTS = System.currentTimeMillis()
        } else {
            val keyboardXml =
                if (keyboardMode == keyboardSymbols) {
                    this.keyboardMode = keyboardSymbolShift
                    R.xml.keys_symbols_shift
                } else {
                    this.keyboardMode = keyboardSymbols
                    R.xml.keys_symbols
                }
            keyboard = KeyboardBase(this, keyboardXml, enterKeyType)
            keyboardView!!.setKeyboard(keyboard!!)

            // Set up the currency symbol if we're using the symbols keyboard layout.
            if (keyboardXml == R.xml.keys_symbols) {
                setupCurrencySymbol()
            }
        }
    }

    /**
     * Handles the delete key press specifically for the command bar text field.
     */
    private fun handleCommandBarDelete() {
        val currentTextWithoutCursor = getCommandBarTextWithoutCursor()
        // If we're already showing the hint, do nothing on delete.
        if (currentTextWithoutCursor == currentCommandBarHint) {
            return
        }

        if (currentTextWithoutCursor.isNotEmpty()) {
            val newText = currentTextWithoutCursor.dropLast(1)
            if (newText.isEmpty()) {
                // All real text has been deleted, so restore the hint.
                setCommandBarTextWithCursor(currentCommandBarHint, cursorAtStart = true)
                binding.commandBar.setTextColor(commandBarHintColor)
            } else {
                // There's still text left, so just update it.
                setCommandBarTextWithCursor(newText)
            }
        }

        // Handle German plural mode shift state
        val finalCommandBarText = getCommandBarTextWithoutCursor()
        val isEmptyOrAHint = finalCommandBarText.isEmpty() || finalCommandBarText == currentCommandBarHint
        val isGerman = language == "German"
        val isPluralState = currentState == ScribeState.PLURAL

        if (isEmptyOrAHint && isGerman && isPluralState) {
            keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
        }
    }

    /**
     * Handles a key press on one of the special conjugation keys.
     * It either commits the text directly or prepares for a subsequent selection view.
     * @param code The key code of the pressed key.
     * @param isSubsequentRequired `true` if a sub-view is needed for more options.
     * @return The label of the key that was pressed.
     */
    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean,
    ): String? {
        val keyLabel = keyboardView?.getKeyLabel(code)
        if (!isSubsequentRequired) {
            currentInputConnection?.commitText(keyLabel, 1)
            suggestionHandler.processLinguisticSuggestions(keyLabel)
        }
        return keyLabel
    }

    /**
     * Handles the logic for the Delete/Backspace key. It deletes characters from either
     * the main input field or the command bar, depending on the context.
     * @param isCommandBar `true` if the deletion should happen in the command bar.
     * @param isLongPress `true` if this is a long press/repeat action, `false` for single tap.
     */
    fun handleDelete(
        isCommandBar: Boolean = false,
        isLongPress: Boolean = false,
    ) {
        if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR) keyboard!!.mShiftState = SHIFT_OFF
        if (isCommandBar) {
            handleCommandBarDelete()
        } else {
            val inputConnection = currentInputConnection ?: return
            if (TextUtils.isEmpty(inputConnection.getSelectedText(0))) {
                val isWordByWordEnabled = PreferencesHelper.getIsWordByWordDeletionEnabled(applicationContext, language)
                // Only use word-by-word deletion on long press when the feature is enabled.
                if (isWordByWordEnabled && isLongPress) {
                    deleteWordByWord(inputConnection)
                } else {
                    deleteSingleCharacter(inputConnection)
                }
            } else {
                inputConnection.commitText("", 1)
            }
            if (inputConnection.getTextBeforeCursor(1, 0)?.isEmpty() != false) {
                keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
                keyboardView!!.invalidateAllKeys()
            }
        }
    }

    /**
     * Deletes a single character.
     */
    private fun deleteSingleCharacter(inputConnection: InputConnection) {
        inputConnection.deleteSurroundingText(1, 0)
    }

    /**
     * Deletes an entire word, including any trailing whitespace.
     * @param inputConnection The current input connection.
     */
    private fun deleteWordByWord(inputConnection: InputConnection) {
        val textBeforeCursor = inputConnection.getTextBeforeCursor(MAX_TEXT_LENGTH, 0)?.toString() ?: ""

        if (textBeforeCursor.isEmpty()) {
            return
        }

        var deletionLength = 0
        var index = textBeforeCursor.length - 1

        // Skip any whitespace.
        while (index >= 0 && textBeforeCursor[index].isWhitespace()) {
            deletionLength++
            index--
        }

        // If we only had whitespace, delete it.
        if (index < 0) {
            if (deletionLength > 0) {
                inputConnection.deleteSurroundingText(deletionLength, 0)
            }
            return
        }

        // Now delete the word characters.
        if (isWordCharacter(textBeforeCursor[index])) {
            // Delete regular word characters (letters, numbers, some punctuation).
            while (index >= 0 && isWordCharacter(textBeforeCursor[index])) {
                deletionLength++
                index--
            }
        } else {
            // If the character at cursor is not a word character (e.g., special punctuation),
            // delete just that single character instead of trying to delete a whole word.
            deletionLength++
        }

        if (deletionLength > 0) {
            inputConnection.deleteSurroundingText(deletionLength, 0)
        }
    }

    /**
     * Determines if a character is considered part of a word for deletion purposes.
     */
    private fun isWordCharacter(char: Char): Boolean {
        // Letters and digits are always word characters.
        if (char.isLetterOrDigit()) {
            return true
        }

        // Check if special characters are considered word.
        return when (Character.getType(char).toByte()) {
            // Connector punctuation.
            Character.CONNECTOR_PUNCTUATION -> true
            Character.DASH_PUNCTUATION -> true
            Character.OTHER_PUNCTUATION -> {
                char in "'\".,@#$%&*+=~`|\\/:;?!^"
            }
            Character.CURRENCY_SYMBOL -> true
            Character.MATH_SYMBOL -> char in "+=<>~^"
            Character.OTHER_SYMBOL -> char in "@#$%&*+=~`|\\/:;?!^"
            else -> false
        }
    }

    /**
     * Handles the input of any non-special character key (e.g., letters, numbers, punctuation).
     * It commits the character to the main input field or the command bar.
     * @param code The character code of the key.
     * @param keyboardMode The current keyboard mode.
     * @param commandBarState `true` if input should go to the command bar.
     */
    fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
        commandBarState: Boolean = false,
    ) {
        if (commandBarState) {
            // Add character before the cursor in command bar
            val codeChar =
                if (Character.isLetter(code.toChar()) && keyboard!!.mShiftState > SHIFT_OFF) {
                    Character.toUpperCase(code.toChar())
                } else {
                    code.toChar()
                }
            val currentTextWithoutCursor = getCommandBarTextWithoutCursor()

            // Check if the command bar is showing the hint
            if (currentTextWithoutCursor == currentCommandBarHint) {
                // This is the first character typed. Overwrite the hint.
                binding.commandBar.setTextColor(commandBarTextColor)
                setCommandBarTextWithCursor(codeChar.toString())
            } else {
                // User is already typing, so just append the new character.
                val newText = currentTextWithoutCursor + codeChar
                setCommandBarTextWithCursor(newText)
            }
        } else {
            // Handle regular input to main text field (unchanged)
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

    /**
     * Gets the current text in the command bar without the cursor.
     * @return The text content without the trailing cursor character.
     */
    private fun getCommandBarTextWithoutCursor(): String {
        val currentText = binding.commandBar.text.toString()
        return when {
            currentText.startsWith(CUSTOM_CURSOR) -> currentText.drop(1)
            currentText.endsWith(CUSTOM_CURSOR) -> currentText.dropLast(1)
            else -> currentText
        }
    }

    /**
     * Sets the command bar text and ensures it ends with the custom cursor.
     * @param text The text to set (without cursor).
     */
    private fun setCommandBarTextWithCursor(
        text: String,
        cursorAtStart: Boolean = false,
    ) {
        val textWithCursor =
            if (cursorAtStart) {
                CUSTOM_CURSOR + text
            } else {
                text + CUSTOM_CURSOR
            }
        binding.commandBar.setText(textWithCursor)
        binding.commandBar.setSelection(textWithCursor.length)
    }

    internal companion object {
        const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500
        const val TEXT_LENGTH = 20
        const val NOUN_TYPE_SIZE = 20f
        const val SUGGESTION_SIZE = 15f
        const val DARK_THEME = "#aeb3be"
        const val LIGHT_THEME = "#4b4b4b"
        const val MAX_TEXT_LENGTH = 1000
        const val COMMIT_TEXT_CURSOR_POSITION = 1
        private const val COMMAND_BUTTON_SPACING_DP = 4
        private const val SEPARATOR_WIDTH = 0.5f
        private const val CUSTOM_CURSOR = "│" // Special tall cursor character
    }
}
