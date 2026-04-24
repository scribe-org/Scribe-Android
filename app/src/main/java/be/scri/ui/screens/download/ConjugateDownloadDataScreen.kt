// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.CircleClickableItemComp
import be.scri.ui.common.components.LanguageItemComp
import be.scri.ui.common.components.SwitchableItemComp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ConjugateDownloadDataScreen(
    onBackNavigation: () -> Unit,
    isDarkTheme: Boolean,
    checkUpdateActions: CheckUpdateActions,
    downloadActions: DownloadActions,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val regularlyUpdateData = remember { mutableStateOf(true) }

    val allLanguages: ImmutableList<LanguageItem> =
        remember {
            listOf(
                LanguageItem("english", context.getString(R.string.i18n_app__global_english), false),
                LanguageItem("french", context.getString(R.string.i18n_app__global_french), false),
                LanguageItem("german", context.getString(R.string.i18n_app__global_german), false),
                LanguageItem("italian", context.getString(R.string.i18n_app__global_italian), false),
                LanguageItem("portuguese", context.getString(R.string.i18n_app__global_portuguese), false),
                LanguageItem("russian", context.getString(R.string.i18n_app__global_russian), false),
                LanguageItem("spanish", context.getString(R.string.i18n_app__global_spanish), false),
                LanguageItem("swedish", context.getString(R.string.i18n_app__global_swedish), false),
            ).sortedBy { it.displayName }.toImmutableList()
        }

    LaunchedEffect(Unit) {
        downloadActions.initializeStates(allLanguages.map { it.key })
    }

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.i18n_app__global_download_data),
        lastPage = stringResource(R.string.i18n_app_conjugate_title),
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
                    text = stringResource(R.string.i18n_app_download_menu_ui_update_data),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
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
                        val hasDownloadedLanguage =
                            downloadActions.downloadStates.values.any {
                                it == DownloadState.Completed || it == DownloadState.Update
                            }
                        if (hasDownloadedLanguage) {
                            CircleClickableItemComp(
                                checkState = checkUpdateActions.checkUpdateState,
                                onStartCheck = checkUpdateActions.checkForNewData,
                                onCancel = checkUpdateActions.cancelCheckForNewData,
                                title = stringResource(R.string.i18n_app_download_menu_ui_update_data_check_new),
                                isDarkTheme = isDarkTheme,
                            )
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            )
                        }
                        SwitchableItemComp(
                            title = stringResource(R.string.i18n_app_download_menu_ui_update_data_regular_update),
                            isChecked = regularlyUpdateData.value,
                            onCheckedChange = { regularlyUpdateData.value = it },
                        )
                    }
                }
            }

            Column {
                Text(
                    text = stringResource(R.string.i18n_app_download_menu_ui_download_data_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
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
                        Text(
                            text = "Update all",
                            color = colorResource(R.color.dark_scribe_blue),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier =
                                Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .align(Alignment.End)
                                    .clickable { downloadActions.onDownloadAll() },
                        )
                        allLanguages.forEachIndexed { index, lang ->
                            val currentStatus = downloadActions.downloadStates[lang.key] ?: DownloadState.Ready
                            LanguageItemComp(
                                title = lang.displayName,
                                onClick = { },
                                onButtonClick = { downloadActions.onDownloadAction(lang.key, false) },
                                isDarkTheme = lang.isDark,
                                buttonState = currentStatus,
                            )
                            if (index < allLanguages.lastIndex) {
                                HorizontalDivider(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
