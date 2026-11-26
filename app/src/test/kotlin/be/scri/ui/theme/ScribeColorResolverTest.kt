// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import be.scri.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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

    @Test
    fun everyScribeColorIgnoresTheDeviceNightMode() {
        val ids =
            R.color::class.java.fields
                .filter { it.name.startsWith("scribe_") }
                .associate { it.name to it.getInt(null) }
        // Guards against the list silently shrinking if the R class shape changes.
        assertEquals(true, ids.size >= 26)

        val onDayDevice = ids.mapValues { (_, id) -> context().bothModes(id) }
        RuntimeEnvironment.setQualifiers("+night")
        val onNightDevice = ids.mapValues { (_, id) -> context().bothModes(id) }

        assertEquals(onDayDevice, onNightDevice)
        // The light and dark halves really come from different files.
        assertEquals(0xFF5EB5E8.toInt() to 0xFF235270.toInt(), onDayDevice.getValue("scribe_background"))
    }

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    private fun Context.bothModes(id: Int): Pair<Int, Int> = scribeColorInt(id, dark = false) to scribeColorInt(id, dark = true)

    private fun assertKeyboardColors(context: Context) {
        // Conjugation sub-labels, was "#E558564B" / "#CAC4D0".
        assertEquals(0xE558564B.toInt(), context.scribeColorInt(R.color.scribe_caption, dark = false))
        assertEquals(0xFFCAC4D0.toInt(), context.scribeColorInt(R.color.scribe_caption, dark = true))
        // Dark invalid and already-plural command bar, was "#1E1E1E".
        assertEquals(0xFF1E1E1E.toInt(), context.scribeColorInt(R.color.scribe_keyboard_background, dark = true))
        // Floating keyboard border, was 0x40888888 in both modes.
        assertEquals(0x40888888, context.scribeColorInt(R.color.scribe_keyboard_border, dark = false))
        assertEquals(0x40888888, context.scribeColorInt(R.color.scribe_keyboard_border, dark = true))
    }
}
