package be.scri.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

@Composable
fun ItemsCardContainer(
    cardItemsList: ScribeItemList,
    isDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(vertical = 10.dp, horizontal = 4.dp),
        ) {
            cardItemsList.items.forEach { item ->
                when (item) {
                    is ScribeItem.ClickableItem -> {
                        ClickableItemComp(
                            title = item.title,
                            desc = item.desc,
                            onClick = item.action,
                        )
                    }

                    is ScribeItem.SwitchItem -> {
                        SwitchableItemComp(
                            title = item.title,
                            desc = item.desc,
                            isChecked = item.state,
                            onCheckedChange = item.onToggle,
                        )
                    }

                    is ScribeItem.CustomItem -> {
                    }

                    is ScribeItem.ExternalLinkItem -> {
                        AboutPageItemComp(
                            title = item.title,
                            leadingIcon = item.leadingIcon,
                            trailingIcon = item.trailingIcon,
                            onClick = item.onClick,
                        )
                    }
                }

                if (
                    isDivider &&
                    cardItemsList.items.indexOf(item) != cardItemsList.items.lastIndex
                ) {
                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.38f),
                        thickness = 1.dp,
                        modifier =
                            Modifier.padding(
                                vertical = 8.dp,
                                horizontal = 12.dp,
                            ),
                    )
                }
            }
        }
    }
}
