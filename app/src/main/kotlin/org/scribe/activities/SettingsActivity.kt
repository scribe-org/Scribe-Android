package org.scribe.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_settings.*
import org.scribe.R
import org.scribe.dialogs.RadioGroupDialog
import org.scribe.extensions.config
import org.scribe.extensions.updateTextColors
import org.scribe.helpers.*
import org.scribe.models.RadioItem

class SettingsActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()
        setupDarkTheme()
        setupPeriodOnSpaceBarDoubleTap()
        setupVibrateOnKeypress()
        setupShowPopupOnKeypress()
        setupKeyboardLanguage()
        updateTextColors(settings_scrollview)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupVibrateOnKeypress() {
        settings_vibrate_on_keypress.isChecked = config.vibrateOnKeypress
        settings_vibrate_on_keypress_holder.setOnClickListener {
            settings_vibrate_on_keypress.toggle()
            config.vibrateOnKeypress = settings_vibrate_on_keypress.isChecked
        }
    }

    private fun setupPeriodOnSpaceBarDoubleTap() {
        settings_period_on_space_bar.isChecked = config.periodOnDoubleTap
        settings_period_on_space_bar_holder.setOnClickListener {
            settings_period_on_space_bar.toggle()
            config.periodOnDoubleTap = settings_period_on_space_bar.isChecked
        }
    }

    private fun setupDarkTheme() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)

        settings_dark_mode.isChecked = isUserDarkMode

        settings_dark_mode_holder.setOnClickListener {
            settings_dark_mode.toggle()
            editor.putBoolean("dark_mode", settings_dark_mode.isChecked)
            editor.apply()

            if (settings_dark_mode.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            recreate()
        }
    }

    private fun setupShowPopupOnKeypress() {
        settings_show_popup_on_keypress.isChecked = config.showPopupOnKeypress
        settings_show_popup_on_keypress_holder.setOnClickListener {
            settings_show_popup_on_keypress.toggle()
            config.showPopupOnKeypress = settings_show_popup_on_keypress.isChecked
        }
    }

    private fun setupKeyboardLanguage() {
        settings_keyboard_language.text = getKeyboardLanguageText(config.keyboardLanguage)
        settings_keyboard_language_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(
                    LANGUAGE_ENGLISH_QWERTY,
                    getKeyboardLanguageText(LANGUAGE_ENGLISH_QWERTY)
                ),
                RadioItem(LANGUAGE_FRENCH, getKeyboardLanguageText(LANGUAGE_FRENCH)),
                RadioItem(LANGUAGE_GERMAN, getKeyboardLanguageText(LANGUAGE_GERMAN)),
                RadioItem(LANGUAGE_ITALIAN, getKeyboardLanguageText(LANGUAGE_ITALIAN)),
                RadioItem(LANGUAGE_PORTUGUESE, getKeyboardLanguageText(LANGUAGE_PORTUGUESE)),
                RadioItem(LANGUAGE_RUSSIAN, getKeyboardLanguageText(LANGUAGE_RUSSIAN)),
                RadioItem(LANGUAGE_SPANISH, getKeyboardLanguageText(LANGUAGE_SPANISH)),
                RadioItem(LANGUAGE_SWEDISH, getKeyboardLanguageText(LANGUAGE_SWEDISH))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.keyboardLanguage) {
                config.keyboardLanguage = it as Int
                settings_keyboard_language.text = getKeyboardLanguageText(config.keyboardLanguage)
            }
        }
    }

    private fun getKeyboardLanguageText(language: Int): String {
        return when (language) {
            LANGUAGE_FRENCH -> getString(R.string.translation_french)
            LANGUAGE_GERMAN -> getString(R.string.translation_german)
            LANGUAGE_ITALIAN -> getString(R.string.translation_italian)
            LANGUAGE_PORTUGUESE -> getString(R.string.translation_portuguese)
            LANGUAGE_RUSSIAN -> getString(R.string.translation_russian)
            LANGUAGE_SPANISH -> getString(R.string.translation_spanish)
            LANGUAGE_SWEDISH -> getString(R.string.translation_swedish)
            else -> getString(R.string.translation_english)
        }
    }
}
