// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The bottom bar that is displayed at the bottom of the screen for navigation purposes.
 */

package be.scri.ui.common.bottombar

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.ui.res.colorResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun ScribeBottomBar(
    onItemClick: (Int) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier,

        ) {
            bottomBarScreens.forEachIndexed { index, item ->
                val isSelected = pagerState.currentPage == index

                val iconSize = if (isSelected) 26.dp else 24.dp
                val textSize = if (isSelected) 13.sp else 12.sp
                val greyColor = colorResource(id = R.color.nav_item_grey)


                val color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        greyColor
                    }

                BottomNavigationItem(
                    selected = pagerState.currentPage == index,
                    onClick = { onItemClick(index) },
                    icon = {
                        androidx.compose.material3.Icon(
                            painter =
                                painterResource(
                                    id = item.icon,
                                ),
                            tint = color,
                            contentDescription = "Keyboard",
                            modifier =
                                Modifier
                                    .size(iconSize),
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontSize = textSize,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.W600,
                                    letterSpacing = (0).sp,
                                    color = color,
                                ),
                        )
                    },
                    alwaysShowLabel = true,
                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                    unselectedContentColor = greyColor,
                    interactionSource = remember {
                        object : MutableInteractionSource {
                            override val interactions = MutableSharedFlow<Interaction>(extraBufferCapacity = Int.MAX_VALUE)
                            override suspend fun emit(interaction: Interaction) {}
                            override fun tryEmit(interaction: Interaction): Boolean = true
                        }
                    }
                )
            }
        }
    }
}
