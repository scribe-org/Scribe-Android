package be.scri.ui.common.bottombar

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.bottomnavigation.BottomNavigationItemView

@Composable
fun ScribeBottomBar(
    onItemClick: (Int) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Column (
        modifier = modifier
    ) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
        ) {
            bottomBarScreens.forEachIndexed { index, item ->
                val isSelected = pagerState.currentPage == index

                val iconSize =
                    if (isSelected) {
                        26.dp
                    } else {
                        24.dp
                    }

                val textSize =
                    if (isSelected) {
                        13.sp
                    } else {
                        12.sp
                    }

                val color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurface
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
                                .clip(MaterialTheme.shapes.medium)
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
                                color = color
                            ),
                        )
                    },
                    alwaysShowLabel = true,
                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
