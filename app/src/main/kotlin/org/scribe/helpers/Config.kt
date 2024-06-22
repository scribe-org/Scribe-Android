package org.scribe.helpers

import android.content.Context
import java.util.*

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var vibrateOnKeypress: Boolean
        get() = prefs.getBoolean(VIBRATE_ON_KEYPRESS, true)
        set(vibrateOnKeypress) = prefs.edit().putBoolean(VIBRATE_ON_KEYPRESS, vibrateOnKeypress).apply()

    var showPopupOnKeypress: Boolean
        get() = prefs.getBoolean(SHOW_POPUP_ON_KEYPRESS, true)
        set(showPopupOnKeypress) = prefs.edit().putBoolean(SHOW_POPUP_ON_KEYPRESS, showPopupOnKeypress).apply()

    var darkTheme: Boolean
        get() = prefs.getBoolean(DARK_THEME, true)
        set(darkTheme) = prefs.edit().putBoolean(DARK_THEME, darkTheme).apply()

    var lastExportedClipsFolder: String
        get() = prefs.getString(LAST_EXPORTED_CLIPS_FOLDER, "")!!
        set(lastExportedClipsFolder) = prefs.edit().putString(LAST_EXPORTED_CLIPS_FOLDER, lastExportedClipsFolder).apply()

    var keyboardLanguage: Int
        get() = prefs.getInt(KEYBOARD_LANGUAGE, getDefaultLanguage())
        set(keyboardLanguage) = prefs.edit().putInt(KEYBOARD_LANGUAGE, keyboardLanguage).apply()

    var periodOnDoubleTap : Boolean
        get() = prefs.getBoolean(PERIOD_ON_DOUBLE_TAP, true)
        set(periodOnDoubleTap) = prefs.edit().putBoolean(PERIOD_ON_DOUBLE_TAP, periodOnDoubleTap).apply()

    private fun getDefaultLanguage(): Int {
        val conf = context.resources.configuration
        return if (conf.locale.toString().toLowerCase(Locale.getDefault()).startsWith("ru_")) {
            LANGUAGE_RUSSIAN
        } else {
            LANGUAGE_ENGLISH_QWERTY
        }
    }
}
