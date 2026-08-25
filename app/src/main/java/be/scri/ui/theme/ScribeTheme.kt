// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import be.scri.helpers.PreferencesHelper.SCRIBE_PREFS

private const val DARK_MODE_PREF = "dark_mode"

@Composable
fun isKeyboardDarkMode(): Boolean {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val sharedPref = remember(context) { context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE) }
    var isDarkMode by remember(sharedPref, isSystemDark) { mutableStateOf(sharedPref.getBoolean(DARK_MODE_PREF, isSystemDark)) }

    DisposableEffect(sharedPref, isSystemDark) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == DARK_MODE_PREF) {
                    isDarkMode = prefs.getBoolean(DARK_MODE_PREF, isSystemDark)
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPref.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return isDarkMode
}

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

/**
 * Light and dark mode themes for the application.
 */
@Composable
fun ScribeTheme(
    useDarkTheme: Boolean,
    isIncreaseTextSize: Boolean = false,
    @Suppress("ktlint:standard:annotation")
    content: @Composable() () -> Unit,
) {
    val colors =
        if (!useDarkTheme) {
            LightColors
        } else {
            DarkColors
        }

    val typography = if (isIncreaseTextSize) createTypography(1.25f) else ScribeTypography

    MaterialTheme(
        colorScheme = colors,
        content = content,
        typography = typography,
    )
}
