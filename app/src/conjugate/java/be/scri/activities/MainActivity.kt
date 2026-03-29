// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Implements the main activity with a custom action bar, ViewPager navigation, and dynamic UI adjustments.
 */

package be.scri.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import be.scri.ScribeApp
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.setLightDarkModePreference
import be.scri.ui.common.bottombar.BottomBarScreen
import be.scri.ui.theme.ScribeTheme

/**
 * The main entry point of the app.
 * Initializes theme settings, navigation, and sets up the main UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Initializes the app on launch. Sets the theme based on user preferences, sets up edge-to-edge
     * layout, and builds the UI using Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(PreferencesHelper.getUserDarkModePreference(this))

        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            val screens = remember(context) { BottomBarScreen.getScreens() }

            val isDarkMode =
                remember {
                    mutableStateOf(
                        PreferencesHelper
                            .getUserDarkModePreference(context) == AppCompatDelegate.MODE_NIGHT_YES,
                    )
                }
            var increaseTextSize by remember {
                mutableStateOf(PreferencesHelper.getIncreaseAppTextSizeEnabled(context))
            }

            DisposableEffect(lifecycleOwner) {
                val observer =
                    LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            increaseTextSize = PreferencesHelper.getIncreaseAppTextSizeEnabled(context)
                        }
                    }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val pagerState =
                rememberPagerState {
                    screens.size
                }

            val navController = rememberNavController()

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
                increaseTextSize = false,
            ) {
                ScribeApp(
                    pagerState = pagerState,
                    isDarkTheme = isDarkMode.value,
                    increaseTextSize = increaseTextSize,
                    onDarkModeChange = { darkMode ->
                        updateTheme(darkMode)
                    },
                    onIncreaseTextSizeChange = { enabled ->
                        increaseTextSize = enabled
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
}
