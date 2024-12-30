/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/**
 * Class of methods to manage Scribe's behaviors.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.scri.extensions

import android.graphics.Color
import be.scri.helpers.DARK_GREY
import java.util.Random

private const val RED_COEFFICIENT = 299
private const val GREEN_COEFFICIENT = 587
private const val BLUE_COEFFICIENT = 114
private const val COEFFICIENT_SUM = 1000
private const val Y_THRESHOLD = 149

fun Int.getContrastColor(): Int {
    val y =
        (
            RED_COEFFICIENT * Color.red(this) +
                GREEN_COEFFICIENT * Color.green(this) +
                BLUE_COEFFICIENT * Color.blue(this)
        ) / COEFFICIENT_SUM
    return if (y >= Y_THRESHOLD && this != Color.BLACK) DARK_GREY else Color.WHITE
}

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start

// Taken from https://stackoverflow.com/a/40964456/1967672.
private const val HSV_COMPONENT_COUNT = 3
private const val DEFAULT_DARKEN_FACTOR = 8
private const val FACTOR_DIVIDER = 100

fun Int.darkenColor(factor: Int = DEFAULT_DARKEN_FACTOR): Int {
    if (this == Color.WHITE || this == Color.BLACK) {
        return this
    }

    val darkFactor = factor
    var hsv = FloatArray(HSV_COMPONENT_COUNT)
    Color.colorToHSV(this, hsv)
    val hsl = hsv2hsl(hsv)
    hsl[2] -= darkFactor / FACTOR_DIVIDER.toFloat()
    if (hsl[2] < 0) {
        hsl[2] = 0f
    }
    hsv = hsl2hsv(hsl)
    return Color.HSVToColor(hsv)
}

fun Int.lightenColor(factor: Int = 8): Int {
    if (this == Color.WHITE || this == Color.BLACK) {
        return this
    }

    val lightFactor = factor
    var hsv = FloatArray(HSV_COMPONENT_COUNT)
    Color.colorToHSV(this, hsv)
    val hsl = hsv2hsl(hsv)
    hsl[2] += lightFactor / FACTOR_DIVIDER.toFloat()
    if (hsl[2] < 0) {
        hsl[2] = 0f
    }
    hsv = hsl2hsv(hsl)
    return Color.HSVToColor(hsv)
}

private const val LIGHTNESS_THRESHOLD = 0.5f

private fun hsl2hsv(hsl: FloatArray): FloatArray {
    val hue = hsl[0]
    var sat = hsl[1]
    val light = hsl[2]
    sat *= if (light < LIGHTNESS_THRESHOLD) light else 1 - light
    return floatArrayOf(hue, 2f * sat / (light + sat), light + sat)
}

private fun hsv2hsl(hsv: FloatArray): FloatArray {
    val hue = hsv[0]
    val sat = hsv[1]
    val value = hsv[2]

    val newHue = (2f - sat) * value
    var newSat = sat * value / if (newHue < 1f) newHue else 2f - newHue
    if (newSat > 1f) {
        newSat = 1f
    }

    return floatArrayOf(hue, newSat, newHue / 2f)
}
