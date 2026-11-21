// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScribeColorSchemeTest {
    @Test
    @Config(qualifiers = "night")
    fun lightSchemeFollowsTheArgumentWhenTheDeviceIsNight() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheme = context.scribeColorScheme(dark = false)
        assertEquals(0xFF5EB5E8.toInt(), scheme.background.toArgb())
        assertEquals(0xFF000000.toInt(), scheme.onPrimary.toArgb())
        assertEquals(0xFFFDAD0D.toInt(), scheme.primary.toArgb())
        assertEquals(0xE558564B.toInt(), scheme.onSurfaceVariant.toArgb())
        assertEquals(0xFFFEDE9E.toInt(), scheme.tertiary.toArgb())
        assertEquals(0xFF9BC53D.toInt(), scheme.surfaceVariant.toArgb())
    }

    @Test
    fun darkSchemeFollowsTheArgumentWhenTheDeviceIsDay() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheme = context.scribeColorScheme(dark = true)
        assertEquals(0xFF235270.toInt(), scheme.background.toArgb())
        assertEquals(0xFFD17B0F.toInt(), scheme.onPrimary.toArgb())
        assertEquals(0xFFD17B0F.toInt(), scheme.primary.toArgb())
        assertEquals(0xFFCAC4D0.toInt(), scheme.onSurfaceVariant.toArgb())
        assertEquals(0xFF2A1903.toInt(), scheme.tertiary.toArgb())
        assertEquals(0xFF08A045.toInt(), scheme.surfaceVariant.toArgb())
    }
}
