// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.content.Context
import be.scri.helpers.BaseConfig
import be.scri.helpers.PREFS_KEY

/**
 * Retrieves the shared preferences using the predefined PREFS_KEY.
 *
 * @receiver Context used to access shared preferences
 * @return SharedPreferences instance
 */
fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

/**
 * Provides an instance of BaseConfig associated with the context.
 *
 * @receiver Context used to create BaseConfig instance
 * @return BaseConfig instance
 */
val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)
