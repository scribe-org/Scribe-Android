// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 *  A composable function that displays a title above a list of items inside a card container.
 */

package be.scri.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.ui.models.ScribeItemList

@Composable
fun ItemCardContainerWithTitle(
    title: String,
    cardItemsList: ScribeItemList,
    modifier: Modifier = Modifier,
    isDivider: Boolean = false,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier =
                Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 10.dp,
                ),
        )

        ItemsCardContainer(
            cardItemsList = cardItemsList,
            isDivider = isDivider,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
        )
    }
}
