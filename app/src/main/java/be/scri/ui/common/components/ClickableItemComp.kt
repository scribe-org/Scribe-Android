// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

/**
 *  A composable component that displays a clickable item with a title, optional description and an arrow icon.
 */
@Composable
fun ClickableItemComp(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    desc: String? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp,
                    ).clip(RoundedCornerShape(8.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    modifier =
                        Modifier
                            .padding(start = 6.dp)
                            .size(17.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Right Arrow",
                )
            }

            if (!desc.isNullOrEmpty()) {
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
