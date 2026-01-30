// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.services

import android.view.inputmethod.InputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneralKeyboardIMETest {
    private lateinit var ime: GeneralKeyboardIME
    private lateinit var inputConnection: InputConnection

    @Before
    fun setUp() {
        inputConnection = mockk(relaxed = true)

        ime = mockk(relaxed = true)

        every { ime.currentInputConnection } returns inputConnection

        every { ime.hasTextBeforeCursor } answers {
            val ic = ime.currentInputConnection
            if (ic == null) {
                false
            } else {
                val text = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.toString() ?: ""
                text.isNotEmpty() && text.trim() != "." && text.trim() != ""
            }
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
}
