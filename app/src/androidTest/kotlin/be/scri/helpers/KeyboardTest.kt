// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    private lateinit var suggestionHandler: SuggestionHandler
    private lateinit var translateBtn: Button
    private lateinit var conjugateBtn: Button
    private lateinit var pluralBtn: Button

    @Before
    fun setUp() {
        mockIME = mockk(relaxed = true)
        mockInputConnection = mockk(relaxed = true)

        every { mockIME.currentInputConnection } returns mockInputConnection
        every { mockIME.keyboard } returns mockk(relaxed = true)
        every { mockIME.currentState } returns ScribeState.IDLE
        every { mockIME.language } returns "German"

        keyHandler = KeyHandler(mockIME)
        suggestionHandler = SuggestionHandler(mockIME)
        translateBtn = mockk(relaxed = true)
        conjugateBtn = mockk(relaxed = true)
        pluralBtn = mockk(relaxed = true)
        every { mockIME.binding.translateBtn } returns translateBtn
        every { mockIME.binding.conjugateBtn } returns conjugateBtn
        every { mockIME.binding.pluralBtn } returns pluralBtn
    }

    @Test
    fun testBackspaceReducesStringLengthByOne() {
        val initialText = "Hello World"
        var currentText = initialText

        every { mockIME.currentState } returns ScribeState.IDLE
        every { mockInputConnection.getTextBeforeCursor(any(), any()) } returns currentText
        every { mockIME.handleDelete(false) } answers {
            if (currentText.isNotEmpty()) {
                currentText = currentText.dropLast(1)
            }
            mockInputConnection.deleteSurroundingText(1, 0)
        }
        every { mockInputConnection.deleteSurroundingText(1, 0) } returns true

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        assert(currentText.length == initialText.length - 1) {
            "Expected length ${initialText.length - 1}, but got ${currentText.length}"
        }
        assert(currentText == "Hello Worl") {
            "Expected 'Hello Worl', but got '$currentText'"
        }

        verify(exactly = 1) { mockIME.handleDelete(false) }
        verify(exactly = 1) { mockInputConnection.deleteSurroundingText(1, 0) }
    }

    @Test
    fun testEnterKeyInsertsNewLine() {
        // Arrange
        every { mockIME.currentState } returns ScribeState.IDLE
        // Simulate the IME behavior for Enter key: sending ACTION_DOWN and ACTION_UP events
        every { mockIME.handleKeycodeEnter() } answers {
            mockInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            mockInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }

        // Act
        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

        // Assert
        verify(exactly = 1) { mockIME.handleKeycodeEnter() }
        verify(exactly = 1) {
            mockInputConnection.sendKeyEvent(match { it.action == KeyEvent.ACTION_DOWN && it.keyCode == KeyEvent.KEYCODE_ENTER })
        }
        verify(exactly = 1) {
            mockInputConnection.sendKeyEvent(match { it.action == KeyEvent.ACTION_UP && it.keyCode == KeyEvent.KEYCODE_ENTER })
        }
    }

    @Test
    fun processSuggestions() {
        every { mockIME.findGenderForLastWord(any(), "in") } returns listOf("Neuter")
        every { mockIME.findWhetherWordIsPlural(any(), "in") } returns false
        every { mockIME.getCaseAnnotationForPreposition(any(), "in") } returns null

        every { mockIME.updateAutoSuggestText(any(), any(), any(), any()) } answers {
            conjugateBtn.text = "der"
            pluralBtn.text = "den"
            translateBtn.text = "die"
        }

        suggestionHandler.processLinguisticSuggestions("in")

        verify { conjugateBtn.text = match { it.isNotEmpty() } }
        verify { pluralBtn.text = match { it.isNotEmpty() } }
        verify { translateBtn.text = match { it.isNotEmpty() } }
    }
}
