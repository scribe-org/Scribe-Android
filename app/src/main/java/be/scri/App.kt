package be.scri

import SettingsScreen
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import be.scri.navigation.Screen
import be.scri.ui.common.bottom_bar.ScribeBottomBar
import be.scri.ui.common.bottom_bar.bottomBarScreens
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.screens.about.AboutScreen
import be.scri.ui.theme.ScribeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//class App : Application() {
//    override fun onCreate() {
//        AppCompatDelegate.setDefaultNightMode(
//            if (config.darkTheme) {
//                AppCompatDelegate.MODE_NIGHT_YES
//            } else {
//                AppCompatDelegate.MODE_NIGHT_NO
//            },
//        )
//        super.onCreate()
//    }
//}


@SuppressLint("ComposeModifierMissing", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScribeApp(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    isDarkTheme: Boolean
) {
    ScribeTheme(isDarkTheme) {
        Scaffold(
            bottomBar = {
                ScribeBottomBar(
                    onItemClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    },
                    pagerState = pagerState,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .background(color = colorResource(R.color.background_color))
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
            ) { page ->
                when (page) {
                    0 -> InstallationScreen()
                    1 -> SettingsScreen(
                        isUserDarkMode = isDarkTheme,
                        onLanguageSettingsClick = { language ->
//                            navController.navigate("${Screen.LanguageSettings.route}/$language")
                        }
                    )
                    2 -> AboutScreen(
//                        navController = navController,
                        onWikiClick = {
//                            navController.navigate(Screen.WikimediaScribe.route)
                        }
                    )
                }
            }
        }
    }
}
