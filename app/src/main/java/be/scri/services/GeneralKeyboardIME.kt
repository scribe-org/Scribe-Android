// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
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
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import be.scri.R
import be.scri.R.color.md_grey_black_dark
import be.scri.R.color.white
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.databinding.KeyboardViewKeyboardBinding
import be.scri.helpers.DatabaseHelper
import be.scri.helpers.HintUtils
import be.scri.helpers.KeyboardBase
import be.scri.helpers.PERIOD_ON_DOUBLE_TAP
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.helpers.PreferencesHelper.getIsEmojiSuggestionsEnabled
import be.scri.helpers.PreferencesHelper.getPreferredTranslationLanguage
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portuguese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables
import be.scri.views.KeyboardView
import androidx.core.graphics.toColorInt

// based on https://www.androidauthority.com/lets-build-custom-keyboard-android-832362/

/**
 * The base keyboard input method (IME) imported into all language keyboards.
 */
@Suppress("TooManyFunctions", "LargeClass")
abstract class GeneralKeyboardIME(
    var language: String,
) : InputMethodService(),
    KeyboardView.OnKeyboardActionListener {
    /**
     * Returns the XML layout resource ID for the current keyboard layout.
     * Subclasses must implement this to provide the appropriate keyboard XML layout.
     *
     * @return The resource ID of the keyboard layout XML file.
     */
    abstract fun getKeyboardLayoutXML(): Int

    abstract val keyboardLetters: Int
    abstract val keyboardSymbols: Int
    abstract val keyboardSymbolShift: Int

    abstract var keyboard: KeyboardBase?
    abstract var keyboardView: KeyboardView?
    abstract var lastShiftPressTS: Long
    abstract var keyboardMode: Int
    abstract var inputTypeClass: Int
    abstract var enterKeyType: Int
    abstract var switchToLetters: Boolean
    abstract var hasTextBeforeCursor: Boolean
    abstract var binding: KeyboardViewCommandOptionsBinding

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
    private var isSingularAndPlural: Boolean = false

    private var SusequentAreaRequired: Boolean = false

    private var SusequentAreaKey: Int = 0

    private var SusequentAreaItems: Int = 0

    private var SubsequentData: MutableList<List<String>> = mutableListOf()

    // How quickly do we have to double-tap shift to enable permanent caps lock.
    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    private lateinit var dbHelper: DatabaseHelper
    var emojiKeywords: HashMap<String, MutableList<String>>? = null
    private lateinit var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>
    private lateinit var conjugateLabels: Set<String>
    private var emojiMaxKeywordLength: Int = 0
    private lateinit var nounKeywords: HashMap<String, List<String>>
    var pluralWords: List<String>? = null
    private lateinit var caseAnnotation: HashMap<String, MutableList<String>>
    var emojiAutoSuggestionEnabled: Boolean = false
    var lastWord: String? = null
    var autoSuggestEmojis: MutableList<String>? = null
    var caseAnnotationSuggestion: MutableList<String>? = null
    var nounTypeSuggestion: List<String>? = null
    var checkIfPluralWord: Boolean = false
    private var currentEnterKeyType: Int? = null
    private val commandCursor = "│"
    private val prepAnnotationConversionDict =
        mapOf(
            "German" to mapOf("Acc" to "Akk"),
            "Russian" to
                mapOf(
                    "Acc" to "Вин",
                    "Dat" to "Дат",
                    "Gen" to "Род",
                    "Loc" to "Мес",
                    "Pre" to "Пре",
                    "Ins" to "Инс",
                ),
        )

    private val nounAnnotationConversionDict =
        mapOf(
            "Swedish" to mapOf("C" to "U"),
            "Russian" to
                mapOf(
                    "F" to "Ж",
                    "M" to "М",
                    "N" to "Н",
                    "PL" to "МН",
                ),
        )

    private val translatePlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.TRANSLATE_KEY_LBL,
            "ES" to ESInterfaceVariables.TRANSLATE_KEY_LBL,
            "DE" to DEInterfaceVariables.TRANSLATE_KEY_LBL,
            "IT" to ITInterfaceVariables.TRANSLATE_KEY_LBL,
            "FR" to FRInterfaceVariables.TRANSLATE_KEY_LBL,
            "PT" to PTInterfaceVariables.TRANSLATE_KEY_LBL,
            "RU" to RUInterfaceVariables.TRANSLATE_KEY_LBL,
            "SV" to SVInterfaceVariables.TRANSLATE_KEY_LBL,
        )

    private val conjugatePlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.CONJUGATE_KEY_LBL,
            "ES" to ESInterfaceVariables.CONJUGATE_KEY_LBL,
            "DE" to DEInterfaceVariables.CONJUGATE_KEY_LBL,
            "IT" to ITInterfaceVariables.CONJUGATE_KEY_LBL,
            "FR" to FRInterfaceVariables.CONJUGATE_KEY_LBL,
            "PT" to PTInterfaceVariables.CONJUGATE_KEY_LBL,
            "RU" to RUInterfaceVariables.CONJUGATE_KEY_LBL,
            "SV" to SVInterfaceVariables.CONJUGATE_KEY_LBL,
        )

    private val pluralPlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.PLURAL_KEY_LBL,
            "ES" to ESInterfaceVariables.PLURAL_KEY_LBL,
            "DE" to DEInterfaceVariables.PLURAL_KEY_LBL,
            "IT" to ITInterfaceVariables.PLURAL_KEY_LBL,
            "FR" to FRInterfaceVariables.PLURAL_KEY_LBL,
            "PT" to PTInterfaceVariables.PLURAL_KEY_LBL,
            "RU" to RUInterfaceVariables.PLURAL_KEY_LBL,
            "SV" to SVInterfaceVariables.PLURAL_KEY_LBL,
        )

    internal var currentState: ScribeState = ScribeState.IDLE
    internal lateinit var keyboardBinding: KeyboardViewKeyboardBinding
    private var earlierValue: Int? = keyboardView?.setEnterKeyIcon(ScribeState.IDLE)

    enum class ScribeState {
        IDLE,
        SELECT_COMMAND,
        TRANSLATE,
        CONJUGATE,
        PLURAL,
        SELECT_VERB_CONJUNCTION,
        SELECT_CASE_DECLENSION,
        ALREADY_PLURAL,
        INVALID,
        DISPLAY_INFORMATION,
    }

    internal fun returnIsSubsequentRequired(): Boolean = SusequentAreaRequired

    internal fun returnSubsequentAreaKey(): Int = SusequentAreaKey

    internal fun returnSubsequentAreaItems(): Int = SusequentAreaItems

    internal fun returnSubsequentData(): List<List<String>> = SubsequentData

    /**
     * Called by the system when the service is first created. This is where you should
     * initialize your service. The service will only be created once, and this method
     * will only be called once.
     */
    override fun onCreate() {
        super.onCreate()
        keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString("conjugate_mode_type", "none")
        }
        setupCommandBarTheme(binding)
    }

    /**
     * Called when the input view is being finished.
     *
     * @param finishingInput Boolean indicating whether the input is finishing.
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        currentState = ScribeState.IDLE
        switchToCommandToolBar()
        updateUI()
    }

    /**
     * Called by the input method framework when the input method is first created.
     * This method is used to perform any one-time initialization tasks.
     * Override this method to set up any resources or configurations needed by the input method.
     */
    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
    }

    /**
     * Checks if there is any text before the cursor in the input field.
     *
     * @return `true` if there is text before the cursor, `false` otherwise.
     */
    override fun hasTextBeforeCursor(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val textBeforeCursor = inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0)?.trim() ?: ""
        return textBeforeCursor.isNotEmpty() && textBeforeCursor.lastOrNull() != '.'
    }

    /**
     * Called by the framework when the input view is being created.
     * This is where you can create and return the view hierarchy that will be used
     * as the input view for the IME.
     *
     * @return The view to be used as the input view for the IME.
     */
    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.invalidateAllKeys()
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        return keyboardHolder
    }

    /**
     * Called when a key is pressed.
     *
     * @param primaryCode The unicode of the key being pressed.
     *If the touch is not on a valid key, the value will be zero.
     */
    override fun onPress(primaryCode: Int) {
        if (primaryCode != 0) {
            keyboardView?.vibrateIfNeeded()
        }
    }

    /**
     * Called when the input method is starting input in a new editor.
     * This is where you can set up any state you need.
     *
     * @param attribute Information about the type of text being edited.
     * @param restarting If true, this is restarting input on the same text field.
     */
    override fun onStartInput(
        attribute: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)

        inputTypeClass = attribute!!.inputType and TYPE_MASK_CLASS
        enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)
        currentEnterKeyType = enterKeyType
        val inputConnection = currentInputConnection
        hasTextBeforeCursor = inputConnection?.getTextBeforeCursor(1, 0)?.isNotEmpty() == true

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
        dbHelper = DatabaseHelper(this)
        dbHelper.loadDatabase(languageAlias)
        emojiKeywords = dbHelper.getEmojiKeywords(languageAlias)
        emojiMaxKeywordLength = dbHelper.getEmojiMaxKeywordLength()
        pluralWords = dbHelper.checkIfWordIsPlural(languageAlias)!!
        nounKeywords = dbHelper.findGenderOfWord(languageAlias)
        caseAnnotation = dbHelper.findCaseAnnnotationForPreposition(languageAlias)
        conjugateOutput = dbHelper.getConjugateData(languageAlias, "describe")
        conjugateLabels = dbHelper.getConjugateLabels(languageAlias)
        keyboard = KeyboardBase(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)
    }

    /**
     * This method is called when a key is released.
     * It handles the actions to be performed on key release.
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
     * Moves the cursor one position to the left.
     * This method is typically used to handle user input for cursor navigation.
     */
    override fun moveCursorLeft() {
        moveCursor(false)
    }

    /**
     * Moves the cursor one position to the right.
     * This method is typically used to handle user input for cursor navigation.
     */
    override fun moveCursorRight() {
        moveCursor(true)
    }

    /**
     * Handles the input text when the user types on the keyboard.
     *
     * @param text The text input by the user.
     */
    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 0)
    }

    /**
     * Called when the input view is starting. This is where you can set up the input view
     * to be shown to the user, such as configuring the keyboard layout or initializing
     * any necessary resources.
     *
     * @param attribute The attributes of the input method editor (IME) that is starting.
     * @param restarting If true, this is a restart of the input view, not the initial start.
     */
    override fun onStartInputView(
        editorInfo: EditorInfo?,
        restarting: Boolean,
    ) {
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        updateEnterKeyColor(isUserDarkMode)
        initializeEmojiButtons()
        emojiAutoSuggestionEnabled = getIsEmojiSuggestionsEnabled(applicationContext, language)
        updateButtonVisibility(emojiAutoSuggestionEnabled)
        setupIdleView()
        super.onStartInputView(editorInfo, restarting)
        setupCommandBarTheme(binding)
    }

    /**
     * Sets up the theme for the toolbar in the keyboard view.
     *
     * @param binding The binding object for the keyboard view layout.
     */
    private fun setupToolBarTheme(binding: KeyboardViewKeyboardBinding) {
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        when (isUserDarkMode) {
            true -> {
                binding.commandField.setBackgroundColor("#1E1E1E".toColorInt())
            }
            else -> {
                binding.commandField.setBackgroundColor("#d2d4da".toColorInt())
            }
        }
    }

    /**
     * Commits a period after a space character.
     * This method is typically used to automatically insert a period
     * when the user types a space, enhancing typing efficiency.
     */
    override fun commitPeriodAfterSpace() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            if (getSharedPreferences("app_preferences", MODE_PRIVATE)
                    .getBoolean(
                        PreferencesHelper.getLanguageSpecificPreferenceKey(PERIOD_ON_DOUBLE_TAP, language),
                        true,
                    )
            ) {
                val inputConnection = currentInputConnection ?: return
                inputConnection.deleteSurroundingText(1, 0)
                inputConnection.commitText(". ", 1)
            } else {
                val inputConnection = currentInputConnection ?: return
                inputConnection.deleteSurroundingText(1, 0)
                inputConnection.commitText("  ", 1)
            }
        }
    }

    /**
     * Updates the color of the enter key based on the current theme mode.
     *
     * @param isDarkMode Optional parameter to specify if dark mode is enabled.
     *                   If null, the current system theme will be used to determine the color.
     */
    private fun updateEnterKeyColor(isDarkMode: Boolean? = null) {
        when (currentState) {
            ScribeState.IDLE -> {
                keyboardView?.setEnterKeyIcon(ScribeState.IDLE, earlierValue)
                keyboardView?.setEnterKeyColor(null, isDarkMode = isDarkMode)
            }
            ScribeState.SELECT_COMMAND -> {
                keyboardView?.setEnterKeyColor(null, isDarkMode = isDarkMode)
                earlierValue = keyboardView?.setEnterKeyIcon(ScribeState.SELECT_COMMAND)
            }
            else -> {
                keyboardView?.setEnterKeyColor(getColor(R.color.color_primary))
                keyboardView?.setEnterKeyIcon(ScribeState.PLURAL, earlierValue)
            }
        }
        if (isDarkMode == true) {
            val color = ContextCompat.getColorStateList(this, R.color.light_key_color)
            binding.scribeKey.foregroundTintList = color
        } else {
            val colorLight = ContextCompat.getColorStateList(this, R.color.light_key_text_color)
            binding.scribeKey.foregroundTintList = colorLight
        }
    }

    /**
     * Updates the command bar hint and prompt based on the current context or state.
     * This function is responsible for modifying the UI elements of the command bar
     * to provide appropriate hints and prompts to the user.
     */
    private fun updateCommandBarHintAndPrompt(
        isUserDarkMode: Boolean? = null,
        text: String? = null,
        word: String? = null,
    ) {
        val commandBarButton = keyboardBinding.commandBar
        val hintMessage = HintUtils.getCommandBarHint(currentState, language, word)
        val promptText = HintUtils.getPromptText(currentState, language, context = this, text)
        val promptTextView = keyboardBinding.promptText
        promptTextView.text = promptText
        commandBarButton.hint = hintMessage

        if (isUserDarkMode == true) {
            commandBarButton.setHintTextColor(getColor(R.color.hint_white))
            commandBarButton.setTextColor(getColor(white))
            commandBarButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.command_bar_color_dark)
            promptTextView.setTextColor(getColor(white))

            promptTextView.setBackgroundColor(getColor(R.color.command_bar_color_dark))
            keyboardBinding.promptTextBorder.setBackgroundColor(getColor(R.color.command_bar_color_dark))
        } else {
            commandBarButton.setHintTextColor(getColor(R.color.hint_black))
            commandBarButton.setTextColor(Color.BLACK)
            commandBarButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            promptTextView.setTextColor(Color.BLACK)
            promptTextView.setBackgroundColor(getColor(white))
            keyboardBinding.promptTextBorder.setBackgroundColor(getColor(white))
        }
        Log.d(
            "KeyboardUpdate",
            "CommandBar Hint Updated: [State: $currentState, Language: $language, Hint: $hintMessage]",
        )
    }

    /**
     * Switches the current input method to the command toolbar.
     * This function is protected and can be accessed within the same class or subclasses.
     */
    internal fun switchToCommandToolBar() {
        val binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        this.binding = binding
        val keyboardHolder = binding.root
        setupCommandBarTheme(binding)
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.mOnKeyboardActionListener = this
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            setupSelectCommandView()
            updateUI()
        }
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        setInputView(keyboardHolder)
    }

    /**
     * Updates the user interface of the keyboard.
     * This function is responsible for refreshing or modifying the UI elements
     * of the keyboard based on the current state or input.
     */
    internal fun updateUI() {
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        when (currentState) {
            ScribeState.IDLE -> {
                setupIdleView()
                handleTextSizeForSuggestion(binding)
                initializeEmojiButtons()
                keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
                keyboardView!!.setKeyboard(keyboard!!)
                updateButtonVisibility(emojiAutoSuggestionEnabled)
                updateButtonText(emojiAutoSuggestionEnabled, autoSuggestEmojis)
            }
            ScribeState.SELECT_COMMAND -> {
                binding.translateBtn.textSize = SUGGESTION_SIZE
                setupSelectCommandView()
            }
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                switchToToolBar()
            }
            else -> switchToToolBar()
        }
        updateEnterKeyColor(isUserDarkMode)
    }

    /**
     * Switches the input method to the toolbar.
     * This function is responsible for changing the current input method
     * to have toolbar interface, allowing the user to interact with the toolbar.
     */
    internal fun switchToToolBar(isSubsequentArea: Boolean = false , dataSize: Int = 0 ) {
        keyboardBinding = initializeKeyboardBinding()
        val keyboardHolder = keyboardBinding.root

        applyToolBarVisualSettings()
        handleModeChange(keyboardSymbols, keyboardView, this)

        val keyboardXmlId = getKeyboardLayoutForState(currentState , isSubsequentArea , dataSize)
        initializeKeyboard(keyboardXmlId)

        setupScribeKeyListener()
        val conjugateIndex = getValidatedConjugateIndex()
        Log.i("MY-TAG", "I am outside the 2x2 and the conjugate output is $conjugateOutput")


        setupConjugateKeysByLanguage(conjugateIndex)

        setInputView(keyboardHolder)
    }

    private fun applyToolBarVisualSettings() {
        setupToolBarTheme(keyboardBinding)
        handleTextSizeForSuggestion(binding)
        binding.translateBtn.textSize = SUGGESTION_SIZE
        val isDarkMode = getIsDarkModeOrNot(applicationContext)
        updateToolBarTheme(isDarkMode)
    }

    private fun getKeyboardLayoutForState(state: ScribeState, isSubsequentArea: Boolean = false , dataSize: Int = 0 ): Int =
        when (state) {
            ScribeState.TRANSLATE -> {
                val language = getPreferredTranslationLanguage(this, language)
                baseKeyboardOfAnyLanguage(language)
            }
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                saveConjugateModeType(language, context = applicationContext)
                if (!isSubsequentArea && dataSize == 0 ) {
                    when (language) {
                        "English" -> R.xml.conjugate_view_2x2
                        "Swedish" -> R.xml.conjugate_view_2x2
                        "Russian" -> R.xml.conjugate_view_2x2
                        else -> R.xml.conjugate_view_3x2
                    }
                }
                else {
                    Log.i("CONJUGATE-ISSUE","The data size is $dataSize")
                    when (dataSize) {
                        2 -> R.xml.conjugate_view_2x1
                        3 -> R.xml.conjugate_view_1x3
                        else -> R.xml.conjugate_view_2x2
                    }
                }
            }
            else -> getKeyboardLayoutXML()
        }

    private fun initializeKeyboard(xmlId: Int) {
        keyboard = KeyboardBase(this, xmlId, enterKeyType)
        keyboardView =
            keyboardBinding.keyboardView.apply {
                setKeyboard(keyboard!!)
                mOnKeyboardActionListener = this@GeneralKeyboardIME
            }
    }

    private fun setupScribeKeyListener() {
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            handleTextSizeForSuggestion(binding)
            updateUI()
        }
    }

    private fun getValidatedConjugateIndex(): Int {
        val prefs = getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        var index = prefs.getInt("conjugate_index", 0)
        val maxIndex = conjugateOutput.keys.count() - 2
        index = if (maxIndex >= 0) {
            index.coerceIn(0, maxIndex + 1)
        } else {
            0
        }
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }


    internal fun setupConjugateKeysByLanguage(conjugateIndex: Int,isSubsequentArea: Boolean = false) {
        val isDarkMode = getIsDarkModeOrNot(applicationContext)


        setUpConjugateKeys(
            startIndex = conjugateIndex,
            conjugateOutput = conjugateOutput,
            isDarkMode = isDarkMode,
            isSubsequentArea
        )
    }

    private fun setUpConjugateKeys(
        startIndex: Int,
        conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>,
        isDarkMode: Boolean,
        isSubsequentArea: Boolean,
    ) {
        val keyCodeMap =
            mapOf(
                "3x2" to
                    listOf(
                        KeyboardBase.CODE_FPS,
                        KeyboardBase.CODE_FPP,
                        KeyboardBase.CODE_SPS,
                        KeyboardBase.CODE_SPP,
                        KeyboardBase.CODE_TPS,
                        KeyboardBase.CODE_TPP,
                    ),
                "1x1" to
                    listOf(
                        KeyboardBase.CODE_1X1,
                    ),
                "1x3" to
                    listOf(
                        KeyboardBase.CODE_1X3_LEFT,
                        KeyboardBase.CODE_1X3_CENTER,
                        KeyboardBase.CODE_1X3_RIGHT,
                    ),
                "2x1" to
                    listOf(
                        KeyboardBase.CODE_2X1_TOP,
                        KeyboardBase.CODE_2X1_BOTTOM,
                    ),
                "2x2" to
                    listOf(
                        KeyboardBase.CODE_TL,
                        KeyboardBase.CODE_TR,
                        KeyboardBase.CODE_BL,
                        KeyboardBase.CODE_BR,
                    ),
            )

        val jsonData = conjugateOutput
        val title = jsonData.keys.elementAtOrNull(startIndex)
        Log.i("CONJUGATE-ISSUE","The language would be $language")
        if (language != "English") {
            keyboardView?.setKeyLabel(jsonData[jsonData.keys.elementAt(startIndex)]?.get(jsonData.keys.elementAt(startIndex))?.elementAt(0)!!, "HI", KeyboardBase.CODE_FPS)
            keyboardView?.setKeyLabel(jsonData[jsonData.keys.elementAt(startIndex)]?.get(jsonData.keys.elementAt(startIndex))?.elementAt(1)!!, "HI", KeyboardBase.CODE_FPP)
            keyboardView?.setKeyLabel(jsonData[jsonData.keys.elementAt(startIndex)]?.get(jsonData.keys.elementAt(startIndex))?.elementAt(2)!!, "HI", KeyboardBase.CODE_SPS)
            keyboardView?.setKeyLabel(jsonData[jsonData.keys.elementAt(startIndex)]?.get(jsonData.keys.elementAt(startIndex))?.elementAt(3)!!, "HI", KeyboardBase.CODE_SPP)
            keyboardView?.setKeyLabel(jsonData[jsonData.keys.elementAt(startIndex)]?.get(jsonData.keys.elementAt(startIndex))?.elementAt(4)!!, "HI", KeyboardBase.CODE_TPS)
            keyboardView?.setKeyLabel(jsonData[jsonData.keys.elementAt(startIndex)]?.get(jsonData.keys.elementAt(startIndex))?.elementAt(5)!!, "HI", KeyboardBase.CODE_TPP)
        } else {
            Log.i("CONJUGATE-ISSUE","The language would be ${jsonData[jsonData.keys.elementAt(startIndex)]}")
            val keys = jsonData[jsonData.keys.elementAt(startIndex)]?.keys
            val output = jsonData[jsonData.keys.elementAt(startIndex)]?.get(keys?.elementAt(0))
            val output2 = jsonData[jsonData.keys.elementAt(startIndex)]?.get(keys?.elementAt(1))
            val output3 = jsonData[jsonData.keys.elementAt(startIndex)]?.get(keys?.elementAt(2))
            val output4 = jsonData[jsonData.keys.elementAt(startIndex)]?.get(keys?.elementAt(3))
            if (!isSubsequentArea) {
                keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_FPS)
                keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_FPP)
                keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_SPS)
                keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_SPP)
                keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_TPS)
                keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_TPP)
            }
            if (output2?.size!! > 1) {
                Log.i("CONJUGATE-ISSUE", "The output2 size is greater than 2 ")
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = true
                SubsequentData.add(output2.toList())
                sharedPreferences.edit() {
                    val myName = "CODE_TR"
                    putString("1", myName)
                }
            }
            else {
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = false
                sharedPreferences.edit() {
                    val myName = "CODE_TR"
                    putString("0", myName)
                }
            }
            if (output3?.size!! > 1) {
                Log.i("CONJUGATE-ISSUE", "The output3 size is greater than 2 ")
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = true
                SubsequentData.add(output3.toList())
                sharedPreferences.edit() {
                    val myName = "CODE_BL"
                    putString("1", myName)
                }
            }
            else {
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = false
                sharedPreferences.edit() {
                    val myName = "CODE_TR"
                    putString("0", myName)
                }
            }
            if (output4?.size!! > 1) {
                Log.i("CONJUGATE-ISSUE", "The output4 size is greater than 2 ")
                SusequentAreaRequired = true
                SubsequentData.add(output4.toList())
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                sharedPreferences.edit() {
                    val myName = "CODE_BR"
                    putString("1", myName)
                }
            }
            else {
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = false
                sharedPreferences.edit() {
                    val myName = "CODE_TR"
                    putString("0", myName)
                }
            }
            if (output?.size!! > 1) {
                Log.i("CONJUGATE-ISSUE", "The output size is greater than 2 ")
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = true
                SubsequentData.add(output.toList())
                sharedPreferences.edit() {
                    val myName = "CODE_TL"
                    putString("1", myName)
                }
            }
            else {
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                SusequentAreaRequired = false
                sharedPreferences.edit() {
                    val myName = "CODE_TR"
                    putString("0", myName)
                }
            }
            keyboardView?.setKeyLabel(output.elementAt(0).toString(), "HI", KeyboardBase.CODE_TL)
            keyboardView?.setKeyLabel(output2.elementAt(0).toString(), "HI", KeyboardBase.CODE_TR)
            keyboardView?.setKeyLabel(output3.elementAt(0).toString(), "HI", KeyboardBase.CODE_BL)
            keyboardView?.setKeyLabel(output4.elementAt(0).toString(), "HI", KeyboardBase.CODE_BR)
            Log.i("CONJUGATE-ISSUE","The keys are $keys")
            Log.i("CONJUGATE-ISSUE","The outputs are $output $output2 $output3 $output4")
        }

        if (isSubsequentArea) {
            keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_FPS)
        }

        updateCommandBarHintAndPrompt(
            text = title,
            isUserDarkMode = isDarkMode,
            word = "hi",
        )

    }


    internal fun setupConjugateSubView(
        data: List<List<String>>,
        word: String?
    ) {
        val uniqueData = data.distinct()
        val filteredData = uniqueData.filter { sublist -> sublist.contains(word) }
        val flattenList = filteredData.flatten()

        Log.i("CONJUGATE-ISSUE", "The length of the data would be ${uniqueData.size}")
        Log.i("CONJUGATE-ISSUE", "the data is $uniqueData")
        Log.i("CONJUGATE-ISSUE", "the filtered data is $filteredData")
        Log.i("CONJUGATE-ISSUE","tHE FLATTEN LIST IS $flattenList")
        Log.i("CONJUGATE-ISSUE","the length of the flatten list is ${flattenList.size}")
        Log.i("CONJUGATE-ISSUE","The length of the data would be ${data.size}")
        Log.i("CONJUGATE-ISSUE","The length of the unique data would be ${uniqueData.size}")
        Log.i("CONJUGATE-ISSUE","The length of the filtered data would be ${filteredData.size}")
        saveConjugateModeType(language = language, true,applicationContext)
        val prefs = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
//        when (flattenList.size) {
//            2 -> {
//                val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
//                sharedPref.edit {
//                    putString("conjugate_mode_type", "2x1")
//                }
//            }
//            3 -> {
//                val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
//                sharedPref.edit {
//                    putString("conjugate_mode_type", "3x1")
//                }
//            }
//        }
        switchToToolBar(true , flattenList.size)
        Log.i("CONJUGATE-ISSUE", "SharedPref value = ${prefs.getString("conjugate_mode_type", "3x1")}")
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        when (flattenList.size) {
            2 -> {
                keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_2X1_TOP)
                keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_2X1_BOTTOM)

                SusequentAreaRequired = false
            }
            3 -> {
                keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_1X3_RIGHT)
                keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_1X3_CENTER)
                keyboardView?.setKeyLabel(flattenList[2], "HI", KeyboardBase.CODE_1X3_RIGHT)
                SusequentAreaRequired = false
            }
        }
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        Log.i("CONJUGATE-ISSUE", "SharedPref value = ${prefs.getString("conjugate_mode_type", "3x1")}")


    }


    /**
     * Updates the toolbar theme based on the current system theme (dark or light).
     *
     * This function adjusts the toolbar's visual elements such as the top divider color
     * and the tint of the custom scribe key, depending on whether dark mode is enabled.
     *
     * @param isDarkMode A boolean indicating if the system is in dark mode.
     */
    private fun updateToolBarTheme(isDarkMode: Boolean) {
        val dividerColor = if (isDarkMode) R.color.special_key_dark else R.color.special_key_light
        keyboardBinding.topKeyboardDivider.setBackgroundColor(getColor(dividerColor))

        val tintColor =
            if (isDarkMode) {
                ContextCompat.getColorStateList(this, R.color.light_key_color)
            } else {
                ContextCompat.getColorStateList(this, R.color.light_key_text_color)
            }

        keyboardBinding.scribeKey.foregroundTintList = tintColor
    }

    /**
     * Sets up the idle view for the keyboard input method editor (IME).
     * This function initializes and configures the view that is displayed
     * when the keyboard is in an idle state.
     */
    private fun setupIdleView() {
        binding.translateBtn.textSize = SUGGESTION_SIZE
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)

        // Set common properties for buttons.
        val textColor = if (isUserDarkMode) Color.WHITE else Color.parseColor("#1E1E1E")
        val separatorColor = Color.parseColor(if (isUserDarkMode) DARK_THEME else LIGHT_THEME)

        // Apply to all buttons.
        listOf(binding.translateBtn, binding.conjugateBtn, binding.pluralBtn).forEach { button ->
            button.setBackgroundColor(getColor(R.color.transparent))
            button.setTextColor(textColor)
            button.text = getString(R.string.suggestion)
        }

        // Apply to all separators.
        listOf(
            binding.separator2,
            binding.separator3,
            binding.separator4,
            binding.separator5,
            binding.separator6,
        ).forEach { separator ->
            separator.setBackgroundColor(separatorColor)
        }

        // Set visibility.
        binding.separator2.visibility = View.VISIBLE
        binding.separator3.visibility = View.VISIBLE

        val isTablet =
            (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE

        binding.separator4.visibility = if (isTablet) View.GONE else View.VISIBLE
        binding.separator5.visibility = if (isTablet) View.VISIBLE else View.GONE
        binding.separator6.visibility = if (isTablet) View.VISIBLE else View.GONE

        setupCommandBarTheme(binding)

        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.SELECT_COMMAND
            disableAutoSuggest()
            updateButtonVisibility(false)
            Log.i("MY-TAG", "SELECT COMMAND STATE")
            binding.scribeKey.foreground = AppCompatResources.getDrawable(this, R.drawable.close)
            updateUI()
            val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
            sharedPref.edit {
                putString("conjugate_mode_type", "none")
            }
            binding.translateBtn.setTextColor(if (isUserDarkMode) Color.WHITE else Color.BLACK)
        }
    }

    /**
     * Sets up the command view for the keyboard input method editor (IME).
     * This function initializes and configures the view that is displayed
     * when the keyboard is in command state. The command state is the state in
     * which the keyboard shows the different command available for the keyboard.
     */
    private fun setupSelectCommandView() {
        binding.translateBtn.background = AppCompatResources.getDrawable(this, R.drawable.button_background_rounded)
        binding.conjugateBtn.background = AppCompatResources.getDrawable(this, R.drawable.button_background_rounded)
        binding.pluralBtn.background = AppCompatResources.getDrawable(this, R.drawable.button_background_rounded)
        getLanguageAlias(language)
        binding.translateBtn.text = translatePlaceholder[getLanguageAlias(language)] ?: "Translate"
        binding.conjugateBtn.text = conjugatePlaceholder[getLanguageAlias(language)] ?: "Conjugate"
        binding.pluralBtn.text = pluralPlaceholder[getLanguageAlias(language)] ?: "Plural"
        binding.separator2.visibility = View.GONE
        binding.separator3.visibility = View.GONE
        binding.separator4.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
        setupCommandBarTheme(binding)
        binding.translateBtn.setTextColor(Color.BLACK)
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            Log.i("MY-TAG", "IDLE STATE")
            binding.translateBtn.setTextColor(Color.WHITE)
            disableAutoSuggest()
            saveConjugateModeType("none", context = applicationContext)
            binding.scribeKey.foreground = AppCompatResources.getDrawable(this, R.drawable.ic_scribe_icon_vector)
            updateUI()
        }
        binding.translateBtn.setOnClickListener {
            Log.i("MY-TAG", "TRANSLATE STATE")
            keyboardView?.invalidateAllKeys()
            updateCommandBarHintAndPrompt()
            saveConjugateModeType("none", context = applicationContext)
            currentState = ScribeState.TRANSLATE

            updateUI()
        }
        binding.conjugateBtn.setOnClickListener {
            Log.i("MY-TAG", "CONJUGATE STATE")
            updateCommandBarHintAndPrompt()
            currentState = ScribeState.CONJUGATE
            updateUI()
        }
        binding.pluralBtn.setOnClickListener {
            Log.i("MY-TAG", "PLURAL STATE")
            updateCommandBarHintAndPrompt()
            currentState = ScribeState.PLURAL
            updateUI()
            saveConjugateModeType("none", context = applicationContext)
            if (language == "German") {
                keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
            }
        }
    }

    /**
     * Saves the conjugate mode type to the shared preferences.
     *
     * This function stores the given conjugate mode type in the shared preferences
     * under the key "conjugate_mode_type". It uses asynchronous saving via `apply()`.
     * This ensures the mode type is stored persistently and can be retrieved later
     * across app sessions.
     *
     * @param mode The conjugate mode type to be saved, represented as a string.
     *              This can be a mode like "none", "3x2", or any other mode type.
     */
    internal fun saveConjugateModeType(language: String, isSubsequentArea: Boolean = false, context: Context) {
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        val mode = if (!isSubsequentArea) {
            when (language) {
                "Swedish", "English" -> "2x2"
                "German", "French", "Russian", "Italian", "Spanish", "Portuguese" -> "2x2"
                else -> "none"
            }
        } else {
            "2x1"
        }
        sharedPref.edit {
            putString("conjugate_mode_type", mode)
        }
    }


    /**
     * Sets up the theme for the command bar in the keyboard view.
     *
     * @param binding The binding object for the keyboard view command options.
     */
    fun setupCommandBarTheme(binding: KeyboardViewCommandOptionsBinding) {
        val isUserDarkMode = getIsDarkModeOrNot(context = applicationContext)
        when (isUserDarkMode) {
            true -> {
                binding.commandField.setBackgroundColor("#1E1E1E".toColorInt())
                binding.translateBtn.setTextColor(getColor(white))
            }
            else -> {
                binding.commandField.setBackgroundColor("#d2d4da".toColorInt())
                binding.translateBtn.setTextColor(getColor(md_grey_black_dark))
            }
        }
    }

    /**
     * Initializes and returns the binding for the keyboard view.
     *
     * @return The binding for the keyboard view.
     */
    private fun initializeKeyboardBinding(): KeyboardViewKeyboardBinding {
        val keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
        return keyboardBinding
    }

    /**
     * Initializes the emoji buttons on the keyboard.
     * This method sets up the necessary configurations and listeners
     * for the emoji buttons to function correctly.
     */
    fun initializeEmojiButtons() {
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
     * Updates the visibility of the button based on whether auto-suggest is enabled.
     *
     * @param isAutoSuggestEnabled A boolean indicating if auto-suggest is enabled.
     */
    fun updateButtonVisibility(isAutoSuggestEnabled: Boolean) {
        val isTablet =
            (
                resources.configuration.screenLayout and
                    Configuration.SCREENLAYOUT_SIZE_MASK
            ) >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            pluralBtn?.visibility = if (isAutoSuggestEnabled) View.INVISIBLE else View.VISIBLE
            emojiBtnTablet1?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
            emojiSpaceTablet1?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
            emojiBtnTablet2?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
            emojiSpaceTablet2?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
            emojiBtnTablet3?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
        } else {
            pluralBtn?.visibility = if (isAutoSuggestEnabled) View.INVISIBLE else View.VISIBLE
            emojiBtnPhone1?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
            emojiSpacePhone?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
            emojiBtnPhone2?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
        }
    }

    /**
     * Retrieves the text from the input method editor (IME).
     *
     * @return A string containing the text from the IME, or null if no text is available.
     */
    fun getText(): String? {
        val inputConnection = currentInputConnection ?: return null
        return inputConnection.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()
    }

    /**
     * Retrieves the last word before the cursor in the current input field.
     *
     * @return The last word before the cursor, or null if there is no word.
     */
    fun getLastWordBeforeCursor(): String? {
        val textBeforeCursor = getText() ?: return null
        val trimmedText = textBeforeCursor.trim()
        val lastWord = trimmedText.split("\\s+".toRegex()).lastOrNull()
        return lastWord
    }

    /**
     * Finds and returns a list of emojis that are relevant to the last word typed.
     *
     * @return A list of emojis that match the last word.
     */
    fun findEmojisForLastWord(
        emojiKeywords: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ): MutableList<String>? {
        lastWord?.let { word ->
            val lowerCaseWord = word.lowercase()
            val emojis = emojiKeywords[lowerCaseWord]
            if (emojis != null) {
                Log.d("Debug", "Emojis for '$word': $emojis")
                return emojis
            } else {
                Log.d("Debug", "No emojis found for '$word'")
            }
        }
        return null
    }

    /**
     * Finds the gender for the last word typed.
     *
     * @return The gender associated with the last word, if any.
     */
    fun findGenderForLastWord(
        nounKeywords: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? {
        lastWord?.let { word ->
            val lowerCaseWord = word.lowercase()
            Log.i("MY-TAG", word)
            Log.i("MY-TAG", nounKeywords.keys.toString())
            Log.i("MY-TAG", nounKeywords[word].toString())
            val gender = nounKeywords[lowerCaseWord]
            if (gender != null) {
                if (pluralWords?.any { it.equals(lastWord, ignoreCase = true) } == true) {
                    Log.i("MY-TAG", "Plural Words : $pluralWords")
                    isSingularAndPlural = true
                    Log.i("MY-TAG", "isSingularPlural Updated to true")
                } else {
                    isSingularAndPlural = false
                    Log.i("MY-TAG", "Plural Words : $pluralWords")
                    Log.i("MY-TAG", "isSingularPlural Updated to false")
                }
                return gender
            } else {
                Log.d("Debug", "No gender found for '$word'")
            }
        }
        return null
    }

    /**
     * Determines whether a given word is plural.
     *
     * @param word The word to be checked.
     * @return `true` if the word is plural, `false` otherwise.
     */
    fun findWhetherWordIsPlural(
        pluralWords: List<String>,
        lastWord: String?,
    ): Boolean {
        for (item in pluralWords) {
            if (item == lastWord) {
                return true
            }
        }
        return false
    }

    /**
     * Retrieves the case annotation for a given preposition.
     *
     * @param preposition The preposition for which the case annotation is to be retrieved.
     * @return The case annotation associated with the specified preposition.
     */
    fun getCaseAnnotationForPreposition(
        caseAnnotation: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ): MutableList<String>? {
        lastWord?.let { word ->
            val lowerCaseWord = word.lowercase()
            val caseAnnotations = caseAnnotation[lowerCaseWord]
            return caseAnnotations
        }
        return null
    }

    /**
     * Updates the text displayed on a button.
     *
     * @param buttonId The ID of the button whose text needs to be updated.
     * @param newText The new text to be displayed on the button.
     */
    fun updateButtonText(
        isAutoSuggestEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        if (isAutoSuggestEnabled) {
            emojiBtnTablet1?.text = autoSuggestEmojis?.get(0)
            emojiBtnTablet2?.text = autoSuggestEmojis?.get(1)
            emojiBtnTablet3?.text = autoSuggestEmojis?.get(2)

            emojiBtnPhone1?.text = autoSuggestEmojis?.get(0)
            emojiBtnPhone2?.text = autoSuggestEmojis?.get(1)

            binding.emojiBtnTablet1.setOnClickListener { insertEmoji(emojiBtnTablet1?.text.toString()) }
            binding.emojiBtnTablet2.setOnClickListener { insertEmoji(emojiBtnTablet2?.text.toString()) }
            binding.emojiBtnTablet3.setOnClickListener { insertEmoji(emojiBtnTablet3?.text.toString()) }

            binding.emojiBtnPhone1.setOnClickListener { insertEmoji(emojiBtnPhone1?.text.toString()) }
            binding.emojiBtnPhone2.setOnClickListener { insertEmoji(emojiBtnPhone2?.text.toString()) }
        }
    }

    /**
     * Updates the first auto suggestion button based on the current input.
     *
     * This function is responsible for generating and displaying
     * suggestions as the user types. It takes into account the
     * current context and input to provide relevant suggestions.It shows wheather
     * the word is plural or the gender of the word.
     *
     * @param inputText The current text input by the user.
     * @param cursorPosition The position of the cursor within the input text.
     */
    fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
    ) {
        if (isPlural) {
            handlePluralAutoSuggest()
        } else {
            Log.i("MY-TAG", "These are the case annotations $caseAnnotationSuggestion")
            nounTypeSuggestion?.size?.let {
                if (it > 1 || isSingularAndPlural) {
                    handleMultipleNounFormats(nounTypeSuggestion, "noun")
                } else {
                    handleSingleType(nounTypeSuggestion, "noun")
                }
            }
            caseAnnotationSuggestion?.size?.let {
                if (it > 1) {
                    handleMultipleNounFormats(caseAnnotationSuggestion, "preposition")
                } else {
                    handleSingleType(caseAnnotationSuggestion, "preposition")
                }
            }
        }
    }

    /**
     * Handles the auto-suggestion of plural forms for words.
     * This function is responsible for providing suggestions for pluralizing words
     * based on the current context and user input.
     */
    private fun handlePluralAutoSuggest() {
        var(colorRes, text) = handleColorAndTextForNounType(nounType = "PL")
        text = "PL"
        colorRes = R.color.annotateOrange
        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtnRight.visibility = View.INVISIBLE
        handleTextSize(binding)
        binding.translateBtn.apply {
            visibility = View.VISIBLE
            binding.translateBtn.text = text
            background =
                ContextCompat.getDrawable(context, R.drawable.rounded_drawable)?.apply {
                    setTintMode(PorterDuff.Mode.SRC_IN)
                    setTint(ContextCompat.getColor(context, colorRes))
                }
        }
    }

    /**
     * Handles a single type event.
     *
     * This function processes a single type event, performing necessary actions based on the input.
     *
     * @param input The input data to be processed.
     * @return The result of processing the input.
     */
    private fun handleSingleType(
        singleTypeSuggestion: List<String>?,
        type: String? = null,
    ) {
        Log.i("MY-TAG", "Single suggestion activated $singleTypeSuggestion")
        val text = singleTypeSuggestion?.get(0).toString()
        var (colorRes, buttonText) = Pair(R.color.transparent, "Suggestion")
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        var textColor = md_grey_black_dark
        if (isUserDarkMode) {
            colorRes = white
            textColor = md_grey_black_dark
        } else {
            colorRes = md_grey_black_dark
            textColor = white
        }
        when (type) {
            "noun" -> {
                val (newColorRes, newButtonText) = handleColorAndTextForNounType(text)
                colorRes = newColorRes
                buttonText = newButtonText
            }
            "preposition" -> {
                val (_, newButtonText) = handleTextForCaseAnnotation(text)
                buttonText = newButtonText
            }
            else -> {
                val (newColorRes, newButtonText) = Pair(R.color.transparent, "Suggestion")
                colorRes = newColorRes
                buttonText = newButtonText
            }
        }
        Log.i("MY-TAG", "These are the colorRes and text $colorRes and $buttonText")
        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtnRight.visibility = View.INVISIBLE
        binding.translateBtn.setTextColor(getColor(textColor))
        handleTextSize(binding)
        binding.translateBtn.apply {
            visibility = View.VISIBLE
            binding.translateBtn.text = buttonText
            setTextColor(getColor(textColor))
            background =
                ContextCompat.getDrawable(context, R.drawable.rounded_drawable)?.apply {
                    setTintMode(PorterDuff.Mode.SRC_IN)
                    setTint(ContextCompat.getColor(context, colorRes))
                }
        }
    }

    /**
     * Adjusts the text size of the keyboard view based on the provided binding.
     *
     * @param binding The binding object for the keyboard view command options.
     */
    private fun handleTextSize(binding: KeyboardViewCommandOptionsBinding) {
        binding.translateBtn.textSize = NOUN_TYPE_SIZE
    }

    /**
     * Handles different formats of nouns.
     *
     * This function processes multiple formats of nouns and applies the necessary transformations
     * or actions based on the specific format of the noun provided.
     *
     * @param noun The noun to be processed.
     * @return The processed noun in the desired format.
     */
    private fun handleMultipleNounFormats(
        multipleTypeSuggestion: List<String>?,
        type: String? = null,
    ) {
        binding.apply {
            translateBtnLeft.visibility = View.VISIBLE
            translateBtnRight.visibility = View.VISIBLE
            translateBtn.visibility = View.INVISIBLE
            binding.translateBtnLeft.setTextColor(getColor(white))
            binding.translateBtnRight.setTextColor(getColor(white))
            val (leftType, rightType) =
                if (isSingularAndPlural) {
                    "PL" to multipleTypeSuggestion?.get(0).toString()
                } else {
                    multipleTypeSuggestion?.get(0).toString() to multipleTypeSuggestion?.get(1).toString()
                }

            when (type) {
                "noun" -> {
                    handleTextForNouns(leftType, rightType, binding)
                }
                "preposition" -> {
                    handleTextForPreposition(leftType, rightType, binding)
                }
            }
        }
    }

    /**
     * Handles the text input specifically for nouns.
     * This function processes the given text and performs necessary actions
     * to handle nouns appropriately within the input method editor (IME).
     *
     * @param text The input text that needs to be processed for nouns.
     */
    private fun handleTextForNouns(
        leftType: String,
        rightType: String,
        binding: KeyboardViewCommandOptionsBinding,
    ) {
        handleColorAndTextForNounType(leftType).let { (colorRes, text) ->
            binding.translateBtnLeft.text = text
            binding.translateBtnLeft.background =
                ContextCompat
                    .getDrawable(
                        applicationContext,
                        R.drawable.gender_suggestion_button_left_background,
                    )?.apply {
                        setTintMode(PorterDuff.Mode.SRC_IN)
                        setTint(ContextCompat.getColor(applicationContext, colorRes))
                    }
        }

        handleColorAndTextForNounType(rightType).let { (colorRes, text) ->
            binding.translateBtnRight.text = text
            binding.translateBtnRight.background =
                ContextCompat
                    .getDrawable(
                        applicationContext,
                        R.drawable.gender_suggestion_button_right_background,
                    )?.apply {
                        setTintMode(PorterDuff.Mode.SRC_IN)
                        setTint(ContextCompat.getColor(applicationContext, colorRes))
                    }
        }
    }

    /**
     * Handles the text input for prepositions.
     *
     * This function processes the given text to identify and handle prepositions
     * appropriately within the input method editor (IME).
     *
     * @param text The input text to be processed for prepositions.
     */
    private fun handleTextForPreposition(
        leftType: String,
        rightType: String,
        binding: KeyboardViewCommandOptionsBinding,
    ) {
        handleTextForCaseAnnotation(leftType).let { (colorRes, text) ->
            binding.translateBtnLeft.text = text
            binding.translateBtnLeft.background =
                ContextCompat
                    .getDrawable(
                        applicationContext,
                        R.drawable.gender_suggestion_button_left_background,
                    )?.apply {
                        setTintMode(PorterDuff.Mode.SRC_IN)
                        setTint(ContextCompat.getColor(applicationContext, colorRes))
                    }
        }

        handleTextForCaseAnnotation(rightType).let { (colorRes, text) ->
            binding.translateBtnRight.text = text
            binding.translateBtnRight.background =
                ContextCompat
                    .getDrawable(
                        applicationContext,
                        R.drawable.gender_suggestion_button_right_background,
                    )?.apply {
                        setTintMode(PorterDuff.Mode.SRC_IN)
                        setTint(ContextCompat.getColor(applicationContext, colorRes))
                    }
        }
    }

    /**
     * Handles text for case annotation based on the provided noun type.
     *
     * @param nounType The type of noun to be annotated.
     * @return A pair containing an integer and a string. The integer represents the status code,
     *         and the string contains the annotated text.
     */
    private fun handleTextForCaseAnnotation(nounType: String): Pair<Int, String> {
        val suggestionMap =
            mapOf(
                "genitive case" to Pair(md_grey_black_dark, processValuesForPreposition(language, "Gen")),
                "accusative case" to Pair(md_grey_black_dark, processValuesForPreposition(language, "Acc")),
                "dative case" to Pair(md_grey_black_dark, processValuesForPreposition(language, "Dat")),
                "locative case" to Pair(md_grey_black_dark, processValuesForPreposition(language, "Loc")),
                "Prepositional case" to Pair(md_grey_black_dark, processValuesForPreposition(language, "Pre")),
                "Instrumental case" to Pair(md_grey_black_dark, processValuesForPreposition(language, "Ins")),
            )
        var (colorRes, text) =
            suggestionMap[nounType]
                ?: Pair(R.color.transparent, "Suggestion")
        return Pair(colorRes, text)
    }

    /**
     * Handles the color and text representation for a given noun type.
     *
     * @param nounType The type of noun for which the color and text need to be determined.
     * @return A pair containing the color (as an Int) and the text (as a String) corresponding to the noun type.
     */
    private fun handleColorAndTextForNounType(nounType: String): Pair<Int, String> {
        val suggestionMap =
            mapOf(
                "PL" to Pair(R.color.annotateOrange, "PL"),
                "neuter" to Pair(R.color.annotateGreen, "N"),
                "common of two genders" to Pair(R.color.annotatePurple, processValueForNouns(language, "C")),
                "common" to Pair(R.color.annotatePurple, processValueForNouns(language, "C")),
                "masculine" to Pair(R.color.annotateBlue, processValueForNouns(language, "M")),
                "feminine" to Pair(R.color.annotateRed, processValueForNouns(language, "F")),
            )

        var (colorRes, text) =
            suggestionMap[nounType]
                ?: Pair(R.color.transparent, "Suggestion")
        return Pair(colorRes, text)
    }

    /**
     * Processes the given value to identify and handle nouns.
     *
     * @param value The input value that needs to be processed for nouns.
     * @return The processed result after handling nouns.
     */
    private fun processValueForNouns(
        language: String,
        text: String,
    ): String {
        var textOutput: String
        if (nounAnnotationConversionDict[language]?.get(text) != null) {
            textOutput = nounAnnotationConversionDict[language]?.get(text).toString()
        } else {
            return text
        }
        return textOutput
    }

    /**
     * Processes the given values to determine the appropriate preposition.
     *
     * @param values The list of values to be processed.
     * @return The determined preposition based on the processed values.
     */
    private fun processValuesForPreposition(
        language: String,
        text: String,
    ): String {
        var textOutput: String
        if (prepAnnotationConversionDict[language]?.get(text) != null) {
            textOutput = prepAnnotationConversionDict[language]?.get(text).toString()
        } else {
            return text
        }
        return textOutput
    }

    /**
     * Disables the auto-suggest feature of the keyboard.
     * This function is used to disable the suggestion of plural or gender
     * when the keyboard switches to one of the other modes.
     */
    fun disableAutoSuggest() {
        binding.translateBtnRight.visibility = View.INVISIBLE
        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtn.visibility = View.VISIBLE
        binding.translateBtn.text = getString(R.string.suggestion)
        binding.translateBtn.setTextColor(getColor(R.color.special_key_dark))
        binding.translateBtn.setBackgroundColor(getColor(R.color.transparent))
        handleTextSizeForSuggestion(binding)
        if (currentState == ScribeState.SELECT_COMMAND) {
            setupIdleView()
            setupSelectCommandView()
        }
    }

    /**
     * Adjusts the text size for the suggestion view in the keyboard.
     *
     * @param binding The binding object for the keyboard view command options.
     */
    private fun handleTextSizeForSuggestion(binding: KeyboardViewCommandOptionsBinding) {
        binding.translateBtn.textSize = SUGGESTION_SIZE
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        if (isUserDarkMode) {
            binding.translateBtn.setTextColor(getColor(white))
        } else {
            binding.translateBtn.setTextColor(getColor(md_grey_black_dark))
        }
    }

    /**
     * Inserts the specified emoji into the current input field.
     * Replaces the last word if there's no trailing space.
     *
     * @param emoji The emoji character to be inserted.
     */
    private fun insertEmoji(emoji: String) {
        val inputConnection = currentInputConnection ?: return
        val maxLookBack = emojiMaxKeywordLength.coerceAtLeast(1)

        inputConnection.beginBatchEdit()
        try {
            val previousText = inputConnection.getTextBeforeCursor(maxLookBack, 0)?.toString() ?: ""

            // Find last word boundary efficiently
            val lastSpaceIndex = previousText.lastIndexOf(' ')
            val hasSpace = lastSpaceIndex != -1

            when {
                // Case 1: Ends with space or empty
                previousText.isEmpty() || hasSpace && lastSpaceIndex == previousText.length - 1 -> {
                    inputConnection.commitText(emoji, 1)
                }

                // Case 2: Has previous word
                hasSpace -> {
                    val lastWord = previousText.substring(lastSpaceIndex + 1)
                    if (emojiKeywords?.containsKey(lastWord.lowercase()) == true) {
                        inputConnection.deleteSurroundingText(lastWord.length, 0)
                        inputConnection.commitText(emoji, 1)
                    } else {
                        inputConnection.commitText(emoji, 1)
                    }
                }

                // Case 3: Entire text is the word
                else -> {
                    if (emojiKeywords?.containsKey(previousText.lowercase()) == true) {
                        inputConnection.deleteSurroundingText(previousText.length, 0)
                        inputConnection.commitText(emoji, 1)
                    } else {
                        inputConnection.commitText(emoji, 1)
                    }
                }
            }
        } finally {
            inputConnection.endBatchEdit()
        }
    }

    /**
     * Returns the plural representation of the given word.
     *
     * @param word The word to be pluralized. Can be null.
     * @return The plural form of the word, or null if the input word is null.
     */
    private fun getPluralRepresentation(word: String?): String? {
        if (word.isNullOrEmpty()) return null
        val languageAlias = getLanguageAlias(language)
        val pluralRepresentationMap = dbHelper.getPluralRepresentation(languageAlias, word)
        return pluralRepresentationMap.values.filterNotNull().firstOrNull()
    }

    /**
     * Returns the alias for the given language.
     *
     * @param language The language for which the alias is to be retrieved.
     * @return The alias corresponding to the provided language.
     */
    private fun getLanguageAlias(language: String): String =
        when (language) {
            "English" -> "EN"
            "French" -> "FR"
            "German" -> "DE"
            "Italian" -> "IT"
            "Portuguese" -> "PT"
            "Russian" -> "RU"
            "Spanish" -> "ES"
            "Swedish" -> "SV"
            else -> ""
        }

    /**
     * Updates the state of the shift key based on the current context.
     * This method should be called whenever there is a change in the input state
     * that might affect the shift key, such as a change in the input type or
     * the current text being edited.
     */
    fun updateShiftKeyState() {
        // The shift state in the Scribe commands should not depend on the Input Connection.
        // The current state should be transferred to the command unless required by the language.
        if ((currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) &&
            keyboardMode == keyboardLetters

        ) {
            val editorInfo = currentInputEditorInfo
            if (
                editorInfo != null &&
                editorInfo.inputType != InputType.TYPE_NULL &&
                keyboard?.mShiftState != SHIFT_ON_PERMANENT
            ) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
                    keyboardView?.invalidateAllKeys()
                }
            }
        }
    }

    /**
     * Moves the cursor in the input field.
     *
     * @param moveRight A boolean indicating the direction to move the cursor.
     *                   If true, the cursor moves to the right.
     *                   If false, the cursor moves to the left.
     */
    private fun moveCursor(moveRight: Boolean) {
        val extractedText = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        var newCursorPosition = extractedText.selectionStart
        newCursorPosition =
            if (moveRight) {
                newCursorPosition + 1
            } else {
                newCursorPosition - 1
            }

        currentInputConnection?.setSelection(newCursorPosition, newCursorPosition)
    }

    /**
     * Safely retrieves the translation of a word between source and destination languages.
     *
     * This function calls `getTranslationSourceAndDestination` and handles the null case
     * by returning an empty string if the translation is null.
     *
     * @param language The language name (e.g., "english") to determine the source and destination languages.
     * @param commandBarInput The word whose translation is to be fetched.
     * @return The translation of the word in the destination language, or an empty string if no translation is found.
     */
    fun getTranslation(
        language: String,
        commandBarInput: String,
    ): String = dbHelper.getTranslationSourceAndDestination(language, commandBarInput) ?: ""

    /**
     * Retrieves the action ID associated with the IME (Input Method Editor) options.
     *
     * @return The action ID as an integer.
     */
    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }

    /**
     * Handles the action to be performed when the Enter key is pressed.
     *
     * This function is responsible for managing the behavior of the Enter key
     * within the input method editor (IME). It determines the appropriate action
     * based on the current context and state of the input field.
     *
     * @param keyCode The keycode of the key event.
     * @param event The key event associated with the Enter key press.
     * @return Boolean indicating whether the key event was handled.
     */
    fun handleKeycodeEnter(
        binding: KeyboardViewKeyboardBinding? = null,
        commandBarState: Boolean? = false,
    ) {
        val inputConnection = currentInputConnection
        val imeOptionsActionId = getImeOptionsActionId()

        val isConjugate = currentState == ScribeState.CONJUGATE
        val isCommandBarMode = commandBarState == true

        if (isConjugate || isCommandBarMode) {
            val rawInput =
                binding
                    ?.commandBar
                    ?.text
                    ?.toString()
                    ?.trim()
                    ?.dropLast(1)
                    .orEmpty()

            if (isConjugate) {
                Log.i("ALPHA", "Inside CONJUGATE mode")
                saveConjugateModeType(language, context = applicationContext)
                currentState = ScribeState.SELECT_VERB_CONJUNCTION
            }

            val processedOutput =
                when (currentState) {
                    ScribeState.PLURAL -> getPluralRepresentation(rawInput).orEmpty()
                    ScribeState.TRANSLATE -> getTranslation(language, rawInput)
                    else -> rawInput
                }

            if (isCommandBarMode) {
                val output = if (processedOutput.length > rawInput.length) "$processedOutput " else processedOutput
                inputConnection.commitText(output, 1)
            }

            binding?.commandBar?.text = ""
            conjugateOutput = dbHelper.getConjugateData(getLanguageAlias(language), processedOutput)
            Log.i("ALPHA", "Processed input: $rawInput")
        } else {
            if (imeOptionsActionId != IME_ACTION_NONE) {
                inputConnection.performEditorAction(imeOptionsActionId)
            } else {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }
    }

    /**
     * Handles the change of input mode in the keyboard.
     * This function is responsible for switching between different input modes
     * such as alphabetic, numeric, or symbolic modes.
     *
     * @param newMode The new input mode to switch to.
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
        keyboardView?.invalidateAllKeys()
        keyboardView?.setKeyboard(keyboard!!)
    }

    /**
     * Handles the input of keyboard letters.
     *
     * This function processes the input from the keyboard when letters are typed.
     * It performs necessary actions based on the input letters.
     *
     * @param input The input string containing the letters typed on the keyboard.
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
        }
    }

    /**
     * Handles the delete action from the command bar.
     *
     * @param binding The binding object for the keyboard view. This can be null.
     */
    private fun handleCommandBarDelete(binding: KeyboardViewKeyboardBinding?) {
        binding?.commandBar?.let { commandBar ->
            var newText = ""
            if (commandBar.text.length <= 2) {
                binding.promptTextBorder.visibility = View.VISIBLE
                binding.commandBar.setPadding(
                    binding.commandBar.paddingRight,
                    binding.commandBar.paddingTop,
                    binding.commandBar.paddingRight,
                    binding.commandBar.paddingBottom,
                )
                if (language == "German" && this.currentState == ScribeState.PLURAL) {
                    keyboard?.mShiftState = SHIFT_ON_ONE_CHAR
                }
            } else {
                newText = "${commandBar.text.trim().dropLast(2)}$commandCursor"
            }
            commandBar.text = newText
        }
    }

    /**
     * To return the conjugation based on the code
     */
    fun handleConjugateKeys(code: Int , isSubsequentRequired: Boolean): String? {
        if (!isSubsequentRequired) {
            val inputConnection = currentInputConnection
            inputConnection.commitText(keyboardView?.getKeyLabel(code), 1)
        }
        return keyboardView?.getKeyLabel(code)
    }

    /**
     * Handles the delete key press event.
     * This function is responsible for managing the behavior when the delete key is pressed
     * on the keyboard. It ensures that the appropriate actions are taken to delete the
     * selected text or character.
     */
    fun handleDelete(
        currentState: Boolean? = false,
        binding: KeyboardViewKeyboardBinding? = null,
    ) {
        val wordBeforeCursor = getText()
        val inputConnection = currentInputConnection
        if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR) {
            keyboard!!.mShiftState = SHIFT_OFF
        }

        if (currentState == true) {
            handleCommandBarDelete(binding)
        } else {
            val selectedText = inputConnection.getSelectedText(0)
            if (TextUtils.isEmpty(selectedText)) {
                if (isEmoji(wordBeforeCursor)) {
                    inputConnection.deleteSurroundingText(2, 0)
                } else {
                    inputConnection.deleteSurroundingText(1, 0)
                }
            } else {
                inputConnection.commitText("", 1)
            }
        }
    }

    /**
     * Checks if the given word is an emoji.
     *
     * @param word The word to check, which can be null.
     * @return True if the word is an emoji, false otherwise.
     */
    private fun isEmoji(word: String?): Boolean {
        if (word.isNullOrEmpty() || word.length < 2) {
            return false
        }

        val lastTwoChars = word.substring(word.length - 2)
        val emojiRegex = Regex("[\\uD83C\\uDF00-\\uD83E\\uDDFF]|[\\u2600-\\u26FF]|[\\u2700-\\u27BF]")
        return emojiRegex.containsMatchIn(lastTwoChars)
    }

    /**
     * Handles the else condition for the given context.
     *
     * This function is called when none of the specific conditions are met.
     * It performs the necessary actions to handle the default case.
     * These are the set of actions performed when the keyboard space , shift or such
     * characters are clicked.
     *
     * @param context The context in which the else condition is being handled.
     */
    fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
        binding: KeyboardViewKeyboardBinding?,
        commandBarState: Boolean = false,
    ) {
        val inputConnection = currentInputConnection ?: return
        var codeChar = code.toChar()

        if (Character.isLetter(codeChar) && keyboard!!.mShiftState > SHIFT_OFF) {
            codeChar = Character.toUpperCase(codeChar)
        }

        if (commandBarState) {
            binding?.commandBar?.let { commandBar ->
                if (commandBar.text.isEmpty()) {
                    binding.promptTextBorder.visibility = View.GONE
                    binding.commandBar.setPadding(
                        0,
                        binding.commandBar.paddingTop,
                        binding.commandBar.paddingRight,
                        binding.commandBar.paddingBottom,
                    )
                }
                val newText = "${commandBar.text.trim().dropLast(1)}$codeChar$commandCursor"
                commandBar.text = newText
            }
        } else {
            // Handling space key logic.
            if (keyboardMode != keyboardLetters && code == KeyboardBase.KEYCODE_SPACE) {
                binding?.commandBar?.text = " "
                val originalText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                inputConnection.commitText(codeChar.toString(), 1)
                val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                switchToLetters = originalText != newText
            } else {
                binding?.commandBar?.append(codeChar.toString())
                inputConnection.commitText(codeChar.toString(), 1)
            }
        }

        if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR && keyboardMode == keyboardLetters) {
            keyboard!!.mShiftState = SHIFT_OFF
            keyboardView!!.invalidateAllKeys()
        }
    }

    /**
     * Returns the XML layout resource ID for the base keyboard of the specified language.
     *
     * This function maps a given language name to its corresponding keyboard layout XML file.
     * If the provided language is `null` or doesn't match any of the predefined options,
     * the function defaults to returning the English keyboard layout.
     *
     * @param language The name of the language for which the base keyboard layout is requested.
     *                 Expected values are: "English", "French", "German", "Italian",
     *                 "Portuguese", "Russian", "Spanish", and "Swedish".
     *
     * @return The resource ID of the XML layout file for the corresponding keyboard.
     */
    private fun baseKeyboardOfAnyLanguage(language: String?): Int =
        when (language) {
            "English" -> R.xml.keys_letters_english
            "French" -> R.xml.keys_letters_french
            "German" -> R.xml.keys_letters_german
            "Italian" -> R.xml.keys_letters_italian
            "Portuguese" -> R.xml.keys_letters_portuguese
            "Russian" -> R.xml.keys_letters_russian
            "Spanish" -> R.xml.keys_letters_spanish
            "Swedish" -> R.xml.keys_letters_swedish
            else -> R.xml.keys_letters_english
        }

    internal companion object {
        const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500
        const val TEXT_LENGTH = 20
        const val NOUN_TYPE_SIZE = 22f
        const val SUGGESTION_SIZE = 15f
        const val DARK_THEME = "#aeb3be"
        const val LIGHT_THEME = "#4b4b4b"
        const val MAX_TEXT_LENGTH = 1000
        const val COMMIT_TEXT_CURSOR_POSITION = 1
    }
}
