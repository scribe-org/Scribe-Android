package be.scri.extensions

import android.content.Context
import be.scri.helpers.BaseConfig
import be.scri.helpers.PREFS_KEY

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)
