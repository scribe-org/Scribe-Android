package be.scri.helpers

import android.os.Build
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast

val DARK_GREY = 0xFF333333.toInt()

const val MEDIUM_ALPHA = 0.5f

// shared preferences
const val PREFS_KEY = "Prefs"
const val TEXT_COLOR = "text_color"
const val KEY_COLOR = "key_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"
const val ACCENT_COLOR = "accent_color"
const val USE_ENGLISH = "use_english"
const val WAS_USE_ENGLISH_TOGGLED = "was_use_english_toggled"
const val IS_USING_SYSTEM_THEME = "is_using_system_theme"

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
