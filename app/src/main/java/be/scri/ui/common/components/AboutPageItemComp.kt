// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * A composable component that displays a row with a title as well as leading and trailing icons.
 */

package be.scri.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

@Composable
fun AboutPageItemComp(
    title: String,
    leadingIcon: Int,
    trailingIcon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 10.dp,
                    ).clip(RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(leadingIcon),
                modifier =
                    Modifier
                        .padding(start = 2.dp)
                        .size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "Leading Icon",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                painter = painterResource(trailingIcon),
                modifier =
                    Modifier
                        .padding(start = 6.dp)
                        .size(24.dp),
                contentDescription = "Trailing Icon",
                tint = Color.Gray,
            )
        }
    }
}
