// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneralKeyboardIMEInputTypeTest {
    @Test
    fun shouldUseNumericKeyboard_returnsTrueForNumberInputs() {
        val inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        assertTrue(GeneralKeyboardIME.shouldUseNumericKeyboard(inputType))
    }

    @Test
    fun shouldUseNumericKeyboard_returnsTrueForDateTimeInputs() {
        val inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE

        assertTrue(GeneralKeyboardIME.shouldUseNumericKeyboard(inputType))
    }

    @Test
    fun shouldUseNumericKeyboard_returnsTrueForPhoneInputs() {
        assertTrue(GeneralKeyboardIME.shouldUseNumericKeyboard(InputType.TYPE_CLASS_PHONE))
    }

    @Test
    fun shouldUseNumericKeyboard_returnsFalseForTextInputs() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        assertFalse(GeneralKeyboardIME.shouldUseNumericKeyboard(inputType))
    }
}
