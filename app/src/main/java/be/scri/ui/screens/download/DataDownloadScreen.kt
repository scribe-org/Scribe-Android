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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import be.scri.helpers.StringUtils
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.appcomponents.ConfirmationDialog
import be.scri.ui.common.appcomponents.InstallKeyboardButton
import be.scri.ui.common.components.CircleClickableItemComp
import be.scri.ui.common.components.LanguageItemComp
import be.scri.ui.common.components.SwitchableItemComp
import be.scri.ui.screens.settings.SettingsUtil
import be.scri.ui.screens.settings.SettingsViewModel
import be.scri.ui.screens.settings.SettingsViewModelFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Screen for downloading and managing language data.
 *
 * @param onBackNavigation Callback for back navigation action.
 * @param onNavigateToTranslation Callback for navigating to translation language selection.
 * @param modifier Modifier for layout and styling.
 * @param downloadStates Map of language keys to their download states.
 * @param onDownloadAction Callback for download action when a language is selected and confirmed.
 * @param initializeStates Callback to initialize download states for given languages.
 * @param checkAllForUpdates Callback to check all languages for available updates.
 */
@Composable
fun DownloadDataScreen(
    onBackNavigation: () -> Unit,
    onNavigateToTranslation: (String) -> Unit,
    checkAllForUpdates: () -> Unit,
    modifier: Modifier = Modifier,
    downloadStates: Map<String, DownloadState> = emptyMap(),
    onDownloadAction: (String, Boolean) -> Unit = { _, _ -> },
    onDownloadAll: () -> Unit = {},
    initializeStates: (List<String>) -> Unit = {},
    viewModel: SettingsViewModel =
        viewModel(
            factory = SettingsViewModelFactory(LocalContext.current),
        ),
) {
    val currentInitializeStates by rememberUpdatedState(initializeStates)
    val scrollState = rememberScrollState()
    val checkForNewData = remember { mutableStateOf(false) }
    val regularlyUpdateData = remember { mutableStateOf(true) }
    val selectedLanguage = remember { mutableStateOf<LanguageItem?>(null) }
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val installedKeyboardLanguages by viewModel.languages.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshSettings(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Prepare the list of languages to display, including the "All Languages" option.
    val languages: ImmutableList<LanguageItem> =
        remember(installedKeyboardLanguages) {
            buildList {
                add(LanguageItem("all", context.getString(R.string.i18n_app_download_menu_ui_download_data_all_languages), false))
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
                    add(LanguageItem(languageCode.lowercase(), displayName, false))
                }
            }.toImmutableList()
        }

    // Determine the state of the "All Languages" item based on individual language states.
    val allLanguagesState =
        when {
            downloadStates.filter { it.key != "all" }.values.all { it == DownloadState.Completed } -> DownloadState.Completed
            downloadStates.filter { it.key != "all" }.values.all { it == DownloadState.Downloading } -> DownloadState.Downloading
            downloadStates.filter { it.key != "all" }.values.all { it == DownloadState.Update } -> DownloadState.Update
            else -> DownloadState.Ready
        }

    LaunchedEffect(languages) {
        val keys = languages.map { it.key }
        currentInitializeStates(keys)
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
            // Update Data Section
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
                        if (installedKeyboardLanguages.isNotEmpty()) {
                            CircleClickableItemComp(
                                title = stringResource(R.string.i18n_app_download_menu_ui_update_data_check_new),
                                onClick = {
                                    checkForNewData.value = !checkForNewData.value
                                    if (checkForNewData.value) checkAllForUpdates()
                                },
                                isSelected = checkForNewData.value,
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

            // Download Data Section
            Column {
                Text(
                    text = stringResource(R.string.i18n_app_download_menu_ui_download_data_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 10.dp),
                )

                if (installedKeyboardLanguages.isEmpty()) {
                    EmptyStateSection(context)
                } else {
                    LanguagesListSection(
                        languages = languages,
                        allLanguagesState = allLanguagesState,
                        downloadStates = downloadStates,
                        onLanguageSelect = { selectedLanguage.value = it },
                        onDownloadAll = onDownloadAll,
                        onDownloadAction = onDownloadAction,
                    )
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
                            onDownloadAction(key, false)
                            selectedLanguage.value = null
                        },
                        onChange = { onNavigateToTranslation(languageId) },
                        onDismiss = { selectedLanguage.value = null },
                    )
                }
            }
        }
    }
}

@Immutable
data class LanguageItem(
    val key: String,
    val displayName: String,
    val isDark: Boolean,
)

/**
 * Represents empty state when no languages are available for download.
 */
@Composable
private fun EmptyStateSection(context: Context) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.i18n_app_download_menu_ui_no_keyboards_installed),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 16.sp,
            )
        }
    }
    InstallKeyboardButton(
        onClick = { SettingsUtil.navigateToKeyboardSettings(context) },
    )
}

/**
 * Composable function to display the list of languages available for download, along with their respective download states and actions.
 *
 * @param languages List of [LanguageItem] representing the available languages.
 * @param allLanguagesState The overall download state for all languages.
 * @param downloadStates Map of individual language keys to their respective [DownloadState].
 * @param onLanguageSelect Callback invoked when a specific language is selected for download.
 * @param onDownloadAll Callback invoked when the "All Languages" option is selected for download.
 * @param onDownloadAction Callback invoked when a specific language's download action is triggered, with parameters for language key and whether it's an "all" action.
 */
@Composable
private fun LanguagesListSection(
    languages: ImmutableList<LanguageItem>,
    allLanguagesState: DownloadState,
    downloadStates: Map<String, DownloadState>,
    onLanguageSelect: (LanguageItem) -> Unit,
    onDownloadAll: () -> Unit,
    onDownloadAction: (String, Boolean) -> Unit,
) {
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
                val currentStatus = if (lang.key == "all") allLanguagesState else (downloadStates[lang.key] ?: DownloadState.Ready)

                LanguageItemComp(
                    title = lang.displayName,
                    onClick = { },
                    onButtonClick = {
                        if (lang.key == "all") {
                            onDownloadAll()
                        } else if (currentStatus == DownloadState.Ready) {
                            onLanguageSelect(lang)
                        } else {
                            onDownloadAction(lang.key, false)
                        }
                    },
                    isDarkTheme = lang.isDark,
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
