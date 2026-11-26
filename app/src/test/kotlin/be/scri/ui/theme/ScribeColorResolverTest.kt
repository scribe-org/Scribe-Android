// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import be.scri.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The keyboard colors moved off hex literals in #783 must keep the exact values they had.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScribeColorResolverTest {
    @Test
    fun keyboardColorsMatchTheOldHexOnADayDevice() {
        assertKeyboardColors(ApplicationProvider.getApplicationContext())
    }

    @Test
    @Config(qualifiers = "night")
    fun keyboardColorsMatchTheOldHexOnANightDevice() {
        assertKeyboardColors(ApplicationProvider.getApplicationContext())
    }

    private fun assertKeyboardColors(context: Context) {
        // Conjugation sub-labels, was "#E558564B" / "#CAC4D0".
        assertEquals(0xE558564B.toInt(), context.scribeColorInt(R.color.scribe_caption, dark = false))
        assertEquals(0xFFCAC4D0.toInt(), context.scribeColorInt(R.color.scribe_caption, dark = true))
        // Dark invalid and already-plural command bar, was "#1E1E1E".
        assertEquals(0xFF1E1E1E.toInt(), context.scribeColorInt(R.color.scribe_keyboard_background, dark = true))
    }
}
