// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * The settings tab for the application including settings for language keyboards as sub menus.
 */

package be.scri.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList
import be.scri.ui.screens.settings.SettingsUtil.getLocalizedLanguageName

@Composable
fun SettingsScreen(
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageSettingsClick: (String) -> Unit,
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel =
        viewModel(
            factory = SettingsViewModelFactory(LocalContext.current),
        ),
) {
    val languages by viewModel.languages.collectAsState()
    val isKeyboardInstalled by viewModel.isKeyboardInstalled.collectAsState()
    val vibrateOnKeypress by viewModel.vibrateOnKeypress.collectAsState()
    val popupOnKeypress by viewModel.popupOnKeypress.collectAsState()
    val isUserDarkMode by viewModel.isUserDarkMode.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Refresh settings when coming back to the app
                    viewModel.refreshSettings(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val appSettingsItemList =
        ScribeItemList(
            items =
                listOf(
                    ScribeItem.ClickableItem(
                        title = R.string.app_settings_menu_app_language,
                        desc = R.string.app_settings_menu_app_language_description,
                        action = {
                            SettingsUtil.selectLanguage(context)
                        },
                    ),
                    ScribeItem.SwitchItem(
                        title = R.string.app_settings_menu_app_color_mode,
                        desc = R.string.app_settings_menu_app_color_mode_description,
                        state = isUserDarkMode,
                        onToggle = { newDarkMode ->
                            viewModel.setLightDarkMode(newDarkMode)
                            onDarkModeChange(newDarkMode)
                        },
                    ),
                    ScribeItem.SwitchItem(
                        title = R.string.app_settings_keyboard_keypress_vibration,
                        desc = R.string.app_settings_keyboard_keypress_vibration_description,
                        state = vibrateOnKeypress,
                        onToggle = { shouldVibrateOnKeypress ->
                            viewModel.setVibrateOnKeypress(context, shouldVibrateOnKeypress)
                        },
                    ),
                    ScribeItem.SwitchItem(
                        title = R.string.app_settings_keyboard_functionality_popup_on_keypress,
                        desc = R.string.app_settings_keyboard_functionality_popup_on_keypress_description,
                        state = popupOnKeypress,
                        onToggle = { shouldPopUpOnKeypress ->
                            viewModel.setPopupOnKeypress(context, shouldPopUpOnKeypress)
                        },
                    ),
                ),
        )

    val installedKeyboardList =
        languages.map { language ->
            ScribeItem.ClickableItem(
                title = getLocalizedLanguageName(language),
                desc = null,
                action = { onLanguageSettingsClick(language) },
            )
        }

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_settings_title),
        onBackNavigation = {},
        modifier = modifier,
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                ItemCardContainerWithTitle(
                    title = stringResource(R.string.app_settings_menu_title),
                    cardItemsList = appSettingsItemList,
                )
            }

            item {
                if (isKeyboardInstalled) {
                    ItemCardContainerWithTitle(
                        title = stringResource(R.string.app_settings_keyboard_title),
                        cardItemsList = ScribeItemList(installedKeyboardList),
                        isDivider = true,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                } else {
                    InstallKeyboardButton(
                        onClick = {
                            SettingsUtil.navigateToKeyboardSettings(context)
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun InstallKeyboardButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(R.color.corner_polygon_color),
            ),
    ) {
        Text(
            text = stringResource(R.string.app_settings_button_install_keyboards),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.button_text_color),
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}
