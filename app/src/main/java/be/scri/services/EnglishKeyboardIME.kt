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
 * The EnglishKeyboardIME class provides the input method for the English language keyboard.
 */
class EnglishKeyboardIME : GeneralKeyboardIME("English") {
    companion object {
        /**
         * Threshold value (in dp) for determining if the device is a tablet.
         */
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    /**
     * Checks whether the current device is a tablet based on smallest screen width.
     *
     * @return Boolean true if the device is a tablet, false otherwise.
     */
    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    /**
     * Returns the appropriate keyboard layout XML resource based on device type and user preferences.
     *
     * @return Int XML layout resource ID.
     */
    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_english_tablet
            getEnablePeriodAndCommaABC(applicationContext, language) -> R.xml.keys_letters_english
            else -> R.xml.keys_letters_english_without_period_and_comma
        }

    /**
     * Constant representing the letter keyboard mode.
     */
    override val keyboardLetters = 0

    /**
     * Constant representing the symbol keyboard mode.
     */
    override val keyboardSymbols = 1

    /**
     * Constant representing the shifted symbol keyboard mode.
     */
    override val keyboardSymbolShift = 2

    /**
     * The active keyboard layout instance.
     */
    override var keyboard: KeyboardBase? = null

    /**
     * The UI view component used to display the keyboard.
     */
    override var keyboardView: KeyboardView? = null

    /**
     * Timestamp of the last time the shift key was pressed.
     */
    override var lastShiftPressTS = 0L

    /**
     * The current mode of the keyboard (e.g., letters, symbols).
     */

    override var keyboardMode = keyboardLetters

    /**
     * Defines the input type class (e.g., text, number).
     */
    override var inputTypeClass = InputType.TYPE_CLASS_TEXT

    /**
     * Defines the type of Enter key action (e.g., none, done, send).
     */
    override var enterKeyType = IME_ACTION_NONE

    /**
     * Indicates whether to switch back to letter mode after a symbol is typed.
     */

    override var switchToLetters = false

    /**
     * Indicates whether there is text before the current cursor position.
     */
    override var hasTextBeforeCursor = false

    /**
     * Binding for the command options layout used with the keyboard view.
     */
    override lateinit var binding: KeyboardViewCommandOptionsBinding

    // Key handling logic extracted to a separate class

    /**
     * Instance of KeyHandler responsible for processing key input events.
     */
    private val keyHandler = KeyHandler(this)

    /**
     * Inflates and returns the keyboard view layout, sets up properties and listeners.
     *
     * @return View The root view of the keyboard UI.
     */

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        setupCommandBarTheme(binding)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView!!.setVibrate = getIsVibrateEnabled(applicationContext, language)
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
     * Handles key input from the keyboard and delegates it to [KeyHandler].
     *
     * @param code The integer code of the key that was pressed.
     */
    override fun onKey(code: Int) {
        keyHandler.handleKey(code)
    }

    /**
     * Initializes the keyboard and sets up the input view upon service creation.
     */

    override fun onCreate() {
        super.onCreate()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }
}
