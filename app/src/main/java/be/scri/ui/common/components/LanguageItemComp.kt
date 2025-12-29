// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.ui.screens.download.DownloadState

/**
 * Simple language item with a title on the left and a download button on the right.
 */
@Composable
fun LanguageItemComp(
    title: String,
    onClick: () -> Unit,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleFontWeight: FontWeight = FontWeight.Normal,
    titleFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    isDarkTheme: Boolean = false,
    buttonState: DownloadState? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = titleFontSize,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = titleFontWeight,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(100.dp))
        DownloadDataOptionComp(
            onClick = onButtonClick,
            isDarkTheme = isDarkTheme,
            downloadState = buttonState ?: DownloadState.Ready,
            modifier = Modifier.widthIn(min = 50.dp),
        )
    }
}
