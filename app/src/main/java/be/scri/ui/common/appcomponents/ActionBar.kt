// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.appcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

/**
 * A reusable action bar component that displays a back button icon
 * and a title text in a horizontal row.
 *
 * @param title The title displayed next to the back button.
 * @param onClickAction Lambda function triggered when the back button is clicked.
 * @param modifier Optional [Modifier] for styling and layout customization.
 */
@Composable
fun ActionBar(
    title: String,
    onClickAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(
            onClick = { onClickAction() },
        ) {
            Icon(
                painter = painterResource(R.drawable.chevron),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Back button",
            )
        }
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
