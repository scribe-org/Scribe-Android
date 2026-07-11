// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Handles application-level initialization and sets the default night mode based on user configuration.
 */

package be.scri

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import be.scri.helpers.AppFlavor
import be.scri.helpers.FlavorProvider
import be.scri.helpers.PreferencesHelper
import be.scri.navigation.Screen
import be.scri.ui.common.appcomponents.HintDialog
import be.scri.ui.common.bottombar.BottomBarScreen
import be.scri.ui.common.bottombar.ScribeBottomBar
import be.scri.ui.screens.ConjugateScreen
import be.scri.ui.screens.ConjugationSelectionScreen
import be.scri.ui.screens.DefaultCurrencySymbolScreen
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.screens.LanguageSettingsScreen
import be.scri.ui.screens.PrivacyPolicyScreen
import be.scri.ui.screens.SelectTranslationSourceLanguageScreen
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.screens.WikimediaScreen
import be.scri.ui.screens.about.AboutScreen
import be.scri.ui.screens.download.CheckUpdateActions
import be.scri.ui.screens.download.ConjugateDataDownloadViewModel
import be.scri.ui.screens.download.ConjugateDownloadDataScreen
import be.scri.ui.screens.download.DataDownloadViewModel
import be.scri.ui.screens.download.DownloadActions
import be.scri.ui.screens.download.DownloadDataScreen
import be.scri.ui.screens.settings.SettingsScreen
import be.scri.ui.screens.tutorial.TutorialNavigator
import be.scri.ui.theme.ScribeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The root composable function that sets up the app's theme, navigation, and screen layout.
 *
 * This function provides the primary navigation structure of the app using [NavHost] and configures
 * the bottom navigation bar using a [HorizontalPager]. It supports screen transitions, hint dialogs,
 * dark mode toggling, and navigation to secondary screens such as Privacy Policy, Wikimedia, and others.
 *
 * @param pagerState The pager state managing horizontal screen swipes.
 * @param navController Navigation controller for composable destinations.
 * @param onDarkModeChange Callback triggered when the user changes the dark mode setting.
 * @param resetHints Callback to reset all hint dialog states.
 * @param isHintChanged A map indicating which hints have been changed or dismissed.
 * @param onDismiss Callback called when a hint dialog is dismissed.
 * @param context The Android application context.
 * @param isDarkTheme Flag to indicate if dark theme is enabled.
 * @param modifier Optional layout modifier for UI customization.
 */
