/**
 * Testing for MainFragment.
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

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.theme.ScribeTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainFragmentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    @Test
    fun testDarkModeIsApplied() {
        sharedPreferences.edit().putInt("dark_mode", AppCompatDelegate.MODE_NIGHT_YES).apply()
        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = true) {
                InstallationScreen()
            }
        }
        composeTestRule.onNodeWithTag("backgroundContainer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("keyboardSettingsCard").assertIsDisplayed()
    }

    @Test
    fun testLightModeIsApplied() {
        sharedPreferences.edit().putInt("dark_mode", AppCompatDelegate.MODE_NIGHT_NO).apply()
        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                InstallationScreen()
            }
        }

        composeTestRule.onNodeWithTag("backgroundContainer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("keyboardSettingsCard").assertIsDisplayed()
    }
}
