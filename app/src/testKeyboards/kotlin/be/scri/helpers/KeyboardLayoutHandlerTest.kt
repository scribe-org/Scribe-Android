// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_TEXT
import be.scri.R
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeyboardLayoutHandlerTest {
    private lateinit var mockIme: GeneralKeyboardIME
    private lateinit var layoutHandler: KeyboardLayoutHandler

    @Before
    fun setUp() {
        mockIme = mockk(relaxed = true)
        layoutHandler = KeyboardLayoutHandler(mockIme)
    }

    @Test
    fun testGetPrimarySymbolKeyboardLayoutXML_numericActive() {
        every { mockIme.isNumericKeyboardActive } returns true
        val xmlResId = layoutHandler.getPrimarySymbolKeyboardLayoutXML()
        assertEquals(R.xml.keys_numeric, xmlResId)
    }

    @Test
    fun testGetPrimarySymbolKeyboardLayoutXML_symbolsActive() {
        every { mockIme.isNumericKeyboardActive } returns false
        val xmlResId = layoutHandler.getPrimarySymbolKeyboardLayoutXML()
        assertEquals(R.xml.keys_symbols, xmlResId)
    }

    @Test
    fun testGetCurrentKeyboardLayoutXML_letterMode() {
        every { mockIme.keyboardMode } returns 0
        every { mockIme.keyboardSymbols } returns 1
        every { mockIme.keyboardSymbolShift } returns 2
        every { mockIme.getKeyboardLayoutXML() } returns R.xml.keys_symbols

        val xmlResId = layoutHandler.getCurrentKeyboardLayoutXML()
        assertEquals(R.xml.keys_symbols, xmlResId)
    }

    @Test
    fun testGetCurrentKeyboardLayoutXML_symbolShiftMode() {
        every { mockIme.keyboardMode } returns 2
        every { mockIme.keyboardSymbols } returns 1
        every { mockIme.keyboardSymbolShift } returns 2

        val xmlResId = layoutHandler.getCurrentKeyboardLayoutXML()
        assertEquals(R.xml.keys_symbols_shift, xmlResId)
    }

    @Test
    fun testGetKeyboardLayoutForState_verbConjunction_dataSize2() {
        every { mockIme.saveConjugateModeType(any()) } returns Unit
        val xmlResId = layoutHandler.getKeyboardLayoutForState(ScribeState.SELECT_VERB_CONJUNCTION, isSubsequentArea = true, dataSize = 2)
        assertEquals(R.xml.conjugate_view_2x1, xmlResId)
    }

    @Test
    fun testGetKeyboardLayoutForState_defaultState() {
        every { mockIme.getKeyboardLayoutXML() } returns R.xml.keys_symbols
        val xmlResId = layoutHandler.getKeyboardLayoutForState(ScribeState.IDLE)
        assertEquals(R.xml.keys_symbols, xmlResId)
    }

    @Test
    fun testShouldUseNumericKeyboard() {
        assertTrue(KeyboardLayoutHandler.shouldUseNumericKeyboard(TYPE_CLASS_NUMBER))
        assertFalse(KeyboardLayoutHandler.shouldUseNumericKeyboard(TYPE_CLASS_TEXT))
    }

    @Test
    fun testGetKeyboardLayoutXMLForInputType() {
        assertEquals(
            R.xml.keys_numeric,
            KeyboardLayoutHandler.getKeyboardLayoutXMLForInputType(TYPE_CLASS_NUMBER, R.xml.keys_symbols),
        )
        assertEquals(
            R.xml.keys_symbols,
            KeyboardLayoutHandler.getKeyboardLayoutXMLForInputType(TYPE_CLASS_TEXT, R.xml.keys_symbols),
        )
    }
}
