// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Functions for retrieving configuration settings and calculating color based on theme and background preferences.
 */

package be.scri.extensions

import android.content.Context
import android.graphics.Color
import be.scri.R
import be.scri.helpers.Config

val Context.config: Config get() = Config.newInstance(applicationContext)

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
