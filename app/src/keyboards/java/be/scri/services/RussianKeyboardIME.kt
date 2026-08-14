// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.helpers.KeyHandler

/**
 * The RussianKeyboardIME class provides the input method for the Russian language keyboard.
 */
class RussianKeyboardIME : GeneralKeyboardIME("Russian") {
    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_russian_tablet
            isPeriodAndCommaEnabled() -> R.xml.keys_letters_russian
            else -> R.xml.keys_letters_russian_without_period_and_comma
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
