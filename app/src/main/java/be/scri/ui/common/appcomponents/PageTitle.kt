// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.appcomponents

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import be.scri.ui.theme.ScribeTypography

/**
 * A composable function that displays a styled page title.
 *
 * @param pageTitle The title text to display
 * @param modifier An optional [Modifier] used to customize the layout and styling.
 */
@Composable
fun PageTitle(
    pageTitle: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = pageTitle,
        fontSize = ScribeTypography.headlineLarge.fontSize,
        style =
            TextStyle.Default.copy(
                fontStyle = ScribeTypography.headlineMedium.fontStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            ),
        modifier = modifier,
    )
}
