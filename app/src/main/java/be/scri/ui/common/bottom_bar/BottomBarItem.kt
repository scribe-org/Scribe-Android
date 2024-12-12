package be.scri.ui.common.bottom_bar

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun BottomBarItem(
    @DrawableRes icon: Int,
    title: String,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) {
        colorResource(R.color.nav_bar_selected_color)
    } else {
        colorResource(R.color.nav_bar_focus_color)
    }

    val iconSize = if (isSelected) {
        30.dp
    } else {
        26.dp
    }

    val textSize = if (isSelected) {
        13.sp
    } else {
        12.sp
    }
    Column(
        modifier = modifier.padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-6).dp)
    ) {
        IconButton(
            onClick = {
                onItemClick()
            }
        ) {
            Icon(
                painter = painterResource(
                    id = icon
                ),
                tint = color,
                contentDescription = "Keyboard",
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .size(iconSize)
            )
        }
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = textSize
            ),
        )
    }
}


@PreviewLightDark
@Composable
private fun BottomBarItemPreview() {
    BottomBarItem(
        icon = R.drawable.keyboard_dark,
        title = "Keyboard",
        isSelected = true,
        onItemClick = {}
    )
}
