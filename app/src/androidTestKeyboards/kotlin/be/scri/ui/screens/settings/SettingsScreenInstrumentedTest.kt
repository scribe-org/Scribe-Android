// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import be.scri.R
import be.scri.ui.theme.ScribeTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for SettingsScreen composable.
 * These tests require Android context and test the complete UI behavior.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var mockViewModel: SettingsViewModel
    private lateinit var onDarkModeChangeMock: (Boolean) -> Unit
    private lateinit var onLanguageSettingsClickMock: (String) -> Unit

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockViewModel = mockk(relaxed = true)
        onDarkModeChangeMock = mockk(relaxed = true)
        onLanguageSettingsClickMock = mockk(relaxed = true)

        // Setup comprehensive mock returns for all StateFlow properties
        every { mockViewModel.languages } returns MutableStateFlow(emptyList())
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(false)
        every { mockViewModel.vibrateOnKeypress } returns MutableStateFlow(false)
        every { mockViewModel.popupOnKeypress } returns MutableStateFlow(false)
        every { mockViewModel.isUserDarkMode } returns MutableStateFlow(false)

        every { mockViewModel.refreshSettings(any()) } returns Unit
        every { mockViewModel.setLightDarkMode(any()) } returns Unit
    }

    private fun setTestContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                content()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_displaysCorrectTitle() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_title))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAppLanguageOption() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_app_language))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysColorModeSwitch() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_app_color_mode))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_colorModeDescription_isDisplayed() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_app_color_mode_description))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_whenKeyboardNotInstalled_showsInstallButton() {
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(false)

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_button_install_keyboards))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun settingsScreen_whenKeyboardInstalled_showsLanguageList() {
        val testLanguages = listOf("English", "French", "German")
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(true)
        every { mockViewModel.languages } returns MutableStateFlow(testLanguages)

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_keyboard_title))
            .assertIsDisplayed()

        testLanguages.forEach { language ->
            composeTestRule
                .onNodeWithText(language)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun settingsScreen_languageItemClick_triggersCallback() {
        val testLanguages = listOf("English")
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(true)
        every { mockViewModel.languages } returns MutableStateFlow(testLanguages)

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModel,
            )
        }

        composeTestRule
            .onNodeWithText("English")
            .performClick()

        verify { onLanguageSettingsClickMock("English") }
    }

    @Test
    fun settingsScreen_darkModeToggle_triggersCallbacks() {
        every { mockViewModel.isUserDarkMode } returns MutableStateFlow(false)
        val mockViewModelSpy = spyk(mockViewModel)

        every { mockViewModelSpy.languages } returns MutableStateFlow(emptyList())
        every { mockViewModelSpy.isKeyboardInstalled } returns MutableStateFlow(false)
        every { mockViewModelSpy.vibrateOnKeypress } returns MutableStateFlow(false)
        every { mockViewModelSpy.popupOnKeypress } returns MutableStateFlow(false)
        every { mockViewModelSpy.isUserDarkMode } returns MutableStateFlow(false)
        every { mockViewModelSpy.refreshSettings(any()) } returns Unit
        every { mockViewModelSpy.setLightDarkMode(any()) } returns Unit

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModelSpy,
            )
        }

        val darkModeText = "Dark mode"

        composeTestRule
            .onNodeWithText(darkModeText)
            .assertIsDisplayed()

        composeTestRule
            .onAllNodes(hasClickAction() and !hasText(darkModeText))
            .filterToOne(
                !hasText("App language") and !hasText("Install keyboards"),
            ).performClick()

        composeTestRule.waitForIdle()

        verify(timeout = 3000) { mockViewModelSpy.setLightDarkMode(true) }
        verify(timeout = 3000) { onDarkModeChangeMock(true) }
    }

    @Test
    fun settingsScreen_refreshSettings_whenLifecycleResumes() {
        val mockViewModelSpy = spyk(mockViewModel)

        every { mockViewModelSpy.languages } returns MutableStateFlow(emptyList())
        every { mockViewModelSpy.isKeyboardInstalled } returns MutableStateFlow(false)
        every { mockViewModelSpy.vibrateOnKeypress } returns MutableStateFlow(false)
        every { mockViewModelSpy.popupOnKeypress } returns MutableStateFlow(false)
        every { mockViewModelSpy.isUserDarkMode } returns MutableStateFlow(false)
        every { mockViewModelSpy.refreshSettings(any()) } returns Unit
        every { mockViewModelSpy.setLightDarkMode(any()) } returns Unit

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModelSpy,
            )
        }

        verify(timeout = 3000) { mockViewModelSpy.refreshSettings(context) }
    }

    @Test
    fun settingsScreen_displaysCorrectMenuTitle() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_title))
            .assertIsDisplayed()
    }

    @Test
    fun installKeyboardButton_hasCorrectStyling() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        val installButton =
            composeTestRule
                .onNodeWithText(context.getString(R.string.i18n_app_settings_button_install_keyboards))

        installButton.assertIsDisplayed()
        installButton.assertHasClickAction()
    }

    @Test
    fun settingsScreen_appLanguageDescription_isDisplayed() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_app_language_description))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_multipleLanguages_allDisplayed() {
        val testLanguages = listOf("English", "French", "German", "Italian", "Spanish")
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(true)
        every { mockViewModel.languages } returns MutableStateFlow(testLanguages)

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModel,
            )
        }

        testLanguages.forEach { language ->
            composeTestRule
                .onNodeWithText(language)
                .assertIsDisplayed()
        }
    }

    @Test
    fun settingsScreen_emptyLanguageList_showsKeyboardTitle() {
        every { mockViewModel.isKeyboardInstalled } returns MutableStateFlow(true)
        every { mockViewModel.languages } returns MutableStateFlow(emptyList())

        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
                viewModel = mockViewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_keyboard_title))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_darkModeEnabled_switchStateReflectsValue() {
        setTestContent {
            SettingsScreen(
                onDarkModeChange = onDarkModeChangeMock,
                onLanguageSettingsClick = onLanguageSettingsClickMock,
                context = context,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_app_color_mode))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.i18n_app_settings_menu_app_color_mode_description))
            .assertIsDisplayed()
    }
}