@Suppress("LongParameterList")
@SuppressLint("ComposeModifierMissing", "LongParameterList")
@Composable
fun ScribeApp(
    pagerState: PagerState,
    navController: NavHostController,
    onDarkModeChange: (Boolean) -> Unit,
    onIncreaseTextSizeChange: (Boolean) -> Unit,
    resetHints: () -> Unit,
    @SuppressLint("ComposeUnstableCollections") isHintChanged: Map<Int, Boolean>,
    onDismiss: (Int) -> Unit,
    context: Context,
    isDarkTheme: Boolean,
    isIncreaseTextSize: Boolean,
    modifier: Modifier = Modifier,
    downloadViewModel: DataDownloadViewModel = viewModel(),
    conjugateDownloadViewModel: ConjugateDataDownloadViewModel = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val downloadStates = downloadViewModel.downloadStates
    val onDownloadAction = downloadViewModel::handleDownloadAction
    val onDownloadAll = downloadViewModel::handleDownloadAllLanguages
    val initializeStates = downloadViewModel::initializeStates
    val downloadActions =
        DownloadActions(
            downloadStates = downloadStates,
            onDownloadAction = onDownloadAction,
            onDownloadAll = onDownloadAll,
            initializeStates = initializeStates,
        )
    val checkUpdateState by downloadViewModel.checkUpdateState.collectAsState()
    val checkUpdateActions =
        CheckUpdateActions(
            checkUpdateState = checkUpdateState,
            checkForNewData = downloadViewModel::checkForNewData,
            cancelCheckForNewData = downloadViewModel::cancelCheckForNewData,
        )

    // Conjugate-specific download actions
    val conjugateDownloadStates = conjugateDownloadViewModel.downloadStates
    val onConjugateDownloadAction = conjugateDownloadViewModel::handleDownloadAction
    val onConjugateDownloadAll = conjugateDownloadViewModel::handleDownloadAllLanguages
    val initializeConjugateStates = conjugateDownloadViewModel::initializeStates
    val conjugateDownloadActions =
        DownloadActions(
            downloadStates = conjugateDownloadStates,
            onDownloadAction = onConjugateDownloadAction,
            onDownloadAll = onConjugateDownloadAll,
            initializeStates = initializeConjugateStates,
        )
    val conjugateCheckUpdateState by conjugateDownloadViewModel.checkUpdateState.collectAsState()
    val conjugateCheckUpdateActions =
        CheckUpdateActions(
            checkUpdateState = conjugateCheckUpdateState,
            checkForNewData = conjugateDownloadViewModel::checkForNewData,
            cancelCheckForNewData = conjugateDownloadViewModel::cancelCheckForNewData,
        )

    val downloadToastMessage by downloadViewModel.toastMessage.collectAsState()
    val conjugateToastMessage by conjugateDownloadViewModel.toastMessage.collectAsState()
    val activeToastMessage = downloadToastMessage ?: conjugateToastMessage
    val isOfflineErrorVisible = activeToastMessage != null

    val screens = remember(context) { BottomBarScreen.getScreens() }
    ScribeTheme(
        useDarkTheme = isDarkTheme,
        isIncreaseTextSize = isIncreaseTextSize,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
            bottomBar = {
                ScribeBottomBar(
                    onItemClick = {
                        coroutineScope.launch {
                            if (navBackStackEntry?.destination?.route != "pager") {
                                navController.popBackStack()
                            }
                            pagerState.animateScrollToPage(it)
                        }
                    },
                    pagerState = pagerState,
                    modifier = Modifier,
                    screens = screens,
                )
            },
            modifier = modifier.fillMaxSize(),
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "pager",
            ) {
                composable("pager") {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = screens.size,
                        modifier = Modifier.padding(innerPadding),
                    ) { page ->
                        when (screens[page]) {
                            is BottomBarScreen.Installation -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    InstallationScreen(
                                        isDark = isDarkTheme,
                                        context = context,
                                        onNavigateToDownloadData = {
                                            navController.navigate("download_data")
                                        },
                                        onTutorialClick = {
                                            navController.navigate("tutorial")

                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(400)
                                                val aboutIndex = screens.indexOfFirst { it is BottomBarScreen.About }
                                                if (aboutIndex != -1) {
                                                    pagerState.scrollToPage(aboutIndex)
                                                }
                                            }
                                        },
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = page,
                                        sharedPrefsKey = "hint_shown_main",
                                        hintMessageResId = R.string.i18n_app_installation_app_hint_tooltip,
                                        isHintChanged = isHintChanged[page] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                    )
                                }
                                HandleBackPress(pagerState, coroutineScope)
                            }
                            is BottomBarScreen.Conjugate -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    ConjugateScreen(
                                        onNavigateToDownloadData = {
                                            navController.navigate("conjugate_download_data")
                                        },
                                        onNavigateToConjugationSelection = { verb, languageAlias ->
                                            navController.navigate(
                                                "${Screen.ConjugationSelection.route}/$verb/$languageAlias",
                                            )
                                        },
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = page,
                                        sharedPrefsKey = "hint_shown_conjugate",
                                        hintMessageResId = R.string.i18n_app_conjugate_app_hint_tooltip,
                                        isHintChanged = isHintChanged[page] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                    )
                                }
                                HandleBackPress(pagerState, coroutineScope)
                            }
                            is BottomBarScreen.Settings -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    SettingsScreen(
                                        onDarkModeChange = { isDarkMode ->
                                            onDarkModeChange(isDarkMode)
                                        },
                                        onIncreaseTextSizeChange = { increaseTextSize ->
                                            onIncreaseTextSizeChange(increaseTextSize)
                                        },
                                        onLanguageSettingsClick = { language ->
                                            navController.navigate(
                                                "${Screen.LanguageSettings.route}/$language",
                                            )
                                        },
                                        context = context,
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = page,
                                        sharedPrefsKey = "hint_shown_settings",
                                        hintMessageResId =
                                            if (FlavorProvider.get() == AppFlavor.CONJUGATE) {
                                                R.string.i18n_app_settings_conjugate_app_hint_tooltip
                                            } else {
                                                R.string.i18n_app_settings_keyboard_app_hint_tooltip
                                            },
                                        isHintChanged = isHintChanged[page] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                                HandleBackPress(pagerState, coroutineScope)
                            }
                            is BottomBarScreen.About -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    AboutScreen(
                                        onPrivacyPolicyClick = {
                                            navController.navigate(Screen.PrivacyPolicy.route)
                                        },
                                        onThirdPartyLicensesClick = {
                                            navController.navigate(Screen.ThirdParty.route)
                                        },
                                        onWikiClick = {
                                            navController.navigate(Screen.WikimediaScribe.route)
                                        },
                                        onTutorialClick = {
                                            navController.navigate("tutorial")
                                        },
                                        resetHints = { resetHints() },
                                        context = context,
                                    )
                                    HintDialog(
                                        pagerState = pagerState,
                                        currentPageIndex = page,
                                        sharedPrefsKey = "hint_shown_about",
                                        hintMessageResId = R.string.i18n_app_about_app_hint_tooltip,
                                        isHintChanged = isHintChanged[page] == true,
                                        onDismiss = { onDismiss(it) },
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                                HandleBackPress(pagerState, coroutineScope)
                            }
                        }
                    }
                }

                composable("tutorial") {
                    TutorialNavigator(
                        onTutorialExit = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable("download_data") {
                    DownloadDataScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        onNavigateToTranslation = { language ->
                            navController.navigate(
                                "translation_language_detail/$language",
                            )
                        },
                        isDarkTheme = isDarkTheme,
                        downloadActions = downloadActions,
                        checkUpdateActions = checkUpdateActions,
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable("conjugate_download_data") {
                    ConjugateDownloadDataScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        isDarkTheme = isDarkTheme,
                        downloadActions = conjugateDownloadActions,
                        checkUpdateActions = conjugateCheckUpdateActions,
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable("${Screen.ConjugationSelection.route}/{verb}/{languageAlias}") { backStackEntry ->
                    val verb = backStackEntry.arguments?.getString("verb") ?: ""
                    val languageAlias = backStackEntry.arguments?.getString("languageAlias") ?: ""
                    ConjugationSelectionScreen(
                        verb = verb,
                        languageAlias = languageAlias,
                        onBackNavigation = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable("${Screen.LanguageSettings.route}/{languageName}") {
                    val language = it.arguments?.getString("languageName")
                    if (language != null) {
                        LanguageSettingsScreen(
                            language = language,
                            onBackNavigation = {
                                navController.popBackStack()
                            },
                            modifier = Modifier.padding(innerPadding),
                            onTranslationLanguageSelect = {
                                navController.navigate("translation_language_detail/$language")
                            },
                            onCurrencySelect = {
                                val currentSymbol = PreferencesHelper.getDefaultCurrencySymbol(context, language)
                                navController.navigate("currency_symbol_detail/$currentSymbol/$language")
                            },
                        )
                    }
                }

                composable("translation_language_detail" + "/{languageName}") { backStackEntry ->
                    val language = backStackEntry.arguments?.getString("languageName") ?: ""
                    SelectTranslationSourceLanguageScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        onNavigateToDownloadData = {
                            navController.navigate("download_data")
                        },
                        onDownloadAction = onDownloadAction,
                        modifier = Modifier.padding(innerPadding),
                        currentLanguage = language,
                    )
                }

                composable("currency_symbol_detail/{symbolName}/{languageName}") { backStackEntry ->
                    val language = backStackEntry.arguments?.getString("languageName") ?: ""
                    DefaultCurrencySymbolScreen(
                        currentLanguage = language,
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable(Screen.WikimediaScribe.route) {
                    WikimediaScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                composable(Screen.ThirdParty.route) {
                    ThirdPartyScreen(
                        onBackNavigation = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            } // Ends NavHost
        } // Ends Scaffold content

            androidx.compose.animation.AnimatedVisibility(
                visible = isOfflineErrorVisible,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
            ) {
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    shadowElevation = 8.dp
                ) {
                    androidx.compose.material3.Text(
                        text = activeToastMessage ?: "",
                        color = androidx.compose.ui.graphics.Color(0xFFE68A00),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
    } // Ends Box
    } // Ends ScribeTheme
} // Ends App

/**
 * Handles the back press behavior within the pager.
 *
 * If the user is not on the first page, pressing the back button will scroll
 * the pager to the previous page instead of exiting the app.
 *
 * @param pagerState The pager state controlling the current screen.
 * @param coroutineScope Coroutine scope used to launch the scroll animation.
 */

@Composable
fun HandleBackPress(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
) {
    BackHandler(enabled = pagerState.currentPage > 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }
}
