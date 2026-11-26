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
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    @Test
    fun invalidBarIsDarkWhenScribeIsDarkOnADayDevice() {
        val binding = showState(ScribeState.INVALID, scribeDark = true, deviceNight = false)
        assertEquals(0xFF1E1E1E.toInt(), binding.toolbarBar.backgroundColor())
    }

    @Test
    @Config(qualifiers = "night")
    fun invalidBarIsLightWhenScribeIsLightOnANightDevice() {
        val binding = showState(ScribeState.INVALID, scribeDark = false, deviceNight = true)
        assertEquals(0xFFD2D4DA.toInt(), binding.toolbarBar.backgroundColor())
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
        assertEquals(0xFFD2D4DA.toInt(), binding.toolbarBar.backgroundColor())
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

    private fun showState(
        state: ScribeState,
        scribeDark: Boolean,
        deviceNight: Boolean,
        conjugateOutput: Map<String, Map<String, List<Pair<String, String>>>>? = null,
        conjugateLabels: Set<String>? = null,
        verb: String? = null,
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
        val listener = mockk<KeyboardUIManager.KeyboardUIListener>(relaxed = true)
        every { listener.getKeyboardLayoutXML() } returns R.xml.keys_letters_english
        every { listener.getCurrentKeyboardLayoutXML() } returns R.xml.keys_letters_english
        every { listener.onKeyboardActionListener() } returns mockk()
        every { listener.isNumericKeyboardActive() } returns false

        KeyboardUIManager(binding, context, listener).updateUI(
            currentState = state,
            language = "English",
            emojiAutoSuggestionEnabled = false,
            autoSuggestEmojis = null,
            conjugateOutput = conjugateOutput,
            conjugateLabels = conjugateLabels,
            selectedConjugationSubCategory = null,
            currentVerbForConjugation = verb,
        )
        return binding
    }

    private fun showConjugationGrid(
        scribeDark: Boolean,
        deviceNight: Boolean,
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
            conjugateOutput =
                mapOf(
                    "Present" to
                        mapOf(
                            "1ps" to listOf("1ps" to "I am"),
                            "2ps" to listOf("2ps" to "You are"),
                        ),
                ),
            conjugateLabels = setOf("1ps", "2ps"),
            verb = "be",
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
