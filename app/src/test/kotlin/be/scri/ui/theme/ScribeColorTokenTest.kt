// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import be.scri.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScribeColorTokenTest {
    @Test
    fun dayTokensMatchTheColorsAlreadyShipping() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFF5EB5E8.toInt(), color(context, R.color.scribe_background))
        assertEquals(0xFF000000.toInt(), color(context, R.color.scribe_on_button))
        assertEquals(0xFFFDAD0D.toInt(), color(context, R.color.scribe_button))
        assertEquals(0xFFD3D6DD.toInt(), color(context, R.color.scribe_keyboard_background))
        assertEquals(0xFF75CEFA.toInt(), color(context, R.color.scribe_accent_blue))
        assertEquals(0xFF5EB5E8.toInt(), color(context, R.color.light_scribe_color))
        assertEquals(0xFF235270.toInt(), color(context, R.color.dark_scribe_color))
        assertEquals(0xFF4CADE6.toInt(), color(context, R.color.theme_scribe_blue))
        assertEquals(0xFFFFFFFF.toInt(), color(context, R.color.light_key_color))
        assertEquals(0xFF434343.toInt(), color(context, R.color.dark_key_color))
    }

    @Test
    @Config(qualifiers = "night")
    fun nightTokensMatchTheColorsAlreadyShipping() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFF235270.toInt(), color(context, R.color.scribe_background))
        assertEquals(0xFFD17B0F.toInt(), color(context, R.color.scribe_on_button))
        assertEquals(0xFFD17B0F.toInt(), color(context, R.color.scribe_button))
        assertEquals(0xFF1E1E1E.toInt(), color(context, R.color.scribe_keyboard_background))
        assertEquals(0xFF4CADE6.toInt(), color(context, R.color.scribe_accent_blue))
        assertEquals(0xFFCAC4D0.toInt(), color(context, R.color.scribe_caption))
        assertEquals(0xFF5EB5E8.toInt(), color(context, R.color.light_scribe_color))
        assertEquals(0xFF235270.toInt(), color(context, R.color.dark_scribe_color))
        assertEquals(0xFF4CADE6.toInt(), color(context, R.color.theme_scribe_blue))
        assertEquals(0xFFFFFFFF.toInt(), color(context, R.color.light_key_color))
        assertEquals(0xFF434343.toInt(), color(context, R.color.dark_key_color))
        assertEquals(0xFFD3D6DD.toInt(), color(context, R.color.light_keyboard_bg_color))
        assertEquals(0xFF1E1E1E.toInt(), color(context, R.color.dark_keyboard_bg_color))
    }

    private fun color(
        context: Context,
        name: Int,
    ): Int = ContextCompat.getColor(context, name)
}
