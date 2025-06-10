// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.helpers.DISABLE_ACCENT_CHARACTER
import be.scri.helpers.EMOJI_SUGGESTIONS
import be.scri.helpers.PERIOD_AND_COMMA
import be.scri.helpers.PERIOD_ON_DOUBLE_TAP
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.SHOW_POPUP_ON_KEYPRESS
import be.scri.helpers.VIBRATE_ON_KEYPRESS
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

/**
 * The settings sub menu page for languages that allows for customization of language keyboard interfaces.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LanguageSettingsScreen(
    language: String,
    onBackNavigation: () -> Unit,
    onTranslationLanguageSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    val scrollState = rememberScrollState()

    val periodOnDoubleTapState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    PreferencesHelper.getLanguageSpecificPreferenceKey(PERIOD_ON_DOUBLE_TAP, language),
                    false,
                ),
            )
        }

    val emojiSuggestionsState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    PreferencesHelper.getLanguageSpecificPreferenceKey(EMOJI_SUGGESTIONS, language),
                    true,
                ),
            )
        }

    val disableAccentCharacterState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    PreferencesHelper.getLanguageSpecificPreferenceKey(DISABLE_ACCENT_CHARACTER, language),
                    false,
                ),
            )
        }

    val popupOnKeyPressState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    PreferencesHelper.getLanguageSpecificPreferenceKey(SHOW_POPUP_ON_KEYPRESS, language),
                    true,
                ),
            )
        }

    val vibrateOnKeyPressState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    PreferencesHelper.getLanguageSpecificPreferenceKey(VIBRATE_ON_KEYPRESS, language),
                    true,
                ),
            )
        }

    val periodAndCommaState =
        remember {
            if (!sharedPref.contains(PreferencesHelper.getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language))) {
                sharedPref
                    .edit()
                    .putBoolean(
                        PreferencesHelper.getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language),
                        true,
                    ).apply()
            }
            mutableStateOf(
                sharedPref.getBoolean(
                    PreferencesHelper.getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language),
                    true,
                ),
            )
        }

    val translationSourceLanguageList =
        ScribeItemList(
            items =
                getTranslationSourceLanguageListData {
                    onTranslationLanguageSelect()
                },
        )

    val layoutList =
        ScribeItemList(
            items =
                getLayoutListData(
                    language = language,
                    togglePeriodAndCommaState = periodAndCommaState.value,
                    onTogglePeriodAndComma = { shouldDisablePeriodAndComma ->
                        periodAndCommaState.value = shouldDisablePeriodAndComma
                        PreferencesHelper.setCommaAndPeriodPreference(
                            context,
                            language,
                            shouldDisablePeriodAndComma,
                        )
                    },
                    toggleDisableAccentCharacter = disableAccentCharacterState.value,
                    onToggleDisableAccentCharacter = { shouldDisableAccentCharacter ->
                        disableAccentCharacterState.value = shouldDisableAccentCharacter
                        PreferencesHelper.setAccentCharacterPreference(
                            context,
                            language,
                            shouldDisableAccentCharacter,
                        )
                    },
                ),
        )
    val functionalityList =
        ScribeItemList(
            items =
                getFunctionalityListData(
                    periodOnDoubleTapState = periodOnDoubleTapState.value,
                    onTogglePeriodOnDoubleTap = { shouldDoubleSpacePeriod ->
                        periodOnDoubleTapState.value = shouldDoubleSpacePeriod
                        PreferencesHelper.setPeriodOnSpaceBarDoubleTapPreference(
                            context,
                            language,
                            shouldDoubleSpacePeriod,
                        )
                    },
                    emojiSuggestionsState = emojiSuggestionsState.value,
                    onToggleEmojiSuggestions = { shouldDoubleSpacePeriod ->
                        emojiSuggestionsState.value = shouldDoubleSpacePeriod
                        PreferencesHelper.setEmojiAutoSuggestionsPreference(
                            context,
                            language,
                            shouldDoubleSpacePeriod,
                        )
                    },
                    togglePopUpOnKeyPress = popupOnKeyPressState.value,
                    onTogglePopUpOnKeyPress = { shouldDisablePopUpOnKeyPress ->
                        popupOnKeyPressState.value = shouldDisablePopUpOnKeyPress
                        PreferencesHelper.setShowPopupOnKeypress(
                            context,
                            language,
                            shouldDisablePopUpOnKeyPress,
                        )
                    },
                    toggleVibrateOnKeyPress = vibrateOnKeyPressState.value,
                    onToggleVibrateOnKeyPress = { shouldVibrateOnKeyPress ->
                        vibrateOnKeyPressState.value = shouldVibrateOnKeyPress
                        PreferencesHelper.setVibrateOnKeypress(
                            context,
                            language,
                            shouldVibrateOnKeyPress,
                        )
                    },
                ),
        )

    ScribeBaseScreen(
        pageTitle = stringResource(getLanguageStringFromi18n(language)),
        lastPage = stringResource(R.string.app_settings_title),
        onBackNavigation = onBackNavigation,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(scrollState),
        ) {
            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_settings_keyboard_translation_title),
                cardItemsList = translationSourceLanguageList,
            )
            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_settings_keyboard_layout_title),
                cardItemsList = layoutList,
            )

            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_settings_keyboard_functionality_title),
                cardItemsList = functionalityList,
                modifier =
                    Modifier
                        .padding(top = 6.dp),
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Builds a list of toggleable functionality settings for the keyboard.
 *
 * These settings include:
 * - Double-space for period
 * - Emoji suggestions
 * - Keypress vibration
 * - Popup on keypress
 *
 * @param periodOnDoubleTapState Current state of the double-space period setting.
 * @param onTogglePeriodOnDoubleTap Callback invoked when the double-space period is toggled.
 * @param emojiSuggestionsState Current state of the emoji suggestions setting.
 * @param onToggleEmojiSuggestions Callback invoked when emoji suggestions is toggled.
 * @param togglePopUpOnKeyPress Current state of popup on keypress setting.
 * @param onTogglePopUpOnKeyPress Callback invoked when popup on keypress is toggled.
 * @param toggleVibrateOnKeyPress Current state of keypress vibration setting.
 * @param onToggleVibrateOnKeyPress Callback invoked when keypress vibration is toggled.
 *
 * @return A list of [ScribeItem]s to be shown in the UI.
 */
