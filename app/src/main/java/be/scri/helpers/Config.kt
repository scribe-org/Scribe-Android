// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context

/**
 * A class that extends [BaseConfig] to manage additional app settings.
 * <p>
 * This class provides access to user preferences related to input behavior and theme settings.
 * </p>
 *
 * @param context The application context used to access resources and shared preferences.
 */
class Config(
    context: Context,
) : BaseConfig(context) {
    /**
     * Function for creating new instances of the class.
     */
    companion object {
        /**
         * Creates a new instance of `Config`.
         *
         * @param context The application context.
         * @return A new instance of `Config`.
         */
        fun newInstance(context: Context) = Config(context)
    }

    var vibrateOnKeypress: Boolean
        get() = prefs.getBoolean(VIBRATE_ON_KEYPRESS, true)
        set(vibrateOnKeypress) = prefs.edit().putBoolean(VIBRATE_ON_KEYPRESS, vibrateOnKeypress).apply()

    var showPopupOnKeypress: Boolean
        get() = prefs.getBoolean(SHOW_POPUP_ON_KEYPRESS, true)
        set(showPopupOnKeypress) = prefs.edit().putBoolean(SHOW_POPUP_ON_KEYPRESS, showPopupOnKeypress).apply()
}
