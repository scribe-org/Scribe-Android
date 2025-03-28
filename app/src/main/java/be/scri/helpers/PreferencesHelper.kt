// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * A helper to facilitate setting preferences for individual language keyboards.
 */

package be.scri.helpers

import android.app.UiModeManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.UI_MODE_SERVICE
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import be.scri.extensions.config

@Suppress("TooManyFunctions")
object PreferencesHelper {
    fun setTranslationSourceLanguage(
        context: Context,
        language: String,
        translationSource: String,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPref.edit { putString(getLanguageSpecificPreferenceKey(TRANSLATION_SOURCE, language), translationSource) }
    }

    fun getTranslationSourceLanguage(
        context: Context,
        language: String,
    ): String {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString(
            getLanguageSpecificPreferenceKey(TRANSLATION_SOURCE, language),
            "English",
        ) ?: "English"
    }

    fun getLanguageSpecificPreferenceKey(
        key: String,
        language: String,
    ): String = "${key}_$language"

    fun setPeriodOnSpaceBarDoubleTapPreference(
        context: Context,
        language: String,
        shouldEnablePeriodOnSpaceBarDoubleTap: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(
            getLanguageSpecificPreferenceKey(PERIOD_ON_DOUBLE_TAP, language),
            shouldEnablePeriodOnSpaceBarDoubleTap,
        )
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
        editor.putBoolean(
            getLanguageSpecificPreferenceKey(DISABLE_ACCENT_CHARACTER, language),
            shouldDisableAccentCharacter,
        )
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
        editor.putBoolean(getLanguageSpecificPreferenceKey(EMOJI_SUGGESTIONS, language), shouldShowEmojiSuggestions)
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
        editor.putBoolean(getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language), shouldEnablePeriodAndComma)
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
        language: String,
        shouldVibrateOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(getLanguageSpecificPreferenceKey(VIBRATE_ON_KEYPRESS, language), shouldVibrateOnKeypress)
        editor.apply()
        Toast
            .makeText(
                context,
                "$language vibrate on key press " +
                    if (shouldVibrateOnKeypress) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
        context.config.vibrateOnKeypress = shouldVibrateOnKeypress
    }

    fun setShowPopupOnKeypress(
        context: Context,
        language: String,
        shouldShowPopupOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(getLanguageSpecificPreferenceKey(SHOW_POPUP_ON_KEYPRESS, language), shouldShowPopupOnKeypress)
        editor.apply()
        Toast
            .makeText(
                context,
                "$language PopUp on Keypress " +
                    if (shouldShowPopupOnKeypress) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
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
        if (!sharedPref.contains("dark_mode")) {
            setLightDarkModePreference(context, isUserDarkMode)
        }
        return if (isUserDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
    }

    fun getIsAccentCharacterDisabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isAccentCharacterDisabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(DISABLE_ACCENT_CHARACTER, language), false)
        return isAccentCharacterDisabled
    }

    fun getIsPreviewEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isPreviewEnabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(SHOW_POPUP_ON_KEYPRESS, language), true)
        return isPreviewEnabled
    }

    fun getIsVibrateEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isPreviewEnabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(VIBRATE_ON_KEYPRESS, language), true)
        return isPreviewEnabled
    }

    fun getEnablePeriodAndCommaABC(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isDisabledPeriodAndCommaABC =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language), false)
        return isDisabledPeriodAndCommaABC
    }

    fun getIsDarkModeOrNot(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        return isUserDarkMode
    }

    fun getIsEmojiSuggestionsEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean(getLanguageSpecificPreferenceKey(EMOJI_SUGGESTIONS, language), true)
        return isEnabled
    }
}
