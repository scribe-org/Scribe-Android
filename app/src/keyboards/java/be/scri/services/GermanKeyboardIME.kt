// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.helpers.KeyHandler
import be.scri.helpers.PreferencesHelper.getIsAccentCharacterDisabled
import be.scri.models.ScribeLanguage

/**
 * The GermanKeyboardIME class provides the input method for the German language keyboard.
 */
class GermanKeyboardIME : GeneralKeyboardIME(ScribeLanguage.GERMAN) {
    override fun getKeyboardLayoutXML(): Int =
        if (isTablet()) {
            R.xml.keys_letters_german_tablet
        } else if (getIsAccentCharacterDisabled(applicationContext, language) &&
            !isPeriodAndCommaEnabled()
        ) {
            R.xml.keys_letter_german_without_accent_characters_and_without_period_and_comma
        } else if (!getIsAccentCharacterDisabled(applicationContext, language) &&
            isPeriodAndCommaEnabled()
        ) {
            R.xml.keys_letters_german
        } else if (getIsAccentCharacterDisabled(applicationContext, language) &&
            isPeriodAndCommaEnabled()
        ) {
            R.xml.keys_letter_german_without_accent_characters
        } else {
            R.xml.keys_letter_german_without_period_and_comma
        }

    // Fulfill the abstract contract from GeneralKeyboardIME.
    override val keyboardLetters: Int = 0
    override val keyboardSymbols: Int = 1
    override val keyboardSymbolShift: Int = 2
    override var lastShiftPressTS: Long = 0L
    override var keyboardMode: Int = keyboardLetters
    override var inputTypeClass: Int = InputType.TYPE_CLASS_TEXT
    override var enterKeyType: Int = IME_ACTION_NONE
    override var switchToLetters: Boolean = false
    override var hasTextBeforeCursor: Boolean = false

    // REFACTOR_FIX: The 'binding' and 'keyboardView' properties are no longer abstract in the parent class,
    // so we must remove the overrides here. They are now inherited directly.
    // override lateinit var binding: KeyboardViewCommandOptionsBinding // REMOVED
    // override var keyboardView: KeyboardView? = null // REMOVED

    private val keyHandler by lazy { KeyHandler(this) }

    override fun onKey(code: Int) {
        keyHandler.handleKey(code, language)
    }
}
