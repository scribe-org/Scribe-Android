// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import be.scri.R
import be.scri.helpers.KeyboardLayoutHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneralKeyboardIMEInputTypeTest {
    @Test
    fun shouldUseNumericKeyboard_returnsTrueForNumberInputs() {
        val inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        assertTrue(KeyboardLayoutHandler.shouldUseNumericKeyboard(inputType))
    }

    @Test
    fun shouldUseNumericKeyboard_returnsTrueForDateTimeInputs() {
        val inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE

        assertTrue(KeyboardLayoutHandler.shouldUseNumericKeyboard(inputType))
    }

    @Test
    fun shouldUseNumericKeyboard_returnsTrueForPhoneInputs() {
        assertTrue(KeyboardLayoutHandler.shouldUseNumericKeyboard(InputType.TYPE_CLASS_PHONE))
    }

    @Test
    fun shouldUseNumericKeyboard_returnsFalseForTextInputs() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        assertFalse(KeyboardLayoutHandler.shouldUseNumericKeyboard(inputType))
    }

    @Test
    fun getKeyboardLayoutXMLForInputType_returnsNumericLayoutForNumberInputs() {
        val inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        assertEquals(
            R.xml.keys_numeric,
            KeyboardLayoutHandler.getKeyboardLayoutXMLForInputType(inputType, R.xml.keys_letters_english),
        )
    }

    @Test
    fun getKeyboardLayoutXMLForInputType_returnsLetterLayoutForTextInputs() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        assertEquals(
            R.xml.keys_letters_english,
            KeyboardLayoutHandler.getKeyboardLayoutXMLForInputType(inputType, R.xml.keys_letters_english),
        )
    }
}
