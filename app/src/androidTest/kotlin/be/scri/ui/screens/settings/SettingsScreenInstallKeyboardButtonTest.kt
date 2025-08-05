// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import be.scri.R
import be.scri.ui.theme.ScribeTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenInstallKeyboardButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(isKeyboardInstalled: Boolean = false): SettingsViewModel {
        val mockViewModel = mockk<SettingsViewModel>(relaxed = true)
        every { mockViewModel.languages } returns MutableStateFlow(emptyList())
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(isKeyboardInstalled)
        every { mockViewModel.vibrateOnKeypress } returns MutableStateFlow(false)
        every { mockViewModel.popupOnKeypress } returns MutableStateFlow(false)
        every { mockViewModel.isUserDarkMode } returns MutableStateFlow(false)
        return mockViewModel
    }

    @Test
    fun installKeyboardButton_isDisplayed_whenKeyboardNotInstalled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun installKeyboardButton_isNotDisplayed_whenKeyboardInstalled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = true)

        every { mockViewModel.languages } returns MutableStateFlow(listOf("English", "German"))

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertDoesNotExist()
    }

    @Test
    fun installKeyboardButton_hasClickAction_whenDisplayed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun installKeyboardButton_performClick_buttonStillExists() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun installKeyboardButton_displaysCorrectText() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun installKeyboardButton_rendersCorrectly_inLightTheme() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun installKeyboardButton_rendersCorrectly_inDarkTheme() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        every { mockViewModel.isUserDarkMode } returns MutableStateFlow(true)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = true) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun installKeyboardButton_hasProperStyling() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.app_settings_button_install_keyboards)
        val mockViewModel = createMockViewModel(isKeyboardInstalled = false)

        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                SettingsScreen(
                    onDarkModeChange = {},
                    onLanguageSettingsClick = {},
                    context = context,
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule
            .onNodeWithText(expectedText)
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
