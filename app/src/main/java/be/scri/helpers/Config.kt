// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * A class that extends BaseConfig to manage additional app settings.
 */

package be.scri.helpers

import android.content.Context

class Config(
    context: Context,
) : BaseConfig(context) {
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

    var periodOnDoubleTap: Boolean
        get() = prefs.getBoolean(PERIOD_ON_DOUBLE_TAP, true)
        set(periodOnDoubleTap) = prefs.edit().putBoolean(PERIOD_ON_DOUBLE_TAP, periodOnDoubleTap).apply()
}
