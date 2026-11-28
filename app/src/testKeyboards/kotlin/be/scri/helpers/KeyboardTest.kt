// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import be.scri.databinding.InputMethodViewBinding
import be.scri.models.ScribeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyboardTest {
    private lateinit var context: Context
    private lateinit var binding: InputMethodViewBinding
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private lateinit var keyHandler: KeyHandler
    private lateinit var suggestionHandler: SuggestionHandler
    private lateinit var translateBtn: Button
    private lateinit var conjugateBtn: Button
    private lateinit var pluralBtn: Button

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))

        every { ime.getInputConnection() } returns inputConnection
        every { ime.keyboard } returns mockk(relaxed = true)
        every { ime.currentState } returns ScribeState.IDLE
        every { ime.language } returns "German"
        every { ime.profanity } returns emptySet()

        translateBtn = binding.translateBtn
        conjugateBtn = binding.conjugateBtn
        pluralBtn = binding.pluralBtn

        every { ime.binding } returns binding

        suggestionHandler = SuggestionHandler(ime)
        every { ime.suggestionHandler } returns suggestionHandler

        keyHandler = KeyHandler(ime)
    }

    @Test
    fun testBackspaceReducesStringLengthByOne() {
        val initialText = "Hello World"
        var currentText = initialText

        every { ime.currentState } returns ScribeState.IDLE
        every { inputConnection.getTextBeforeCursor(any(), any()) } returns currentText
        every { ime.handleDelete(false) } answers {
            if (currentText.isNotEmpty()) {
                currentText = currentText.dropLast(1)
            }
            inputConnection.deleteSurroundingText(1, 0)
        }
        every { inputConnection.deleteSurroundingText(1, 0) } returns true

        keyHandler.handleKey(KeyboardBase.KEYCODE_DELETE, "en")

        assert(currentText.length == initialText.length - 1) {
            "Expected length ${initialText.length - 1}, but got ${currentText.length}"
        }
        assert(currentText == "Hello Worl") {
            "Expected 'Hello Worl', but got '$currentText'"
        }

        verify(exactly = 1) { ime.handleDelete(false) }
        verify(exactly = 1) { inputConnection.deleteSurroundingText(1, 0) }
    }

    @Test
    fun testEnterKeyInsertsNewLine() {
        every { ime.currentState } returns ScribeState.IDLE
        every { ime.handleKeycodeEnter() } answers {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }

        keyHandler.handleKey(KeyboardBase.KEYCODE_ENTER, "en")

        verify(exactly = 1) { ime.handleKeycodeEnter() }
        verify(exactly = 1) {
            inputConnection.sendKeyEvent(match { it.action == KeyEvent.ACTION_DOWN && it.keyCode == KeyEvent.KEYCODE_ENTER })
        }
        verify(exactly = 1) {
            inputConnection.sendKeyEvent(match { it.action == KeyEvent.ACTION_UP && it.keyCode == KeyEvent.KEYCODE_ENTER })
        }
    }

    @Test
    fun testPrepositionTriggersDeclensionSuggestion() {
        val handler = mockk<Handler>(relaxed = true)
        val runnableSlot = slot<Runnable>()
        every { handler.postDelayed(capture(runnableSlot), any()) } returns true

        val preposition = "mit"
        every { ime.language } returns "German"
        every { ime.findGenderForLastWord(any(), any()) } returns null
        every { ime.getCaseAnnotationForPreposition(any(), preposition) } returns mutableListOf("dative case")

        suggestionHandler = SuggestionHandler(ime, handler)
        suggestionHandler.processLinguisticSuggestions(preposition)

        runnableSlot.captured.run()

        verify(exactly = 1) {
            ime.updateAutoSuggestText(
                nounTypeSuggestion = null,
                isPlural = false,
                caseAnnotationSuggestion = mutableListOf("dative case"),
            )
        }
    }

    @Test
    fun processSuggestions() {
        val handler = mockk<Handler>(relaxed = true)
        val runnableSlot = slot<Runnable>()
        every { handler.postDelayed(capture(runnableSlot), any()) } returns true

        every { ime.findGenderForLastWord(any(), "in") } returns listOf("Neuter")
        every { ime.findWhetherWordIsPlural(any(), "in") } returns false
        every { ime.getCaseAnnotationForPreposition(any(), "in") } returns null

        every { ime.updateAutoSuggestText(any(), any(), any()) } answers {
            conjugateBtn.text = "der"
            pluralBtn.text = "den"
            translateBtn.text = "die"
        }

        suggestionHandler = SuggestionHandler(ime, handler)
        suggestionHandler.processLinguisticSuggestions("in")

        runnableSlot.captured.run()

        assert(conjugateBtn.text == "der")
        assert(pluralBtn.text == "den")
        assert(translateBtn.text == "die")
    }
}
