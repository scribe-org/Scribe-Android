// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.common.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.theme.ScribeTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Surface



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
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    downloadState: DownloadState = DownloadState.Ready,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    val backgroundColor =
        when (downloadState) {
            DownloadState.Ready,
            DownloadState.Downloading,
            -> {
                if (isDarkTheme) {
                    colorScheme.tertiary
                } else {
                    colorScheme.primary
                }
            }

            DownloadState.Completed -> {
                if (isDarkTheme) {
                    colorScheme.surfaceVariant.copy(alpha = 0.25f)
                } else {
                    colorScheme.surfaceVariant
                }
            }
        }

    val textColor =
        when (downloadState) {
            DownloadState.Ready,
            DownloadState.Downloading,
            -> if (isDarkTheme) {
                colorScheme.primary
            } else {
                Color.Black
            }

            DownloadState.Completed -> {
                if (isDarkTheme) {
                    colorScheme.surfaceVariant
                } else {
                    Color.Black
                }
            }
        }

    val iconColor =
        if (isDarkTheme) {
            textColor
        } else {
            Color.Black
        }

    val borderColor =
        if (isDarkTheme) {
            textColor
        } else {
            backgroundColor
        }

    Button(
        onClick = {
            if (downloadState == DownloadState.Ready) {
                onClick()
            }
        },
        enabled = downloadState != DownloadState.Downloading,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = textColor,
                disabledContainerColor = backgroundColor,
                disabledContentColor = textColor,
            ),
        shape = RoundedCornerShape(12.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp),
                ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text =
                    when (downloadState) {
                        DownloadState.Ready -> "Download data"
                        DownloadState.Downloading -> "Downloading..."
                        DownloadState.Completed -> "Up to date"
                    },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )

            DownloadStateIcon(
                downloadState = downloadState,
                iconColor = iconColor,
            )
        }
    }
}

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
    }
}

/**
 * Represents the state of the download button.
 */
enum class DownloadState {
    Ready,
    Downloading,
    Completed,
}
