// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import be.scri.databinding.InputMethodViewBinding
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
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
    private val ime = mockk<GeneralKeyboardIME>(relaxed = true)
    private val uiManager = mockk<KeyboardUIManager>(relaxed = true)
    private val themeManager = mockk<KeyboardThemeManager>(relaxed = true)
    private lateinit var handler: SuggestionUIHandler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))

        every { uiManager.binding } returns binding
        every { ime.uiManager } returns uiManager
        every { ime.themeManager } returns themeManager
        every { ime.applicationContext } returns context
        every { ime.language } returns "English"
        every { ime.isUiManagerInitialized } returns true
        handler = SuggestionUIHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun updateAutoSuggestText_whenNotIdle_disablesAutoSuggest() {
        every { ime.currentState } returns ScribeState.PLURAL

        handler.updateAutoSuggestText(nounTypeSuggestion = listOf("masculine"))

        verify { uiManager.disableAutoSuggest("English") }
    }

    @Test
    fun updateAutoSuggestText_setsStatePropertiesOnIME() {
        every { ime.currentState } returns ScribeState.IDLE

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
    fun updateTypedWordSuggestion_whenNullOrEmpty_disablesAutoSuggest() {
        every { ime.currentState } returns ScribeState.IDLE

        handler.updateTypedWordSuggestion(null)
        verify { uiManager.disableAutoSuggest("English") }

        handler.updateTypedWordSuggestion("")
        verify(exactly = 2) { uiManager.disableAutoSuggest("English") }
    }

    @Test
    fun updateTypedWordSuggestion_whenNotIdle_disablesAutoSuggest() {
        every { ime.currentState } returns ScribeState.PLURAL

        handler.updateTypedWordSuggestion("word")

        verify { uiManager.disableAutoSuggest("English") }
    }

    @Test
    fun updateAutocompleteCompletions_whenNotIdle_returnsEarly() {
        every { ime.currentState } returns ScribeState.PLURAL

        handler.updateAutocompleteCompletions(listOf("completion1"))

        verify(exactly = 0) { uiManager.disableAutoSuggest("English") }
    }

    @Test
    fun clearAutocomplete_whenUiManagerInitialized_disablesAutoSuggest() {
        handler.clearAutocomplete()

        verify { uiManager.disableAutoSuggest("English") }
    }
}
