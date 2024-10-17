package be.scri.helpers

import android.content.Context
import android.widget.Toast
import be.scri.extensions.config

class PreferencesHelper {
    companion object {
        fun getIsAccentCharacter(context: Context, lang: String): Boolean {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val isAccentCharacter = sharedPref.getBoolean("disable_accent_character_$lang", false)
            return isAccentCharacter
        }

        fun setPeriodOnSpaceBarDoubleTapPreference(context: Context, language: String, shouldEnablePeriodOnSpaceBarDoubleTap: Boolean) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("period_on_double_tap_$language", shouldEnablePeriodOnSpaceBarDoubleTap)
            editor.apply()
            Toast.makeText(context, "$language Period on Double Tap of Space Bar ${if (shouldEnablePeriodOnSpaceBarDoubleTap) "on" else "off"}", Toast.LENGTH_SHORT).show()
        }

        fun setAccentCharacterPreference(context: Context, language: String, shouldDisableAccentCharacter: Boolean) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("disable_accent_character_$language", shouldDisableAccentCharacter)
            editor.apply()
            Toast.makeText(context, "$language Accent Characters ${if (shouldDisableAccentCharacter) "off" else "on"}", Toast.LENGTH_SHORT).show()
        }

        fun setEmojiAutoSuggestionsPreference(context: Context, language: String, shouldShowEmojiSuggestions: Boolean) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("emoji_suggestions_$language", shouldShowEmojiSuggestions)
            editor.apply()
            Toast.makeText(context, "$language Emoji Autosuggestions ${if (shouldShowEmojiSuggestions) "on" else "off"}", Toast.LENGTH_SHORT).show()
        }

        fun setVibrateOnKeypress(context: Context, shouldVibrateOnKeypress: Boolean) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("vibrate_on_keypress", shouldVibrateOnKeypress)
            editor.apply()
            context.config.vibrateOnKeypress = shouldVibrateOnKeypress
        }

        fun setShowPopupOnKeypress(context: Context, shouldShowPopupOnKeypress: Boolean) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("show_popup_on_keypress", shouldShowPopupOnKeypress)
            editor.apply()
            context.config.showPopupOnKeypress = shouldShowPopupOnKeypress
        }

        fun setLightDarkModePreference(context: Context, darkMode: Boolean) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("dark_mode", darkMode)
            editor.apply()
        }
    }
}
