package be.scri.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.models.ScribeItemList

@Composable
fun ItemCardContainerWithTitle(
    title: String,
    cardItemsList: ScribeItemList,
    modifier: Modifier = Modifier,
    isDivider: Boolean = false
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            color = colorResource(R.color.app_text_color),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                bottom = 10.dp,
            ),
        )

        ItemsCardContainer(
            cardItemsList = cardItemsList,
            isDivider = isDivider,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        )
    }
}
