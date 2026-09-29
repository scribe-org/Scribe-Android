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
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val uiManager = mockk<KeyboardUIManager>(relaxed = true)
    private val keyboardView = mockk<KeyboardView>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private val suggestionHandler = mockk<SuggestionHandler>(relaxed = true)

    private lateinit var handler: ConjugationHandler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))

        every { ime.imeContext } returns context
        every { ime.keyboardView } returns keyboardView
        every { ime.getInputConnection() } returns inputConnection
        every { ime.suggestionHandler } returns suggestionHandler
        every { ime.uiManager } returns uiManager
        every { uiManager.binding } returns binding

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
    fun saveConjugateModeType_usesDefaultConjugateModeTypeFromIme() {
        every { ime.defaultConjugateModeType } returns "2x2"

        handler.saveConjugateModeType("Spanish", isSubsequent = false)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("2x2", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_3x2ImeReturns3x2() {
        every { ime.defaultConjugateModeType } returns "3x2"

        handler.saveConjugateModeType("French", isSubsequent = false)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("3x2", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_unsupportedLanguageReturnsNoneFromIme() {
        // Hindi is not a supported conjugation language — the IME default covers this.
        every { ime.defaultConjugateModeType } returns "none"

        handler.saveConjugateModeType("Hindi", isSubsequent = false)

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("none", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_subsequentSavesSubViewMode() {
        handler.saveConjugateModeType("English", isSubsequent = true, subViewMode = "2x1")

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("2x1", prefs.getString("conjugate_mode_type", null))
    }

    @Test
    fun saveConjugateModeType_noneSentinelSavesNoneRegardlessOfImeDefault() {
        // IME default is "2x2" but passing "none" as language resets to idle mode.
        every { ime.defaultConjugateModeType } returns "2x2"

        handler.saveConjugateModeType("none", isSubsequent = false)

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
    fun setupConjugateSubView_withDataSizeZero_earlyReturnsWithoutInflatingKeyboard() {
        every { ime.currentState } returns ScribeState.SELECT_VERB_CONJUNCTION
        every { ime.defaultConjugateLayoutXML } returns R.xml.conjugate_view_3x2

        // Empty data — flattenList.size == 0 hits else -> return before initializeKeyboard.
        handler.setupConjugateSubView(emptyList(), word = null)

        verify(exactly = 0) { uiManager.initializeKeyboard(any()) }
    }

    @Test
    fun setupConjugateSubView_withUnsupportedDataSize_earlyReturnsWithoutInflatingKeyboard() {
        // 4 items → not 2 or 3, so setupConjugateSubView early-returns without inflating keyboard.
        val data = listOf(listOf("hablo", "hablas", "habla", "hablamos"))
        handler.setupConjugateSubView(data, word = "hablo")

        verify(exactly = 0) { uiManager.initializeKeyboard(any()) }
    }

    @Test
    fun setupConjugateSubView_withTwoItems_configures2x1Layout() {
        every { ime.language } returns "Spanish"
        every { ime.currentState } returns ScribeState.SELECT_VERB_CONJUNCTION
        every { ime.defaultConjugateModeType } returns "2x2"

        val data = listOf(listOf("hablo", "hablas"))
        handler.setupConjugateSubView(data, word = "hablo")

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("2x1", prefs.getString("conjugate_mode_type", null))
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
        every { ime.defaultConjugateModeType } returns "2x2"

        val data = listOf(listOf("hablo", "hablas", "habla"))
        handler.setupConjugateSubView(data, word = "hablo")

        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        assertEquals("1x3", prefs.getString("conjugate_mode_type", null))
        verify { uiManager.initializeKeyboard(R.xml.conjugate_view_1x3) }
        verify { keyboardView.setKeyLabel("hablo", "HI", KeyboardBase.CODE_1X3_LEFT) }
        verify { keyboardView.setKeyLabel("hablas", "HI", KeyboardBase.CODE_1X3_CENTER) }
        verify { keyboardView.setKeyLabel("habla", "HI", KeyboardBase.CODE_1X3_RIGHT) }
        assertEquals(View.GONE, binding.ivInfo.visibility)
        assertFalse(handler.subsequentAreaRequired)
    }
}
