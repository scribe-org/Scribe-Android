package be.scri

import SettingsScreen
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import be.scri.navigation.Screen
import be.scri.ui.common.app_components.HintDialog
import be.scri.ui.common.bottom_bar.ScribeBottomBar
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
    resetHints: () -> Unit,
    isHintChanged: Map<Int, Boolean>,
    onDismiss: (Int) -> Unit,
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
                            0 -> {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    InstallationScreen(
                                        isDark = isDarkTheme
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = 0,
                                        sharedPrefsKey = "hint_shown_main",
                                        hintMessageResId = R.string.app_installation_app_hint,
                                        isHintChanged = isHintChanged[0] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    )
                                }
                            }
                            1 -> {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    SettingsScreen(
                                        isUserDarkMode = isDarkTheme,
                                        onDarkModeChange = { onDarkModeChange(it) },
                                        onLanguageSettingsClick = { language ->
                                            navController.navigate(
                                                "${Screen.LanguageSettings.route}/$language"
                                            )
                                        }
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = 1,
                                        sharedPrefsKey = "hint_shown_settings",
                                        hintMessageResId = R.string.app_settings_app_hint,
                                        isHintChanged = isHintChanged[1] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            2 -> {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    AboutScreen(
                                        onPrivacyPolicyClick = {
                                            navController.navigate(Screen.PrivacyPolicy.route)
                                        },
                                        onThirdPartyLicensesClick = {
                                            navController.navigate(Screen.ThirdParty.route)
                                        },
                                        onWikiClick = {
                                            navController.navigate(Screen.WikimediaScribe.route)
                                        },
                                        resetHints = { resetHints() }
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = 2,
                                        sharedPrefsKey = "hint_shown_about",
                                        hintMessageResId = R.string.app_about_app_hint,
                                        isHintChanged = isHintChanged[2] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
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
