// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The input method (IME) for the English language keyboard.
 */

package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyboardBase
import be.scri.views.KeyboardView

class EnglishKeyboardIME : GeneralKeyboardIME("English") {
    companion object {
        private const val SMALLEST_SCREEN_WIDTH_TABLET = 600
        private const val MAX_TEXT_LENGTH = 1000
        private const val COMMIT_TEXT_CURSOR_POSITION = 1
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    override fun getKeyboardLayoutXML(): Int =
        when {
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

    // Key handling logic extracted to a separate class
    private val keyHandler = KeyHandler(this)

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
        keyHandler.handleKey(code)
    }

    override fun onCreate() {
        super.onCreate()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }

    // Inner class to handle key events
    inner class KeyHandler(
        private val ime: EnglishKeyboardIME,
    ) {
        fun handleKey(code: Int) {
            val inputConnection = ime.currentInputConnection
            if (ime.keyboard == null || inputConnection == null) {
                return
            }
            if (code != KeyboardBase.KEYCODE_SHIFT) {
                ime.lastShiftPressTS = 0
            }

            when (code) {
                KeyboardBase.KEYCODE_TAB -> inputConnection.commitText("\t", COMMIT_TEXT_CURSOR_POSITION)
                KeyboardBase.KEYCODE_CAPS_LOCK -> handleCapsLock()
                KeyboardBase.KEYCODE_DELETE -> handleDeleteKey()
                KeyboardBase.KEYCODE_SHIFT -> handleShiftKey()
                KeyboardBase.KEYCODE_ENTER -> handleEnterKey()
                KeyboardBase.KEYCODE_MODE_CHANGE -> handleModeChangeKey()
                KeyboardBase.KEYCODE_SPACE -> handleKeycodeSpace()
                KeyboardBase.KEYCODE_LEFT_ARROW -> handleArrowKey(false)
                KeyboardBase.KEYCODE_RIGHT_ARROW -> handleArrowKey(true)
                else -> handleDefaultKey(code)
            }
            updateKeyboardState(code)
        }

        private fun updateKeyboardState(code: Int) {
            ime.lastWord = ime.getLastWordBeforeCursor()
            Log.d("Debug", "${ime.lastWord}")
            ime.autosuggestEmojis = ime.findEmojisForLastWord(ime.emojiKeywords, ime.lastWord)
            ime.checkIfPluralWord = ime.findWheatherWordIsPlural(ime.pluralWords, ime.lastWord)

            Log.i("MY-TAG", "${ime.checkIfPluralWord}")
            Log.d("Debug", "${ime.autosuggestEmojis}")
            Log.d("MY-TAG", "${ime.nounTypeSuggestion}")
            ime.updateButtonText(ime.isAutoSuggestEnabled, ime.autosuggestEmojis)
            if (code != KeyboardBase.KEYCODE_SHIFT) {
                ime.updateShiftKeyState()
            }
        }

        private fun handleCapsLock() {
            ime.keyboard?.let {
                val newState =
                    when (it.mShiftState) {
                        KeyboardBase.SHIFT_OFF -> KeyboardBase.SHIFT_LOCKED
                        else -> KeyboardBase.SHIFT_OFF
                    }
                if (it.setShifted(newState)) {
                    ime.keyboardView?.invalidateAllKeys()
                }
            }
        }

        private fun handleDefaultKey(code: Int) {
            if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
                ime.handleElseCondition(code, ime.keyboardMode, binding = null)
            } else {
                ime.handleElseCondition(code, ime.keyboardMode, ime.keyboardBinding, commandBarState = true)
            }
            ime.disableAutoSuggest()
        }

        private fun handleDeleteKey() {
            val shouldDelete =
                when (ime.currentState) {
                    ScribeState.IDLE, ScribeState.SELECT_COMMAND -> false
                    else -> true
                }
            ime.handleDelete(shouldDelete, ime.keyboardBinding)
            ime.keyboardView!!.invalidateAllKeys()
            ime.disableAutoSuggest()
        }

        private fun handleShiftKey() {
            ime.handleKeyboardLetters(ime.keyboardMode, ime.keyboardView)
            ime.keyboardView!!.invalidateAllKeys()
            ime.disableAutoSuggest()
        }

        private fun handleEnterKey() {
            if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
                ime.handleKeycodeEnter(ime.keyboardBinding, false)
            } else {
                ime.handleKeycodeEnter(ime.keyboardBinding, true)
                ime.currentState = ScribeState.IDLE
                ime.switchToCommandToolBar()
                ime.updateUI()
            }
            ime.disableAutoSuggest()
        }

        private fun handleModeChangeKey() {
            ime.handleModeChange(ime.keyboardMode, ime.keyboardView, ime)
            ime.disableAutoSuggest()
        }

        private fun handleArrowKey(isRight: Boolean) {
            ime.currentInputConnection?.let { ic ->
                val currentPos = ic.getTextBeforeCursor(MAX_TEXT_LENGTH, 0)?.length ?: 0
                val newPos =
                    if (isRight) {
                        val textAfter = ic.getTextAfterCursor(MAX_TEXT_LENGTH, 0)?.toString() ?: ""
                        (currentPos + 1).coerceAtMost(currentPos + textAfter.length)
                    } else {
                        (currentPos - 1).coerceAtLeast(0)
                    }
                ic.setSelection(newPos, newPos)
            }
        }

        private fun handleKeycodeSpace() {
            val code = KeyboardBase.KEYCODE_SPACE
            if (ime.currentState == ScribeState.IDLE || ime.currentState == ScribeState.SELECT_COMMAND) {
                ime.handleElseCondition(code, ime.keyboardMode, binding = null)
                ime.updateAutoSuggestText(isPlural = ime.checkIfPluralWord)
            } else {
                ime.handleElseCondition(code, ime.keyboardMode, ime.keyboardBinding, commandBarState = true)
                ime.disableAutoSuggest()
            }
        }
    }
}
