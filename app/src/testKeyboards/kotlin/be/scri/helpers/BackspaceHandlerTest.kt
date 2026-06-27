// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.view.inputmethod.InputConnection
import androidx.test.core.app.ApplicationProvider
import be.scri.services.GeneralKeyboardIME
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackspaceHandlerTest {
    private lateinit var context: Context
    private lateinit var ime: GeneralKeyboardIME
    private lateinit var inputConnection: InputConnection
    private lateinit var backspaceHandler: BackspaceHandler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        inputConnection = mockk(relaxed = true)
        ime = mockk(relaxed = true)

        every { ime.currentInputConnection } returns inputConnection
        every { ime.applicationContext } returns context
        every { ime.language } returns "English"

        backspaceHandler = BackspaceHandler(ime)

        mockkObject(PreferencesHelper)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `performSwipeDelete with word deletion disabled deletes single character`() {
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns false
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "Hello World"

        backspaceHandler.performSwipeDelete()

        // Should delete 1 character ("d")
        verify { inputConnection.deleteSurroundingText(1, 0) }
    }

    @Test
    fun `performSwipeDelete with word deletion enabled deletes last word`() {
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns true
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "Hello World "

        backspaceHandler.performSwipeDelete()

        // For "Hello World ", the last word is "World" plus trailing space " ", length = 6
        verify { inputConnection.deleteSurroundingText(6, 0) }
    }

    @Test
    fun `performSwipeRestore commits previously deleted chunk`() {
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns false
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "Hello World"

        // Perform delete of "d"
        backspaceHandler.performSwipeDelete()

        // Perform restore
        backspaceHandler.performSwipeRestore()

        // Should commit "d" back
        verify { inputConnection.commitText("d", 1) }
    }

    @Test
    fun `clearUndoStack prevents restoration`() {
        every { PreferencesHelper.getIsWordByWordDeletionEnabled(any(), any()) } returns false
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns "Hello World"

        // Perform delete of "d"
        backspaceHandler.performSwipeDelete()

        // Clear stack
        backspaceHandler.clearUndoStack()

        // Perform restore
        backspaceHandler.performSwipeRestore()

        // Should NOT commit anything
        verify(exactly = 0) { inputConnection.commitText(any(), any()) }
    }
}
