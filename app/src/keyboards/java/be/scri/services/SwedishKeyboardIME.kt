// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.helpers.KeyHandler
import be.scri.helpers.PreferencesHelper.getIsAccentCharacterDisabled

/**
 * The SwedishKeyboardIME class provides the input method for the Swedish language keyboard.
 */
class SwedishKeyboardIME : GeneralKeyboardIME("Swedish") {
    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_swedish_tablet
            getIsAccentCharacterDisabled(applicationContext, language) &&
                !isPeriodAndCommaEnabled() ->
                R.xml.keys_letter_swedish_without_accent_characters_and_without_period_and_comma
            !getIsAccentCharacterDisabled(applicationContext, language) &&
                isPeriodAndCommaEnabled() ->
                R.xml.keys_letters_swedish
            getIsAccentCharacterDisabled(applicationContext, language) &&
                isPeriodAndCommaEnabled() ->
                R.xml.keys_letter_swedish_without_accent_characters
            else ->
                R.xml.keys_letter_swedish_without_period_and_comma
        }

    override val defaultConjugateModeType: String = "2x2"
    override val defaultConjugateLayoutXML: Int = R.xml.conjugate_view_2x2

    override val keyboardLetters: Int = 0
    override val keyboardSymbols: Int = 1
    override val keyboardSymbolShift: Int = 2
    override var lastShiftPressTS: Long = 0L
    override var keyboardMode: Int = keyboardLetters
    override var inputTypeClass: Int = InputType.TYPE_CLASS_TEXT
    override var enterKeyType: Int = IME_ACTION_NONE
    override var switchToLetters: Boolean = false

    private val keyHandler by lazy { KeyHandler(this) }

    override fun onKey(code: Int) {
        keyHandler.handleKey(code, language)
    }
}
