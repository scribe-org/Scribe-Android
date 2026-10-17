// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.text.InputType
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.InputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.R
import be.scri.helpers.SuggestionHandler
import be.scri.helpers.ui.KeyboardUIManager
import be.scri.models.ScribeLanguage
import be.scri.models.ScribeState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneralKeyboardIMETest {
    private lateinit var ime: TestKeyboardIME
    private lateinit var inputConnection: InputConnection
    private lateinit var uiManager: KeyboardUIManager
    private lateinit var suggestionHandler: SuggestionHandler

    private class TestKeyboardIME : GeneralKeyboardIME(ScribeLanguage.ENGLISH) {
        var mockInputConnection: InputConnection? = null
        var moveToIdleStateCalled = false

        override fun getCurrentInputConnection(): InputConnection? = mockInputConnection

        override fun moveToIdleState() {
            moveToIdleStateCalled = true
        }

        override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_english
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
    }

    @Before
    fun setUp() {
        inputConnection = mockk(relaxed = true)
        uiManager = mockk(relaxed = true)
        suggestionHandler = mockk(relaxed = true)

        ime = TestKeyboardIME().apply {
            mockInputConnection = inputConnection
            this.uiManager = this@GeneralKeyboardIMETest.uiManager
            this.suggestionHandler = this@GeneralKeyboardIMETest.suggestionHandler
        }
    }

    @Test
    fun hasTextBeforeCursor_returnsTrue_whenTextExists() {
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "hello"

        assertTrue(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenTextIsEmpty() {
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns ""

        assertFalse(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenTextIsPeriod() {
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns ". "

        assertFalse(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenInputConnectionIsNull() {
        ime.mockInputConnection = null
        assertFalse(ime.hasTextBeforeCursor)
    }

    @Test
    fun commitText_inConjugationState_handlesNullConjugateOutput_withoutCrashing() {
        ime.currentState = ScribeState.SELECT_VERB_CONJUNCTION
        ime.conjugateOutput = null

        ime.commitText("test")

        assertTrue(ime.moveToIdleStateCalled)
    }
}
