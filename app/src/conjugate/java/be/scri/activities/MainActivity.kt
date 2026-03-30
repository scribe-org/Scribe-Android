// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Implements the main activity with a custom action bar, ViewPager navigation, and dynamic UI adjustments.
 */

package be.scri.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import be.scri.R
import be.scri.ScribeApp
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.setLightDarkModePreference
import be.scri.ui.common.bottombar.BottomBarScreen
import be.scri.ui.theme.ScribeTheme
import kotlinx.coroutines.delay

/**
 * The main entry point of the app.
 * Initializes theme settings, navigation, and sets up the main UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Incremented when the keyboard asks to show the Installation tab.
     */
    private var installTabOpenRequestCount by mutableIntStateOf(0)

    /**
     * Initializes the app on launch. Sets the theme based on user preferences, sets up edge-to-edge
     * layout, and builds the UI using Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(PreferencesHelper.getUserDarkModePreference(this))

        enableEdgeToEdge()
        consumeInstallTabIntentExtra(intent)

        setContent {
            val context = LocalContext.current

            val screens = remember(context) { BottomBarScreen.getScreens() }

            val isDarkMode =
                remember {
                    mutableStateOf(
                        PreferencesHelper
                            .getUserDarkModePreference(context) == AppCompatDelegate.MODE_NIGHT_YES,
                    )
                }

            val pagerState =
                rememberPagerState {
                    screens.size
                }

            val navController = rememberNavController()

            LaunchedEffect(installTabOpenRequestCount) {
                if (installTabOpenRequestCount == 0) return@LaunchedEffect
                delay(50)
                navController.popBackStack(
                    route = "pager",
                    inclusive = false,
                    saveState = false,
                )
                pagerState.animateScrollToPage(0)
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.keyboard_opened_install_tab),
                        Toast.LENGTH_SHORT,
                    ).show()
            }

            val isHintChangedMap = remember { mutableStateMapOf<Int, Boolean>() }

            /**
             * Updates the app's dark/light theme based on user preference and applies it
             * immediately.
             *
             * @param darkMode Whether the dark mode should be enabled.
             */
            fun updateTheme(darkMode: Boolean) {
                setLightDarkModePreference(context, darkMode)

                AppCompatDelegate.setDefaultNightMode(
                    if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
                )

                isDarkMode.value = darkMode
            }

            ScribeTheme(
                useDarkTheme = isDarkMode.value,
            ) {
                ScribeApp(
                    pagerState = pagerState,
                    isDarkTheme = isDarkMode.value,
                    onDarkModeChange = { darkMode ->
                        updateTheme(darkMode)
                    },
                    resetHints = {
                        isHintChangedMap[0] = true
                        isHintChangedMap[1] = true
                        isHintChangedMap[2] = true
                    },
                    isHintChanged = isHintChangedMap,
                    onDismiss = { pageIndex ->
                        isHintChangedMap[pageIndex] = false
                    },
                    context = context,
                    navController = navController,
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeInstallTabIntentExtra(intent)
    }

    private fun consumeInstallTabIntentExtra(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_OPEN_LANGUAGE_DATA, false)) {
            intent.removeExtra(EXTRA_OPEN_LANGUAGE_DATA)
            installTabOpenRequestCount++
        }
    }

    companion object {
        const val EXTRA_OPEN_LANGUAGE_DATA = "be.scri.extra.OPEN_LANGUAGE_DATA"
        const val EXTRA_KEYBOARD_LANGUAGE = "be.scri.extra.KEYBOARD_LANGUAGE"
    }
}
