// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import be.scri.R
import be.scri.extensions.getSharedPrefs
import kotlin.apply

/**
 * A configuration helper class for managing app settings.
 * <p>
 * This class provides access to various UI-related preferences such as colors, language settings,
 * and theme preferences. Preferences are stored using `SharedPreferences`.
 * </p>
 *
 * @param context The application context used to retrieve resources and shared preferences.
 */
open class BaseConfig(
    val context: Context,
) {
    protected val prefs = context.getSharedPrefs()

    /**
     * Function for creating new instances of the class.
     */
    companion object {
        /**
         * Creates a new instance of `BaseConfig`.
         *
         * @param context The application context.
         * @return A new instance of `BaseConfig`.
         */
        fun newInstance(context: Context) = BaseConfig(context)
    }

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, context.resources.getColor(R.color.default_text_color))
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var keyColor: Int
        get() = prefs.getInt(KEY_COLOR, context.resources.getColor(R.color.default_key_color))
        set(keyColor) = prefs.edit().putInt(KEY_COLOR, keyColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, context.resources.getColor(R.color.default_background_color))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, context.resources.getColor(R.color.color_primary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    var accentColor: Int
        get() = prefs.getInt(ACCENT_COLOR, context.resources.getColor(R.color.color_primary))
        set(accentColor) = prefs.edit().putInt(ACCENT_COLOR, accentColor).apply()

    var isUsingSystemTheme: Boolean
        get() = prefs.getBoolean(IS_USING_SYSTEM_THEME, false)
        set(isUsingSystemTheme) = prefs.edit().putBoolean(IS_USING_SYSTEM_THEME, isUsingSystemTheme).apply()

    var recentlyUsedEmojis: List<String>
        get() = prefs.getString(RECENTLY_USED_EMOJIS, "\uD83D\uDC30")!!.split("|").filter { it.isNotEmpty() }
        set(recentlyUsedEmojis) = prefs.edit().putString(
            RECENTLY_USED_EMOJIS, recentlyUsedEmojis.joinToString("|")
        ).apply()

    fun addRecentEmoji(emoji: String) {
        val recentEmojis = recentlyUsedEmojis.toMutableList()
        recentEmojis.remove(emoji)
        recentEmojis.add(0, emoji)
        recentlyUsedEmojis = recentEmojis.take(RECENT_EMOJIS_LIMIT)
    }
}
