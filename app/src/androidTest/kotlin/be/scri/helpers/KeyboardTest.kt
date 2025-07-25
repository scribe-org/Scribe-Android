// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui

import android.view.inputmethod.InputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.helpers.KeyHandler
import be.scri.helpers.KeyboardBase
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardTest {
    private lateinit var mockIME: GeneralKeyboardIME
    private lateinit var mockInputConnection: InputConnection
    private lateinit var keyHandler: KeyHandler

    @Before
    fun setUp() {
        mockIME = mockk(relaxed = true)
        mockInputConnection = mockk(relaxed = true)

        every { mockIME.currentInputConnection } returns mockInputConnection
        every { mockIME.keyboard } returns mockk(relaxed = true)
        every { mockIME.currentState } returns ScribeState.IDLE

        keyHandler = KeyHandler(mockIME)
    }

    @Test
    fun testBackspaceKeyCallsHandleDelete() {
        every { mockIME.currentState } returns ScribeState.IDLE

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(false) }
    }

    @Test
    fun testBackspaceInTranslateMode() {
        every { mockIME.currentState } returns ScribeState.TRANSLATE

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(true) }
    }

    @Test
    fun testBackspaceInConjugateMode() {
        every { mockIME.currentState } returns ScribeState.CONJUGATE

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(true) }
    }

    @Test
    fun testBackspaceInPluralMode() {
        every { mockIME.currentState } returns ScribeState.PLURAL

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(true) }
    }

    @Test
    fun testBackspaceWithNullInputConnection() {
        every { mockIME.currentInputConnection } returns null

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 0) { mockIME.handleDelete(any()) }
    }

    @Test
    fun testBackspaceWithNullKeyboard() {
        every { mockIME.keyboard } returns null

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 0) { mockIME.handleDelete(any()) }
    }

    @Test
    fun testBackspaceTriggersEmojiSuggestionsInIdleState() {
        every { mockIME.currentState } returns ScribeState.IDLE
        every { mockIME.getLastWordBeforeCursor() } returns "test"

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(false) }
        verify(exactly = 1) { mockIME.getLastWordBeforeCursor() }
    }

    @Test
    fun testBackspaceDoesNotTriggerSuggestionsInCommandMode() {
        every { mockIME.currentState } returns ScribeState.TRANSLATE

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(true) }
        verify(exactly = 0) { mockIME.getLastWordBeforeCursor() }
    }

    @Test
    fun testMultipleBackspacePresses() {
        every { mockIME.currentState } returns ScribeState.IDLE

        repeat(3) {
            keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")
        }

        verify(exactly = 3) { mockIME.handleDelete(false) }
    }

    @Test
    fun testBackspaceResetsWasLastKeySpace() {
        every { mockIME.currentState } returns ScribeState.IDLE

        keyHandler.handleKey(KeyboardBase.KEYCODE_SPACE, "en")
        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        verify(exactly = 1) { mockIME.handleDelete(false) }
    }
}
