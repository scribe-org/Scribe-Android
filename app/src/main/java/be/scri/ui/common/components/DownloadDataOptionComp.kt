// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.screens.download.DownloadState

/**
 * A button component that reflects the state of a data download.
 *
 * @param isDarkTheme Whether the dark theme is active.
 * @param modifier Modifier for layout and styling.
 * @param downloadState The current state of the download.
 * @param onClick Callback triggered when the button is clicked.
 */
@Composable
fun DownloadDataOptionComp(
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    downloadState: DownloadState = DownloadState.Ready,
) {
    val colorScheme = MaterialTheme.colorScheme

    val backgroundColor = getBackgroundColor(downloadState, isDarkTheme, colorScheme)
    val textColor = getTextColor(downloadState, isDarkTheme, colorScheme)
    val iconColor = getIconColor(isDarkTheme, textColor)

    Button(
        onClick = onClick,
        enabled = true,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = textColor,
                disabledContainerColor = backgroundColor,
                disabledContentColor = textColor,
            ),
        shape = RoundedCornerShape(8.dp),
        modifier =
            modifier
                .height(40.dp)
                .width(150.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        DownloadButtonContent(downloadState, iconColor)
    }
}

@Composable
private fun DownloadButtonContent(
    downloadState: DownloadState,
    iconColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.wrapContentWidth(),
    ) {
        Text(
            text =
                when (downloadState) {
                    DownloadState.Ready -> "Download"
                    DownloadState.Downloading -> "Downloading"
                    DownloadState.Completed -> "Up to Date"
                    DownloadState.Update -> "Update"
                },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )

        DownloadStateIcon(downloadState, iconColor)
    }
}

private fun getBackgroundColor(
    downloadState: DownloadState,
    isDarkTheme: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
): Color =
    when (downloadState) {
        DownloadState.Ready, DownloadState.Downloading, DownloadState.Update ->
            if (isDarkTheme) colorScheme.tertiary else colorScheme.primary

        DownloadState.Completed ->
            if (isDarkTheme) colorScheme.surfaceVariant.copy(alpha = 0.25f) else colorScheme.surfaceVariant
    }

private fun getTextColor(
    downloadState: DownloadState,
    isDarkTheme: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
): Color =
    when (downloadState) {
        DownloadState.Ready, DownloadState.Downloading, DownloadState.Update ->
            if (isDarkTheme) colorScheme.primary else Color.Black

        DownloadState.Completed ->
            if (isDarkTheme) colorScheme.surfaceVariant else Color.Black
    }

private fun getIconColor(
    isDarkTheme: Boolean,
    textColor: Color,
): Color = if (isDarkTheme) textColor else Color.Black

/**
 * Icon to show for the current download state.
 *
 * @param downloadState Current state of the download.
 * @param iconColor Tint color of the icon or progress.
 */
@Composable
private fun DownloadStateIcon(
    downloadState: DownloadState,
    iconColor: Color,
) {
    when (downloadState) {
        DownloadState.Ready -> {
            Icon(
                painter = painterResource(id = R.drawable.clouddownload),
                contentDescription = "Download",
                modifier = Modifier.size(24.dp),
                tint = iconColor,
            )
        }

        DownloadState.Downloading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = iconColor,
                strokeWidth = 2.dp,
            )
        }

        DownloadState.Completed -> {
            Icon(
                painter = painterResource(id = R.drawable.check),
                contentDescription = "Check",
                modifier = Modifier.size(24.dp),
                tint = iconColor,
            )
        }

        DownloadState.Update -> {
            Icon(
                painter = painterResource(id = R.drawable.clouddownload),
                contentDescription = "Update",
                modifier = Modifier.size(24.dp),
                tint = iconColor,
            )
        }
    }
}
