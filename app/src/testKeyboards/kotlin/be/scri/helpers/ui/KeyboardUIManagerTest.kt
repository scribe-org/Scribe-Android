// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
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
    private val clipboardPanelHolder get() = binding.clipboardPanelHolder
    private val emojiPaletteHolder get() = binding.emojiPaletteHolder

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
                        "1ps" to listOf("1ps" to "I am"),
                        "2ps" to listOf("2ps" to "You are"),
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
    fun commandBar_HasNoSuggestionsInputTypeFlag() {
        val inputType = binding.commandBar.inputType
        assertTrue(
            "commandBar should have TYPE_TEXT_FLAG_NO_SUGGESTIONS to prevent spell check underlines",
            (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0,
        )
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
                        "Present" to listOf("1ps" to "am", "2ps" to "are", "3ps" to "is", "1pp" to "are"),
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
                "Прошедшее" to
                    mapOf(
                        "Прошедшее" to listOf("m" to "читал", "f" to "читала", "n" to "читало", "pl" to "читали"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "Russian",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Прошедшее"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "читать",
        )

        // Assert
        assertNotNull("Button 1 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 4 should be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
        assertNull("Button 5 should not be present in 2x2 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_5))
    }

    @Test
    fun updateUI_ThreeForms_Inflates3x1Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Presens" to
                    mapOf(
                        "Presens" to listOf("1ps" to "är", "2ps" to "var", "3ps" to "varit"),
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
        assertNotNull("Button 1 should be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 2 should be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_2))
        assertNotNull("Button 3 should be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_3))
        assertNull("Button 4 should not be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
    }

    @Test
    fun updateUI_GermanWithLargeForms_Inflates3x2Grid() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Präsens" to
                    mapOf(
                        "Präsens" to
                            listOf(
                                "ich" to "habe",
                                "du" to "hast",
                                "er/sie/es" to "hat",
                                "wir" to "haben",
                                "ihr" to "habt",
                                "sie/Sie" to "haben",
                            ),
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
                        "Singular" to listOf("1ps" to "I am", "2ps" to "You are"),
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
                        "Present" to listOf("1ps" to "form1", "2ps" to "form2"),
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

    @Test
    fun updateUI_CategoryWithMultipleForms_ClickingOpensSubCategoryWithoutCommittingText() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Past" to
                    mapOf(
                        "Past Continuous" to
                            listOf(
                                "I/he/she/it" to "was being",
                                "you/plural" to "were being",
                            ),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Past"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "be",
        )

        val btn = binding.conjugateGrid.findViewById<Button>(R.id.conjugate_btn_1)
        assertNotNull(btn)
        assertEquals("was being / were being", btn.text.toString())

        btn.performClick()

        verify(exactly = 1) { listener.onConjugationSubCategorySelected("Past Continuous") }
        verify(exactly = 0) { listener.commitText(any()) }
    }

    @Test
    fun updateUI_SubSelectionActive_DisplaysLabelsAndClickingCommitsOnlyForm() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Past" to
                    mapOf(
                        "Past Continuous" to
                            listOf(
                                "I/he/she/it" to "was being",
                                "you/plural" to "were being",
                            ),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Past"),
            selectedConjugationSubCategory = "Past Continuous",
            currentVerbForConjugation = "be",
        )

        val btn1 = binding.conjugateGrid.findViewById<Button>(R.id.conjugate_btn_1)
        assertNotNull(btn1)
        assertTrue(btn1.text.toString().contains("I/he/she/it"))
        assertTrue(btn1.text.toString().contains("was being"))

        btn1.performClick()

        verify(exactly = 1) { listener.commitText("was being ") }
        verify(exactly = 1) { listener.processLinguisticSuggestions("was being") }
    }

    @Test
    fun updateUI_SubSelectionActive_ArrowClickDismissesSubCategory() {
        // Arrange
        val mockConjugateOutput =
            mapOf(
                "Past" to
                    mapOf(
                        "Past Continuous" to
                            listOf(
                                "I/he/she/it" to "was being",
                                "you/plural" to "were being",
                            ),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Past"),
            selectedConjugationSubCategory = "Past Continuous",
            currentVerbForConjugation = "be",
        )

        val leftArrow = binding.conjugateGrid.findViewById<Button>(R.id.conjugate_arrow_left)
        assertNotNull(leftArrow)
        leftArrow.performClick()

        verify(exactly = 1) { listener.onConjugationSubCategoryDismissed() }
        verify(exactly = 0) { listener.onConjugateClicked() }
    }

    @Test
    fun updateUI_DirectButtons_DisplaysLabelsAndClickingCommitsOnlyForm() {
        // Arrange (Spanish direct buttons as shown in issue #750)
        val mockConjugateOutput =
            mapOf(
                "Presente" to
                    mapOf(
                        "Presente" to
                            listOf(
                                "1ps" to "estoy",
                                "2ps" to "estás",
                                "3ps" to "está",
                                "1pp" to "estamos",
                                "2pp" to "estáis",
                                "3pp" to "están",
                            ),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "Spanish",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Presente"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "estar",
        )

        val btn1 = binding.conjugateGrid.findViewById<Button>(R.id.conjugate_btn_1)
        assertNotNull(btn1)
        assertTrue(btn1.text.toString().contains("1ps"))
        assertTrue(btn1.text.toString().contains("estoy"))

        btn1.performClick()

        verify(exactly = 1) { listener.commitText("estoy ") }
        verify(exactly = 1) { listener.processLinguisticSuggestions("estoy") }
    }

    @Test
    fun updateUI_ThreeCategories_Inflates3x1Grid() {
        // Arrange: 3 categories for English (e.g. Past without simplePast for "be")
        val mockConjugateOutput =
            mapOf(
                "Past" to
                    mapOf(
                        "Past Perfect" to listOf("all" to "had been"),
                        "Past Continuous" to listOf("1" to "was being", "2" to "were being"),
                        "Past Perf. Continuous" to listOf("all" to "had been being"),
                    ),
            )

        // Act
        uiManager.updateUI(
            currentState = ScribeState.SELECT_VERB_CONJUNCTION,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = mockConjugateOutput,
            conjugateLabels = setOf("Past"),
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = "be",
        )

        assertNotNull("Button 1 should be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_1))
        assertNotNull("Button 2 should be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_2))
        assertNotNull("Button 3 should be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_3))
        assertNull("Button 4 should not be present in 3x1 grid", binding.conjugateGrid.findViewById(R.id.conjugate_btn_4))
    }
        
    @Test  
    fun `showClipboardPanel hides toolbarBar and shows clipboardPanelHolder`() {
        uiManager.updateUI(
            currentState = ScribeState.TRANSLATE,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
        )
        assertEquals(View.VISIBLE, toolbarBar.visibility)

        uiManager.showClipboardPanel()

        assertEquals(View.VISIBLE, clipboardPanelHolder.visibility)
        assertEquals(View.INVISIBLE, keyboardView.visibility)
        assertEquals(View.INVISIBLE, commandOptionsBar.visibility)
        assertEquals(View.GONE, toolbarBar.visibility)
        assertEquals(View.GONE, invalidInfoBar.visibility)
    }

    @Test
    fun `hideClipboardPanel restores keyboardView and commandOptionsBar while hiding toolbarBar`() {
        uiManager.showClipboardPanel()
        uiManager.hideClipboardPanel()

        assertEquals(View.GONE, clipboardPanelHolder.visibility)
        assertEquals(View.VISIBLE, keyboardView.visibility)
        assertEquals(View.VISIBLE, commandOptionsBar.visibility)
        assertEquals(View.GONE, toolbarBar.visibility)
        assertEquals(View.GONE, invalidInfoBar.visibility)
    }

    @Test
    fun `showInvalidInfo hides toolbarBar and keyboardView and shows invalidInfoBar`() {
        uiManager.updateUI(
            currentState = ScribeState.INVALID,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = null,
            conjugateLabels = null,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = null,
            invalidCommandSource = ScribeState.CONJUGATE,
        )
        assertEquals(View.VISIBLE, toolbarBar.visibility)
        assertEquals(View.GONE, invalidInfoBar.visibility)

        ivInfo.performClick()

        assertEquals(View.GONE, toolbarBar.visibility)
        assertEquals(View.GONE, keyboardView.visibility)
        assertEquals(View.VISIBLE, invalidInfoBar.visibility)
        assertEquals("Not in Wikidata", binding.invalidText.text.toString())
        assertNotNull(binding.invalidText.backgroundTintList)
    }

    @Test
    fun `showEmojiPalette and hideEmojiPalette keep toolbarBar hidden`() {
        uiManager.showEmojiPalette("English")

        assertEquals(View.VISIBLE, emojiPaletteHolder.visibility)
        assertEquals(View.GONE, toolbarBar.visibility)
        assertEquals(View.GONE, keyboardView.visibility)
        assertEquals(View.GONE, commandOptionsBar.visibility)

        uiManager.hideEmojiPalette()

        assertEquals(View.GONE, emojiPaletteHolder.visibility)
        assertEquals(View.VISIBLE, keyboardView.visibility)
        assertEquals(View.VISIBLE, commandOptionsBar.visibility)
        assertEquals(View.GONE, toolbarBar.visibility)
    }
}
