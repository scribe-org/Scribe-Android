// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import be.scri.databinding.InputMethodViewBinding
import be.scri.ui.theme.clearScribeColorCache
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * The floating keyboard border reads `scribe_keyboard_border` (#40888888) in both modes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FloatingKeyboardBorderTest {
    @Before
    fun resetColorCache() = clearScribeColorCache()

    @Test
    fun floatingBorderUsesScribeKeyboardBorderInLightMode() {
        assertEquals(0x40888888, floatingBorderColor(scribeDark = false))
    }

    @Test
    @Config(qualifiers = "night")
    fun floatingBorderUsesScribeKeyboardBorderInDarkMode() {
        assertEquals(0x40888888, floatingBorderColor(scribeDark = true))
    }

    private fun floatingBorderColor(scribeDark: Boolean): Int {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deviceNight = context.resources.configuration.isNightModeActive
        context
            .getSharedPreferences(PreferencesHelper.SCRIBE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("dark_mode", scribeDark)
            .putBoolean("last_system_dark_mode", deviceNight)
            .commit()
        PreferencesHelper.setIsFloatingModeEnabled(context, LANGUAGE, true)

        val binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))
        val ime = mockk<KeyboardIMEContext>(relaxed = true)
        every { ime.imeContext } returns context
        every { ime.language } returns LANGUAGE
        every { ime.binding } returns binding
        every { ime.isUiManagerInitialized } returns true
        every { ime.getImeResources() } returns context.resources
        every { ime.getImeWindow() } returns null

        FloatingKeyboardHandler(ime).initFloatingMode()
        return shadowOf(binding.keyboardCard.background as GradientDrawable).strokeColor
    }

    private companion object {
        const val LANGUAGE = "English"
    }
}
