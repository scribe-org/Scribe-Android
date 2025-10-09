// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private enum class EnterKeyExpectation {
    PERFORM_ACTION,
    COMMIT_NEWLINE,
    DEFERRED_TO_IME,
}

/** this repreesents a single test case for the parameterized Enter key test. */
private data class EnterKeyTest(
    val imeAction: Int,
    val scribeState: ScribeState,
    val expectation: EnterKeyExpectation,
)

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
        every { mockIME.handleKeycodeEnter() } returns Unit

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
    fun testEnterKeyBehavior_Parameterized() {
        val testCases =
            listOf(
                EnterKeyTest(EditorInfo.IME_ACTION_NONE, ScribeState.IDLE, EnterKeyExpectation.COMMIT_NEWLINE),
                EnterKeyTest(EditorInfo.IME_ACTION_SEARCH, ScribeState.IDLE, EnterKeyExpectation.PERFORM_ACTION),
                EnterKeyTest(EditorInfo.IME_ACTION_SEND, ScribeState.IDLE, EnterKeyExpectation.PERFORM_ACTION),
                EnterKeyTest(EditorInfo.IME_ACTION_DONE, ScribeState.IDLE, EnterKeyExpectation.PERFORM_ACTION),
                EnterKeyTest(EditorInfo.IME_ACTION_SEND, ScribeState.TRANSLATE, EnterKeyExpectation.DEFERRED_TO_IME),
                EnterKeyTest(EditorInfo.IME_ACTION_NONE, ScribeState.CONJUGATE, EnterKeyExpectation.DEFERRED_TO_IME),
                EnterKeyTest(EditorInfo.IME_ACTION_SEARCH, ScribeState.PLURAL, EnterKeyExpectation.DEFERRED_TO_IME),
            )

        testCases.forEach { case ->
            every { mockIME.currentInputEditorInfo } returns EditorInfo().apply { imeOptions = case.imeAction }
            every { mockIME.currentState } returns case.scribeState

            keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

            when (case.expectation) {
                EnterKeyExpectation.PERFORM_ACTION -> {
                    verify(exactly = 1) { mockInputConnection.performEditorAction(case.imeAction) }
                    verify(exactly = 0) { mockInputConnection.commitText(any(), any()) }
                    verify(exactly = 0) { mockIME.handleKeycodeEnter() }
                }
                EnterKeyExpectation.COMMIT_NEWLINE -> {
                    verify(exactly = 1) { mockInputConnection.commitText("\n", 1) }
                    verify(exactly = 0) { mockInputConnection.performEditorAction(any()) }
                    verify(exactly = 0) { mockIME.handleKeycodeEnter() }
                }
                EnterKeyExpectation.DEFERRED_TO_IME -> {
                    // ensures the  custom command logic takes precedence
                    verify(exactly = 1) { mockIME.handleKeycodeEnter() }
                    verify(exactly = 0) { mockInputConnection.performEditorAction(any()) }
                    verify(exactly = 0) { mockInputConnection.commitText(any(), any()) }
                }
            }

            // cleans up mocks for the next iteration in the loop
            clearMocks(mockInputConnection, mockIME)
            every { mockIME.currentInputConnection } returns mockInputConnection
            // fix: restores the mock using returns Unit
            every { mockIME.handleKeycodeEnter() } returns Unit
            every { mockIME.keyboard } returns mockk(relaxed = true)
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
