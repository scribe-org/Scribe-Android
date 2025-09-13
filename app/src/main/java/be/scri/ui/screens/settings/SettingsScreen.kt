// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
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

/**
 * The settings tab for the application including settings for language keyboards as sub menus.
 */
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
                .padding(Dimensions.PaddingSmallXL)
                .shadow(Dimensions.ElevationSmall, RoundedCornerShape(Dimensions.PaddingLarge)),
        shape = RoundedCornerShape(Dimensions.PaddingLarge),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Text(
            text = stringResource(R.string.app_settings_button_install_keyboards),
            fontSize = Dimensions.TextSizeExtraLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(vertical = Dimensions.PaddingLarge),
        )
    }
}

/**
 * Defines commonly used dimensions for the Settings screen UI.
 * Includes padding, text sizes, and elevation values.
 */
object Dimensions {
    val PaddingLarge = 20.dp
    val PaddingSmallXL = 12.dp

    val TextSizeExtraLarge = 24.sp

    val ElevationSmall = 4.dp
}
