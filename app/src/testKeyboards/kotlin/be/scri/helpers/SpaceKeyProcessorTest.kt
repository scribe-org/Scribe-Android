// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.view.inputmethod.InputConnection
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpaceKeyProcessorTest {
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val suggestionHandler = mockk<SuggestionHandler>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private lateinit var spaceKeyProcessor: SpaceKeyProcessor

    @Before
    fun setUp() {
        mockkObject(PreferencesHelper)
        every { PreferencesHelper.getEnablePeriodOnSpaceBarDoubleTap(any(), any()) } returns true
        spaceKeyProcessor = SpaceKeyProcessor(ime, suggestionHandler)
        every { ime.language } returns "en"
        every { ime.getInputConnection() } returns inputConnection
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun processKeycodeSpace_outsideCommandBar_returnsTrue() {
        every { ime.currentState } returns be.scri.models.ScribeState.IDLE

        val result = spaceKeyProcessor.processKeycodeSpace(currentWasLastKeySpace = false)

        assertTrue(result)
        verify { suggestionHandler.processWordSuggestions(any()) }
    }

    @Test
    fun processKeycodeSpace_inCommandBar_returnsFalse() {
        every { ime.currentState } returns be.scri.models.ScribeState.TRANSLATE

        val result = spaceKeyProcessor.processKeycodeSpace(currentWasLastKeySpace = false)

        assertFalse(result)
        verify { suggestionHandler.clearAllSuggestionsAndHideButtonUI() }
    }

    @Test
    fun processKeycodeSpace_doubleSpaceAfterWord_commitsPeriod() {
        every { ime.currentState } returns be.scri.models.ScribeState.IDLE
        every { inputConnection.getTextBeforeCursor(2, 0) } returns "s "

        // First press initializes lastSpacePressTime.
        spaceKeyProcessor.processKeycodeSpace(currentWasLastKeySpace = false)

        // Rapid second press with wasLastKeySpace = true.
        spaceKeyProcessor.processKeycodeSpace(currentWasLastKeySpace = true)

        verify { ime.commitPeriodAfterSpace() }
    }
}
