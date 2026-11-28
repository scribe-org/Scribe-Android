// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.view.inputmethod.InputConnection
import be.scri.models.ScribeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class KeyHandlerTest {
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val keyboard = mockk<KeyboardBase>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private lateinit var keyHandler: KeyHandler

    @Before
    fun setUp() {
        every { ime.keyboard } returns keyboard
        every { ime.getInputConnection() } returns inputConnection
        every { ime.currentState } returns ScribeState.IDLE
        every { ime.language } returns "English"
        keyHandler = KeyHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun handleKey_whenInputConnectionNull_returnsEarly() {
        every { ime.getInputConnection() } returns null

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "English")

        verify(exactly = 0) { ime.handleDelete(any()) }
    }

    @Test
    fun handleKey_whenKeyboardNull_returnsEarly() {
        every { ime.keyboard } returns null

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "English")

        verify(exactly = 0) { ime.handleDelete(any()) }
    }

    @Test
    fun handleKey_deleteKey_invokesHandleDelete() {
        every { ime.isDeleteRepeating() } returns false

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "English")

        verify { ime.handleDelete(false) }
    }

    @Test
    fun handleKey_enterKey_invokesHandleKeycodeEnter() {
        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "English")

        verify { ime.handleKeycodeEnter() }
    }
}
