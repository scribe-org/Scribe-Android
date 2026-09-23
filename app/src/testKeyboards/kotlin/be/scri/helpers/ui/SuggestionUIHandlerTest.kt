// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import androidx.test.core.app.ApplicationProvider
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.KeyboardIMEContext
import be.scri.helpers.SuggestionHandler
import be.scri.models.ScribeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SuggestionUIHandlerTest {
    private lateinit var context: Context
    private lateinit var binding: InputMethodViewBinding
    private lateinit var uiManager: KeyboardUIManager
    private lateinit var themeManager: KeyboardThemeManager
    private val ime = mockk<KeyboardIMEContext>(relaxed = true)
    private val inputConnection = mockk<InputConnection>(relaxed = true)
    private val suggestionHandler = mockk<SuggestionHandler>(relaxed = true)
    private lateinit var handler: SuggestionUIHandler

    private val defaultSuggestions = listOf("ich", "die", "das")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))
        val listener = mockk<KeyboardUIManager.KeyboardUIListener>(relaxed = true)
        uiManager = KeyboardUIManager(binding, context, listener)
        themeManager = KeyboardThemeManager()

        every { ime.uiManager } returns uiManager
        every { ime.themeManager } returns themeManager
        every { ime.imeContext } returns context
        every { ime.language } returns "German"
        every { ime.isUiManagerInitialized } returns true
        every { ime.currentState } returns ScribeState.IDLE
        every { ime.getInputConnection() } returns inputConnection
        every { ime.suggestionHandler } returns suggestionHandler

        handler = SuggestionUIHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun updateAutoSuggestText_whenNotIdle_disablesAutoSuggestAndRestoresDefaults() {
        every { ime.currentState } returns ScribeState.PLURAL
        binding.translateBtn.text = "custom"

        handler.updateAutoSuggestText(nounTypeSuggestion = listOf("masculine"))

        assertEquals(defaultSuggestions[0], binding.translateBtn.text.toString())
        assertEquals(defaultSuggestions[1], binding.conjugateBtn.text.toString())
        assertEquals(defaultSuggestions[2], binding.pluralBtn.text.toString())
    }

    @Test
    fun updateAutoSuggestText_setsStatePropertiesOnIME() {
        handler.updateAutoSuggestText(
            nounTypeSuggestion = listOf("masculine"),
            isPlural = true,
            caseAnnotationSuggestion = mutableListOf("accusative"),
            wordSuggestions = listOf("word1", "word2"),
        )

        verify { ime.nounTypeSuggestion = listOf("masculine") }
        verify { ime.checkIfPluralWord = true }
        verify { ime.caseAnnotationSuggestion = mutableListOf("accusative") }
        verify { ime.wordSuggestions = listOf("word1", "word2") }
    }

    @Test
    fun updateAutoSuggestText_singleGender_drawsSingleBadgeOnTranslateButton() {
        handler.updateAutoSuggestText(nounTypeSuggestion = listOf("masculine"))

        assertEquals(View.VISIBLE, binding.translateBtn.visibility)
        assertEquals("M", binding.translateBtn.text.toString())
        assertEquals(View.INVISIBLE, uiManager.genderSuggestionLeft?.visibility)
        assertEquals(View.INVISIBLE, uiManager.genderSuggestionRight?.visibility)
    }

    @Test
    fun updateAutoSuggestText_plural_drawsPluralBadgeOnTranslateButton() {
        handler.updateAutoSuggestText(isPlural = true)

        assertEquals(View.VISIBLE, binding.translateBtn.visibility)
        assertEquals("PL", binding.translateBtn.text.toString())
        assertEquals(View.INVISIBLE, uiManager.genderSuggestionLeft?.visibility)
        assertEquals(View.INVISIBLE, uiManager.genderSuggestionRight?.visibility)
    }

    @Test
    fun updateAutoSuggestText_multipleGenders_drawsDualBadgesAndHidesTranslateButton() {
        handler.updateAutoSuggestText(nounTypeSuggestion = listOf("masculine", "feminine"))

        assertEquals(View.INVISIBLE, binding.translateBtn.visibility)
        assertEquals(View.VISIBLE, uiManager.genderSuggestionLeft?.visibility)
        assertEquals(View.VISIBLE, uiManager.genderSuggestionRight?.visibility)
        assertEquals("M", uiManager.genderSuggestionLeft?.text.toString())
        assertEquals("F", uiManager.genderSuggestionRight?.text.toString())
    }

    @Test
    fun updateAutoSuggestText_wordSuggestions_drawsPredictionsAcrossButtons() {
        handler.updateAutoSuggestText(
            wordSuggestions = listOf("Haus", "Hause", "Häuser"),
        )

        assertEquals("Haus", binding.conjugateBtn.text.toString())
        assertEquals("Hause", binding.translateBtn.text.toString())
        assertEquals("Häuser", binding.pluralBtn.text.toString())
        assertEquals(View.VISIBLE, binding.conjugateBtn.visibility)
        assertEquals(View.VISIBLE, binding.translateBtn.visibility)
    }

    @Test
    fun updateAutoSuggestText_wordSuggestionClick_commitsTextAndMovesToIdle() {
        handler.updateAutoSuggestText(
            wordSuggestions = listOf("Haus", "Hause", "Häuser"),
        )

        binding.conjugateBtn.performClick()

        verify { inputConnection.commitText("Haus ", 1) }
        verify { ime.moveToIdleState() }
    }

    @Test
    fun updateAutoSuggestText_genderWithEmojisAndNoWordSuggestions_updatesButtonVisibilityAndDrawsGenderBadge() {
        val emojis = mutableListOf("😀", "😃")
        every { ime.autoSuggestEmojis } returns emojis

        handler.updateAutoSuggestText(
            nounTypeSuggestion = listOf("masculine"),
            wordSuggestions = null,
        )

        assertEquals("M", binding.translateBtn.text.toString())
        assertEquals(defaultSuggestions[0], binding.conjugateBtn.text.toString())
        assertEquals(View.VISIBLE, binding.emojiBtnPhone1.visibility)
        assertEquals(View.VISIBLE, binding.emojiBtnPhone2.visibility)
        assertEquals(View.INVISIBLE, binding.pluralBtn.visibility)
    }

    @Test
    fun updateTypedWordSuggestion_whenValidWord_drawsQuotedWordAndSeparators() {
        handler.updateTypedWordSuggestion("Buch")

        assertEquals("\"Buch\"", binding.translateBtn.text.toString())
        assertEquals("", binding.conjugateBtn.text.toString())
        assertEquals(View.VISIBLE, binding.separator1.visibility)
        assertEquals(View.VISIBLE, binding.separator2.visibility)
    }

    @Test
    fun updateTypedWordSuggestion_click_commitsSpaceAndTriggersSuggestionProcessing() {
        handler.updateTypedWordSuggestion("Buch")

        binding.translateBtn.performClick()

        verify { inputConnection.commitText(" ", 1) }
        verify { suggestionHandler.processLinguisticSuggestions("Buch") }
        verify { suggestionHandler.processWordSuggestions("Buch") }
        verify { ime.moveToIdleState() }
    }

    @Test
    fun updateTypedWordSuggestion_whenNullOrEmpty_disablesAutoSuggestAndRestoresDefaults() {
        binding.translateBtn.text = "custom"
        handler.updateTypedWordSuggestion(null)
        assertEquals(defaultSuggestions[0], binding.translateBtn.text.toString())
        assertEquals(defaultSuggestions[1], binding.conjugateBtn.text.toString())
        assertEquals(defaultSuggestions[2], binding.pluralBtn.text.toString())

        binding.translateBtn.text = "custom"
        handler.updateTypedWordSuggestion("")
        assertEquals(defaultSuggestions[0], binding.translateBtn.text.toString())
        assertEquals(defaultSuggestions[1], binding.conjugateBtn.text.toString())
        assertEquals(defaultSuggestions[2], binding.pluralBtn.text.toString())
    }

    @Test
    fun updateTypedWordSuggestion_whenNotIdle_disablesAutoSuggestAndRestoresDefaults() {
        every { ime.currentState } returns ScribeState.PLURAL
        binding.translateBtn.text = "custom"

        handler.updateTypedWordSuggestion("Buch")

        assertEquals(defaultSuggestions[0], binding.translateBtn.text.toString())
        assertEquals(defaultSuggestions[1], binding.conjugateBtn.text.toString())
        assertEquals(defaultSuggestions[2], binding.pluralBtn.text.toString())
    }

    @Test
    fun updateAutocompleteCompletions_drawsCompletionsOnButtons() {
        handler.updateAutocompleteCompletions(listOf("Apfel", "Apfelsaft"))

        assertEquals("Apfel", binding.conjugateBtn.text.toString())
        assertEquals("Apfelsaft", binding.pluralBtn.text.toString())
    }

    @Test
    fun updateAutocompleteCompletions_click_replacesCurrentWord() {
        every { inputConnection.getTextBeforeCursor(50, 0) } returns "Apf"

        handler.updateAutocompleteCompletions(listOf("Apfel", "Apfelsaft"))
        binding.conjugateBtn.performClick()

        verify { inputConnection.deleteSurroundingText(3, 0) }
        verify { inputConnection.commitText("Apfel", 1) }
        verify { ime.moveToIdleState() }
    }

    @Test
    fun updateAutocompleteCompletions_whenNotIdle_returnsEarly() {
        every { ime.currentState } returns ScribeState.PLURAL

        handler.updateAutocompleteCompletions(listOf("Apfel"))

        assertEquals("", binding.conjugateBtn.text.toString())
    }

    @Test
    fun clearAutocomplete_whenUiManagerInitialized_disablesAutoSuggestAndRestoresDefaults() {
        binding.translateBtn.text = "custom"

        handler.clearAutocomplete()

        assertEquals(defaultSuggestions[0], binding.translateBtn.text.toString())
        assertEquals(defaultSuggestions[1], binding.conjugateBtn.text.toString())
        assertEquals(defaultSuggestions[2], binding.pluralBtn.text.toString())
    }
}
