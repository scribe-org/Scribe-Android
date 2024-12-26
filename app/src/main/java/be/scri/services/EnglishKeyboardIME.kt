package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.MyKeyboard
import be.scri.views.MyKeyboardView

class EnglishKeyboardIME : SimpleKeyboardIME("English") {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_english

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

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                handleKeycodeDelete()
                keyboardView!!.invalidateAllKeys()
                disableAutoSuggest()
            }

            MyKeyboard.KEYCODE_SHIFT -> {
                super.handleKeyboardLetters(keyboardMode, keyboardView)
                keyboardView!!.invalidateAllKeys()
                disableAutoSuggest()
            }

            MyKeyboard.KEYCODE_ENTER -> {
                handleKeycodeEnter()
                disableAutoSuggest()
            }

            MyKeyboard.KEYCODE_MODE_CHANGE -> {
                handleModeChange(keyboardMode, keyboardView, this)
                disableAutoSuggest()
            }

            MyKeyboard.KEYCODE_SPACE -> {
                handleElseCondition(code , keyboardMode , binding = null)
                updateAutoSuggestText(nounTypeSuggestion)
            }

            else -> {
                if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                    handleElseCondition(code, keyboardMode, binding = null)
                    disableAutoSuggest()
                } else {
                    handleElseCondition(code, keyboardMode, keyboardBinding, commandBarState = true)
                    disableAutoSuggest()
                }
            }
        }

        lastWord = getLastWordBeforeCursor()
        Log.d("Debug", "$lastWord")
        autosuggestEmojis = findEmojisForLastWord(emojiKeywords, lastWord)
        nounTypeSuggestion = findNounTypeForLastWord(nounKeywords, lastWord)
        Log.d("Debug", "$autosuggestEmojis")
        Log.d("MY-TAG", "$nounTypeSuggestion")
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
