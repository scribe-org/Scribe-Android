// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
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
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_english_tablet
            getEnablePeriodAndCommaABC(applicationContext, language) -> R.xml.keys_letters_english
            else -> R.xml.keys_letters_english_without_period_and_comma
        }

    // --- Step 1: Fulfill the contract for the abstract 'val' properties ---
    override val keyboardLetters: Int = 0
    override val keyboardSymbols: Int = 1
    override val keyboardSymbolShift: Int = 2
    override var keyboard: KeyboardBase? = null
    override var keyboardView: KeyboardView? = null
    override var lastShiftPressTS: Long = 0L
    override var keyboardMode: Int = keyboardLetters // Default to letters
    override var inputTypeClass: Int = InputType.TYPE_CLASS_TEXT
    override var enterKeyType: Int = IME_ACTION_NONE
    override var switchToLetters: Boolean = false
    override var hasTextBeforeCursor: Boolean = false
    override lateinit var binding: KeyboardViewCommandOptionsBinding

    private val keyHandler by lazy { KeyHandler(this) }

    /**
     * Initializes the keyboard. This is where the magic happens.
     */
    override fun onCreate() {
        super.onCreate()

        // Now, we can add customizations specific to the English keyboard
        keyboardView?.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView?.setVibrate = getIsVibrateEnabled(applicationContext, language)
    }

    /**
     * Handles key input from the keyboard and delegates it to [KeyHandler].
     */
    override fun onKey(code: Int) {
        keyHandler.handleKey(code, language)
    }
}
