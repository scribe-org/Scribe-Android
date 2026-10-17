// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.view.inputmethod.InputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.helpers.ui.KeyboardUIManager
import be.scri.models.ScribeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneralKeyboardIMETest {
    private lateinit var ime: GeneralKeyboardIME
    private lateinit var inputConnection: InputConnection
    private lateinit var uiManager: KeyboardUIManager

    @Before
    fun setUp() {
        inputConnection = mockk(relaxed = true)
        uiManager = mockk(relaxed = true)

        ime = spyk(EnglishKeyboardIME()).apply {
            every { currentInputConnection } returns inputConnection
            every { this@apply.uiManager } returns uiManager
            every { suggestionHandler } returns mockk(relaxed = true)
            every { imeContext } returns mockk(relaxed = true)
            every { applicationContext } returns mockk(relaxed = true)
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
        every { ime.currentInputConnection } returns null
        assertFalse(ime.hasTextBeforeCursor)
    }

    @Test
    fun commitText_inConjugationState_handlesNullConjugateOutput_withoutCrashing() {
        ime.currentState = ScribeState.SELECT_VERB_CONJUNCTION
        ime.conjugateOutput = null
        every { ime.moveToIdleState() } returns Unit

        ime.commitText("test")
    }
}
