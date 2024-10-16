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
import be.scri.helpers.MyKeyboard.Companion.KEYCODE_ENTER
import be.scri.views.MyKeyboardView

class EnglishKeyboardIME : ScribeKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_english

    override var shiftPermToggleSpeed = 500
    override val keyboardLetters = 0
    override val keyboardSymbols = 1
    override val keyboardSymbolShift = 2

    override var keyboard: MyKeyboard? = null
    override var keyboardView: MyKeyboardView? = null
    override var lastShiftPressTS = 0L
    override var keyboardMode = keyboardLetters
    override var inputTypeClass = InputType.TYPE_CLASS_TEXT
    override var enterKeyType = IME_ACTION_NONE
    override var switchToLetters = false
    override var hasTextBeforeCursor = false
    override lateinit var binding: KeyboardViewCommandOptionsBinding

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
        if (shouldCommitPeriodAfterSpace("English")) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
        }
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        setupCommandBarTheme(binding)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        when (currentState) {
            ScribeState.IDLE -> keyboardView!!.setEnterKeyColor(null)
            else -> keyboardView!!.setEnterKeyColor(R.color.dark_scribe_blue)
        }
        keyboardView!!.setKeyboardHolder()
        keyboardView?.mOnKeyboardActionListener = this
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
            Log.i("MY-TAG", "SELECT COMMAND STATE FROM English IME")
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
            KEYCODE_ENTER -> {
                if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                    handleKeycodeEnter(keyboardBinding, false)
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
        this.keyboardBinding = run {
            val keyboardBinding1 = KeyboardViewKeyboardBinding.inflate(layoutInflater)
            keyboardBinding1
        }
        val keyboardHolder = keyboardBinding.root
        super.setupToolBarTheme(keyboardBinding)
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

    override fun updateUI() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        when (currentState) {
            ScribeState.IDLE -> setupIdleView()
            ScribeState.SELECT_COMMAND -> setupSelectCommandView()
            else -> switchToToolBar()
        }
        updateEnterKeyColor(isUserDarkMode)
    }

}
