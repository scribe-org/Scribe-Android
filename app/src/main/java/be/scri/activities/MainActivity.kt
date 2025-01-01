/**
 * Implements the main activity with a custom action bar, ViewPager navigation, and dynamic UI adjustments.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package be.scri.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import be.scri.ScribeApp
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.setLightDarkModePreference
import be.scri.services.EnglishKeyboardIME
import be.scri.ui.common.bottombar.bottomBarScreens
import be.scri.ui.theme.ScribeTheme

class MainActivity : ComponentActivity() {
    private var englishKeyboardIME: EnglishKeyboardIME? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(PreferencesHelper.getUserDarkModePreference(this))

        enableEdgeToEdge()

        englishKeyboardIME = EnglishKeyboardIME()

        setContent {
            val context = LocalContext.current

            val isDarkMode =
                remember {
                    mutableStateOf(
                        PreferencesHelper
                            .getUserDarkModePreference(context) == AppCompatDelegate.MODE_NIGHT_YES,
                    )
                }
            val pagerState =
                rememberPagerState {
                    bottomBarScreens.size
                }

            val navController = rememberNavController()

            val isHintChangedMap = remember { mutableStateMapOf<Int, Boolean>() }

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
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}
