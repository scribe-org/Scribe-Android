package be.scri.extensions

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import be.scri.R
import be.scri.helpers.appIconColorStrings
import be.scri.views.MyTextView

fun Context.getColorWithDefault(color: Int, defaultColor: Int) =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(color, theme)
    } else {
        defaultColor
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
            baseConfig.isUsingSystemTheme -> getColorWithDefault(R.color.you_neutral_text_color, baseConfig.textColor)
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

fun Context.checkAppIconColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
        resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList()).forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList()).forEachIndexed { index, color ->
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
