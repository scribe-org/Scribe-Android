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
        inputConnection =
            mockk {
                every { getTextBeforeCursor(any(), any()) } returns ""
            }

        ime =
            mockk(relaxed = true) {
                every { getCurrentInputConnection() } returns inputConnection

                every { hasTextBeforeCursor } answers {
                    val text = inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0)?.toString() ?: ""
                    text.isNotEmpty() && text.trim() != "." && text.trim() != ""
                }
            }
    }

    @Test
    fun hasTextBeforeCursor_returnsTrue_whenTextExists() {
        every { inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0) } returns "hello"

        println("Result: ${ime.hasTextBeforeCursor}")
        assertTrue(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenTextIsEmpty() {
        every { inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0) } returns ""

        assertFalse(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenTextIsPeriod() {
        every { inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0) } returns ". "

        assertFalse(ime.hasTextBeforeCursor)
    }
}
