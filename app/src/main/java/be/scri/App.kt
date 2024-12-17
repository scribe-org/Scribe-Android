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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import be.scri.navigation.Screen
import be.scri.ui.common.bottom_bar.ScribeBottomBar
import be.scri.ui.common.bottom_bar.bottomBarScreens
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.screens.LanguageSettingsScreen
import be.scri.ui.screens.PrivacyPolicyScreen
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.screens.WikimediaScreen
import be.scri.ui.screens.about.AboutScreen
import be.scri.ui.theme.ScribeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("ComposeModifierMissing", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScribeApp(
    pagerState: PagerState,
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    onDarkModeChange: (Boolean) -> Unit,
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
            NavHost(
                navController = navController,
                startDestination = "pager"
            ) {
                composable("pager") {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 3,
                        modifier = Modifier
                    ) { page ->
                        when (page) {
                            0 -> InstallationScreen(
                                isDark = isDarkTheme
                            )
                            1 -> SettingsScreen(
                                isUserDarkMode = isDarkTheme,
                                onDarkModeChange = { onDarkModeChange(it) },
                                onLanguageSettingsClick = { language ->
                                    navController.navigate(
                                        "${Screen.LanguageSettings.route}/$language"
                                    )
                                }
                            )
                            2 -> AboutScreen(
                                onPrivacyPolicyClick = {
                                    navController.navigate(Screen.PrivacyPolicy.route)
                                },
                                onThirdPartyLicensesClick = {
                                    navController.navigate(Screen.ThirdParty.route)
                                },
                                onWikiClick = {
                                    navController.navigate(Screen.WikimediaScribe.route)
                                }
                            )
                        }
                    }
                }

                composable(
                    route = "${Screen.LanguageSettings.route}/{languageName}",
                ) {
                    val language = it.arguments?.getString("languageName")
                    if (language != null) {
                        LanguageSettingsScreen(
                            language = language,
                            onBackNavigation = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(Screen.WikimediaScribe.route) {
                    WikimediaScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.ThirdParty.route) {
                    ThirdPartyScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
