// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import be.scri.R
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.PreferencesHelper
import be.scri.models.ScribeState
import be.scri.ui.theme.clearScribeColorCache
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The keyboard colors moved to shared `scribe_*` names in #783 keep their old hex, and follow the
 * Scribe dark mode setting even when the device night mode says the opposite.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyboardSharedColorsTest {
    @Before
    fun resetColorCache() = clearScribeColorCache()

    @Test
    fun invalidBarIsDarkWhenScribeIsDarkOnADayDevice() {
        val binding = showState(ScribeState.INVALID, scribeDark = true, deviceNight = false)
        assertEquals(0xFF1E1E1E.toInt(), binding.toolbarBar.backgroundColor())
    }

    @Test
    @Config(qualifiers = "night")
    fun invalidBarIsLightWhenScribeIsLightOnANightDevice() {
        val binding = showState(ScribeState.INVALID, scribeDark = false, deviceNight = true)
        assertEquals(0xFFD3D6DD.toInt(), binding.toolbarBar.backgroundColor())
    }

    @Test
    fun alreadyPluralBarIsDarkWhenScribeIsDarkOnADayDevice() {
        val binding = showState(ScribeState.ALREADY_PLURAL, scribeDark = true, deviceNight = false)
        assertEquals(0xFF1E1E1E.toInt(), binding.toolbarBar.backgroundColor())
    }

    @Test
    @Config(qualifiers = "night")
    fun alreadyPluralBarIsLightWhenScribeIsLightOnANightDevice() {
        val binding = showState(ScribeState.ALREADY_PLURAL, scribeDark = false, deviceNight = true)
        assertEquals(0xFFD3D6DD.toInt(), binding.toolbarBar.backgroundColor())
    }

    @Test
    fun conjugationLabelUsesTheDarkCaptionWhenScribeIsDarkOnADayDevice() {
        val binding = showConjugationGrid(scribeDark = true, deviceNight = false)
        assertEquals(0xFFCAC4D0.toInt(), binding.root.labelColor("1ps"))
    }

    @Test
    @Config(qualifiers = "night")
    fun conjugationLabelUsesTheLightCaptionWhenScribeIsLightOnANightDevice() {
        val binding = showConjugationGrid(scribeDark = false, deviceNight = true)
        assertEquals(0xE558564B.toInt(), binding.root.labelColor("1ps"))
    }

    @Test
    fun commandSeparatorsStayTransparentInBothModes() {
        listOf(true, false).forEach { dark ->
            val binding = showState(ScribeState.SELECT_COMMAND, scribeDark = dark, deviceNight = false)
            assertEquals(0, binding.separator2.backgroundColor())
            assertEquals(0, binding.separator3.backgroundColor())
        }
    }

    @Test
    @Config(qualifiers = "night")
    fun letterKeysUseScribeKeyAndTextOnANightDevice() {
        val binding = showState(ScribeState.IDLE, scribeDark = false, deviceNight = true)
        val light = binding.keyboardView.keyColorsFor(dark = false)
        val dark = binding.keyboardView.keyColorsFor(dark = true)
        assertEquals(0xFFFFFFFF.toInt(), light.key)
        assertEquals(0xFF000000.toInt(), light.label)
        assertEquals(0xFF434343.toInt(), dark.key)
        assertEquals(0xFFFFFFFF.toInt(), dark.label)
        // Enter, delete, shift and the mode key use the Figma special-key values.
        assertEquals(0xFFADB3BD.toInt(), light.special)
        assertEquals(0xFF2D2D2D.toInt(), dark.special)
    }

    @Test
    fun letterKeysUseScribeKeyAndTextOnADayDevice() {
        val binding = showState(ScribeState.IDLE, scribeDark = true, deviceNight = false)
        assertEquals(0xFF434343.toInt(), binding.keyboardView.keyColorsFor(dark = true).key)
        assertEquals(0xFFFFFFFF.toInt(), binding.keyboardView.keyColorsFor(dark = false).key)
        assertEquals(0xFF2D2D2D.toInt(), binding.keyboardView.keyColorsFor(dark = true).special)
        assertEquals(0xFFADB3BD.toInt(), binding.keyboardView.keyColorsFor(dark = false).special)
    }

    @Test
    @Config(qualifiers = "night")
    fun suggestionsMatchTheKeyLabelsWhenScribeIsLightOnANightDevice() {
        val binding = showState(ScribeState.IDLE, scribeDark = false, deviceNight = true)
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFF000000.toInt(), KeyboardThemeManager().getSuggestionTextColor(context))
        assertEquals(0xFF000000.toInt(), binding.translateBtn.currentTextColor)
    }

    @Test
    fun suggestionsMatchTheKeyLabelsWhenScribeIsDarkOnADayDevice() {
        val binding = showState(ScribeState.IDLE, scribeDark = true, deviceNight = false)
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFFFFFFFF.toInt(), KeyboardThemeManager().getSuggestionTextColor(context))
        assertEquals(0xFFFFFFFF.toInt(), binding.translateBtn.currentTextColor)
    }

    @Test
    @Config(qualifiers = "night")
    fun clipboardClearAllUsesTheLightDeleteRedWhenScribeIsLightOnANightDevice() {
        val binding = showState(ScribeState.IDLE, scribeDark = false, deviceNight = true)
        KeyboardUIManager(binding, ApplicationProvider.getApplicationContext(), relaxedListener()).applyClipboardPanelTheme()
        assertEquals(0xFFE4572E.toInt(), binding.clipboardClearAll.imageTintList!!.defaultColor)
    }

    @Test
    fun clipboardClearAllUsesTheDarkDeleteRedWhenScribeIsDarkOnADayDevice() {
        val binding = showState(ScribeState.IDLE, scribeDark = true, deviceNight = false)
        KeyboardUIManager(binding, ApplicationProvider.getApplicationContext(), relaxedListener()).applyClipboardPanelTheme()
        assertEquals(0xFFD11149.toInt(), binding.clipboardClearAll.imageTintList!!.defaultColor)
    }

    @Test
    @Config(qualifiers = "night")
    fun directConjugationLabelsFollowTheScribeSetting() {
        val light = showConjugationGrid(scribeDark = false, deviceNight = true, output = DIRECT_OUTPUT)
        assertEquals(0xE558564B.toInt(), light.root.labelColor("1ps"))
        val dark = showConjugationGrid(scribeDark = true, deviceNight = true, output = DIRECT_OUTPUT)
        assertEquals(0xFFCAC4D0.toInt(), dark.root.labelColor("1ps"))
    }

    @Test
    fun subSelectionConjugationLabelsFollowTheScribeSetting() {
        val dark = showConjugationGrid(scribeDark = true, deviceNight = false, output = SUB_OUTPUT, subCategory = "1ps")
        assertEquals(0xFFCAC4D0.toInt(), dark.root.labelColor("sg"))
        val light = showConjugationGrid(scribeDark = false, deviceNight = false, output = SUB_OUTPUT, subCategory = "1ps")
        assertEquals(0xE558564B.toInt(), light.root.labelColor("sg"))
    }

    @Test
    fun enterKeyUsesTheSpecialKeyColorForTheScribeSetting() {
        val binding = showState(ScribeState.IDLE, scribeDark = false, deviceNight = false)
        binding.keyboardView.setEnterKeyColor(null, isDarkMode = true)
        assertEquals(0xFF2D2D2D.toInt(), binding.keyboardView.enterKeyColor)
        binding.keyboardView.setEnterKeyColor(null, isDarkMode = false)
        assertEquals(0xFFADB3BD.toInt(), binding.keyboardView.enterKeyColor)
    }

    @Test
    fun clearAllHasNoXmlTintAndShowingTheClipboardTintsIt() {
        val binding = showState(ScribeState.IDLE, scribeDark = true, deviceNight = false)
        // The XML tint followed the device night mode, so the color now only comes from code.
        val inflated = InputMethodViewBinding.inflate(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        assertNull(inflated.clipboardClearAll.imageTintList)

        KeyboardUIManager(binding, ApplicationProvider.getApplicationContext(), relaxedListener()).showClipboardPanel()
        assertEquals(0xFFD11149.toInt(), binding.clipboardClearAll.imageTintList!!.defaultColor)
    }

    private fun showState(
        state: ScribeState,
        scribeDark: Boolean,
        deviceNight: Boolean,
        conjugateOutput: Map<String, Map<String, List<Pair<String, String>>>>? = null,
        conjugateLabels: Set<String>? = null,
        verb: String? = null,
        subCategory: String? = null,
    ): InputMethodViewBinding {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context
            .getSharedPreferences(PreferencesHelper.SCRIBE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("dark_mode", scribeDark)
            .putBoolean("last_system_dark_mode", deviceNight)
            .commit()
        assertEquals(scribeDark, PreferencesHelper.getIsDarkModeOrNot(context))

        val binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))
        KeyboardUIManager(binding, context, relaxedListener()).updateUI(
            currentState = state,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = conjugateOutput,
            conjugateLabels = conjugateLabels,
            selectedConjugationSubCategory = subCategory,
            currentVerbForConjugation = verb,
        )
        return binding
    }

    private fun relaxedListener(): KeyboardUIManager.KeyboardUIListener {
        val listener = mockk<KeyboardUIManager.KeyboardUIListener>(relaxed = true)
        every { listener.getKeyboardLayoutXML() } returns R.xml.keys_letters_english
        every { listener.getCurrentKeyboardLayoutXML() } returns R.xml.keys_letters_english
        every { listener.onKeyboardActionListener() } returns mockk()
        every { listener.isNumericKeyboardActive() } returns false
        return listener
    }

    private fun showConjugationGrid(
        scribeDark: Boolean,
        deviceNight: Boolean,
        output: Map<String, Map<String, List<Pair<String, String>>>> = CATEGORY_OUTPUT,
        subCategory: String? = null,
    ): InputMethodViewBinding {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context
            .getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
            .edit()
            .putInt("conjugate_index", 0)
            .commit()
        return showState(
            ScribeState.SELECT_VERB_CONJUNCTION,
            scribeDark = scribeDark,
            deviceNight = deviceNight,
            conjugateOutput = output,
            conjugateLabels = setOf("1ps", "2ps"),
            verb = "be",
            subCategory = subCategory,
        )
    }

    private companion object {
        val CATEGORY_OUTPUT =
            mapOf(
                "Present" to
                    mapOf(
                        "1ps" to listOf("1ps" to "I am"),
                        "2ps" to listOf("2ps" to "You are"),
                    ),
            )
        val DIRECT_OUTPUT =
            mapOf(
                "Present" to mapOf("Present" to listOf("1ps" to "I am", "2ps" to "You are")),
            )
        val SUB_OUTPUT =
            mapOf(
                "Present" to mapOf("1ps" to listOf("sg" to "I am", "pl" to "we are")),
            )
    }

    private fun View.backgroundColor(): Int = (background as ColorDrawable).color

    private fun View.labelColor(label: String): Int {
        val button = findButtonStartingWith(label)
        assertNotNull("No conjugation button starts with $label", button)
        val text = button!!.text as Spanned
        val span = text.getSpans(0, label.length, ForegroundColorSpan::class.java).single()
        return span.foregroundColor
    }

    private fun View.findButtonStartingWith(label: String): Button? {
        if (this is Button && text is Spanned && text.startsWith(label)) return this
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).findButtonStartingWith(label)?.let { return it }
            }
        }
        return null
    }
}
