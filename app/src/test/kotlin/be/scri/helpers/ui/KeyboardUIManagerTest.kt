// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.ui.KeyboardUIManager.KeyboardUIListener
import be.scri.services.GeneralKeyboardIME.ScribeState
import be.scri.views.KeyboardView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
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

    // Real Views (Robolectric handles these)
    private lateinit var keyboardView: KeyboardView
    private lateinit var commandOptionsBar: ConstraintLayout
    private lateinit var toolbarBar: ConstraintLayout
    private lateinit var translateBtn: Button
    private lateinit var conjugateBtn: Button
    private lateinit var pluralBtn: Button
    private lateinit var translateBtnLeft: Button
    private lateinit var translateBtnRight: Button
    private lateinit var scribeKeyOptions: Button
    private lateinit var separator2: View
    private lateinit var separator3: View
    private lateinit var commandBarEditText: EditText
    private lateinit var promptText: TextView
    private lateinit var ivInfo: AppCompatImageView
    private lateinit var scribeKeyToolbar: Button
    private lateinit var separator1: View
    private lateinit var conjugateGridContainer: FrameLayout
    private lateinit var conjugateGrid: LinearLayout
    private lateinit var commandBarLayout: LinearLayout

    // Emoji Buttons
    private lateinit var emojiBtnPhone1: Button
    private lateinit var emojiBtnPhone2: Button
    private lateinit var emojiSpacePhone: TextView
    private lateinit var emojiBtnTablet1: Button
    private lateinit var emojiBtnTablet2: Button
    private lateinit var emojiBtnTablet3: Button
    private lateinit var emojiSpaceTablet1: TextView
    private lateinit var emojiSpaceTablet2: TextView
    private lateinit var separator4: View
    private lateinit var separator5: View
    private lateinit var separator6: View

    // Invalid Info Views
    private lateinit var invalidInfoBar: ConstraintLayout
    private lateinit var invalidText: TextView
    private lateinit var scribeKeyClose: Button
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button
    private lateinit var middleTextview: TextView
    private lateinit var pageIndicators: LinearLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Initialize Real Views
        keyboardView = KeyboardView(context, null)
        commandOptionsBar = ConstraintLayout(context)
        toolbarBar = ConstraintLayout(context)
        translateBtn = Button(context)
        conjugateBtn = Button(context)
        pluralBtn = Button(context)
        translateBtnLeft = Button(context)
        translateBtnRight = Button(context)
        scribeKeyOptions = Button(context)
        separator2 = View(context)
        separator3 = View(context)
        commandBarEditText = EditText(context)
        promptText = TextView(context)
        ivInfo = AppCompatImageView(context)
        scribeKeyToolbar = Button(context)
        separator1 = View(context)
        conjugateGridContainer = FrameLayout(context)
        conjugateGrid = LinearLayout(context)
        commandBarLayout = LinearLayout(context)

        emojiBtnPhone1 = Button(context)
        emojiBtnPhone2 = Button(context)
        emojiSpacePhone = TextView(context)
        emojiBtnTablet1 = Button(context)
        emojiBtnTablet2 = Button(context)
        emojiBtnTablet3 = Button(context)
        emojiSpaceTablet1 = TextView(context)
        emojiSpaceTablet2 = TextView(context)
        separator4 = View(context)
        separator5 = View(context)
        separator6 = View(context)

        invalidInfoBar = ConstraintLayout(context)
        invalidText = TextView(context)
        scribeKeyClose = Button(context)
        buttonLeft = Button(context)
        buttonRight = Button(context)
        middleTextview = TextView(context)
        pageIndicators = LinearLayout(context)

        // Mock Binding to return our Real Views
        binding = mockk(relaxed = true)
        every { binding.root } returns ConstraintLayout(context)
        every { binding.keyboardView } returns keyboardView
        every { binding.commandOptionsBar } returns commandOptionsBar
        every { binding.toolbarBar } returns toolbarBar
        every { binding.translateBtn } returns translateBtn
        every { binding.conjugateBtn } returns conjugateBtn
        every { binding.pluralBtn } returns pluralBtn
        every { binding.translateBtnLeft } returns translateBtnLeft
        every { binding.translateBtnRight } returns translateBtnRight
        every { binding.scribeKeyOptions } returns scribeKeyOptions
        every { binding.separator2 } returns separator2
        every { binding.separator3 } returns separator3
        every { binding.commandBar } returns commandBarEditText
        every { binding.promptText } returns promptText
        every { binding.ivInfo } returns ivInfo
        every { binding.scribeKeyToolbar } returns scribeKeyToolbar
        every { binding.separator1 } returns separator1
        every { binding.conjugateGridContainer } returns conjugateGridContainer
        every { binding.conjugateGrid } returns conjugateGrid
        every { binding.commandBarLayout } returns commandBarLayout

        every { binding.emojiBtnPhone1 } returns emojiBtnPhone1
        every { binding.emojiBtnPhone2 } returns emojiBtnPhone2
        every { binding.emojiSpacePhone } returns emojiSpacePhone
        every { binding.emojiBtnTablet1 } returns emojiBtnTablet1
        every { binding.emojiBtnTablet2 } returns emojiBtnTablet2
        every { binding.emojiBtnTablet3 } returns emojiBtnTablet3
        every { binding.emojiSpaceTablet1 } returns emojiSpaceTablet1
        every { binding.emojiSpaceTablet2 } returns emojiSpaceTablet2
        every { binding.separator4 } returns separator4
        every { binding.separator5 } returns separator5
        every { binding.separator6 } returns separator6

        every { binding.invalidInfoBar } returns invalidInfoBar
        every { binding.invalidText } returns invalidText
        every { binding.scribeKeyClose } returns scribeKeyClose
        every { binding.buttonLeft } returns buttonLeft
        every { binding.buttonRight } returns buttonRight
        every { binding.middleTextview } returns middleTextview
        every { binding.pageIndicators } returns pageIndicators

        // Mock Listener
        listener = mockk(relaxed = true)
        every { listener.getKeyboardLayoutXML() } returns 0
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
    @Config(qualifiers = "large") // Force Tablet layout
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
    fun `disableAutoSuggest resets buttons to commands`() {
        uiManager.updateUI(ScribeState.IDLE, "English", false, null, null, null, null, null)

        uiManager.disableAutoSuggest("English")

        translateBtn.performClick()

        verify { listener.onSuggestionClicked(any()) }
        assertEquals(View.VISIBLE, translateBtn.visibility)
        assertEquals(View.INVISIBLE, translateBtnLeft.visibility)
    }
}
