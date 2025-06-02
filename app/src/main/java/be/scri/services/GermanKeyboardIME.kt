// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyHandler
import be.scri.helpers.KeyboardBase
import be.scri.helpers.PreferencesHelper.getEnablePeriodAndCommaABC
import be.scri.helpers.PreferencesHelper.getIsAccentCharacterDisabled
import be.scri.helpers.PreferencesHelper.getIsPreviewEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.views.KeyboardView

/**
 * The GermanKeyboardIME class provides the input method for the German language keyboard.
 */
class GermanKeyboardIME : GeneralKeyboardIME("German") {
    companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    /**
     * Returns the XML layout resource for the keyboard based on user preferences.
     * @return The resource ID of the keyboard layout XML.
     */
    override fun getKeyboardLayoutXML(): Int =
        if (isTablet()) {
            R.xml.keys_letters_german_tablet
        } else if (getIsAccentCharacterDisabled(applicationContext, language) &&
            !getEnablePeriodAndCommaABC(applicationContext, language)
        ) {
            R.xml.keys_letter_german_without_accent_characters_and_without_period_and_comma
        } else if (!getIsAccentCharacterDisabled(applicationContext, language) &&
            getEnablePeriodAndCommaABC(applicationContext, language)
        ) {
            R.xml.keys_letters_german
        } else if (getIsAccentCharacterDisabled(applicationContext, language) &&
            getEnablePeriodAndCommaABC(applicationContext, language)
        ) {
            R.xml.keys_letter_german_without_accent_characters
        } else {
            R.xml.keys_letter_german_without_period_and_comma
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

    // Key handling logic extracted to a separate class
    private val keyHandler = KeyHandler(this)

    /**
     * Creates and returns the input view for the keyboard.
     * @return The root view of the keyboard layout.
     */
    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        setupCommandBarTheme(binding)
        val keyboardHolder = binding.root
        Log.i("MY-TAG", "From German Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView!!.setVibrate = getIsVibrateEnabled(applicationContext, language)
        when (currentState) {
            ScribeState.IDLE -> keyboardView!!.setEnterKeyColor(null)
            else -> keyboardView!!.setEnterKeyColor(R.color.dark_scribe_blue)
        }
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        initializeEmojiButtons()
        updateUI()
        return keyboardHolder
    }

    /**
     * Handles key press events on the keyboard.
     * @param code The key code of the pressed key.
     */
    override fun onKey(code: Int) {
        keyHandler.handleKey(code,language)
    }

    /**
     * Initializes the keyboard and sets up the input view.
     */
    override fun onCreate() {
        super.onCreate()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }
}
