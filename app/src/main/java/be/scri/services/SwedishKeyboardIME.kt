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
import be.scri.databinding.KeyboardViewKeyboardBinding
import be.scri.helpers.MyKeyboard
import be.scri.services.EnglishKeyboardIME.ScribeState
import be.scri.views.MyKeyboardView

class SwedishKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int =
        if (getIsAccentCharacter()) {
            R.xml.keys_letters_swedish
        } else {
            R.xml.keys_letter_swedish_without_accent_characters
        }

    private fun getIsAccentCharacter(): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isAccentCharacter = sharedPref.getBoolean("disable_accent_character_Swedish", true)
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

    private var currentState: ScribeState = ScribeState.IDLE
    private lateinit var keyboardBinding: KeyboardViewKeyboardBinding
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

    private fun shouldCommitPeriodAfterSpace(language: String): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("period_on_double_tap_$language", false)
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
        setupIdleView()
        super.onStartInputView(editorInfo, restarting)
        setupCommandBarTheme(binding)
    }

    override fun commitPeriodAfterSpace() {
        if (shouldCommitPeriodAfterSpace("Swedish")) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
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
                    handleDelete(false, keyboardBinding)
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
                    handleElseCondition(code, keyboardMode, binding = null)
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

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        Log.i("MY-TAG", "From Swedish Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        setupCommandBarTheme(binding)
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        updateUI()
        return keyboardHolder
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
            }
            else -> {
                binding.translateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.conjugateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.pluralBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.translateBtn.setTextColor(Color.BLACK)
                binding.conjugateBtn.setTextColor(Color.BLACK)
                binding.pluralBtn.setTextColor(Color.BLACK)
            }
        }
        setupCommandBarTheme(binding)
        binding.translateBtn.text = "Suggestion"
        binding.conjugateBtn.text = "Suggestion"
        binding.pluralBtn.text = "Suggestion"
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.SELECT_COMMAND
            Log.i("MY-TAG", "SELECT COMMAND STATE")
            binding.scribeKey.foreground = getDrawable(R.drawable.close)
            updateUI()
        }
    }

    private fun setupSelectCommandView() {
        binding.translateBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.conjugateBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.pluralBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        setupCommandBarTheme(binding)
        binding.translateBtn.text = "Translate"
        binding.conjugateBtn.text = "Conjugate"
        binding.pluralBtn.text = "Plural"
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

    private fun switchToToolBar() {
        val keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
        this.keyboardBinding = keyboardBinding
        val keyboardHolder = keyboardBinding.root
        keyboardView = keyboardBinding.keyboardView
        super.setupToolBarTheme(keyboardBinding)
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.mOnKeyboardActionListener = this
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            updateUI()
        }
        setInputView(keyboardHolder)
    }

    private fun updateEnterKeyColor(isDarkMode: Boolean? = null) {
        when (currentState) {
            ScribeState.IDLE -> keyboardView?.setEnterKeyColor(null, isDarkMode = isDarkMode)
            ScribeState.SELECT_COMMAND -> keyboardView?.setEnterKeyColor(null, isDarkMode = isDarkMode)
            else -> keyboardView?.setEnterKeyColor(getColor(R.color.dark_scribe_blue))
        }
    }

    private fun switchToCommandToolBar() {
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

    override fun onCreate() {
        super.onCreate()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }

    private fun updateUI() {
        when (currentState) {
            ScribeState.IDLE -> setupIdleView()
            ScribeState.SELECT_COMMAND -> setupSelectCommandView()
            else -> switchToToolBar()
        }
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        updateEnterKeyColor(isUserDarkMode)
    }
}
