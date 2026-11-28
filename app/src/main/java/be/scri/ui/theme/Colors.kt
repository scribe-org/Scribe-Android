// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import be.scri.R

/**
 * Compose color scheme from the shared `scribe_*` resources.
 * [dark] follows the app theme argument, not the device night mode.
 */
fun Context.scribeColorScheme(dark: Boolean): ColorScheme {
    val themed = scribeNightContext(dark)
    val button = themed.scribeColor(R.color.scribe_button)
    val onButton = themed.scribeColor(R.color.scribe_on_button)
    val background = themed.scribeColor(R.color.scribe_background)
    val text = themed.scribeColor(R.color.scribe_text)
    val surface = themed.scribeColor(R.color.scribe_surface)
    val outline = themed.scribeColor(R.color.scribe_outline)
    val selected = themed.scribeColor(R.color.scribe_selected)
    val switchThumb = themed.scribeColor(R.color.scribe_switch_thumb)
    val switchTrack = themed.scribeColor(R.color.scribe_switch_track)
    val switchThumbOff = themed.scribeColor(R.color.scribe_switch_thumb_off)
    val cornerButton = themed.scribeColor(R.color.scribe_corner_button)
    val success = themed.scribeColor(R.color.scribe_success)
    val caption = themed.scribeColor(R.color.scribe_caption)
    return if (dark) {
        darkColorScheme(
            primary = button,
            onPrimary = onButton,
            background = background,
            onBackground = text,
            surface = surface,
            onSurface = text,
            outline = outline,
            secondary = selected,
            tertiary = switchThumb,
            tertiaryContainer = switchTrack,
            outlineVariant = switchThumbOff,
            surfaceContainer = cornerButton,
            surfaceVariant = success,
            onSurfaceVariant = caption,
        )
    } else {
        lightColorScheme(
            primary = button,
            onPrimary = onButton,
            background = background,
            onBackground = text,
            surface = surface,
            onSurface = text,
            outline = outline,
            secondary = selected,
            tertiary = switchThumb,
            tertiaryContainer = switchTrack,
            outlineVariant = switchThumbOff,
            surfaceContainer = cornerButton,
            surfaceVariant = success,
            onSurfaceVariant = caption,
        )
    }
}

private fun Context.scribeColor(id: Int): Color = Color(color = ContextCompat.getColor(this, id))
