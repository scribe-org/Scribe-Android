// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import be.scri.R

val theme_light_card_view_color = Color(color = 0xFFFFFFFF)
val theme_light_button_color = Color(color = 0xFFFDAD0D)
val theme_light_button_text_color = Color(color = 0xFF000000)
val theme_light_background = Color(color = 0xFF5EB5E8)
val theme_light_text_color = Color(color = 0xFF000000)
val theme_light_selected_button_color = Color(color = 0xFF235270)
val theme_light_switch_container_color = Color(color = 0xFFAAAAAA)
val theme_light_switch_selector_color = Color(color = 0xFFFEDE9E)
val theme_light_unchecked_switch_selector_color = Color(color = 0xFFEEEEEE)
val theme_light_corner_button_color = Color(color = 0xFFFDAD0D)
val theme_light_success_color = Color(color = 0xFF9BC53D)
val theme_light_caption_color = Color(color = 0xE558564B)
val theme_light_delete_button_color = Color(color = 0xFFE4572E)

val theme_dark_card_view_color = Color(color = 0xFF000000)
val theme_dark_button_color = Color(color = 0xFFD17B0F)
val theme_dark_button_text_color = Color(color = 0xFFD17B0F)
val theme_dark_background = Color(color = 0xFF235270)
val theme_dark_text_color = Color(color = 0xFFFFFFFF)
val theme_dark_button_outline_color = Color(color = 0xFFD17B0F)
val theme_dark_selected_button_color = Color(color = 0xFF5EB5E8)
val theme_dark_switch_container_color = Color(color = 0xFFAAAAAA)
val theme_dark_switch_selector_color = Color(color = 0xFF2A1903)
val theme_dark_unchecked_switch_selector_color = Color(color = 0xFF2D2D2D)
val theme_dark_corner_button_color = Color(color = 0xFF2A1903)
val theme_dark_success_color = Color(color = 0xFF08A045)
val theme_dark_delete_button_color = Color(color = 0xFFD11149)

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
