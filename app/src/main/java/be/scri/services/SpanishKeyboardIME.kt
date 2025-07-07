// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.helpers.KeyHandler
import be.scri.helpers.PreferencesHelper.getIsAccentCharacterDisabled

/**
 * The SpanishKeyboardIME class provides the input method for the Spanish language keyboard.
 */
class SpanishKeyboardIME : GeneralKeyboardIME("Spanish") {
    companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_spanish_tablet
            getIsAccentCharacterDisabled(applicationContext, language) &&
                !isPeriodAndComaEnabled() ->
                R.xml.keys_letter_spanish_without_accent_characters_and_without_period_and_comma
            !getIsAccentCharacterDisabled(applicationContext, language) &&
                isPeriodAndComaEnabled() ->
                R.xml.keys_letters_spanish
            getIsAccentCharacterDisabled(applicationContext, language) &&
                isPeriodAndComaEnabled() ->
                R.xml.keys_letter_spanish_without_accent_character
            else ->
                R.xml.keys_letter_spanish_without_period_and_comma
        }

    override val keyboardLetters: Int = 0
    override val keyboardSymbols: Int = 1
    override val keyboardSymbolShift: Int = 2
    override var lastShiftPressTS: Long = 0L
    override var keyboardMode: Int = keyboardLetters
    override var inputTypeClass: Int = InputType.TYPE_CLASS_TEXT
    override var enterKeyType: Int = IME_ACTION_NONE
    override var switchToLetters: Boolean = false
    override var hasTextBeforeCursor: Boolean = false

    private val keyHandler by lazy { KeyHandler(this) }

    override fun onKey(code: Int) {
        keyHandler.handleKey(code, language)
    }
}
