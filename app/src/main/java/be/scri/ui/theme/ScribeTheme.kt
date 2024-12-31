/**
 * Light and dark mode themes for the application.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
