// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Light and dark mode themes for the application.
 */

package be.scri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = theme_light_button_color,
        onPrimary = theme_light_button_text_color,
        background = theme_light_background,
        onBackground = theme_light_text_color,
        surface = theme_light_card_view_color,
        onSurface = theme_light_text_color,
        outline = theme_light_button_color,
        secondary = theme_light_selected_button_color,
        tertiary = theme_light_switch_selector_color,
        tertiaryContainer = theme_light_switch_container_color,
        outlineVariant = theme_light_unchecked_switch_selector_color,
        surfaceContainer = theme_light_corner_button_color,
    )

private val DarkColors =
    darkColorScheme(
        primary = theme_dark_button_color,
        onPrimary = theme_dark_button_text_color,
        background = theme_dark_background,
        onBackground = theme_dark_text_color,
        surface = theme_dark_card_view_color,
        onSurface = theme_dark_text_color,
        outline = theme_dark_button_outline_color,
        secondary = theme_dark_selected_button_color,
        tertiary = theme_dark_switch_selector_color,
        tertiaryContainer = theme_dark_switch_container_color,
        outlineVariant = theme_dark_unchecked_switch_selector_color,
        surfaceContainer = theme_dark_corner_button_color,
    )

@Composable
fun ScribeTheme(
    useDarkTheme: Boolean,
    @Suppress("ktlint:standard:annotation")
    content: @Composable() () -> Unit,
) {
    val colors =
        if (!useDarkTheme) {
            LightColors
        } else {
            DarkColors
        }

    MaterialTheme(
        colorScheme = colors,
        content = content,
        typography = ScribeTypography,
    )
}
