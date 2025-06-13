// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import DataContract
import android.content.Context
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
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
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
import be.scri.helpers.SuggestionHandler
import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portuguese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables
import be.scri.views.KeyboardView

// based on https://www.androidauthority.com/lets-build-custom-keyboard-android-832362/

private const val DATA_SIZE_2 = 2

private const val DATA_CONSTANT_3 = 3

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
    internal var isSingularAndPlural: Boolean = false

    private var subsequentAreaRequired: Boolean = false

    private var subsequentData: MutableList<List<String>> = mutableListOf()

    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var suggestionHandler: SuggestionHandler
    private var dataContract: DataContract? = null // <<< ADD THIS PROPERTY TO CACHE THE CONTRACT
    var emojiKeywords: HashMap<String, MutableList<String>>? = null
    private lateinit var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>
    private lateinit var conjugateLabels: Set<String>
    private var emojiMaxKeywordLength: Int = 0
    internal lateinit var nounKeywords: HashMap<String, List<String>>
    var pluralWords: Set<String>? = null
    internal lateinit var caseAnnotation: HashMap<String, MutableList<String>>
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
        INVALID,
    }

    internal fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    internal fun returnSubsequentData(): List<List<String>> = subsequentData

    override fun onCreate() {
        super.onCreate()
        suggestionHandler = SuggestionHandler(this)
        val themedContext = androidx.appcompat.view.ContextThemeWrapper(this, R.style.AppTheme)
        val themedInflater = layoutInflater.cloneInContext(themedContext)
        keyboardBinding = KeyboardViewKeyboardBinding.inflate(themedInflater)
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        updateCommandBarHintAndPrompt()
        saveConjugateModeType("none")
        updateUI()
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        sharedPref.edit {
            putString("conjugate_mode_type", "none")
        }
        setupCommandBarTheme(binding)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        currentState = ScribeState.IDLE
        updateCommandBarHintAndPrompt()
        saveConjugateModeType("none")
        updateUI()
        switchToCommandToolBar()
        updateUI()
        moveToIdleState()
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        updateCommandBarHintAndPrompt()
        saveConjugateModeType("none")
        updateUI()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
    }

    override fun hasTextBeforeCursor(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val textBeforeCursor = inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0)?.trim() ?: ""
        return textBeforeCursor.isNotEmpty() && textBeforeCursor.lastOrNull() != '.'
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        currentState = ScribeState.IDLE
        updateCommandBarHintAndPrompt()
        saveConjugateModeType("none")
        updateUI()
        keyboardView!!.invalidateAllKeys()
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this

        return keyboardHolder
    }

    override fun onPress(primaryCode: Int) {
        if (primaryCode != 0) {
            keyboardView?.vibrateIfNeeded()
        }
    }

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

        dataContract = dbHelper.getLanguageContract(languageAlias)

        emojiKeywords = dbHelper.getEmojiKeywords(languageAlias)
        emojiMaxKeywordLength = dbHelper.getEmojiMaxKeywordLength()
        pluralWords = dbHelper.checkIfWordIsPlural(languageAlias, dataContract)?.toSet()
        nounKeywords = dbHelper.findGenderOfWord(languageAlias, dataContract)
        caseAnnotation = dbHelper.findCaseAnnnotationForPreposition(languageAlias)
        conjugateOutput = dbHelper.getConjugateData(languageAlias, "describe")
        conjugateLabels = dbHelper.getConjugateLabels(languageAlias, "describe")

        keyboard = KeyboardBase(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)
    }

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

    override fun moveCursorLeft() {
        moveCursor(false)
    }

    override fun moveCursorRight() {
        moveCursor(true)
    }

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 0)
    }

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
        val textBefore =
            currentInputConnection
                ?.getTextBeforeCursor(1, 0)
                ?.toString()
                .orEmpty()
        if (textBefore.isEmpty()) {
            keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
        }
        setupCommandBarTheme(binding)
    }

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

    private fun updateCommandBarHintAndPrompt(
        isUserDarkMode: Boolean? = null,
        text: String? = null,
        word: String? = null,
    ) {
        val commandBarEditText = keyboardBinding.commandBar
        val hintMessage = HintUtils.getCommandBarHint(currentState, language, word)
        val promptText = HintUtils.getPromptText(currentState, language, context = this, text)
        val promptTextView = keyboardBinding.promptText
        promptTextView.text = promptText
        commandBarEditText.hint = hintMessage

        commandBarEditText.requestFocus()

        if (isUserDarkMode == true) {
            commandBarEditText.setHintTextColor(getColor(R.color.hint_white))
            commandBarEditText.setTextColor(getColor(white))
            keyboardBinding.commandBarLayout.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.command_bar_color_dark)
            promptTextView.setTextColor(getColor(white))

            promptTextView.setBackgroundColor(getColor(R.color.command_bar_color_dark))
            keyboardBinding.promptTextBorder.setBackgroundColor(getColor(R.color.command_bar_color_dark))
        } else {
            commandBarEditText.setHintTextColor(getColor(R.color.hint_black))
            commandBarEditText.setTextColor(Color.BLACK)
            keyboardBinding.commandBarLayout.backgroundTintList = ContextCompat.getColorStateList(this, white)
            promptTextView.setTextColor(Color.BLACK)
            promptTextView.setBackgroundColor(getColor(white))
            keyboardBinding.promptTextBorder.setBackgroundColor(getColor(white))
        }
        Log.d(
            "KeyboardUpdate",
            "CommandBar Hint Updated: [State: $currentState, Language: $language, Hint: $hintMessage]",
        )
    }

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
        setInputView(keyboardHolder)
    }

    internal fun updateUI() {
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        when (currentState) {
            ScribeState.IDLE -> {
                setupIdleView()
                handleTextSizeForSuggestion(binding)
                initializeEmojiButtons()
                saveConjugateModeType("none")
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

            ScribeState.INVALID -> {
                setupInvalidView(isUserDarkMode)
            }
            else -> switchToToolBar()
        }
        updateEnterKeyColor(isUserDarkMode)
    }

    private fun setupInvalidView(isUserDarkMode: Boolean) {
        keyboardBinding.scribeKey.foreground = AppCompatResources.getDrawable(this, R.drawable.ic_scribe_icon_vector)
        keyboardBinding.scribeKey.setOnClickListener {
            switchToCommandToolBar()
            moveToSelectCommandState(isUserDarkMode)
        }
        keyboardBinding.ivInfo?.visibility = View.VISIBLE
        setDefaultKeyboardLanguage()
        val promptText = HintUtils.getInvalidHint(language = language)
        val commandBarButton = keyboardBinding.commandBar
        val promptTextView = keyboardBinding.promptText
        promptTextView.text = promptText
        commandBarButton.hint = ""
        Log.i(TAG, "INVALID STATE ${commandBarButton.text}")
    }

    private fun setDefaultKeyboardLanguage() {
        val keyboardXmlId = getKeyboardLayoutXML()
        keyboard = KeyboardBase(this, keyboardXmlId, enterKeyType)
        keyboardView =
            keyboardBinding.keyboardView.apply {
                setKeyboard(keyboard!!)
                mOnKeyboardActionListener = this@GeneralKeyboardIME
            }
    }

    private fun moveToSelectCommandState(isUserDarkMode: Boolean) {
        currentState = ScribeState.SELECT_COMMAND
        disableAutoSuggest()
        updateButtonVisibility(false)
        Log.i(TAG, "SELECT COMMAND STATE")
        binding.scribeKey.foreground = AppCompatResources.getDrawable(this, R.drawable.close)
        updateUI()
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        sharedPref.edit {
            putString("conjugate_mode_type", "none")
        }
        binding.translateBtn.setTextColor(if (isUserDarkMode) Color.WHITE else Color.BLACK)
    }

    internal fun switchToToolBar(
        isSubsequentArea: Boolean = false,
        dataSize: Int = 0,
    ) {
        keyboardBinding = initializeKeyboardBinding()
        val keyboardHolder = keyboardBinding.root

        applyToolBarVisualSettings()
        handleModeChange(keyboardSymbols, keyboardView, this)

        val keyboardXmlId = getKeyboardLayoutForState(currentState, isSubsequentArea, dataSize)
        initializeKeyboard(keyboardXmlId)

        setupScribeKeyListener()
        val conjugateIndex = getValidatedConjugateIndex()

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

    private fun getKeyboardLayoutForState(
        state: ScribeState,
        isSubsequentArea: Boolean = false,
        dataSize: Int = 0,
    ): Int =
        when (state) {
            ScribeState.TRANSLATE -> {
                val language = getPreferredTranslationLanguage(this, language)
                baseKeyboardOfAnyLanguage(language)
            }
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                saveConjugateModeType(language)
                if (!isSubsequentArea && dataSize == 0) {
                    when (language) {
                        "English" -> R.xml.conjugate_view_2x2
                        "Swedish" -> R.xml.conjugate_view_2x2
                        "Russian" -> R.xml.conjugate_view_2x2
                        else -> R.xml.conjugate_view_3x2
                    }
                } else {
                    Log.i("CONJUGATE-ISSUE", "The data size is $dataSize")
                    when (dataSize) {
                        DATA_SIZE_2 -> R.xml.conjugate_view_2x1
                        DATA_CONSTANT_3 -> R.xml.conjugate_view_1x3
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
        val maxIndex = conjugateOutput.keys.count() - DATA_SIZE_2
        index =
            if (maxIndex >= 0) {
                index.coerceIn(0, maxIndex + 1)
            } else {
                0
            }
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }

    internal fun setupConjugateKeysByLanguage(
        conjugateIndex: Int,
        isSubsequentArea: Boolean = false,
    ) {
        val isDarkMode = getIsDarkModeOrNot(applicationContext)

        setUpConjugateKeys(
            startIndex = conjugateIndex,
            conjugateOutput = conjugateOutput,
            isDarkMode = isDarkMode,
            isSubsequentArea,
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
                "1x1" to listOf(KeyboardBase.CODE_1X1),
                "1x3" to
                    listOf(
                        KeyboardBase.CODE_1X3_LEFT,
                        KeyboardBase.CODE_1X3_CENTER,
                        KeyboardBase.CODE_1X3_RIGHT,
                    ),
                "2x1" to listOf(KeyboardBase.CODE_2X1_TOP, KeyboardBase.CODE_2X1_BOTTOM),
                "2x2" to
                    listOf(
                        KeyboardBase.CODE_TL,
                        KeyboardBase.CODE_TR,
                        KeyboardBase.CODE_BL,
                        KeyboardBase.CODE_BR,
                    ),
            )
        val title = conjugateOutput.keys.elementAtOrNull(startIndex) ?: return
        val languageOutput = conjugateOutput[title] ?: return
        val conjugateLabel = conjugateLabels.toList()
        if (language != "English") {
            keyCodeMap["3x2"]?.forEachIndexed { index, code ->
                val value = languageOutput[title]?.elementAtOrNull(index) ?: return@forEachIndexed
                keyboardView?.setKeyLabel(value, conjugateLabel[index], code)
            }
        } else {
            val keys = languageOutput.keys.toList()
            val sharedPreferences = this.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)

            fun handleOutput(
                index: Int,
                code: Int,
                prefKey: String,
            ) {
                val output = languageOutput[keys.getOrNull(index)] ?: return
                if (output.size > 1) {
                    subsequentAreaRequired = true
                    subsequentData.add(output.toList())
                    sharedPreferences.edit { putString("1", prefKey) }
                } else {
                    subsequentAreaRequired = false
                    sharedPreferences.edit { putString("0", prefKey) }
                }
                keyboardView?.setKeyLabel(output.firstOrNull().toString(), "HI", code)
            }
            if (!isSubsequentArea) {
                keyCodeMap["3x2"]?.forEach { code ->
                    keyboardView?.setKeyLabel("HI", "HI", code)
                }
            }

            handleOutput(0, KeyboardBase.CODE_TL, "CODE_TL")
            handleOutput(1, KeyboardBase.CODE_TR, "CODE_TR")
            handleOutput(DATA_SIZE_2, KeyboardBase.CODE_BL, "CODE_BL")
            handleOutput(DATA_CONSTANT_3, KeyboardBase.CODE_BR, "CODE_BR")
        }
        if (isSubsequentArea) {
            keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_FPS)
        }
        updateCommandBarHintAndPrompt(
            text = title,
            isUserDarkMode = isDarkMode,
            word = conjugateLabels.last(),
        )
    }

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
        switchToToolBar(true, flattenList.size)
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
        keyboardBinding.ivInfo?.visibility = View.GONE
    }

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

    private fun setupIdleView() {
        binding.translateBtn.textSize = SUGGESTION_SIZE
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)

        val textColor = if (isUserDarkMode) Color.WHITE else "#1E1E1E".toColorInt()
        val separatorColor = Color.parseColor(if (isUserDarkMode) DARK_THEME else LIGHT_THEME)

        listOf(binding.translateBtn, binding.conjugateBtn, binding.pluralBtn).forEach { button ->
            button.setBackgroundColor(getColor(R.color.transparent))
            button.setTextColor(textColor)
            button.text = getString(R.string.suggestion)
        }

        listOf(
            binding.separator2,
            binding.separator3,
            binding.separator4,
            binding.separator5,
            binding.separator6,
        ).forEach { separator ->
            separator.setBackgroundColor(separatorColor)
        }

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
            moveToSelectCommandState(isUserDarkMode)
        }
    }

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
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            Log.i(TAG, "IDLE STATE")
            binding.translateBtn.setTextColor(Color.WHITE)
            disableAutoSuggest()
            saveConjugateModeType("none")
            binding.scribeKey.foreground = AppCompatResources.getDrawable(this, R.drawable.ic_scribe_icon_vector)
            updateUI()
        }
        binding.translateBtn.setOnClickListener {
            Log.i(TAG, "TRANSLATE STATE")
            keyboardView?.invalidateAllKeys()
            updateCommandBarHintAndPrompt()
            saveConjugateModeType("none")
            currentState = ScribeState.TRANSLATE

            updateUI()
        }
        binding.conjugateBtn.setOnClickListener {
            Log.i(TAG, "CONJUGATE STATE")
            updateCommandBarHintAndPrompt()
            currentState = ScribeState.CONJUGATE
            updateUI()
        }
        binding.pluralBtn.setOnClickListener {
            Log.i(TAG, "PLURAL STATE")
            updateCommandBarHintAndPrompt()
            currentState = ScribeState.PLURAL
            updateUI()
            saveConjugateModeType("none")
            if (language == "German") {
                keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
            }
        }
    }

    internal fun saveConjugateModeType(
        language: String,
        isSubsequentArea: Boolean = false,
    ) {
        val sharedPref = applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        val mode =
            if (!isSubsequentArea) {
                when (language) {
                    "Swedish", "English" -> "2x2"
                    "German", "French", "Russian", "Italian", "Spanish", "Portuguese" -> "3x2"
                    else -> "none"
                }
            } else {
                "none"
            }
        sharedPref.edit {
            putString("conjugate_mode_type", mode)
        }
    }

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

    private fun initializeKeyboardBinding(): KeyboardViewKeyboardBinding =
        KeyboardViewKeyboardBinding.inflate(
            layoutInflater,
        )

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

    internal fun updateButtonVisibility(isAutoSuggestEnabled: Boolean) {
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

    fun getText(): String? {
        val inputConnection = currentInputConnection ?: return null
        return inputConnection.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()
    }

    fun getLastWordBeforeCursor(): String? {
        val textBeforeCursor = getText() ?: return null
        val trimmedText = textBeforeCursor.trim()
        return trimmedText.split("\\s+".toRegex()).lastOrNull()
    }

    fun findEmojisForLastWord(
        emojiKeywords: HashMap<String, MutableList<String>>?,
        lastWord: String?,
    ): MutableList<String>? {
        lastWord?.let { word ->
            val lowerCaseWord = word.lowercase()
            val emojis = emojiKeywords?.get(lowerCaseWord)
            if (emojis != null) {
                Log.d("Debug", "Emojis for '$word': $emojis")
                return emojis
            } else {
                Log.d("Debug", "No emojis found for '$word'")
            }
        }
        return null
    }

    fun findGenderForLastWord(
        nounKeywords: HashMap<String, List<String>>,
        lastWord: String?,
    ): List<String>? {
        lastWord?.let { word ->
            val lowerCaseWord = word.lowercase()
            val gender = nounKeywords[lowerCaseWord]
            if (gender != null) {
                Log.d("Debug", "Gender for '$word': $gender")
                // We will handle this logic cleanly in the SuggestionHandler
                isSingularAndPlural = pluralWords?.contains(lowerCaseWord) == true
                if (isSingularAndPlural) {
                    Log.i(TAG, "isSingularPlural Updated to true")
                } else {
                    Log.i(TAG, "isSingularPlural Updated to false")
                }
                return gender
            } else {
                Log.d("Debug", "No gender found for '$word'")
            }
        }
        return null
    }

    fun findWhetherWordIsPlural(
        pluralWords: Set<String>?,
        lastWord: String?,
    ): Boolean = pluralWords?.contains(lastWord) == true

    fun getCaseAnnotationForPreposition(
        caseAnnotation: HashMap<String, MutableList<String>>,
        lastWord: String?,
    ): MutableList<String>? {
        lastWord?.let { word ->
            val lowerCaseWord = word.lowercase()
            return caseAnnotation[lowerCaseWord]
        }
        return null
    }

    fun updateButtonText(
        isAutoSuggestEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        if (isAutoSuggestEnabled) {
            val safeEmojis = autoSuggestEmojis ?: return
            emojiBtnTablet1?.text = safeEmojis.getOrNull(0)
            emojiBtnTablet2?.text = safeEmojis.getOrNull(1)
            emojiBtnTablet3?.text = safeEmojis.getOrNull(DATA_SIZE_2)

            emojiBtnPhone1?.text = safeEmojis.getOrNull(0)
            emojiBtnPhone2?.text = safeEmojis.getOrNull(1)

            binding.emojiBtnTablet1.setOnClickListener { insertEmoji(emojiBtnTablet1?.text.toString()) }
            binding.emojiBtnTablet2.setOnClickListener { insertEmoji(emojiBtnTablet2?.text.toString()) }
            binding.emojiBtnTablet3.setOnClickListener { insertEmoji(emojiBtnTablet3?.text.toString()) }

            binding.emojiBtnPhone1.setOnClickListener { insertEmoji(emojiBtnPhone1?.text.toString()) }
            binding.emojiBtnPhone2.setOnClickListener { insertEmoji(emojiBtnPhone2?.text.toString()) }
        }
    }

    fun updateAutoSuggestText(
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
    ) {
        val handled =
            when {
                ((nounTypeSuggestion?.size ?: 0) > 1 || this.isSingularAndPlural) -> {
                    Log.i("MY-TAG", "Condition for handleMultipleNouns met.")
                    handleMultipleNouns(nounTypeSuggestion)
                }

                handlePluralIfNeeded(isPlural) -> {
                    true
                }

                handleSingleNounSuggestion(nounTypeSuggestion) -> {
                    true
                }

                handleMultipleCases(caseAnnotationSuggestion) -> {
                    true
                }

                handleSingleCaseSuggestion(caseAnnotationSuggestion) -> {
                    true
                }

                handleFallbackSuggestions(nounTypeSuggestion, caseAnnotationSuggestion) -> {
                    true
                }
                else -> false
            }

        if (!handled) {
            disableAutoSuggest()
        }
    }

    private fun handlePluralIfNeeded(isPlural: Boolean): Boolean {
        if (isPlural) {
            handlePluralAutoSuggest()
            return true
        }
        return false
    }

    private fun handleSingleNounSuggestion(nounTypeSuggestion: List<String>?): Boolean {
        if (nounTypeSuggestion?.size == 1 && !isSingularAndPlural) {
            val (colorRes, text) = handleColorAndTextForNounType(nounTypeSuggestion[0])
            if (text != getString(R.string.suggestion) || colorRes != R.color.transparent) {
                Log.i("MY-TAG", "Applying specific single noun suggestion: $text")
                handleSingleType(nounTypeSuggestion, "noun")
                return true
            }
        }
        return false
    }

    private fun handleSingleCaseSuggestion(caseAnnotationSuggestion: List<String>?): Boolean {
        if (caseAnnotationSuggestion?.size == 1) {
            val (colorRes, text) = handleTextForCaseAnnotation(caseAnnotationSuggestion[0])
            if (text != getString(R.string.suggestion) || colorRes != R.color.transparent) {
                Log.i("MY-TAG", "Applying specific single case suggestion: $text")
                handleSingleType(caseAnnotationSuggestion, "preposition")
                return true
            }
        }
        return false
    }

    private fun handleMultipleNouns(nounTypeSuggestion: List<String>?): Boolean {
        if ((nounTypeSuggestion?.size ?: 0) > 1 || isSingularAndPlural) {
            Log.i("MY-TAG", "Applying multiple noun formats")
            handleMultipleNounFormats(nounTypeSuggestion, "noun")
            return true
        }
        return false
    }

    private fun handleMultipleCases(caseAnnotationSuggestion: List<String>?): Boolean {
        if ((caseAnnotationSuggestion?.size ?: 0) > 1) {
            Log.i("MY-TAG", "Applying multiple case formats")
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
            val (_, text) = handleColorAndTextForNounType(it[0])
            if (text != getString(R.string.suggestion)) appliedSomething = true
        }

        if (!appliedSomething) {
            caseAnnotationSuggestion?.let {
                handleSingleType(it, "preposition")
                val (_, text) = handleTextForCaseAnnotation(it[0])
                if (text != getString(R.string.suggestion)) appliedSomething = true
            }
        }

        return appliedSomething
    }

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

    private fun handleSingleType(
        singleTypeSuggestion: List<String>?,
        type: String? = null,
    ) {
        Log.i(TAG, "Single suggestion activated $singleTypeSuggestion")
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
        Log.i(TAG, "These are the colorRes and text $colorRes and $buttonText")
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

    private fun handleTextSize(binding: KeyboardViewCommandOptionsBinding) {
        binding.translateBtn.textSize = NOUN_TYPE_SIZE
    }

    /**
     * Determines the types for the left and right suggestion buttons based on the suggestion type.
     * Handles the special case for nouns that can be both singular and plural.
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
     * Gets the (Color, Text) pairs for the left and right suggestion buttons.
     */
    private fun getSuggestionPairs(
        type: String?,
        suggestions: List<String>?,
    ): Pair<Pair<Int, String>, Pair<Int, String>>? {
        val (leftType, rightType) = getSuggestionTypes(type, suggestions)

        return when (type) {
            "noun" -> handleColorAndTextForNounType(leftType) to handleColorAndTextForNounType(rightType)
            "preposition" -> handleTextForCaseAnnotation(leftType) to handleTextForCaseAnnotation(rightType)
            else -> null
        }
    }

    /**
     * Applies styling to a single suggestion button.
     */
    private fun applySuggestionButtonStyle(
        button: Button,
        colorRes: Int,
        text: String,
        backgroundRes: Int,
    ) {
        button.text = text
        button.setTextColor(getColor(white))
        button.background =
            ContextCompat.getDrawable(applicationContext, backgroundRes)?.apply {
                setTintMode(PorterDuff.Mode.SRC_IN)
                setTint(ContextCompat.getColor(applicationContext, colorRes))
            }
    }

    /**
     * Sets up the UI to display two separate suggestion buttons.
     */
    private fun setupDualSuggestionButtons(
        leftSuggestion: Pair<Int, String>,
        rightSuggestion: Pair<Int, String>,
    ) {
        binding.apply {
            translateBtnLeft.visibility = View.VISIBLE
            translateBtnRight.visibility = View.VISIBLE
            translateBtn.visibility = View.INVISIBLE

            applySuggestionButtonStyle(
                translateBtnLeft,
                leftSuggestion.first,
                leftSuggestion.second,
                R.drawable.gender_suggestion_button_left_background,
            )

            applySuggestionButtonStyle(
                translateBtnRight,
                rightSuggestion.first,
                rightSuggestion.second,
                R.drawable.gender_suggestion_button_right_background,
            )
        }
    }

    /**
     * Handles the case where one of the multiple suggestions is invalid. It attempts to fall back
     * to showing a single valid suggestion or disables the auto-suggest UI if none are valid.
     */
    private fun handleFallbackOrSingleSuggestion(multipleTypeSuggestion: List<String>?) {
        val suggestionText = getString(R.string.suggestion)

        val validNouns =
            multipleTypeSuggestion?.filter {
                handleColorAndTextForNounType(it).second != suggestionText
            }
        val validCases =
            caseAnnotationSuggestion?.filter {
                handleTextForCaseAnnotation(it).second != suggestionText
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
     * Handles displaying multiple format suggestions (e.g., two noun genders, two preposition cases).
     * It orchestrates getting suggestion data, checking for invalid suggestions, and updating the UI.
     */
    private fun handleMultipleNounFormats(
        multipleTypeSuggestion: List<String>?,
        type: String? = null,
    ) {
        // 1. Get the suggestion pairs (color, text) for the left and right buttons.
        val suggestionPairs = getSuggestionPairs(type, multipleTypeSuggestion) ?: return
        val (leftSuggestion, rightSuggestion) = suggestionPairs

        val suggestionText = getString(R.string.suggestion)

        // 2. Check if either suggestion is invalid. If so, fall back.
        if (leftSuggestion.second == suggestionText || rightSuggestion.second == suggestionText) {
            handleFallbackOrSingleSuggestion(multipleTypeSuggestion)
            return
        }

        // 3. If both suggestions are valid, display them in the dual-button UI.
        setupDualSuggestionButtons(leftSuggestion, rightSuggestion)
    }

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
        val (colorRes, text) =
            suggestionMap[nounType]
                ?: Pair(R.color.transparent, getString(R.string.suggestion))
        return Pair(colorRes, text)
    }

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

        val (colorRes, text) =
            suggestionMap[nounType]
                ?: Pair(R.color.transparent, getString(R.string.suggestion))
        return Pair(colorRes, text)
    }

    private fun processValueForNouns(
        language: String,
        text: String,
    ): String = nounAnnotationConversionDict[language]?.get(text) ?: text

    private fun processValuesForPreposition(
        language: String,
        text: String,
    ): String = prepAnnotationConversionDict[language]?.get(text) ?: text

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

    private fun handleTextSizeForSuggestion(binding: KeyboardViewCommandOptionsBinding) {
        binding.translateBtn.textSize = SUGGESTION_SIZE
        val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
        if (isUserDarkMode) {
            binding.translateBtn.setTextColor(getColor(white))
        } else {
            binding.translateBtn.setTextColor(getColor(md_grey_black_dark))
        }
    }

    private fun insertEmoji(emoji: String) {
        val inputConnection = currentInputConnection ?: return
        val maxLookBack = emojiMaxKeywordLength.coerceAtLeast(1)

        inputConnection.beginBatchEdit()
        try {
            val previousText = inputConnection.getTextBeforeCursor(maxLookBack, 0)?.toString() ?: ""
            val lastSpaceIndex = previousText.lastIndexOf(' ')
            val hasSpace = lastSpaceIndex != -1

            when {
                previousText.isEmpty() || hasSpace && lastSpaceIndex == previousText.length - 1 -> {
                    inputConnection.commitText(emoji, 1)
                }
                hasSpace -> {
                    val lastWord = previousText.substring(lastSpaceIndex + 1)
                    if (emojiKeywords?.containsKey(lastWord.lowercase()) == true) {
                        inputConnection.deleteSurroundingText(lastWord.length, 0)
                        inputConnection.commitText(emoji, 1)
                    } else {
                        inputConnection.commitText(emoji, 1)
                    }
                }
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

    private fun getPluralRepresentation(word: String?): String? {
        if (word.isNullOrEmpty()) return null
        val languageAlias = getLanguageAlias(language)
        val pluralRepresentationMap = dbHelper.getPluralRepresentation(languageAlias, dataContract, word)
        return pluralRepresentationMap.values.filterNotNull().firstOrNull()
    }

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

    fun getTranslation(
        language: String,
        commandBarInput: String,
    ): String = dbHelper.getTranslationSourceAndDestination(language, commandBarInput)

    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }

    fun handleKeycodeEnter(
        binding: KeyboardViewKeyboardBinding? = null,
        commandBarState: Boolean = false,
    ) {
        val inputConnection = currentInputConnection ?: return
        val imeOptionsActionId = getImeOptionsActionId()

        if (commandBarState) {
            val rawInput =
                binding
                    ?.commandBar
                    ?.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            val commandModeOutput =
                when (currentState) {
                    ScribeState.PLURAL -> getPluralRepresentation(rawInput).orEmpty()
                    ScribeState.TRANSLATE -> getTranslation(language, rawInput)
                    else -> {
                        Log.w(TAG, "handleKeycodeEnter called in unhandled state: $currentState")
                        rawInput
                    }
                }
            applyCommandOutput(commandModeOutput, inputConnection, binding)
        } else {
            if (currentState == ScribeState.CONJUGATE) {
                val rawInput =
                    binding
                        ?.commandBar
                        ?.text
                        ?.toString()
                        ?.trim()
                        .orEmpty()
                Log.i("ALPHA", "Inside CONJUGATE mode")
                saveConjugateModeType(language)
                currentState = ScribeState.SELECT_VERB_CONJUNCTION
                conjugateOutput = dbHelper.getConjugateData(getLanguageAlias(language), rawInput)
                conjugateLabels = dbHelper.getConjugateLabels(getLanguageAlias(language), rawInput)
            } else {
                handleNonCommandEnter(imeOptionsActionId, inputConnection)
            }
        }
    }

    internal fun moveToIdleState() {
        Log.i(TAG, "IDLE STATE")
        currentState = ScribeState.IDLE
        switchToCommandToolBar()
        updateUI()
    }

    private fun applyCommandOutput(
        commandModeOutput: String,
        inputConnection: InputConnection,
        binding: KeyboardViewKeyboardBinding?,
    ) {
        val outputBuilder = StringBuilder()
        outputBuilder.append(commandModeOutput)

        if (commandModeOutput.isNotEmpty() && !commandModeOutput.endsWith(" ")) {
            outputBuilder.append(" ")
        }

        val newText = outputBuilder.toString()
        inputConnection.commitText(newText, COMMIT_TEXT_CURSOR_POSITION)
        binding?.commandBar?.setText("")

        moveToIdleState()
        suggestionHandler.processWordSuggestions(newText.trim())
    }

    private fun handleNonCommandEnter(
        imeOptionsActionId: Int,
        inputConnection: InputConnection,
    ) {
        if (imeOptionsActionId != IME_ACTION_NONE) {
            inputConnection.performEditorAction(imeOptionsActionId)
        } else {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }

        // A normal enter should ALWAYS clear suggestions.
        suggestionHandler.clearAllSuggestionsAndHideButtonUI()
        moveToIdleState()
    }

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

    private fun handleCommandBarDelete(binding: KeyboardViewKeyboardBinding?) {
        binding?.commandBar?.let { commandBar ->
            var newText = ""
            if (commandBar.text.length <= DATA_SIZE_2) {
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
                newText = "${commandBar.text.trim().dropLast(DATA_SIZE_2)}$commandCursor"
            }
            commandBar.setText(newText)
            commandBar.setSelection(newText.length)
        }
    }

    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean,
    ): String? {
        if (!isSubsequentRequired) {
            val inputConnection = currentInputConnection
            inputConnection.commitText(keyboardView?.getKeyLabel(code), 1)
        }
        return keyboardView?.getKeyLabel(code)
    }

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
                    inputConnection.deleteSurroundingText(DATA_SIZE_2, 0)
                } else {
                    inputConnection.deleteSurroundingText(1, 0)
                }
            } else {
                inputConnection.commitText("", 1)
            }
            val before = inputConnection.getTextBeforeCursor(1, 0)?.isEmpty() != false
            if (before) {
                keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
                keyboardView!!.invalidateAllKeys()
            }
        }
    }

    private fun isEmoji(word: String?): Boolean {
        if (word.isNullOrEmpty() || word.length < DATA_SIZE_2) {
            return false
        }

        val lastTwoChars = word.substring(word.length - DATA_SIZE_2)
        val emojiRegex = Regex("[\\uD83C\\uDF00-\\uD83E\\uDDFF]|[\\u2600-\\u26FF]|[\\u2700-\\u27BF]")
        return emojiRegex.containsMatchIn(lastTwoChars)
    }

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
                val newText = "${commandBar.text}$codeChar"
                commandBar.setText(newText)
                commandBar.setSelection(newText.length)
            }
        } else {
            if (keyboardMode != keyboardLetters && code == KeyboardBase.KEYCODE_SPACE) {
                binding?.commandBar?.setText(" ")
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
        private const val TAG = "ScribeKeyboardLog"
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
