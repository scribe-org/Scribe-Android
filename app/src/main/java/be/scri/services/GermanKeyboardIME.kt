package be.scri.services

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.MyKeyboard
import be.scri.helpers.PreferencesHelper
import be.scri.views.MyKeyboardView

class GermanKeyboardIME : ScribeKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int =
        if (getIsAccentCharacter()) {
            R.xml.keys_letter_german_without_accent_character
        } else {
            R.xml.keys_letters_german
        }

    private fun getIsAccentCharacter(): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isAccentCharacter = sharedPref.getBoolean("disable_accent_character_German", false)
        return isAccentCharacter
    }

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

    private var isAutoSuggestEnabled: Boolean = false
    override lateinit var binding: KeyboardViewCommandOptionsBinding
    override var keyboardView: MyKeyboardView? = null
    override var keyboard: MyKeyboard? = null
    override var enterKeyType = IME_ACTION_NONE
    override var shiftPermToggleSpeed = 500
    override val keyboardLetters = 0
    override val keyboardSymbols = 1
    override val keyboardSymbolShift = 2
    override var lastShiftPressTS = 0L
    override var keyboardMode = keyboardLetters
    override var inputTypeClass = InputType.TYPE_CLASS_TEXT
    override var switchToLetters = false
    override var hasTextBeforeCursor = false

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
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
        isAutoSuggestEnabled = sharedPref.getBoolean("emoji_suggestions_German", true)
        updateButtonVisibility(isAutoSuggestEnabled)
        setupIdleView()
        super.onStartInputView(editorInfo, restarting)
        onInitializeInterface()
        setupCommandBarTheme(binding)
    }

    override fun commitPeriodAfterSpace() {
        if (shouldCommitPeriodAfterSpace("German")) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
        }
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        Log.i("MY-TAG", "From German Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        when (currentState) {
            ScribeState.IDLE -> keyboardView!!.setEnterKeyColor(null)
            else -> keyboardView!!.setEnterKeyColor(R.color.dark_scribe_blue)
        }
        setupCommandBarTheme(binding)
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        initializeEmojiButtons()
        updateUI()
        return keyboardHolder
    }

    private fun setupIdleView() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", true)
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

    override fun onKey(code: Int) {
        val inputConnection = currentInputConnection
        if (keyboard == null || inputConnection == null) {
            return
        }

        if (code != MyKeyboard.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                    handleDelete(false, binding = null)
                } else {
                    handleDelete(true, keyboardBinding)
                }
                keyboardView!!.invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_SHIFT -> {
                super.handleKeyboardLetters(keyboardMode, keyboardView)
                keyboardView!!.invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_ENTER -> {
                if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                    handleKeycodeEnter(binding = null, false)
                } else {
                    handleKeycodeEnter(keyboardBinding, true)
                    currentState = ScribeState.IDLE
                    switchToCommandToolBar()
                    updateUI()
                }
            }
            MyKeyboard.KEYCODE_MODE_CHANGE -> {
                handleModeChange(keyboardMode, keyboardView, this)
            }
            else -> {
                if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                    handleElseCondition(code, keyboardMode, binding = null)
                } else {
                    handleElseCondition(code, keyboardMode, keyboardBinding, commandBarState = true)
                }
            }
        }

        if (code != MyKeyboard.KEYCODE_SHIFT) {
            super.updateShiftKeyState()
        }
    }

    private fun switchToToolBar() {
        this.keyboardBinding = initializeKeyboardBinding()
        val keyboardHolder = keyboardBinding.root
        keyboardView = keyboardBinding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        super.setupToolBarTheme(keyboardBinding)
        keyboardView!!.mOnKeyboardActionListener = this
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            updateUI()
        }
        setInputView(keyboardHolder)
    }

    override fun onCreate() {
        super.onCreate()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }

    override fun updateUI() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (currentState) {
            ScribeState.IDLE -> {
                setupIdleView()
                initializeEmojiButtons()
                updateButtonVisibility(isAutoSuggestEnabled)
            }
            ScribeState.SELECT_COMMAND -> setupSelectCommandView()
            else -> switchToToolBar()
        }
        updateEnterKeyColor(isUserDarkMode)
    }
}
