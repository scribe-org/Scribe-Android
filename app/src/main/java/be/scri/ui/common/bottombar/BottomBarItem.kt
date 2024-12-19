package be.scri.ui.common.bottombar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("MagicNumber")
@Composable
fun BottomBarItem(
    @DrawableRes icon: Int,
    title: String,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color =
        if (isSelected) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    val iconSize =
        if (isSelected) {
            27.dp
        } else {
            24.dp
        }

    val textSize =
        if (isSelected) {
            12.sp
        } else {
            11.sp
        }
    Column(
        modifier = modifier.padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-9).dp),
    ) {
        IconButton(
            onClick = {
                onItemClick()
            },
        ) {
            Icon(
                painter =
                    painterResource(
                        id = icon,
                    ),
                tint = color,
                contentDescription = "Keyboard",
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .size(iconSize),
            )
        }
        Text(
            text = title,
            color = color,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontSize = textSize,
                ),
        )
    }
}
