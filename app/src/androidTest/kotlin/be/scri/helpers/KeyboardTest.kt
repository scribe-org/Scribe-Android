// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.view.inputmethod.EditorInfo
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

// Unit tests for KeyHandler
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
    fun testEnterKeyInsertsNewLineForNormalInput() {
        every { mockIME.currentInputEditorInfo } returns EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_NONE }
        every { mockInputConnection.commitText(any(), any()) } returns true

        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

        verify(exactly = 1) { mockInputConnection.commitText("\n", 1) }
        verify(exactly = 0) { mockInputConnection.sendKeyEvent(any()) }
    }

    @Test
    fun testEnterKeySendsActionForSearch() {
        every { mockIME.currentInputEditorInfo } returns EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_SEARCH }
        every { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH) } returns true

        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

        verify(exactly = 1) { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH) }
        verify(exactly = 0) { mockInputConnection.commitText(any(), any()) }
        verify(exactly = 0) { mockInputConnection.sendKeyEvent(any()) }
    }

    @Test
    fun testEnterKeySendsActionForSend() {
        every { mockIME.currentInputEditorInfo } returns EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_SEND }
        every { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND) } returns true

        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

        verify(exactly = 1) { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND) }
        verify(exactly = 0) { mockInputConnection.commitText(any(), any()) }
        verify(exactly = 0) { mockInputConnection.sendKeyEvent(any()) }
    }

    @Test
    fun testEnterKeySendsActionForDone() {
        every { mockIME.currentInputEditorInfo } returns EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_DONE }
        every { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE) } returns true

        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

        verify(exactly = 1) { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE) }
        verify(exactly = 0) { mockInputConnection.commitText(any(), any()) }
        verify(exactly = 0) { mockInputConnection.sendKeyEvent(any()) }
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
