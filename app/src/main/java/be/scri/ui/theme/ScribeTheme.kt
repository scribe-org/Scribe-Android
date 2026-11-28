// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Light and dark mode themes for the application.
 */
@Composable
fun ScribeTheme(
    useDarkTheme: Boolean,
    isIncreaseTextSize: Boolean = false,
    @Suppress("ktlint:standard:annotation")
    content: @Composable() () -> Unit,
) {
    val context = LocalContext.current
    val colors = remember(context, useDarkTheme) { context.scribeColorScheme(useDarkTheme) }

    val typography = if (isIncreaseTextSize) createTypography(1.25f) else ScribeTypography

    MaterialTheme(
        colorScheme = colors,
        content = content,
        typography = typography,
    )
}
