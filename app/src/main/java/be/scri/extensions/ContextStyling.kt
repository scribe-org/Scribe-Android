package be.scri.extensions

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.view.ViewGroup
import androidx.loader.content.CursorLoader
import be.scri.R
import be.scri.helpers.DARK_GREY
import be.scri.helpers.INVALID_NAVIGATION_BAR_COLOR
import be.scri.helpers.MyContentProvider
import be.scri.helpers.appIconColorStrings
import be.scri.helpers.ensureBackgroundThread
import be.scri.models.SharedTheme
import be.scri.views.MyTextView

// handle system default theme (Material You) specially as the color is taken from the system, not hardcoded by us
fun Context.getProperTextColor() =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_neutral_text_color, theme)
    } else {
        baseConfig.textColor
    }

fun Context.getProperKeyColor() =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_neutral_text_color, theme)
    } else {
        baseConfig.keyColor
    }

fun Context.getProperBackgroundColor() =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_background_color, theme)
    } else {
        baseConfig.backgroundColor
    }

fun Context.getProperPrimaryColor() =
    when {
        baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_primary_color, theme)
        isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
        else -> baseConfig.primaryColor
    }

fun Context.getProperStatusBarColor() =
    when {
        baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_primary_color, theme)
        else -> baseConfig.primaryColor
    }

fun Context.updateTextColors(viewGroup: ViewGroup) {
    val textColor =
        when {
            baseConfig.isUsingSystemTheme -> getProperTextColor()
            else -> baseConfig.textColor
        }

    val accentColor =
        when {
            isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
            else -> getProperPrimaryColor()
        }

    for (i in 0 until viewGroup.childCount) {
        when (val view = viewGroup.getChildAt(i)) {
            is MyTextView -> view.setColors(textColor, accentColor)
            is ViewGroup -> updateTextColors(view)
        }
    }
}

fun Context.isBlackAndWhiteTheme() = baseConfig.textColor == Color.WHITE && baseConfig.primaryColor == Color.BLACK && baseConfig.backgroundColor == Color.BLACK

fun Context.isWhiteTheme() = baseConfig.textColor == DARK_GREY && baseConfig.primaryColor == Color.WHITE && baseConfig.backgroundColor == Color.WHITE

fun Context.isUsingSystemDarkTheme() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

fun Context.getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
    val cursorLoader = getMyContentProviderCursorLoader()
    ensureBackgroundThread {
        callback(getSharedThemeSync(cursorLoader))
    }
}

fun Context.getSharedThemeSync(cursorLoader: CursorLoader): SharedTheme? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                val textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR)
                val backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR)
                val primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR)
                val accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR)
                val appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR)
                val navigationBarColor = cursor.getIntValueOrNull(MyContentProvider.COL_NAVIGATION_BAR_COLOR) ?: INVALID_NAVIGATION_BAR_COLOR
                val lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS)
                return SharedTheme(textColor, backgroundColor, primaryColor, appIconColor, navigationBarColor, lastUpdatedTS, accentColor)
            } catch (e: Exception) {
            }
        }
    }
    return null
}

fun Context.checkAppIconColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
        getAppIconColors().forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        getAppIconColors().forEachIndexed { index, color ->
            if (baseConfig.appIconColor == color) {
                toggleAppIconColor(appId, index, color, true)
            }
        }
    }
}

fun Context.toggleAppIconColor(
    appId: String,
    colorIndex: Int,
    color: Int,
    enable: Boolean,
) {
    val className = "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(ComponentName(appId, className), state, PackageManager.DONT_KILL_APP)
        if (enable) {
            baseConfig.lastIconColor = color
        }
    } catch (e: Exception) {
    }
}

fun Context.getAppIconColors() = resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())
