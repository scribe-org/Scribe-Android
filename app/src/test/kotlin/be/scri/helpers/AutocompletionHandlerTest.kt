package be.scri.helpers

import android.os.Handler
import android.os.Looper
import be.scri.models.ScribeState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AutocompletionHandlerTest {

    private lateinit var looper: Looper
    private lateinit var handler: Handler

    @MockK(relaxed = true)
    private lateinit var ime: KeyboardIMEContext
    private lateinit var autocompletionHandler: AutocompletionHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        looper = mockk<Looper>(relaxed = true)
        handler = mockk<Handler>(relaxed = true)
        autocompletionHandler = AutocompletionHandler(ime, handler)
    }

    @Test
    fun buildCompletions_completionsMatchTypedWord_emptyResponse() {
        val typedWord = "word"
        val completions = listOf("word", "word")

        val result = AutocompletionHandler.buildCompletions(typedWord, completions)

        assert(result.isEmpty())
    }

    @Test
    fun buildCompletions_completionsContainTypedWord_removesIt() {
        val typedWord = "word"
        val completions = listOf("word", "wordy")

        val result = AutocompletionHandler.buildCompletions(typedWord, completions)

        assert(result.size == 1)
        assert(result[0] == "wordy")
    }

    @Test
    fun buildCompletions_completionsContainManyMore_returnsOnlyTwo() {
        val typedWord = "word"
        val completions = List(10) { "word$it" }

        val result = AutocompletionHandler.buildCompletions(typedWord, completions)

        assert(result.size == 2)
        assert(result[0] == "word0")
        assert(result[1] == "word1")
    }

    @Test
    fun processAutocomplete_emptyWord_clearsAutocomplete() {
        every { ime.currentState } returns ScribeState.IDLE

        autocompletionHandler.processAutocomplete("")

        verify { ime.clearAutocomplete() }
    }

    @Test
    fun processAutocomplete_inWrongState_clearsAutocomplete() {
        every { ime.currentState } returns ScribeState.PLURAL

        autocompletionHandler.processAutocomplete("word")

        verify { ime.clearAutocomplete() }
    }

    @Test
    fun processAutocomplete_withWord_updatesTypedWordSuggestion() {
        val runnableSlot = slot<Runnable>()
        every { handler.postDelayed(capture(runnableSlot), any()) } returns true

        every { ime.currentState } returns ScribeState.IDLE

        val currentWord = "word"
        autocompletionHandler.processAutocomplete(currentWord)

        runnableSlot.captured.run()
        verify { ime.updateTypedWordSuggestion(currentWord) }
    }

    @Test
    fun processAutocomplete_withWord_updatesAutocompleteCompletions() {
        val runnableSlot = slot<Runnable>()
        every { handler.postDelayed(capture(runnableSlot), any()) } returns true

        every { ime.currentState } returns ScribeState.IDLE

        val currentWord = "word"
        autocompletionHandler.processAutocomplete(currentWord)

        runnableSlot.captured.run()

        verify { ime.updateAutocompleteCompletions(any()) }
    }

    @Test
    fun processAutocomplete_calledMultipleTimes_removesPreviousCallbacks() {
        every { ime.currentState } returns ScribeState.IDLE

        autocompletionHandler.processAutocomplete("first")

        autocompletionHandler.processAutocomplete("second")

        verify(exactly = 1) { handler.removeCallbacks(any<Runnable>()) }
    }

    @Test
    fun clearAutocomplete_cancelsPendingTaskAndClearsIme() {
        every { ime.currentState } returns ScribeState.IDLE

        autocompletionHandler.processAutocomplete("word")

        autocompletionHandler.clearAutocomplete()

        verify { handler.removeCallbacks(any<Runnable>()) }
        verify { ime.clearAutocomplete() }
    }

    @Test
    fun processAutocomplete_stateChangesBeforeRunnableExecutes_abortsExecution() {
        val runnableSlot = slot<Runnable>()
        every { handler.postDelayed(capture(runnableSlot), any()) } returns true

        every { ime.currentState } returns ScribeState.IDLE
        autocompletionHandler.processAutocomplete("word")

        every { ime.currentState } returns ScribeState.PLURAL

        runnableSlot.captured.run()

        verify(exactly = 0) { ime.getAutocompletions(any(), any(), any()) }
        verify(exactly = 0) { ime.updateAutocompleteCompletions(any()) }
    }
}
