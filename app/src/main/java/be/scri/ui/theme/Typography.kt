// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Text styles for the application.
 */

package be.scri.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun TextUnit.scaled(scale: Float): TextUnit = if (isSp) (value * scale).sp else this

fun createTypography(scale: Float): Typography {
    return Typography(
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp.scaled(scale),
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp.scaled(scale),
            ),
    )
}

val ScribeTypography = createTypography(1f)
