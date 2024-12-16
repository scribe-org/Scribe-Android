package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.MyKeyboard
import be.scri.views.MyKeyboardView

class RussianKeyboardIME : SimpleKeyboardIME("Russian") {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_russian

    override lateinit var binding: KeyboardViewCommandOptionsBinding
    override var keyboardView: MyKeyboardView? = null
    override var keyboard: MyKeyboard? = null
    override var enterKeyType = IME_ACTION_NONE
    override val keyboardLetters = 0
    override val keyboardSymbols = 1
    override val keyboardSymbolShift = 2
    override var lastShiftPressTS = 0L
    override var keyboardMode = keyboardLetters
    override var inputTypeClass = InputType.TYPE_CLASS_TEXT
    override var switchToLetters = false
    override var hasTextBeforeCursor = false

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        Log.i("MY-TAG", "From Russian Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        setupCommandBarTheme(binding)
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        initializeEmojiButtons()
        updateUI()
        return keyboardHolder
    }

    override fun onKey(code: Int) {
        val inputConnection = currentInputConnection
        if (keyboard == null || inputConnection == null) {
            return
        }
        if (code != MyKeyboard.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        nounTypeSuggestion = findNounTypeForLastWord(nounKeywords,lastWord)
        updateAutoSuggestText(nounTypeSuggestion)

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                handleKeycodeDelete()
                keyboardView!!.invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_SHIFT -> {
                super.handleKeyboardLetters(keyboardMode, keyboardView)
                keyboardView!!.invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_ENTER -> {
                handleKeycodeEnter()
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

        lastWord = getLastWordBeforeCursor()
        Log.d("Debug", "$lastWord")
        autosuggestEmojis = findEmojisForLastWord(emojiKeywords, lastWord)
        Log.d("Debug", "$autosuggestEmojis")
        updateButtonText(isAutoSuggestEnabled, autosuggestEmojis)

        if (code != MyKeyboard.KEYCODE_SHIFT) {
            super.updateShiftKeyState()
        }
    }

    fun handleKeycodeDelete() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleDelete(false, keyboardBinding)
        } else {
            handleDelete(true, keyboardBinding)
        }
    }

    fun handleKeycodeEnter() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleKeycodeEnter(keyboardBinding, false)
        } else {
            handleKeycodeEnter(keyboardBinding, true)
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            updateUI()
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }
}
