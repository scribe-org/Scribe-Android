// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.view.inputmethod.InputConnection
import be.scri.views.KeyboardView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class BackspaceHandlerTest {
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val keyboard = mockk<KeyboardBase>(relaxed = true)
    private val keyboardView = mockk<KeyboardView>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private lateinit var backspaceHandler: BackspaceHandler

    @Before
    fun setUp() {
        mockkObject(PreferencesHelper)
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns false
        every { ime.keyboard } returns keyboard
        every { ime.keyboardView } returns keyboardView
        every { ime.getInputConnection() } returns inputConnection
        every { ime.language } returns "English"
        backspaceHandler = BackspaceHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun handleBackspace_whenInputConnectionNull_doesNotCrash() {
        every { ime.getInputConnection() } returns null

        backspaceHandler.handleBackspace(isCommandBar = false, isLongPress = false)

        verify(exactly = 0) { inputConnection.deleteSurroundingText(any(), any()) }
    }

    @Test
    fun handleBackspace_singleCharacter_deletesOneCharacter() {
        every { inputConnection.getSelectedText(any()) } returns null

        backspaceHandler.handleBackspace(isCommandBar = false, isLongPress = false)

        verify { inputConnection.deleteSurroundingText(1, 0) }
    }

    @Test
    fun handleBackspace_longPress_wordByWordEnabled_deletesWord() {
        every { inputConnection.getSelectedText(any()) } returns null
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns true
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "hello world"

        backspaceHandler.handleBackspace(isCommandBar = false, isLongPress = true)

        verify { inputConnection.deleteSurroundingText(5, 0) }
    }

    @Test
    fun handleBackspace_longPress_wordByWordEnabled_withTrailingSpace_deletesWordAndSpace() {
        every { inputConnection.getSelectedText(any()) } returns null
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns true
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "hello world "

        backspaceHandler.handleBackspace(isCommandBar = false, isLongPress = true)

        verify { inputConnection.deleteSurroundingText(6, 0) }
    }

    @Test
    fun handleBackspace_longPress_wordByWordDisabled_deletesSingleCharacter() {
        every { inputConnection.getSelectedText(any()) } returns null
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns false
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "hello world"

        backspaceHandler.handleBackspace(isCommandBar = false, isLongPress = true)

        verify { inputConnection.deleteSurroundingText(1, 0) }
    }
}
