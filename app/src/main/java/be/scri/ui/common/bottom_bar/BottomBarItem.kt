package be.scri.ui.common.bottom_bar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import be.scri.R

@Composable
fun BottomBarItem(
    @DrawableRes icon: Int,
    title: String,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.onBackground
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
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
                tint = contentColor,
                contentDescription = "Keyboard"
            )
        }
        Text(
            text = title,
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
