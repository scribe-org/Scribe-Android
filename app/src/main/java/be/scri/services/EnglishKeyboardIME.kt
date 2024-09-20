package be.scri.services

import android.content.Context
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

class EnglishKeyboardIME : SimpleKeyboardIME() {
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

    private fun shouldCommitPeriodAfterSpace(language: String): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("period_on_double_tap_$language", false)
    }

    override fun onStartInputView(
        editorInfo: EditorInfo?,
        restarting: Boolean,
    ) {
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
            ScribeState.IDLE -> keyboardView!!.setEnterKeyColor(0)
            else -> keyboardView!!.setEnterKeyColor(R.color.dark_scribe_blue)
        }

        keyboardView!!.setKeyboardHolder(binding.keyboardHolder)
        keyboardView?.mOnKeyboardActionListener = this
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
            }
            else -> {
                binding.translateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.conjugateBtn.setBackgroundColor(getColor(R.color.transparent))
                binding.pluralBtn.setBackgroundColor(getColor(R.color.transparent))
            }
        }

        setupCommandBarTheme(binding)
        binding.translateBtn.text = ""
        binding.conjugateBtn.text = ""
        binding.pluralBtn.text = ""
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.SELECT_COMMAND
            Log.i("MY-TAG", "SELECT COMMAND STATE FROM English IME")
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
        super.setupCommandBarTheme(binding)
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

    private fun updateEnterKeyColor() {
        when (currentState) {
            ScribeState.IDLE -> keyboardView?.setEnterKeyColor(Color.TRANSPARENT)
            ScribeState.SELECT_COMMAND -> keyboardView?.setEnterKeyColor(Color.TRANSPARENT)
            else -> keyboardView?.setEnterKeyColor(getColor(R.color.dark_scribe_blue))
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
                super.handleDelete()
                keyboardView!!.invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_SHIFT -> {
                super.handleKeyboardLetters(keyboardMode, keyboardView, this)
                keyboardView!!.invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_ENTER -> {
                super.handleKeycodeEnter()
            }
            MyKeyboard.KEYCODE_MODE_CHANGE -> {
                handleModeChange(keyboardMode, keyboardView, this)
            }
            else -> {
                super.handleElseCondition(code, keyboardMode)
            }
        }

        if (code != MyKeyboard.KEYCODE_SHIFT) {
            super.updateShiftKeyState()
        }
    }

    private fun switchToToolBar() {
        val keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
        this.keyboardBinding = keyboardBinding
        val keyboardHolder = keyboardBinding.root
        super.setupToolBarTheme(keyboardBinding)
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
        updateEnterKeyColor()
    }
}
