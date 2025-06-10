// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.content.Context
import android.graphics.Color
import be.scri.R
import be.scri.helpers.Config

/**
 * Lazily retrieves the [Config] object scoped to the application context.
 */
val Context.config: Config get() = Config.newInstance(applicationContext)

/**
 * Determines the stroke color based on the current theme and user preferences.
 *
 * @return A color integer suitable for use in outlining UI elements.
 */
fun Context.getStrokeColor(): Int =
    if (config.isUsingSystemTheme) {
        if (isUsingSystemDarkTheme()) {
            resources.getColor(R.color.md_grey_800, theme)
        } else {
            resources.getColor(R.color.md_grey_400, theme)
        }
    } else {
        val lighterColor = getProperBackgroundColor().lightenColor()
        if (lighterColor == Color.WHITE || lighterColor == Color.BLACK) {
            resources.getColor(R.color.divider_grey, theme)
        } else {
            lighterColor
        }
    }
