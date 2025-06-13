// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
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
 * The SwedishKeyboardIME class provides the input method for the Swedish language keyboard.
 */
class SwedishKeyboardIME : GeneralKeyboardIME("Swedish") {
    companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    /**
     * Returns the XML layout resource for the keyboard based on user preferences.
     * @return The resource ID of the keyboard layout XML.
     */
    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_swedish_tablet
            getIsAccentCharacterDisabled(applicationContext, language) &&
                !getEnablePeriodAndCommaABC(applicationContext, language) ->
                R.xml.keys_letter_swedish_without_accent_characters_and_without_period_and_comma
            !getIsAccentCharacterDisabled(applicationContext, language) &&
                getEnablePeriodAndCommaABC(applicationContext, language) ->
                R.xml.keys_letters_swedish
            getIsAccentCharacterDisabled(applicationContext, language) &&
                getEnablePeriodAndCommaABC(applicationContext, language) ->
                R.xml.keys_letter_swedish_without_accent_characters
            else ->
                R.xml.keys_letter_swedish_without_period_and_comma
        }

    // --- Fulfill the abstract contract from GeneralKeyboardIME ---
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

    // Key handling logic extracted to a separate class
    private val keyHandler by lazy { KeyHandler(this) }

    /**
     * Initializes the keyboard. Let the parent class handle the setup.
     */
    override fun onCreate() {
        // 1. Let the parent class initialize everything first. This is crucial.
        super.onCreate()

        // 2. Now, apply any customizations specific to this keyboard.
        keyboardView?.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView?.setVibrate = getIsVibrateEnabled(applicationContext, language)
    }

    /**
     * Handles key press events on the keyboard.
     * @param code The key code of the pressed key.
     */
    override fun onKey(code: Int) {
        keyHandler.handleKey(code, language)
    }
}
