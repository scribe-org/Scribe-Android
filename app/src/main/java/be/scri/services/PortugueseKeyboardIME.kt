// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The input method (IME) for the Portuguese language keyboard.
 */

package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyboardBase
import be.scri.helpers.PreferencesHelper.getEnablePeriodAndCommaABC
import be.scri.helpers.PreferencesHelper.getIsPreviewEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.views.KeyboardView

class PortugueseKeyboardIME : GeneralKeyboardIME("Portuguese") {
    override fun getKeyboardLayoutXML(): Int =
        if (getEnablePeriodAndCommaABC(applicationContext, language)) {
            R.xml.keys_letters_portuguese
        } else {
            R.xml.keys_letters_portuguese_without_period_and_comma
        }

    override lateinit var binding: KeyboardViewCommandOptionsBinding
    override var keyboardView: KeyboardView? = null
    override var keyboard: KeyboardBase? = null
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
        Log.i("MY-TAG", "From Portuguese Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        setupCommandBarTheme(binding)
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView!!.setVibrate = getIsVibrateEnabled(applicationContext, language)
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
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        when (code) {
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
        autoSuggestEmojis = findEmojisForLastWord(emojiKeywords, lastWord)
        nounTypeSuggestion = findGenderForLastWord(nounKeywords, lastWord)
        checkIfPluralWord = findWheatherWordIsPlural(pluralWords, lastWord)
        Log.d("Debug", "$autoSuggestEmojis")
        Log.d("MY-TAG", "$nounTypeSuggestion")
        updateButtonText(isAutoSuggestEnabled, autoSuggestEmojis)
        if (code != KeyboardBase.KEYCODE_SHIFT) {
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

    fun handleKeycodeSpace() {
        val code = KeyboardBase.KEYCODE_SPACE
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleElseCondition(code, keyboardMode, binding = null)
            updateAutoSuggestText(isPlural = checkIfPluralWord, nounTypeSuggestion = nounTypeSuggestion)
        } else {
            handleElseCondition(code, keyboardMode, keyboardBinding, commandBarState = true)
            disableAutoSuggest()
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }
}
