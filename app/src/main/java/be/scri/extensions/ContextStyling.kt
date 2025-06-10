// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import be.scri.R
import be.scri.helpers.DARK_GREY

// Handle system default theme (Material You) specially as the color is taken from the system, not hardcoded by us.

/**
 * Retrieves the appropriate text color based on the user's theme settings.
 *
 * @receiver Context used to access resources and configuration
 * @return Int representing the text color
 */
fun Context.getProperTextColor() =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_neutral_text_color, theme)
    } else {
        baseConfig.textColor
    }

/**
 * Retrieves the appropriate key color based on the user's theme settings.
 *
 * @receiver Context used to access resources and configuration
 * @return Int representing the key color
 */

fun Context.getProperKeyColor() =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_neutral_text_color, theme)
    } else {
        baseConfig.keyColor
    }

/**
 * Retrieves the appropriate background color based on the user's theme settings.
 *
 * @receiver Context used to access resources and configuration
 * @return Int representing the background color
 */
fun Context.getProperBackgroundColor() =
    if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_background_color, theme)
    } else {
        baseConfig.backgroundColor
    }

/**
 * Retrieves the appropriate primary color based on the user's theme settings.
 *
 * @receiver Context used to access resources and configuration
 * @return Int representing the primary color
 */
fun Context.getProperPrimaryColor() =
    when {
        baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_primary_color, theme)
        isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
        else -> baseConfig.primaryColor
    }

/**
 * Determines if the current theme is black and white.
 *
 * @receiver Context used to access configuration
 * @return Boolean indicating if the theme is black and white
 */
fun Context.isBlackAndWhiteTheme() =
    baseConfig.textColor == Color.WHITE &&
        baseConfig.primaryColor == Color.BLACK &&
        baseConfig.backgroundColor == Color.BLACK

/**
 * Determines if the current theme is white.
 *
 * @receiver Context used to access configuration
 * @return Boolean indicating if the theme is white
 */

fun Context.isWhiteTheme() =
    baseConfig.textColor == DARK_GREY &&
        baseConfig.primaryColor == Color.WHITE &&
        baseConfig.backgroundColor == Color.WHITE

/**
 * Determines if the system is using a dark theme.
 *
 * @receiver Context used to access configuration
 * @return Boolean indicating if the system is using a dark theme
 */
fun Context.isUsingSystemDarkTheme() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0