@Composable
private fun getFunctionalityListData(
    periodOnDoubleTapState: Boolean,
    onTogglePeriodOnDoubleTap: (Boolean) -> Unit,
    emojiSuggestionsState: Boolean,
    onToggleEmojiSuggestions: (Boolean) -> Unit,
    togglePopUpOnKeyPress: Boolean,
    onTogglePopUpOnKeyPress: (Boolean) -> Unit,
    toggleVibrateOnKeyPress: Boolean,
    onToggleVibrateOnKeyPress: (Boolean) -> Unit,
): List<ScribeItem> {
    val list =
        listOf(
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_double_space_period,
                desc = R.string.app_settings_keyboard_functionality_double_space_period_description,
                state = periodOnDoubleTapState,
                onToggle = onTogglePeriodOnDoubleTap,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_auto_suggest_emoji,
                desc = R.string.app_settings_keyboard_functionality_auto_suggest_emoji_description,
                state = emojiSuggestionsState,
                onToggle = onToggleEmojiSuggestions,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_keypress_vibration,
                desc = R.string.app_settings_keyboard_keypress_vibration_description,
                state = toggleVibrateOnKeyPress,
                onToggle = onToggleVibrateOnKeyPress,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_popup_on_keypress,
                desc = R.string.app_settings_keyboard_functionality_popup_on_keypress_description,
                state = togglePopUpOnKeyPress,
                onToggle = onTogglePopUpOnKeyPress,
            ),
        )
    return list
}

/**
 * Returns a list of [ScribeItem]s representing layout-specific settings based on the selected [language].
 *
 * Includes toggles such as:
 * - "Disable accent characters" (conditionally shown for German, Swedish, and Spanish)
 * - "Period and comma" toggle (always included)
 *
 * @param language The currently selected language, used to determine which layout-specific settings to show.
 * @param togglePeriodAndCommaState Current state of the "Period and comma" toggle.
 * @param onTogglePeriodAndComma Callback invoked when the "Period and comma" toggle is changed.
 * @param toggleDisableAccentCharacter Current state of the "Disable accent characters" toggle.
 * @param onToggleDisableAccentCharacter Callback invoked when the "Disable accent characters" toggle is changed.
 *
 * @return A list of [ScribeItem]s to be displayed in the UI.
 */
@Composable
private fun getLayoutListData(
    language: String,
    togglePeriodAndCommaState: Boolean,
    onTogglePeriodAndComma: (Boolean) -> Unit,
    toggleDisableAccentCharacter: Boolean,
    onToggleDisableAccentCharacter: (Boolean) -> Unit,
): List<ScribeItem> {
    val list: MutableList<ScribeItem> = mutableListOf()

    when (language) {
        "German", "Swedish", "Spanish" -> {
            list.add(
                ScribeItem.SwitchItem(
                    title = R.string.app_settings_keyboard_layout_disable_accent_characters,
                    desc = R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                    state = toggleDisableAccentCharacter,
                    onToggle = onToggleDisableAccentCharacter,
                ),
            )
        }
    }

    list.add(
        ScribeItem.SwitchItem(
            title = R.string.app_settings_keyboard_layout_period_and_comma,
            desc = R.string.app_settings_keyboard_layout_period_and_comma_description,
            state = togglePeriodAndCommaState,
            onToggle = onTogglePeriodAndComma,
        ),
    )

    return list
}

/**
 * Returns the string resource ID for the localized display name of a given language.
 *
 * If the specified language is not recognized, defaults to the English string resource.
 *
 * @param language The name of the language (e.g., "German", "French").
 * @return The string resource ID corresponding to the localized name.
 */
fun getLanguageStringFromi18n(language: String): Int {
    val languageMap =
        mapOf(
            "German" to R.string.app__global_german,
            "French" to R.string.app__global_french,
            "Spanish" to R.string.app__global_spanish,
            "Italian" to R.string.app__global_italian,
            "Russian" to R.string.app__global_russian,
            "Portuguese" to R.string.app__global_portuguese,
            "Swedish" to R.string.app__global_swedish,
        )
    return languageMap[language] ?: R.string.app__global_english
}

/**
 * Builds a list of ScribeItems for the translation source language settings screen.
 *
 * This list contains items that allow users to select the source language used in keyboard translation.
 * When the user selects the source language item, the provided [onTranslationLanguageSelect] callback is triggered.
 *
 * @param onTranslationLanguageSelect A lambda function invoked when the user clicks the "Select Source Language" item.
 * @return A list of [ScribeItem]s to be displayed in the UI.
 */
@Composable
private fun getTranslationSourceLanguageListData(onTranslationLanguageSelect: () -> Unit): List<ScribeItem> {
    val list: MutableList<ScribeItem> = mutableListOf()
    list.add(
        ScribeItem.ClickableItem(
            title = R.string.app_settings_keyboard_translation_select_source,
            desc = R.string.app_settings_keyboard_translation_select_source_description,
            action = {
                Log.d("Navigation", "onTranslationLanguageSelect clicked")
                onTranslationLanguageSelect()
            },
        ),
    )

    return list
}
