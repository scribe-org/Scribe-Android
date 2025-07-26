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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.CircleClickableItemComp
import be.scri.ui.common.components.LanguageItemComp
import be.scri.ui.common.components.SwitchableItemComp
import be.scri.ui.theme.theme_light_button_color

/**
 * The Download Data screen for managing language data downloads.
 */
@Composable
fun DownloadDataScreen(
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val checkForNewData = remember { mutableStateOf(false) }
    val regularlyUpdateData = remember { mutableStateOf(true) }

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_download_menu_ui_update_data_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier =
                        Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 10.dp,
                        ),
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    ) {
                        CircleClickableItemComp(
                            title = stringResource(R.string.app_download_menu_ui_update_data_check_new),
                            onClick = { checkForNewData.value = !checkForNewData.value },
                            isSelected = checkForNewData.value,
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
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
                    modifier =
                        Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 10.dp,
                        ),
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    ) {
                        LanguageItemComp(
                            title = stringResource(R.string.app_download_menu_ui_select_all_languages),
                            onClick = { /* Handle all languages click */ },
                            statusText = "Up to date",
                            titleFontWeight = FontWeight.Bold,
                            titleFontSize = 20.sp,
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_german),
                            onClick = { /* Handle German click */ },
                            statusText = "Up to date",
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_english),
                            onClick = { /* Handle English click */ },
                            statusText = "Download data",
                            statusColor = theme_light_button_color,
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_spanish),
                            onClick = { /* Handle Spanish click */ },
                            statusText = "Up to date",
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_french),
                            onClick = { /* Handle French click */ },
                            statusText = "Up to date",
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_italian),
                            onClick = { /* Handle Italian click */ },
                            statusText = "Up to date",
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_portuguese),
                            onClick = { /* Handle Portuguese click */ },
                            statusText = "Up to date",
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_russian),
                            onClick = { /* Handle Russian click */ },
                            statusText = "Up to date",
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp,
                                    horizontal = 12.dp,
                                ),
                        )

                        LanguageItemComp(
                            title = stringResource(R.string.app__global_swedish),
                            onClick = { /* Handle Swedish click */ },
                            statusText = "Download data",
                            statusColor = theme_light_button_color,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
