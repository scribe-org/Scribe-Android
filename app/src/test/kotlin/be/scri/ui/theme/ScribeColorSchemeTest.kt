// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
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
        // All 14 slots set in Colors.kt, from the light scribe_* resources.
        scheme.assertSlots(
            "primary" to 0xFFFDAD0D.toInt(),
            "onPrimary" to 0xFF000000.toInt(),
            "background" to 0xFF5EB5E8.toInt(),
            "onBackground" to 0xFF000000.toInt(),
            "surface" to 0xFFFFFFFF.toInt(),
            "onSurface" to 0xFF000000.toInt(),
            "outline" to 0xFFFDAD0D.toInt(),
            "secondary" to 0xFF235270.toInt(),
            "tertiary" to 0xFFFEDE9E.toInt(),
            "tertiaryContainer" to 0xFFAAAAAA.toInt(),
            "outlineVariant" to 0xFFEEEEEE.toInt(),
            "surfaceContainer" to 0xFFFDAD0D.toInt(),
            "surfaceVariant" to 0xFF9BC53D.toInt(),
            "onSurfaceVariant" to 0xE558564B.toInt(),
        )
    }

    @Test
    fun darkSchemeFollowsTheArgumentWhenTheDeviceIsDay() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheme = context.scribeColorScheme(dark = true)
        // All 14 slots set in Colors.kt, from the night scribe_* resources.
        scheme.assertSlots(
            "primary" to 0xFFD17B0F.toInt(),
            "onPrimary" to 0xFFD17B0F.toInt(),
            "background" to 0xFF235270.toInt(),
            "onBackground" to 0xFFFFFFFF.toInt(),
            "surface" to 0xFF000000.toInt(),
            "onSurface" to 0xFFFFFFFF.toInt(),
            "outline" to 0xFFD17B0F.toInt(),
            "secondary" to 0xFF5EB5E8.toInt(),
            "tertiary" to 0xFF2A1903.toInt(),
            "tertiaryContainer" to 0xFFAAAAAA.toInt(),
            "outlineVariant" to 0xFF2D2D2D.toInt(),
            "surfaceContainer" to 0xFF2A1903.toInt(),
            "surfaceVariant" to 0xFF08A045.toInt(),
            "onSurfaceVariant" to 0xFFCAC4D0.toInt(),
        )
    }
}

private fun ColorScheme.assertSlots(vararg expected: Pair<String, Int>) {
    val actual =
        listOf(
            "primary" to primary.toArgb(),
            "onPrimary" to onPrimary.toArgb(),
            "background" to background.toArgb(),
            "onBackground" to onBackground.toArgb(),
            "surface" to surface.toArgb(),
            "onSurface" to onSurface.toArgb(),
            "outline" to outline.toArgb(),
            "secondary" to secondary.toArgb(),
            "tertiary" to tertiary.toArgb(),
            "tertiaryContainer" to tertiaryContainer.toArgb(),
            "outlineVariant" to outlineVariant.toArgb(),
            "surfaceContainer" to surfaceContainer.toArgb(),
            "surfaceVariant" to surfaceVariant.toArgb(),
            "onSurfaceVariant" to onSurfaceVariant.toArgb(),
        )
    assertEquals(14, actual.size)
    assertEquals(expected.toList(), actual)
}
