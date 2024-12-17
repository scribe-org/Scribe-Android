package be.scri.ui.common.bottom_bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import be.scri.R
import kotlinx.coroutines.launch

//@Composable
//fun ScribeBottomBar(
//    navController: NavHostController,
//    modifier: Modifier = Modifier
//) {
//
//}
//
//@Composable
//fun ScribeBottomBar(
//    isDarkTheme: Boolean,
//    coroutineScope: CoroutineScope,
//    modifier: Modifier = Modifier
//) {
//   BottomAppBar(
//       modifier = Modifier
//           .fillMaxWidth()
//   ) {
//        bottomBarScreens.forEach { item ->
//            BottomBarItem(
//                icon = item.icon,
//                title = item.label,
//                isSelected = item.route == Screen.Installation.route,
//                onItemClick = {
//                    // Handle item click
//                }
//            )
//        }
//   }
//}

@Composable
fun ScribeBottomBar(
    onItemClick: (Int) -> Unit,
    pagerState: PagerState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier) {


        BottomAppBar(
            containerColor = colorResource(R.color.nav_bar_color),
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
        ) {
            bottomBarScreens.forEachIndexed { index, item ->
                BottomBarItem(
                    icon = item.icon,
                    title = item.label,
                    isSelected = pagerState.currentPage == index,
                    onItemClick = {
                        onItemClick(index)
//                        coroutineScope.launch {
////                            pagerState.animateScrollToPage(index)
//                            onItemClick
//                        }
                    },
                    modifier = Modifier.weight(1f)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                        )
                        .offset(
                            y = (-8).dp
                        )
                )
            }
        }
    }

//    LaunchedEffect(pagerState.currentPage) {
//        val route = bottomBarScreens[pagerState.currentPage].route
//        navController.navigate("$route/page") {
////            popUpTo(Screen.Installation.route) { saveState = true }
//            launchSingleTop = true
//            restoreState = true
//        }
//    }
}


