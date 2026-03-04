// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.screens.download.CheckUpdateState

/**
 * A composable component that displays a clickable item with a title and a circular clickable icon on the right.
 */
@Composable
fun CircleClickableItemComp(
    title: String,
    onStartCheck: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    checkState: CheckUpdateState = CheckUpdateState.Idle,
    isDarkTheme: Boolean = false,
) {
    Box(
        modifier =
            modifier.clickable {
                when (checkState) {
                    CheckUpdateState.Idle, CheckUpdateState.Done -> onStartCheck()
                    CheckUpdateState.Checking -> onCancel()
                }
            },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 5.dp,
                    ).clip(RoundedCornerShape(8.dp)),
        ) {
            Row(
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(36.dp),
                ) {
                    when (checkState) {
                        CheckUpdateState.Idle -> {
                            Box(
                                modifier =
                                    Modifier
                                        .size(28.dp)
                                        .border(
                                            width = 2.dp,
                                            color = if (isDarkTheme)colorResource(R.color.light_special_key_color) else colorResource(R.color.md_grey_600),
                                            shape = CircleShape,
                                        ),
                            )
                        }

                        CheckUpdateState.Checking -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                strokeCap = StrokeCap.Round,
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = if (isDarkTheme) colorResource(R.color.light_special_key_color) else colorResource(R.color.md_grey_600),
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        CheckUpdateState.Done -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier =
                                    Modifier
                                        .size(28.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                        ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Up to date",
                                    tint = if (isDarkTheme) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
