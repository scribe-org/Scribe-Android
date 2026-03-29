// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
        surfaceVariant = theme_light_success_color,
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
        surfaceVariant = theme_dark_success_color,
    )

private const val ACCESSIBILITY_TEXT_SIZE_MULTIPLIER = 1.25f

@Composable
fun ScribeTheme(
    useDarkTheme: Boolean,
    increaseTextSize: Boolean = false,
    @Suppress("ktlint:standard:annotation")
    content: @Composable() () -> Unit,
) {
    val colors =
        if (!useDarkTheme) {
            LightColors
        } else {
            DarkColors
        }

    val baseDensity = LocalDensity.current
    val targetFontScale =
        baseDensity.fontScale *
            if (increaseTextSize) {
                ACCESSIBILITY_TEXT_SIZE_MULTIPLIER
            } else {
                1f
            }
    val scaledDensity =
        remember(baseDensity.density, targetFontScale) {
            Density(
                density = baseDensity.density,
                fontScale = targetFontScale,
            )
        }

    MaterialTheme(
        colorScheme = colors,
        typography = ScribeTypography,
    ) {
        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            content()
        }
    }
}
