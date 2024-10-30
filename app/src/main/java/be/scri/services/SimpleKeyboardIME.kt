package be.scri.services

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
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
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.databinding.KeyboardViewKeyboardBinding
import be.scri.helpers.DatabaseHelper
import be.scri.helpers.MyKeyboard
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import be.scri.views.MyKeyboardView

// based on https://www.androidauthority.com/lets-build-custom-keyboard-android-832362/

@Suppress("TooManyFunctions")
abstract class SimpleKeyboardIME(
    var language: String,
) : InputMethodService(),
    MyKeyboardView.OnKeyboardActionListener {
    abstract fun getKeyboardLayoutXML(): Int

    abstract val keyboardLetters: Int
    abstract val keyboardSymbols: Int
    abstract val keyboardSymbolShift: Int

    abstract var keyboard: MyKeyboard?
    abstract var keyboardView: MyKeyboardView?
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

    // How quickly do we have to doubletap shift to enable permanent caps lock.
    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED
    private lateinit var dbHelper: DatabaseHelper
    lateinit var emojiKeywords: HashMap<String, MutableList<String>>
    var isAutoSuggestEnabled: Boolean = false
    var lastWord: String? = null
    var autosuggestEmojis: MutableList<String>? = null
    //    abstract var keyboardViewKeyboardBinding : KeyboardViewKeyboardBinding

    protected var currentState: ScribeState = ScribeState.IDLE
    protected lateinit var keyboardBinding: KeyboardViewKeyboardBinding

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

    fun getIsAccentCharacter(): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isAccentCharacter = sharedPref.getBoolean("disable_accent_character_Swedish", false)
        return isAccentCharacter
    }

    private fun updateEnterKeyColor(isDarkMode: Boolean? = null) {
        when (currentState) {
            ScribeState.IDLE -> keyboardView?.setEnterKeyColor(null, isDarkMode = isDarkMode)
            ScribeState.SELECT_COMMAND -> keyboardView?.setEnterKeyColor(null, isDarkMode = isDarkMode)
            else -> keyboardView?.setEnterKeyColor(getColor(R.color.dark_scribe_blue))
        }
    }

    override fun commitPeriodAfterSpace() {
        if (getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                .getBoolean("period_on_double_tap_$language", true)
        ) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
        keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
    }

    protected fun switchToCommandToolBar() {
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

    fun updateUI() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (currentState) {
            ScribeState.IDLE -> {
                setupIdleView()
                initializeEmojiButtons()
                updateButtonVisibility(isAutoSuggestEnabled)
                updateButtonText(isAutoSuggestEnabled, autosuggestEmojis)
            }

            ScribeState.SELECT_COMMAND -> setupSelectCommandView()
            else -> switchToToolBar()
        }
        updateEnterKeyColor(isUserDarkMode)
    }

    private fun switchToToolBar() {
        this.keyboardBinding = initializeKeyboardBinding()
        val keyboardHolder = keyboardBinding.root
        setupToolBarTheme(keyboardBinding)
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (isUserDarkMode) {
            true -> {
                keyboardBinding.topKeyboardDivider.setBackgroundColor(getColor(R.color.special_key_dark))
            }

            false -> {
                keyboardBinding.topKeyboardDivider.setBackgroundColor(getColor(R.color.special_key_light))
            }
        }
        keyboardView = keyboardBinding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.mOnKeyboardActionListener = this
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            updateUI()
        }
        setInputView(keyboardHolder)
    }

    private fun setupIdleView() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (isUserDarkMode) {
            true -> {
                binding.translateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.conjugateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.pluralBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.translateBtn.setTextColor(Color.WHITE)
                binding.conjugateBtn.setTextColor(Color.WHITE)
                binding.pluralBtn.setTextColor(Color.WHITE)
                binding.separator2.setBackgroundColor(getColor(R.color.special_key_dark))
                binding.separator3.setBackgroundColor(getColor(R.color.special_key_dark))
            }

            else -> {
                binding.translateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.conjugateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.pluralBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.translateBtn.setTextColor(Color.BLACK)
                binding.conjugateBtn.setTextColor(Color.BLACK)
                binding.pluralBtn.setTextColor(Color.BLACK)
                binding.separator2.setBackgroundColor(getColor(R.color.special_key_light))
                binding.separator3.setBackgroundColor(getColor(R.color.special_key_light))
            }
        }

        setupCommandBarTheme(binding)
        binding.translateBtn.text = "Suggestion"
        binding.conjugateBtn.text = "Suggestion"
        binding.pluralBtn.text = "Suggestion"
        binding.separator2.visibility = View.VISIBLE
        binding.separator3.visibility = View.VISIBLE
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.SELECT_COMMAND
            updateButtonVisibility(false)
            Log.i("MY-TAG", "SELECT COMMAND STATE")
            binding.scribeKey.foreground = getDrawable(R.drawable.close)
            updateUI()
        }
    }

    private fun setupSelectCommandView() {
        binding.translateBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.conjugateBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.pluralBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.translateBtn.text = "Translate"
        binding.conjugateBtn.text = "Conjugate"
        binding.pluralBtn.text = "Plural"
        binding.separator2.visibility = View.GONE
        binding.separator3.visibility = View.GONE
        setupCommandBarTheme(binding)
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            Log.i("MY-TAG", "IDLE STATE")
            binding.scribeKey.foreground = getDrawable(R.drawable.ic_scribe_icon_vector)
            updateUI()
        }
        binding.translateBtn.setOnClickListener {
            currentState = ScribeState.TRANSLATE
            Log.i("MY-TAG", "TRANSLATE STATE")
            updateUI()
        }
        binding.conjugateBtn.setOnClickListener {
            Log.i("MY-TAG", "CONJUGATE STATE")
            currentState = ScribeState.CONJUGATE
            updateUI()
        }
        binding.pluralBtn.setOnClickListener {
            Log.i("MY-TAG", "PLURAL STATE")
            currentState = ScribeState.PLURAL
            updateUI()
        }
    }

    private fun initializeKeyboardBinding(): KeyboardViewKeyboardBinding {
        val keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
        return keyboardBinding
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
    }

    override fun hasTextBeforeCursor(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val textBeforeCursor = inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0)
        if (textBeforeCursor.isNullOrBlank()) {
            return false
        }
        val trimmedText = textBeforeCursor.trim()
        val lastChar = trimmedText.lastOrNull()
        return lastChar != '.'
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        return keyboardHolder
    }

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
    }

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

    fun getText(): String? {
        val inputConnection = currentInputConnection ?: return null
        return inputConnection.getTextBeforeCursor(TEXT_LENGTH, 0)?.toString()
    }

    fun getLastWordBeforeCursor(): String? {
        val textBeforeCursor = getText() ?: return null
        val trimmedText = textBeforeCursor.trim().toString()
        val lastWord = trimmedText.split("\\s+".toRegex()).lastOrNull()
        return lastWord
    }

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

    fun updateButtonText(
        isAutoSuggestEnabled: Boolean,
        autosuggestEmojis: MutableList<String>?,
    ) {
        if (isAutoSuggestEnabled) {
            emojiBtnTablet1?.text = autosuggestEmojis?.get(0)
            emojiBtnTablet2?.text = autosuggestEmojis?.get(1)
            emojiBtnTablet3?.text = autosuggestEmojis?.get(2)

            emojiBtnPhone1?.text = autosuggestEmojis?.get(0)
            emojiBtnPhone2?.text = autosuggestEmojis?.get(1)

            binding.emojiBtnTablet1.setOnClickListener { insertEmoji(emojiBtnTablet1?.text.toString()) }
            binding.emojiBtnTablet2.setOnClickListener { insertEmoji(emojiBtnTablet2?.text.toString()) }
            binding.emojiBtnTablet3.setOnClickListener { insertEmoji(emojiBtnTablet3?.text.toString()) }

            binding.emojiBtnPhone1.setOnClickListener { insertEmoji(emojiBtnPhone1?.text.toString()) }
            binding.emojiBtnPhone2.setOnClickListener { insertEmoji(emojiBtnPhone2?.text.toString()) }
        }
    }

    private fun insertEmoji(emoji: String) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(emoji, 1)
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

        keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)
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

    fun updateShiftKeyState() {
        if (keyboardMode == keyboardLetters) {
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

    override fun onActionUp() {
        if (switchToLetters) {
            keyboardMode = keyboardLetters
            keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)

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

    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }

    fun handleKeycodeEnter(
        binding: KeyboardViewKeyboardBinding? = null,
        commandBarState: Boolean? = false,
    ) {
        val inputConnection = currentInputConnection
        val imeOptionsActionId = getImeOptionsActionId()

        if (commandBarState == true) {
            inputConnection.commitText(binding?.commandBar?.text.toString(), 1)
            binding?.commandBar?.text = ""
        } else {
            if (imeOptionsActionId != IME_ACTION_NONE) {
                inputConnection.performEditorAction(imeOptionsActionId)
            } else {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }
    }

    fun handleModeChange(
        keyboardMode: Int,
        keyboardView: MyKeyboardView?,
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
        keyboard = MyKeyboard(context, keyboardXml, enterKeyType)
        keyboardView?.invalidateAllKeys()
        keyboardView?.setKeyboard(keyboard!!)
    }

    fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: MyKeyboardView?,
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
            keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
            keyboardView!!.setKeyboard(keyboard!!)
        }
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
            binding?.commandBar?.let { commandBar ->
                val newText = "${commandBar.text.trim().dropLast(1)}"
                commandBar.text = newText
            }
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

    private fun isEmoji(word: String?): Boolean {
        if (word.isNullOrEmpty() || word.length < 2) {
            return false
        }

        val lastTwoChars = word.substring(word.length - 2)
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
                val newText = "${commandBar.text}$codeChar"
                commandBar.text = newText
            }
        } else {
            // Handling space key logic.
            if (keyboardMode != keyboardLetters && code == MyKeyboard.KEYCODE_SPACE) {
                binding?.commandBar?.text = " "
                val originalText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                inputConnection.commitText(codeChar.toString(), 1)
                val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                switchToLetters = originalText != newText
            } else {
                binding?.commandBar?.append(codeChar.toString())
                inputConnection.commitText(codeChar.toString(), 1)
            }

            if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR && keyboardMode == keyboardLetters) {
                keyboard!!.mShiftState = SHIFT_OFF
                keyboardView!!.invalidateAllKeys()
            }
        }
    }

    override fun onStartInputView(
        editorInfo: EditorInfo?,
        restarting: Boolean,
    ) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        updateEnterKeyColor(isUserDarkMode)
        initializeEmojiButtons()
        isAutoSuggestEnabled = sharedPref.getBoolean("emoji_suggestions_$language", true)
        updateButtonVisibility(isAutoSuggestEnabled)
        setupIdleView()
        super.onStartInputView(editorInfo, restarting)
        setupCommandBarTheme(binding)
    }

    private fun setupToolBarTheme(binding: KeyboardViewKeyboardBinding) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (isUserDarkMode) {
            true -> {
                binding.commandField.setBackgroundColor(getColor(R.color.md_grey_black_dark))
            }

            else -> {
                binding.commandField.setBackgroundColor(getColor(R.color.light_cmd_bar_border_color))
            }
        }
    }

    fun setupCommandBarTheme(binding: KeyboardViewCommandOptionsBinding) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (isUserDarkMode) {
            true -> {
                binding.commandField.setBackgroundColor(getColor(R.color.md_grey_black_dark))
            }

            else -> {
                binding.commandField.setBackgroundColor(getColor(R.color.light_cmd_bar_border_color))
            }
        }
    }

    private companion object {
        const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500
        const val TEXT_LENGTH = 20
    }
}
