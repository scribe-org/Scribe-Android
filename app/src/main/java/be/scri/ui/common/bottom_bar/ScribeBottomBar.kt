package be.scri.ui.common.bottom_bar

import SettingsScreen
import android.text.Layout.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import be.scri.R
import be.scri.navigation.Screen
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.screens.about.AboutScreen
import kotlinx.coroutines.CoroutineScope
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
fun ScribeBottomBarWithPager(
    navController: NavHostController,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { bottomBarScreens.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> InstallationScreen()
                1 -> SettingsScreen(
                    isUserDarkMode = isDarkTheme,
                    onLanguageSettingsClick = { language ->
                        navController.navigate("${Screen.LanguageSettings.route}/$language")
                    }
                )
                2 -> AboutScreen(
                    onWikimediaAndScribeClick = {
                        navController.navigate(Screen.WikimediaScribe.route)
                    },
                    onPrivacyPolicyClick = {
                        navController.navigate(Screen.PrivacyPolicy.route)
                    },
                    onThirdPartyLicensesClick = {
                        navController.navigate(Screen.ThirdParty.route)
                    }
                )
            }
        }

        BottomAppBar(
            containerColor = colorResource(R.color.nav_bar_color),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
//            Spacer(modifier = Modifier.width(10.dp))
            bottomBarScreens.forEachIndexed { index, item ->
                BottomBarItem(
                    icon = item.icon,
                    title = item.label,
                    isSelected = pagerState.currentPage == index,
                    onItemClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.weight(1f) // Equal space modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
//                            top = (-8).dp
                        ) // Dynamic spacing padding
                        .offset(
                            y = (-8).dp
                        )
                )
            }
//            Spacer(modifier = Modifier.width(10.dp))
        }
    }

    // Sync pager navigation with navController
    LaunchedEffect(pagerState.currentPage) {
        val route = bottomBarScreens[pagerState.currentPage].route
        navController.navigate("$route/page") {
            popUpTo(Screen.Installation.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}


