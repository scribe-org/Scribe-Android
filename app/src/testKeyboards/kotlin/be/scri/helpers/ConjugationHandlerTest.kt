// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import androidx.test.core.app.ApplicationProvider
import be.scri.R
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.ui.KeyboardUIManager
import be.scri.models.ScribeState
import be.scri.views.KeyboardView
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConjugationHandlerTest {
    private lateinit var context: Context
    private lateinit var binding: InputMethodViewBinding
    private lateinit var uiManager: KeyboardUIManager
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val keyboardView = mockk<KeyboardView>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private val suggestionHandler = mockk<SuggestionHandler>(relaxed = true)

    private lateinit var handler: ConjugationHandler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))
        val listener = mockk<KeyboardUIManager.KeyboardUIListener>(relaxed = true)
        uiManager = KeyboardUIManager(binding, context, listener)

        every { ime.imeContext } returns context
        every { ime.keyboardView } returns keyboardView
        every { ime.getInputConnection() } returns inputConnection
        every { ime.suggestionHandler } returns suggestionHandler
        every { ime.uiManager } returns uiManager
        every { ime.binding } returns binding

        handler = ConjugationHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun applyCapitalizationToConjugations_firstLetterCapitalization() {
        val input: MutableMap<String, MutableMap<String, Collection<String>>> =
            mutableMapOf(
                "Present" to mutableMapOf("I" to listOf("go", "run", "")),
            )

        val result = ConjugationHandler.applyCapitalizationToConjugations(input, isAllCaps = false)

        assertEquals(listOf("Go", "Run", ""), result["Present"]?.get("I"))
    }

    @Test
    fun applyCapitalizationToConjugations_allCapsFormat() {
        val input: MutableMap<String, MutableMap<String, Collection<String>>> =
            mutableMapOf(
                "Present" to mutableMapOf("I" to listOf("go", "run", "")),
            )

        val result = ConjugationHandler.applyCapitalizationToConjugations(input, isAllCaps = true)

        assertEquals(listOf("GO", "RUN", ""), result["Present"]?.get("I"))
    }

    @Test
    fun getValidatedConjugateIndex_clampsToValidRange() {
        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        prefs.edit().putInt("conjugate_index", 5).commit()

        every { ime.conjugateOutput } returns
            mutableMapOf(
                "Present" to mutableMapOf(),
                "Past" to mutableMapOf(),
            )

        val index = handler.getValidatedConjugateIndex()

        // Max index is 1 (size 2 - 1)
        assertEquals(1, index)
        assertEquals(1, prefs.getInt("conjugate_index", -1))
    }

    @Test
    fun getValidatedConjugateIndex_nullOrEmptyOutputDefaultsToZero() {
        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        prefs.edit().putInt("conjugate_index", 3).commit()

        every { ime.conjugateOutput } returns null

        val index = handler.getValidatedConjugateIndex()

        assertEquals(0, index)
        assertEquals(0, prefs.getInt("conjugate_index", -1))
    }

    @Test
    fun saveConjugateModeType_spanishReturns2x2() {
        handler.saveConjugateModeType("Spanish", isSubsequent = false)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("2x2", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_englishReturns2x2() {
        handler.saveConjugateModeType("English", isSubsequent = false)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("2x2", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_noneReturnsNone() {
        handler.saveConjugateModeType("none", isSubsequent = false)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("none", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_subsequentReturnsNone() {
        handler.saveConjugateModeType("English", isSubsequent = true)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("none", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun handleConjugateKeys_whenNotSubsequent_commitsTextAndProcessesSuggestions() {
        every { keyboardView.getKeyLabel(10) } returns "hablo"

        val label = handler.handleConjugateKeys(code = 10, isSubsequentRequired = false)

        assertEquals("hablo", label)
        verify { inputConnection.commitText("hablo ", 1) }
        verify { suggestionHandler.processLinguisticSuggestions("hablo") }
    }

    @Test
    fun handleConjugateKeys_whenSubsequentRequired_doesNotCommitText() {
        every { keyboardView.getKeyLabel(10) } returns "hablo"

        val label = handler.handleConjugateKeys(code = 10, isSubsequentRequired = true)

        assertEquals("hablo", label)
        verify(exactly = 0) { inputConnection.commitText(any(), any()) }
        verify(exactly = 0) { suggestionHandler.processLinguisticSuggestions(any()) }
    }

    @Test
    fun handleConjugateKeys_whenKeyLabelNull_returnsNullAndDoesNotCommit() {
        every { keyboardView.getKeyLabel(10) } returns null

        val label = handler.handleConjugateKeys(code = 10, isSubsequentRequired = false)

        assertNull(label)
        verify(exactly = 0) { inputConnection.commitText(any(), any()) }
    }

    @Test
    fun setupConjugateSubView_withTwoItems_configures2x1Layout() {
        every { ime.language } returns "Spanish"
        every { ime.currentState } returns ScribeState.SELECT_VERB_CONJUNCTION
        handler.subsequentAreaRequired = true

        val data = listOf(listOf("hablo", "hablas"))
        handler.setupConjugateSubView(data, word = "hablo")

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("none", prefs.getString("conjugate_mode_type", null))
        verify { uiManager.initializeKeyboard(R.xml.conjugate_view_2x1) }
        verify { keyboardView.setKeyLabel("hablo", "HI", KeyboardBase.CODE_2X1_TOP) }
        verify { keyboardView.setKeyLabel("hablas", "HI", KeyboardBase.CODE_2X1_BOTTOM) }
        assertEquals(View.GONE, binding.ivInfo.visibility)
        assertFalse(handler.subsequentAreaRequired)
    }

    @Test
    fun setupConjugateSubView_withThreeItems_configures1x3Layout() {
        every { ime.language } returns "Spanish"
        every { ime.currentState } returns ScribeState.SELECT_VERB_CONJUNCTION
        handler.subsequentAreaRequired = true

        val data = listOf(listOf("hablo", "hablas", "habla"))
        handler.setupConjugateSubView(data, word = "hablo")

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("none", prefs.getString("conjugate_mode_type", null))
        verify { uiManager.initializeKeyboard(R.xml.conjugate_view_1x3) }
        verify { keyboardView.setKeyLabel("hablo", "HI", KeyboardBase.CODE_1X3_LEFT) }
        verify { keyboardView.setKeyLabel("hablas", "HI", KeyboardBase.CODE_1X3_CENTER) }
        verify { keyboardView.setKeyLabel("habla", "HI", KeyboardBase.CODE_1X3_RIGHT) }
        assertEquals(View.GONE, binding.ivInfo.visibility)
        assertFalse(handler.subsequentAreaRequired)
    }
}
