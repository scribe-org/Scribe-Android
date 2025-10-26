// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import be.scri.R
import be.scri.helpers.PreferencesHelper

/** This file provides utility functions for settings page. */
object SettingsUtil {
    /**
     * Checks whether the custom keyboard is already installed and enabled.
     *
     * @param context The context to access system services.
     * @return True if the keyboard is installed, false otherwise.
     */
    fun checkKeyboardInstallation(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        return imm.enabledInputMethodList.any { it.packageName == "be.scri.debug" }
    }

    /**
     * Sets the app theme to light or dark mode and saves the user's preference.
     *
     * @param isDarkMode True to enable dark mode, false for light mode.
     * @param context The context used to save preferences.
     */
    fun setLightDarkMode(
        isDarkMode: Boolean,
        context: Context,
    ) {
        PreferencesHelper.setLightDarkModePreference(context, isDarkMode)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            },
        )
    }

    /**
     * Opens the system settings screen to allow the user to change the app's language.
     *
     * @param context The context used to start the settings activity.
     */
    fun selectLanguage(context: Context) {
        val packageName = context.packageName
        val intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(ACTION_APP_LOCALE_SETTINGS)
            } else {
                Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    putExtra(
                        "android.intent.extra.SHOW_FRAGMENT",
                        "com.android.settings.localepicker.LocaleListEditor",
                    )
                }
            }
        intent.data = Uri.fromParts("package", packageName, null)
        context.startActivity(intent)
    }

    /**
     * Opens the keyboard/input method settings screen.
     *
     * @param context The context used to start the settings activity.
     */
    fun navigateToKeyboardSettings(context: Context) {
        Intent(ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    /**
     * Retrieves the list of available keyboard languages based on enabled input methods.
     *
     * @param context The context to access input methods.
     * @return A list of language names.
     */
    fun getKeyboardLanguages(context: Context): List<String> {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.mapNotNull { inputMethod ->
            when (inputMethod.serviceName) {
                "be.scri.services.EnglishKeyboardIME" -> "English"
                "be.scri.services.FrenchKeyboardIME" -> "French"
                "be.scri.services.GermanKeyboardIME" -> "German"
                "be.scri.services.IndonesianKeyboardIME" -> "Indonesian"
                "be.scri.services.ItalianKeyboardIME" -> "Italian"
                "be.scri.services.PortugueseKeyboardIME" -> "Portuguese"
                "be.scri.services.RussianKeyboardIME" -> "Russian"
                "be.scri.services.SpanishKeyboardIME" -> "Spanish"
                "be.scri.services.SwedishKeyboardIME" -> "Swedish"
                else -> null
            }
        }
    }

    /**
     * Maps a language name to its corresponding localized string resource ID.
     *
     * @param language The name of the language.
     * @return The string resource ID for the localized name.
     */
    fun getLocalizedLanguageName(language: String): Int {
        return when (language) {
            "English" -> R.string.i18n_app__global_english
            "French" -> R.string.i18n_app__global_french
            "German" -> R.string.i18n_app__global_german
            "Indonesian" -> R.string.i18n_app__global_indonesian
            "Italian" -> R.string.i18n_app__global_italian
            "Portuguese" -> R.string.i18n_app__global_portuguese
            "Russian" -> R.string.i18n_app__global_russian
            "Spanish" -> R.string.i18n_app__global_spanish
            "Swedish" -> R.string.i18n_app__global_swedish
            else -> return R.string.language
        }
    }
}
