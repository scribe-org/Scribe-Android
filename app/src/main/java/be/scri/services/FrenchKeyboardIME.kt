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
 * The FrenchKeyboardIME class provides the input method for the French language keyboard.
 */
class FrenchKeyboardIME : GeneralKeyboardIME("French") {
    companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_french_tablet
            getEnablePeriodAndCommaABC(applicationContext, language) -> R.xml.keys_letters_french
            else -> R.xml.keys_letter_french_without_period_and_comma
        }

    // --- These properties correctly fulfill the abstract contract ---
    override val keyboardLetters: Int = 0
    override val keyboardSymbols: Int = 1
    override val keyboardSymbolShift: Int = 2

    override var keyboard: KeyboardBase? = null
    override var keyboardView: KeyboardView? = null
    override var lastShiftPressTS: Long = 0L
    override var keyboardMode: Int = keyboardLetters
    override var inputTypeClass: Int = InputType.TYPE_CLASS_TEXT
    override var enterKeyType: Int = IME_ACTION_NONE
    override var switchToLetters: Boolean = false
    override var hasTextBeforeCursor: Boolean = false
    override lateinit var binding: KeyboardViewCommandOptionsBinding

    private val keyHandler by lazy { KeyHandler(this) }

    /**
     * Initializes the keyboard. Let the parent class handle the setup.
     */
    override fun onCreate() {
        // 1. Let the parent class initialize everything first.
        super.onCreate()

        // 2. Now, apply any customizations specific to this keyboard.
        keyboardView?.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView?.setVibrate = getIsVibrateEnabled(applicationContext, language)
    }

    /**
     * Handles key press events on the keyboard.
     */
    override fun onKey(code: Int) {
        keyHandler.handleKey(code, language)
    }
}
