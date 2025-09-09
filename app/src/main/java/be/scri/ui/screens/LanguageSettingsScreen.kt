// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.annotation.SuppressLint
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
import be.scri.helpers.PreferencesHelper
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

/**
 * Data class to hold functionality settings state and callbacks
 */
private data class FunctionalitySettings(
    val periodOnDoubleTapState: Boolean,
    val onTogglePeriodOnDoubleTap: (Boolean) -> Unit,
    val emojiSuggestionsState: Boolean,
    val onToggleEmojiSuggestions: (Boolean) -> Unit,
    val togglePopUpOnKeyPress: Boolean,
    val onTogglePopUpOnKeyPress: (Boolean) -> Unit,
    val toggleVibrateOnKeyPress: Boolean,
    val onToggleVibrateOnKeyPress: (Boolean) -> Unit,
    val toggleSoundOnKeyPress: Boolean,
    val onToggleSoundOnKeyPress: (Boolean) -> Unit,
    val wordByWordDeletionState: Boolean,
    val onToggleWordByWordDeletion: (Boolean) -> Unit,
    val disableSwipeAltKeysState: Boolean,
    val onToggleDisableSwipeAltKeys: (Boolean) -> Unit,
)

/**
 * The settings sub menu page for languages that allows for customization of language keyboard interfaces.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LanguageSettingsScreen(
    language: String,
    onBackNavigation: () -> Unit,
    onTranslationLanguageSelect: () -> Unit,
    onCurrencySelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val periodOnDoubleTapState =
        remember {
            mutableStateOf(
                PreferencesHelper.getEnablePeriodOnSpaceBarDoubleTap(context, language),
            )
        }

    val emojiSuggestionsState =
        remember {
            mutableStateOf(
                PreferencesHelper.getIsEmojiSuggestionsEnabled(context, language),
            )
        }

    val disableAccentCharacterState =
        remember {
            mutableStateOf(
                PreferencesHelper.getIsAccentCharacterDisabled(context, language),
            )
        }

    val popupOnKeyPressState =
        remember {
            mutableStateOf(
                PreferencesHelper.isShowPopupOnKeypressEnabled(context, language),
            )
        }

    val vibrateOnKeyPressState =
        remember {
            mutableStateOf(
                PreferencesHelper.getIsVibrateEnabled(context, language),
            )
        }

    val soundOnKeyPressState =
        remember {
            mutableStateOf(
                PreferencesHelper.getIsSoundEnabled(context, language),
            )
        }

    val periodAndCommaState =
        remember {
            mutableStateOf(
                PreferencesHelper.getEnablePeriodAndCommaABC(context, language),
            )
        }

    val wordByWordDeletionState =
        remember {
            mutableStateOf(
                PreferencesHelper.getIsWordByWordDeletionEnabled(context, language),
            )
        }

    val disableSwipeAltKeysState =
        remember {
            mutableStateOf(
                PreferencesHelper.getHoldKeyStyle(context, language),
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
                    onCurrencySelect = onCurrencySelect,
                ),
        )

    // Create functionality settings object
    val functionalitySettings =
        FunctionalitySettings(
            periodOnDoubleTapState = periodOnDoubleTapState.value,
            onTogglePeriodOnDoubleTap = { isEnabled ->
                periodOnDoubleTapState.value = isEnabled
                PreferencesHelper.setPeriodOnSpaceBarDoubleTapPreference(
                    context,
                    language,
                    isEnabled,
                )
            },
            emojiSuggestionsState = emojiSuggestionsState.value,
            onToggleEmojiSuggestions = { isEnabled ->
                emojiSuggestionsState.value = isEnabled
                PreferencesHelper.setEmojiAutoSuggestionsPreference(
                    context,
                    language,
                    isEnabled,
                )
            },
            togglePopUpOnKeyPress = popupOnKeyPressState.value,
            onTogglePopUpOnKeyPress = { isEnabled ->
                popupOnKeyPressState.value = isEnabled
                PreferencesHelper.setShowPopupOnKeypress(
                    context,
                    language,
                    isEnabled,
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
            toggleSoundOnKeyPress = soundOnKeyPressState.value,
            onToggleSoundOnKeyPress = { shouldSoundOnKeyPress ->
                soundOnKeyPressState.value = shouldSoundOnKeyPress
                PreferencesHelper.setSoundOnKeypress(
                    context,
                    language,
                    shouldSoundOnKeyPress,
                )
            },
            wordByWordDeletionState = wordByWordDeletionState.value,
            onToggleWordByWordDeletion = { isEnabled ->
                wordByWordDeletionState.value = isEnabled
                PreferencesHelper.setWordByWordDeletionPreference(
                    context,
                    language,
                    isEnabled,
                )
            },
            disableSwipeAltKeysState = disableSwipeAltKeysState.value,
            onToggleDisableSwipeAltKeys = { disableSwipeAltKeys ->
                disableSwipeAltKeysState.value = disableSwipeAltKeys
                PreferencesHelper.setHoldKeyStyle(
                    context,
                    language,
                    disableSwipeAltKeys,
                )
            },
        )

    val functionalityList =
        ScribeItemList(
            items = getFunctionalityListData(functionalitySettings),
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
 * - Word by word deletion
 *
 * @param settings The functionality settings containing state and callbacks.
 *
 * @return A list of [ScribeItem]s to be shown in the UI.
 */
@Composable
private fun getFunctionalityListData(settings: FunctionalitySettings): List<ScribeItem> {
    val list =
        listOf(
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_double_space_period,
                desc = R.string.app_settings_keyboard_functionality_double_space_period_description,
                state = settings.periodOnDoubleTapState,
                onToggle = settings.onTogglePeriodOnDoubleTap,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_auto_suggest_emoji,
                desc = R.string.app_settings_keyboard_functionality_auto_suggest_emoji_description,
                state = settings.emojiSuggestionsState,
                onToggle = settings.onToggleEmojiSuggestions,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_keypress_vibration,
                desc = R.string.app_settings_keyboard_keypress_vibration_description,
                state = settings.toggleVibrateOnKeyPress,
                onToggle = settings.onToggleVibrateOnKeyPress,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_keypress_sound,
                desc = R.string.app_settings_keyboard_keypress_sound_description,
                state = settings.toggleSoundOnKeyPress,
                onToggle = settings.onToggleSoundOnKeyPress,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_popup_on_keypress,
                desc = R.string.app_settings_keyboard_functionality_popup_on_keypress_description,
                state = settings.togglePopUpOnKeyPress,
                onToggle = settings.onTogglePopUpOnKeyPress,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_delete_word_by_word,
                desc = R.string.app_settings_keyboard_functionality_delete_word_by_word_description,
                state = settings.wordByWordDeletionState,
                onToggle = settings.onToggleWordByWordDeletion,
            ),
            ScribeItem.SwitchItem(
                title = R.string.app_settings_keyboard_functionality_disable_swipe_alt_keys,
                desc = R.string.app_settings_keyboard_functionality_disable_swipe_alt_keys_description,
                state = settings.disableSwipeAltKeysState,
                onToggle = settings.onToggleDisableSwipeAltKeys,
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
    onCurrencySelect: () -> Unit,
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
    list.add(
        ScribeItem.ClickableItem(
            title = R.string.app_settings_keyboard_layout_default_currency,
            desc = R.string.app_settings_keyboard_layout_default_currency_description,
            action = {
                Log.d("Navigation", "onCurrencySelect clicked")
                onCurrencySelect()
            },
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
