package be.scri.navigation

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import be.scri.ui.screens.about.AboutScreen
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.screens.LanguageSettingsScreen
import be.scri.ui.screens.PrivacyPolicyScreen
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.screens.WikimediaScreen

@Composable
fun ScribeNavigation(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Installation.route,
        modifier = modifier
    ) {
        composable(Screen.Installation.route) {
            InstallationScreen()
        }

        composable(Screen.Settings.route) {
//            SettingsScreen()
        }

        composable(Screen.LanguageSettings.route) {
//            LanguageSettingsScreen()
        }

        composable(Screen.About.route) {
//            AboutScreen()
        }

        composable(Screen.PrivacyPolicy.route) {
//            PrivacyPolicyScreen()
        }

        composable(Screen.WikimediaScribe.route) {
//            WikimediaScreen()
            }

        composable(Screen.ThirdParty.route) {
            ThirdPartyScreen()
        }
    }
}
