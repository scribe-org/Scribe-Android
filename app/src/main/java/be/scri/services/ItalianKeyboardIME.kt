// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.helpers.KeyHandler
import be.scri.helpers.PreferencesHelper.getEnablePeriodAndCommaABC

/**
 * The ItalianKeyboardIME class provides the input method for the Italian language keyboard.
 */
class ItalianKeyboardIME : GeneralKeyboardIME("Italian") {
    companion object {
        const val SMALLEST_SCREEN_WIDTH_TABLET = 600
    }

    private fun isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= SMALLEST_SCREEN_WIDTH_TABLET

    override fun getKeyboardLayoutXML(): Int =
        when {
            isTablet() -> R.xml.keys_letters_italian_tablet
            getEnablePeriodAndCommaABC(applicationContext, language) || isSearchBar() -> R.xml.keys_letters_italian
            else -> R.xml.keys_letter_italian_without_period_and_comma
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
