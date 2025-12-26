// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.CircleClickableItemComp
import be.scri.ui.common.components.LanguageItemComp
import be.scri.ui.common.components.SwitchableItemComp
import be.scri.ui.screens.settings.SettingsUtil

@Composable
fun DownloadDataScreen(
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val checkForNewData = remember { mutableStateOf(false) }
    val regularlyUpdateData = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val installedKeyboardLanguages =
        remember {
            SettingsUtil.getKeyboardLanguages(context)
        }

    val languages =
        remember(installedKeyboardLanguages) {
            buildList {
                add(Triple("all", context.getString(R.string.app_download_menu_ui_select_all_languages), false))

                installedKeyboardLanguages.forEach { languageCode ->
                    val displayName =
                        when (languageCode.lowercase()) {
                            "english" -> context.getString(R.string.app__global_english)
                            "french" -> context.getString(R.string.app__global_french)
                            "german" -> context.getString(R.string.app__global_german)
                            "italian" -> context.getString(R.string.app__global_italian)
                            "portuguese" -> context.getString(R.string.app__global_portuguese)
                            "russian" -> context.getString(R.string.app__global_russian)
                            "spanish" -> context.getString(R.string.app__global_spanish)
                            "swedish" -> context.getString(R.string.app__global_swedish)
                            else -> languageCode.replaceFirstChar { it.uppercase() }
                        }

                    val key = languageCode.lowercase()
                    add(Triple(key, displayName, false))
                }
            }
        }

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_download_menu_ui_title),
        lastPage = stringResource(R.string.app_installation_title),
        onBackNavigation = onBackNavigation,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_download_menu_ui_update_data_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 10.dp),
                )
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
                        CircleClickableItemComp(
                            title = stringResource(R.string.app_download_menu_ui_update_data_check_new),
                            onClick = { checkForNewData.value = !checkForNewData.value },
                            isSelected = checkForNewData.value,
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        )

                        SwitchableItemComp(
                            title = stringResource(R.string.app_download_menu_ui_update_data_regular_update),
                            isChecked = regularlyUpdateData.value,
                            onCheckedChange = { regularlyUpdateData.value = it },
                        )
                    }
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.app_download_menu_ui_select_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 10.dp),
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
                        languages.forEachIndexed { index, (key, title, isDark) ->
                            LanguageItemComp(
                                title = title,
                                onClick = {
                                },
                                isDarkTheme = isDark,
                            )
                            if (index < languages.lastIndex) {
                                HorizontalDivider(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
