// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import android.content.Context
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
import be.scri.helpers.StringUtils
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.appcomponents.ConfirmationDialog
import be.scri.ui.common.components.CircleClickableItemComp
import be.scri.ui.common.components.LanguageItemComp
import be.scri.ui.common.components.SwitchableItemComp
import be.scri.ui.screens.settings.SettingsUtil

/**
 * Screen for downloading and managing language data.
 *
 * @param onBackNavigation Callback for back navigation action.
 * @param onNavigateToTranslation Callback for navigating to translation language selection.
 * @param modifier Modifier for layout and styling.
 * @param downloadStates Map of language keys to their download states.
 * @param onDownloadAction Callback for download action when a language is selected and confirmed.
 */
@Composable
fun DownloadDataScreen(
    onBackNavigation: () -> Unit,
    onNavigateToTranslation: (String) -> Unit,
    modifier: Modifier = Modifier,
    downloadStates: Map<String, DownloadState> = emptyMap(),
    onDownloadAction: (String) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val checkForNewData = remember { mutableStateOf(false) }
    val regularlyUpdateData = remember { mutableStateOf(true) }
    val selectedLanguage = remember { mutableStateOf<Triple<String, String, Boolean>?>(null) }
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val installedKeyboardLanguages =
        remember {
            SettingsUtil.getKeyboardLanguages(context)
        }

    val languages =
        remember(installedKeyboardLanguages) {
            buildList {
                add(Triple("all", context.getString(R.string.i18n_app_download_menu_ui_download_data_all_languages), false))

                installedKeyboardLanguages.forEach { languageCode ->
                    val displayName =
                        when (languageCode.lowercase()) {
                            "english" -> context.getString(R.string.i18n_app__global_english)
                            "french" -> context.getString(R.string.i18n_app__global_french)
                            "german" -> context.getString(R.string.i18n_app__global_german)
                            "italian" -> context.getString(R.string.i18n_app__global_italian)
                            "portuguese" -> context.getString(R.string.i18n_app__global_portuguese)
                            "russian" -> context.getString(R.string.i18n_app__global_russian)
                            "spanish" -> context.getString(R.string.i18n_app__global_spanish)
                            "swedish" -> context.getString(R.string.i18n_app__global_swedish)
                            else -> languageCode.replaceFirstChar { it.uppercase() }
                        }

                    val key = languageCode.lowercase()
                    add(Triple(key, displayName, false))
                }
            }
        }

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.i18n_app__global_download_data),
        lastPage = stringResource(R.string.i18n_app_installation_title),
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
                            title = stringResource(R.string.i18n_app_download_menu_ui_update_data_check_new),
                            onClick = { checkForNewData.value = !checkForNewData.value },
                            isSelected = checkForNewData.value,
                        )

                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        )

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
                        languages.forEachIndexed { index, lang ->
                            val (key, title, isDark) = lang
                            val currentStatus = downloadStates[key] ?: DownloadState.Ready

                            LanguageItemComp(
                                title = title,
                                onClick = { },
                                onButtonClick = {
                                    if (currentStatus == DownloadState.Ready) {
                                        selectedLanguage.value = lang
                                    } else {
                                        onDownloadAction(key)
                                    }
                                },
                                isDarkTheme = isDark,
                                buttonState = currentStatus,
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

            selectedLanguage.value?.let { lang ->
                val (key, title, _) = lang
                val languageId = key.replaceFirstChar { it.uppercase() }
                val sourceLang = sharedPref.getString("translation_source_$languageId", "English") ?: "English"
                ConfirmationDialog(
                    text =
                        StringUtils.stringResourceWithParams(
                            R.string.i18n_app_download_menu_ui_translation_source_tooltip_download_warning,
                            sourceLang,
                            title,
                        ),
                    textConfirm =
                        StringUtils.stringResourceWithParams(
                            R.string.i18n_app_download_menu_ui_translation_source_tooltip_use_source_language,
                            sourceLang,
                        ),
                    textChange = stringResource(R.string.i18n_app_download_menu_ui_translation_source_tooltip_change_language),
                    onConfirm = {
                        onDownloadAction(key)
                        selectedLanguage.value = null
                    },
                    onChange = { onNavigateToTranslation(languageId) },
                    onDismiss = { selectedLanguage.value = null },
                )
            }
        }
    }
}
