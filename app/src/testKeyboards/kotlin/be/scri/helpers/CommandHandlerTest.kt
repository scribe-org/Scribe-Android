// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.view.inputmethod.InputConnection
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CommandHandlerTest {
    private val ime = mockk<GeneralKeyboardIME>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private lateinit var handler: CommandHandler

    @Before
    fun setUp() {
        every { ime.currentInputConnection } returns inputConnection
        handler = CommandHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun handleKeycodeEnter_nullInputConnection_doesNothing() {
        every { ime.currentInputConnection } returns null

        handler.handleKeycodeEnter()

        verify(exactly = 0) { ime.moveToIdleState() }
    }

    @Test
    fun handleKeycodeEnter_invalidState_movesToIdle() {
        every { ime.currentState } returns ScribeState.INVALID

        handler.handleKeycodeEnter()

        verify { ime.moveToIdleState() }
    }

    @Test
    fun handleKeycodeEnter_alreadyPluralState_movesToIdle() {
        every { ime.currentState } returns ScribeState.ALREADY_PLURAL

        handler.handleKeycodeEnter()

        verify { ime.moveToIdleState() }
    }

    @Test
    fun applyCommandOutput_nonEmptyText_commitsTextAndMovesToIdle() {
        handler.applyCommandOutput("translated text", inputConnection)

        verify { inputConnection.commitText("translated text ", 1) }
        verify { ime.moveToIdleState() }
    }
}
