package be.scri.navigation

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import be.scri.ui.screens.about.AboutScreen
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.screens.LanguageSettingsScreen
import be.scri.ui.screens.PrivacyPolicyScreen
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.screens.WikimediaScreen

@Composable
fun ScribeNavigation(
    isUserDarkMode: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "${Screen.Installation.route}/page",
        modifier = modifier
    ) {
        composable(route = "${Screen.Installation.route}/page") {
            InstallationScreen()
        }

        navigation(
            startDestination = Screen.Settings.route,
            route = "${Screen.Settings.route}/page"
        ) {
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isUserDarkMode = isUserDarkMode,
                    onLanguageSettingsClick = {
                        navController.navigate("${Screen.LanguageSettings.route}/$it")
                    },
                    modifier = Modifier,
                )
            }

            composable(
                "${Screen.LanguageSettings.route}/{language}",
                arguments = listOf(
                    navArgument("language") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val language = backStackEntry.arguments?.getString("language") ?: "Unknown"
                LanguageSettingsScreen(
                    language = language,
                    onBackNavigation = { navController.popBackStack() },
                    modifier = Modifier
                )
            }
        }

        navigation(
            startDestination = Screen.About.route,
            route = "${Screen.About.route}/page"
        ) {
            composable(Screen.About.route) {
                AboutScreen(
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

            composable(Screen.WikimediaScribe.route) {
                WikimediaScreen(
                    onBackNavigation = { navController.popBackStack() }
                )
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onBackNavigation = { navController.popBackStack() }
                )
            }

            composable(Screen.ThirdParty.route) {
                ThirdPartyScreen(
                    onBackNavigation = { navController.popBackStack() }
                )
            }
        }
    }
}
