// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The input method (IME) for the English language keyboard.
 */

package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyboardBase
import be.scri.views.KeyboardView

class EnglishKeyboardIME : GeneralKeyboardIME("English") {
    private fun isTablet(): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }

    override fun getKeyboardLayoutXML(): Int = when {
        isTablet() -> R.xml.keys_letters_english_tablet
        getEnablePeriodAndCommaABC() -> R.xml.keys_letters_english
        else -> R.xml.keys_letters_english_without_period_and_comma
    }

    override val keyboardLetters = 0
    override val keyboardSymbols = 1
    override val keyboardSymbolShift = 2

    override var keyboard: KeyboardBase? = null
    override var keyboardView: KeyboardView? = null
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
        keyboardView!!.setPreview = getIsPreviewEmabled()
        keyboardView!!.setVibrate = getIsVibrateEnabled()
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
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        when (code) {

            KeyboardBase.KEYCODE_TAB -> {
                currentInputConnection?.commitText("\t", 1)
                return
            }

            KeyboardBase.KEYCODE_CAPS_LOCK -> {
                keyboard?.let {
                    val newState = when (it.mShiftState) {
                        KeyboardBase.SHIFT_OFF -> KeyboardBase.SHIFT_LOCKED
                        else -> KeyboardBase.SHIFT_OFF
                    }
                    if (it.setShifted(newState)) {
                        keyboardView?.invalidateAllKeys()
                    }
                }
                return
            }

            KeyboardBase.KEYCODE_DELETE -> {
                handleKeycodeDelete()
                keyboardView!!.invalidateAllKeys()
                disableAutoSuggest()
            }

            KeyboardBase.KEYCODE_SHIFT -> {
                super.handleKeyboardLetters(keyboardMode, keyboardView)
                keyboardView!!.invalidateAllKeys()
                disableAutoSuggest()
            }

            KeyboardBase.KEYCODE_ENTER -> {
                disableAutoSuggest()
                handleKeycodeEnter()
                updateAutoSuggestText(isPlural = checkIfPluralWord, nounTypeSuggestion = nounTypeSuggestion)
            }

            KeyboardBase.KEYCODE_MODE_CHANGE -> {
                handleModeChange(keyboardMode, keyboardView, this)
                disableAutoSuggest()
            }

            KeyboardBase.KEYCODE_SPACE -> {
                handleKeycodeSpace()
            }

            KeyboardBase.KEYCODE_LEFT_ARROW -> handleLeftArrow()
            KeyboardBase.KEYCODE_RIGHT_ARROW -> handleRightArrow()

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
        checkIfPluralWord = findWheatherWordIsPlural(pluralWords, lastWord)

        Log.i("MY-TAG", "$checkIfPluralWord")
        Log.d("Debug", "$autosuggestEmojis")
        Log.d("MY-TAG", "$nounTypeSuggestion")
        updateButtonText(isAutoSuggestEnabled, autosuggestEmojis)
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            super.updateShiftKeyState()
        }
    }

    private fun handleLeftArrow() {
        currentInputConnection?.let { ic ->
            val currentPos = ic.getTextBeforeCursor(1000, 0)?.length ?: 0
            val newPos = (currentPos - 1).coerceAtLeast(0)
            ic.setSelection(newPos, newPos)
        }
    }

    private fun handleRightArrow() {
        currentInputConnection?.let { ic ->
            val currentPos = ic.getTextBeforeCursor(1000, 0)?.length ?: 0
            val textAfter = ic.getTextAfterCursor(1000, 0)?.toString() ?: ""
            val newPos = (currentPos + 1).coerceAtMost(currentPos + textAfter.length + 1)
            ic.setSelection(newPos, newPos)
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

    fun handleKeycodeSpace() {
        val code = KeyboardBase.KEYCODE_SPACE
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleElseCondition(code, keyboardMode, binding = null)
            updateAutoSuggestText(isPlural = checkIfPluralWord)
        } else {
            handleElseCondition(code, keyboardMode, keyboardBinding, commandBarState = true)
            disableAutoSuggest()
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType).apply {
        }
        onCreateInputView()
        setupCommandBarTheme(binding)
    }
}
