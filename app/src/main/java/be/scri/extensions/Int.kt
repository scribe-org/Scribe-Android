// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.graphics.Color
import be.scri.helpers.DARK_GREY
import java.util.Random

private const val RED_COEFFICIENT = 299
private const val GREEN_COEFFICIENT = 587
private const val BLUE_COEFFICIENT = 114
private const val COEFFICIENT_SUM = 1000
private const val Y_THRESHOLD = 149

/**
 * Returns a contrasting color (either [Color.WHITE] or [DARK_GREY]) for the current color.
 * This is useful for determining readable foreground text colors on backgrounds.
 *
 * @receiver Int The base color in ARGB or RGB format.
 * @return Int A contrasting color for readability.
 */
fun Int.getContrastColor(): Int {
    val y =
        (
            RED_COEFFICIENT * Color.red(this) +
                GREEN_COEFFICIENT * Color.green(this) +
                BLUE_COEFFICIENT * Color.blue(this)
        ) / COEFFICIENT_SUM
    return if (y >= Y_THRESHOLD && this != Color.BLACK) DARK_GREY else Color.WHITE
}

/**
 * Adjusts the alpha (transparency) of a color by a given factor.
 *
 * @receiver Int The color to adjust.
 * @param factor Float The factor to multiply the alpha by (0f = fully transparent, 1f = original alpha).
 * @return Int The color with modified alpha.
 */
fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

/**
 * Generates a random integer within the specified closed range.
 *
 * @receiver ClosedRange<Int> The range to generate the random number in.
 * @return Int A random integer within the specified range.
 */
fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start

// Taken from https://stackoverflow.com/a/40964456/1967672.
private const val HSV_COMPONENT_COUNT = 3
private const val DEFAULT_DARKEN_FACTOR = 8
private const val FACTOR_DIVIDER = 100

/**
 * Darkens the color by a given factor using HSL conversion.
 *
 * @receiver Int The original color.
 * @param factor Int The factor to darken the color by (default is 8).
 * @return Int The darkened color.
 */

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

/**
 * Lightens the color by a given factor using HSL conversion.
 *
 * @receiver Int The original color.
 * @param factor Int The factor to lighten the color by (default is 8).
 * @return Int The lightened color.
 */

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

/**
 * Converts a color from HSL to HSV.
 *
 * @param hsl FloatArray The color in HSL format.
 * @return FloatArray The converted color in HSV format.
 */
private fun hsl2hsv(hsl: FloatArray): FloatArray {
    val hue = hsl[0]
    var sat = hsl[1]
    val light = hsl[2]
    sat *= if (light < LIGHTNESS_THRESHOLD) light else 1 - light
    return floatArrayOf(hue, 2f * sat / (light + sat), light + sat)
}

/**
 * Converts a color from HSV to HSL.
 *
 * @param hsv FloatArray The color in HSV format.
 * @return FloatArray The converted color in HSL format.
 */

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
