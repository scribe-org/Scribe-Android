// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Functions for determining and retrieving proper text and colors based on user settings.
 */

package be.scri.extensions

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import be.scri.R
import be.scri.helpers.DARK_GREY

// Handle system default theme (Material You) specially as the color is taken from the system, not hardcoded by us.
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

fun Context.isBlackAndWhiteTheme() =
    baseConfig.textColor == Color.WHITE &&
        baseConfig.primaryColor == Color.BLACK &&
        baseConfig.backgroundColor == Color.BLACK

fun Context.isWhiteTheme() =
    baseConfig.textColor == DARK_GREY &&
        baseConfig.primaryColor == Color.WHITE &&
        baseConfig.backgroundColor == Color.WHITE

fun Context.isUsingSystemDarkTheme() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0
