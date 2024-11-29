package be.scri.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import be.scri.ui.model.ScribeItem
import be.scri.ui.model.ScribeItemList

@Composable
fun ItemsCardContainer(
    cardItemsList: ScribeItemList,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            cardItemsList.items.forEach { item ->
                when(item) {
                    is ScribeItem.ClickableItem -> {
                        ClickableItemComp(
                            item.title,
                            item.desc,
                            item.action
                        )
                    }

                    is ScribeItem.SwitchItem -> {
                        SwitchableItemComp(
                            item.title,
                            item.desc,
                            item.state,
                            item.onToggle
                        )
                    }

                    is ScribeItem.CustomItem -> {

                    }

                    is ScribeItem.ExternalLinkItem -> {

                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ItemsCardContainerPreview() {
    val cardItemsList = ScribeItemList(
        listOf(
            ScribeItem.ClickableItem(
                "",
                "",
                action = {}
            )
        )
    )

    ItemsCardContainer(cardItemsList)
}
