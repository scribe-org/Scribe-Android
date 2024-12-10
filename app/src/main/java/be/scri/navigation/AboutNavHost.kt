package be.scri.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import be.scri.activities.MainActivity
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper
import be.scri.ui.screens.PrivacyPolicyScreen
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.screens.WikimediaScreen
import be.scri.ui.screens.about.AboutScreen

@Composable
fun AboutNavHost(
    navController: NavHostController,
    context: Context,
    resetHints: () -> Unit,
    onShareScribeClick: () -> Unit,
    onRateScribeClick: () -> Unit,
    onMailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.About.route,
        modifier = modifier
    ) {
        composable(Screen.About.route) {
            AboutScreen(
                onWikimediaAndScribeClick = {
                    navController.navigate(Screen.WikimediaScribe.route)
                },
                onShareScribeClick = onShareScribeClick,
                onPrivacyPolicyClick = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                },
                onThirdPartyLicensesClick = {
                    navController.navigate(Screen.ThirdParty.route)
                },
                onRateScribeClick = onRateScribeClick,
                onMailClick = onMailClick,
                onResetHintsClick = resetHints,
                context = context,
            )
        }

        composable(Screen.WikimediaScribe.route) {
            WikimediaScreen()
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen()
        }

        composable(Screen.ThirdParty.route) {
            ThirdPartyScreen(
                onBackNavigation = { navController.popBackStack() }
            )
        }
    }
}
