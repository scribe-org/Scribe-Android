// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The input method (IME) for the English language keyboard.
 */

package be.scri.services

import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyboardBase
import be.scri.views.KeyboardView

class EnglishKeyboardIME : GeneralKeyboardIME("English") {
    companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
        const val MAX_TEXT_LENGTH = 1000
        const val COMMIT_TEXT_CURSOR_POSITION = 1
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
}
