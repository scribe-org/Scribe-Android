package be.scri.ui.common.bottombar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScribeBottomBar(
    onItemClick: (Int) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(57.dp),
        ) {
            bottomBarScreens.forEachIndexed { index, item ->
                BottomBarItem(
                    icon = item.icon,
                    title = item.label,
                    isSelected = pagerState.currentPage == index,
                    onItemClick = {
                        onItemClick(index)
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                            ).offset(
                                y = (-8).dp,
                            ),
                )
            }
        }
    }

//    LaunchedEffect(pagerState.currentPage) {
//        val route = bottomBarScreens[pagerState.currentPage].route
//        navController.navigate("$route/page") {
// //            popUpTo(Screen.Installation.route) { saveState = true }
//            launchSingleTop = true
//            restoreState = true
//        }
//    }
}
