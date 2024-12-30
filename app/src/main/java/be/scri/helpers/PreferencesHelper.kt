/**
 * Utility class to manage app preferences, including settings for space bar double-tap, accent characters, emoji suggestions, keypress vibration, popups, and light/dark mode.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.scri.helpers

import android.app.UiModeManager
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.UI_MODE_SERVICE
import androidx.appcompat.app.AppCompatDelegate
import be.scri.extensions.config

object PreferencesHelper {
    fun setPeriodOnSpaceBarDoubleTapPreference(
        context: Context,
        language: String,
        shouldEnablePeriodOnSpaceBarDoubleTap: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", shouldEnablePeriodOnSpaceBarDoubleTap)
        editor.apply()
        Toast
            .makeText(
                context,
                "$language Period on Double Tap of Space Bar " +
                    if (shouldEnablePeriodOnSpaceBarDoubleTap) "on" else "off",
                Toast.LENGTH_SHORT,
            ).show()
    }

    fun setAccentCharacterPreference(
        context: Context,
        language: String,
        shouldDisableAccentCharacter: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("disable_accent_character_$language", shouldDisableAccentCharacter)
        editor.apply()
        Toast
            .makeText(
                context,
                "$language Accent Characters " +
                    if (shouldDisableAccentCharacter) "off" else "on",
                Toast.LENGTH_SHORT,
            ).show()
    }

    fun setEmojiAutoSuggestionsPreference(
        context: Context,
        language: String,
        shouldShowEmojiSuggestions: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("emoji_suggestions_$language", shouldShowEmojiSuggestions)
        editor.apply()
        Toast
            .makeText(
                context,
                "$language Emoji Autosuggestions " +
                    if (shouldShowEmojiSuggestions) "on" else "off",
                Toast.LENGTH_SHORT,
            ).show()
    }

    fun setCommaAndPeriodPreference(
        context: Context,
        language: String,
        shouldEnablePeriodAndComma: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_and_comma_$language", shouldEnablePeriodAndComma)
        editor.apply()
        Toast
            .makeText(
                context,
                "$language period and comma on ABC " +
                    if (shouldEnablePeriodAndComma) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    fun setVibrateOnKeypress(
        context: Context,
        shouldVibrateOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("vibrate_on_keypress", shouldVibrateOnKeypress)
        editor.apply()
        context.config.vibrateOnKeypress = shouldVibrateOnKeypress
    }

    fun setShowPopupOnKeypress(
        context: Context,
        shouldShowPopupOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("show_popup_on_keypress", shouldShowPopupOnKeypress)
        editor.apply()
        context.config.showPopupOnKeypress = shouldShowPopupOnKeypress
    }

    fun setLightDarkModePreference(
        context: Context,
        darkMode: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("dark_mode", darkMode)
        editor.apply()
    }

    fun getUserDarkModePreference(context: Context): Int {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val uiModeManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager
        val isSystemDarkTheme = uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkTheme)
        return if (isUserDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
    }
}
