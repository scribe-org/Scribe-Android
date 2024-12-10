package be.scri.navigation

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import be.scri.ui.screens.LanguageSettingsScreen

@Composable
fun SettingsNavHost(
    onLanguageSelect: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onInstallKeyboard: () -> Unit,
    isKeyboardInstalled: Boolean,
    isUserDarkMode: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Settings.route,
        modifier = modifier
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLanguageSelect = onLanguageSelect,
                onDarkModeChange = onDarkModeChange,
                onInstallKeyboard = onInstallKeyboard,
                isKeyboardInstalled = isKeyboardInstalled,
                isUserDarkMode = isUserDarkMode,
                onLanguageSettingsClick = { language ->
                    navController.navigate("${Screen.LanguageSettings.route}/$language")
                },
                modifier = Modifier
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
}
