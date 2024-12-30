/**
 * Composable UI screen that allows users to customize language-specific keyboard settings, including toggles for period on double-tap, emoji suggestions, and accent character handling.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.scri.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.helpers.PreferencesHelper
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LanguageSettingsScreen(
    language: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    val periodOnDoubleTapState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    "period_on_double_tap_$language",
                    false,
                ),
            )
        }
    val emojiSuggestionsState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    "emoji_suggestions_$language",
                    true,
                ),
            )
        }
    val disableAccentCharacterState =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(
                    "disable_accent_characters_$language",
                    false,
                ),
            )
        }
    val periodAndCommaState =
        remember {
            if (!sharedPref.contains("period_and_comma_$language")) {
                sharedPref.edit().putBoolean("period_and_comma_$language", true).apply()
            }
            mutableStateOf(
                sharedPref.getBoolean(
                    "period_and_comma_$language",
                    true,
                ),
            )
        }

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
                ),
        )

    Scaffold(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Column {
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
        }
    }
}

@Composable
private fun getFunctionalityListData(
    periodOnDoubleTapState: Boolean,
    onTogglePeriodOnDoubleTap: (Boolean) -> Unit,
    emojiSuggestionsState: Boolean,
    onToggleEmojiSuggestions: (Boolean) -> Unit,
): List<ScribeItem> {
    val list =
        listOf(
            ScribeItem.SwitchItem(
                title =
                    stringResource(
                        R.string.app_settings_keyboard_functionality_double_space_period,
                    ),
                desc =
                    stringResource(
                        R.string.app_settings_keyboard_functionality_double_space_period_description,
                    ),
                state = periodOnDoubleTapState,
                onToggle = onTogglePeriodOnDoubleTap,
            ),
            ScribeItem.SwitchItem(
                title =
                    stringResource(
                        R.string.app_settings_keyboard_functionality_auto_suggest_emoji,
                    ),
                desc =
                    stringResource(
                        R.string.app_settings_keyboard_functionality_auto_suggest_emoji_description,
                    ),
                state = emojiSuggestionsState,
                onToggle = onToggleEmojiSuggestions,
            ),
        )

    return list
}

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
                    title =
                        stringResource(
                            R.string.app_settings_keyboard_layout_disable_accent_characters,
                        ),
                    desc =
                        stringResource(
                            R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                        ),
                    state = toggleDisableAccentCharacter,
                    onToggle = onToggleDisableAccentCharacter,
                ),
            )
        }
    }

    list.add(
        ScribeItem.SwitchItem(
            title =
                stringResource(
                    R.string.app_settings_keyboard_layout_period_and_comma,
                ),
            desc =
                stringResource(
                    R.string.app_settings_keyboard_layout_period_and_comma_description,
                ),
            state = togglePeriodAndCommaState,
            onToggle = onTogglePeriodAndComma,
        ),
    )

    return list
}
