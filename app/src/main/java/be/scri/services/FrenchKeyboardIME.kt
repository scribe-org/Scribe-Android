// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyHandler
import be.scri.helpers.KeyboardBase
import be.scri.helpers.PreferencesHelper.getEnablePeriodAndCommaABC
import be.scri.helpers.PreferencesHelper.getIsPreviewEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.views.KeyboardView

/**
 * The FrenchKeyboardIME class provides the input method for the French language keyboard.
 */
class FrenchKeyboardIME : GeneralKeyboardIME("French") {
    companion object {
        // Define the smallest screen width threshold for tablets
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    /**
     * Determines if the device is a tablet based on screen width.
     * @return True if the device is a tablet, false otherwise.
     */
    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    /**
     * Returns the XML layout resource for the keyboard based on user preferences.
     * @return The resource ID of the keyboard layout XML.
     */
    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_french_tablet
            getEnablePeriodAndCommaABC(applicationContext, language) -> R.xml.keys_letters_french
            else -> R.xml.keys_letter_french_without_period_and_comma
        }

    // Keyboard modes
    override val keyboardLetters = 0
    override val keyboardSymbols = 1
    override val keyboardSymbolShift = 2

    // Keyboard components
    override var keyboard: KeyboardBase? = null
    override var keyboardView: KeyboardView? = null
    override var lastShiftPressTS = 0L
    override var keyboardMode = keyboardLetters
    override var inputTypeClass = InputType.TYPE_CLASS_TEXT
    override var enterKeyType = IME_ACTION_NONE
    override var switchToLetters = false
    override var hasTextBeforeCursor = false
    override lateinit var binding: KeyboardViewCommandOptionsBinding

    private val keyHandler = KeyHandler(this)

    /**
     * Creates and returns the input view for the keyboard.
     * @return The root view of the keyboard layout.
     */
    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        setupCommandBarTheme(binding)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView!!.setVibrate = getIsVibrateEnabled(applicationContext, language)

        // Update the enter key color based on the current state
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
