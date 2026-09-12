// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import be.scri.R
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.ui.KeyboardUIManager.KeyboardUIListener
import be.scri.models.ScribeState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyboardUIManagerTest {
    private lateinit var context: Context
    private lateinit var binding: InputMethodViewBinding
    private lateinit var listener: KeyboardUIListener
    private lateinit var uiManager: KeyboardUIManager

    // Convenience references resolved from the inflated binding.
    private val keyboardView get() = binding.keyboardView
    private val commandOptionsBar get() = binding.commandOptionsBar
    private val toolbarBar get() = binding.toolbarBar
    private val invalidInfoBar get() = binding.invalidInfoBar
    private val ivInfo get() = binding.ivInfo
    private val scribeKeyClose get() = binding.scribeKeyClose
    private val translateBtn get() = binding.translateBtn
    private val translateBtnLeft get() = binding.translateBtnLeft
    private val conjugateGridContainer get() = binding.conjugateGridContainer
    private val commandBarEditText get() = binding.commandBar
    private val emojiBtnPhone1 get() = binding.emojiBtnPhone1
    private val emojiBtnPhone2 get() = binding.emojiBtnPhone2
    private val emojiBtnTablet1 get() = binding.emojiBtnTablet1
    private val emojiBtnTablet2 get() = binding.emojiBtnTablet2
    private val emojiBtnTablet3 get() = binding.emojiBtnTablet3

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Inflate the real layout — Robolectric handles Android view inflation natively.
        // This avoids MockK's inability to stub generated ViewBinding field accessors
        // in Robolectric's sandboxed classloader.
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))

        // Mock Listener
        listener = mockk(relaxed = true)
        every { listener.getKeyboardLayoutXML() } returns be.scri.R.xml.keys_letters_english
        every { listener.getCurrentKeyboardLayoutXML() } returns be.scri.R.xml.keys_letters_english
        every { listener.onKeyboardActionListener() } returns mockk()

        // Init Manager
        uiManager = KeyboardUIManager(binding, context, listener)
    }

    @Test
    fun `updateUI IDLE state shows options bar and hides toolbar`() {
        uiManager.updateUI(
            currentState = ScribeState.IDLE,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )

        assertEquals(View.VISIBLE, commandOptionsBar.visibility)
        assertEquals(View.GONE, toolbarBar.visibility)
        assertEquals(View.VISIBLE, keyboardView.visibility)
    }

    @Test
    fun `updateUI SELECT_VERB_CONJUNCTION shows grid and hides keyboard`() {
        val mockConjugateOutput =
            mapOf(
                "Present" to
                    mapOf(
                        "1ps" to listOf("I am"),
                        "2ps" to listOf("You are"),
                    ),
            )

        val sharedPrefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt("conjugate_index", 0).commit()

        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("1ps", "2ps"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "be",
        )

        assertEquals("Toolbar should be visible", View.VISIBLE, toolbarBar.visibility)
        assertEquals("Grid container should be visible", View.VISIBLE, conjugateGridContainer.visibility)
        assertEquals("Keyboard view should be GONE", View.GONE, keyboardView.visibility)

        assertTrue(commandBarEditText.text.toString().contains("be"))
    }

    @Test
    fun `updateUI INVALID state shows info icon`() {
        uiManager.updateUI(
            currentState = ScribeState.INVALID,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )

        assertEquals(View.VISIBLE, toolbarBar.visibility)
        assertEquals(View.VISIBLE, ivInfo.visibility)

        ivInfo.performClick()
        assertEquals(View.VISIBLE, invalidInfoBar.visibility)
        assertEquals(View.GONE, keyboardView.visibility)
    }

    @Test
    @Config(qualifiers = "normal") // Force Phone layout
    fun `updateEmojiSuggestion updates phone layout buttons correctly`() {
        val emojis = mutableListOf("😀", "😂")

        uiManager.updateEmojiSuggestion(ScribeState.IDLE, true, emojis)
        uiManager.updateButtonVisibility(ScribeState.IDLE, true, emojis)

        assertEquals("Phone Btn 1 Visible", View.VISIBLE, emojiBtnPhone1.visibility)
        assertEquals("Phone Btn 2 Visible", View.VISIBLE, emojiBtnPhone2.visibility)
        assertEquals("Tablet Btn 1 Gone", View.GONE, emojiBtnTablet1.visibility)

        assertEquals("😀", emojiBtnPhone1.text)
        assertEquals("😂", emojiBtnPhone2.text)

        emojiBtnPhone1.performClick()
        verify { listener.onEmojiSelected("😀") }
    }

    @Test
    @Config(qualifiers = "large") // force tablet layout
    fun `updateEmojiSuggestion updates tablet layout buttons correctly`() {
        val emojis = mutableListOf("😀", "😂", "🚀")

        uiManager.updateEmojiSuggestion(ScribeState.IDLE, true, emojis)
        uiManager.updateButtonVisibility(ScribeState.IDLE, true, emojis)

        assertEquals("Tablet Btn 1 Visible", View.VISIBLE, emojiBtnTablet1.visibility)
        assertEquals("Tablet Btn 3 Visible", View.VISIBLE, emojiBtnTablet3.visibility)
        assertEquals("Phone Btn 1 Gone", View.GONE, emojiBtnPhone1.visibility)

        assertEquals("🚀", emojiBtnTablet3.text)
    }

    @Test
    fun `invalidInfoBar is hidden and keyboard restored when returning to IDLE after info panel shown from INVALID state`() {
        // Transition to INVALID state.
        uiManager.updateUI(
            currentState = ScribeState.INVALID,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )
        assertEquals("Toolbar bar should be visible in INVALID state", View.VISIBLE, toolbarBar.visibility)
        assertEquals("Info icon should be visible in INVALID state", View.VISIBLE, ivInfo.visibility)

        // User taps ⓘ to open the info panel.
        ivInfo.performClick()
        assertEquals("invalidInfoBar should be visible after tapping info icon", View.VISIBLE, invalidInfoBar.visibility)
        assertEquals("keyboardView should be GONE while info panel is open", View.GONE, keyboardView.visibility)

        // User taps X (scribeKeyClose) to simulate IME returning to IDLE.
        scribeKeyClose.performClick()
        verify { listener.onCloseClicked() }

        // Simulate what the IME does after onCloseClicked(): Transition UI to IDLE.
        uiManager.updateUI(
            currentState = ScribeState.IDLE,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )

        // Assert no orphaned UI elements remain.
        assertEquals("invalidInfoBar must be GONE after returning to IDLE", View.GONE, invalidInfoBar.visibility)
        assertEquals("keyboardView must be VISIBLE after returning to IDLE", View.VISIBLE, keyboardView.visibility)
        assertEquals("commandOptionsBar must be VISIBLE in IDLE state", View.VISIBLE, commandOptionsBar.visibility)
    }

    @Test
    fun `invalidInfoBar is hidden and keyboard restored when returning to IDLE after info panel shown from ALREADY_PLURAL state`() {
        // Transition to ALREADY_PLURAL state.
        uiManager.updateUI(
            currentState = ScribeState.ALREADY_PLURAL,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )
        assertEquals("Info icon should be visible in ALREADY_PLURAL state", View.VISIBLE, ivInfo.visibility)

        // User taps ⓘ to open the info panel.
        ivInfo.performClick()
        assertEquals("invalidInfoBar should be visible after tapping info icon", View.VISIBLE, invalidInfoBar.visibility)
        assertEquals("keyboardView should be GONE while info panel is open", View.GONE, keyboardView.visibility)

        // User taps X (scribeKeyClose) to simulate IME returning to IDLE.
        uiManager.updateUI(
            currentState = ScribeState.IDLE,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )

        // Assert that no orphaned UI elements remain.
        assertEquals("invalidInfoBar must be GONE after returning to IDLE from ALREADY_PLURAL", View.GONE, invalidInfoBar.visibility)
        assertEquals("keyboardView must be VISIBLE after returning to IDLE from ALREADY_PLURAL", View.VISIBLE, keyboardView.visibility)
    }

    @Test
    fun `disableAutoSuggest resets buttons to commands`() {
        uiManager.updateUI(ScribeState.IDLE, "English", false, null, null, null, null, null)

        uiManager.disableAutoSuggest("English")

        translateBtn.performClick()

        verify { listener.onSuggestionClicked(any()) }
        assertEquals(View.VISIBLE, translateBtn.visibility)
        assertEquals(View.INVISIBLE, translateBtnLeft.visibility)
    }

    @Test
    fun updateUI_EnglishWithSmallForms_Inflates2x2Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Present" to
                    mapOf(
                        "Present" to listOf("am", "are", "is", "are"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Present"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "be",
        )

        // Assert
        assertNotNull("Button 1 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 4 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
        assertNull("Button 5 should not be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
        assertNull("Button 6 should not be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_6))
    }

    @Test
    fun updateUI_RussianWithSmallForms_Inflates2x2Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Настоящее" to
                    mapOf(
                        "Настоящее" to listOf("читаю", "читаешь", "читает"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "Russian",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Настоящее"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "читать",
        )

        // Assert
        assertNotNull("Button 1 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 4 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
        assertNull("Button 5 should not be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
    }

    @Test
    fun updateUI_SwedishWithSmallForms_Inflates2x2Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Presens" to
                    mapOf(
                        "Presens" to listOf("är", "var", "varit"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "Swedish",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Presens"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "vara",
        )

        // Assert
        assertNotNull("Button 1 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 4 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
        assertNull("Button 5 should not be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
    }

    @Test
    fun updateUI_GermanWithLargeForms_Inflates3x2Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Präsens" to
                    mapOf(
                        "Präsens" to listOf("habe", "hast", "hat", "haben", "habt", "haben"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "German",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Präsens"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "haben",
        )

        // Assert
        assertNotNull("Button 1 should be present in 3x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 5 should be present in 3x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
        assertNotNull("Button 6 should be present in 3x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_6))
    }

    @Test
    fun updateUI_SubSelectionActive_Prioritizes2x1Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Present" to
                    mapOf(
                        "Singular" to listOf("I am", "You are"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Present"),
            selectedConjugationSubCategory = "Singular",
            currentVerbForConjugation = "be",
        )

        // Assert
        assertNotNull("Button 1 should be present in 2x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 2 should be present in 2x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_2))
        assertNull("Button 3 should not be present in 2x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_3))
        assertNull("Button 4 should not be present in 2x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
        assertNull("Button 5 should not be present in 2x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
    }

    @Test
    fun updateUI_UnknownLanguageString_FallsBackSafelyWithoutCrashing() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Present" to
                    mapOf(
                        "Present" to listOf("form1", "form2"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "UnknownLanguageXYZ",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Present"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "verb",
        )

        // Assert
        assertEquals("Toolbar bar should be visible", View.VISIBLE, toolbarBar.visibility)
        assertEquals("Conjugate grid container should be visible", View.VISIBLE, conjugateGridContainer.visibility)
        assertNotNull("Button 1 should be present in fallback grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNull("Button 5 should not be present in fallback 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
    }
}
