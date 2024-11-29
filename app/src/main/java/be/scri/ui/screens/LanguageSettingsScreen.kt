package be.scri.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.PreferencesHelper
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LanguageSettingsScreen(
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    val layoutList = ScribeItemList(
        items = getLayoutListData(context, language, sharedPref)
    )
    val functionalityList = ScribeItemList(
        items = getFunctionalityListData(context, language, sharedPref)
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column {
            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_settings_keyboard_layout_title),
                cardItemsList = layoutList,
            )

            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_settings_keyboard_functionality_title),
                cardItemsList = functionalityList
            )
        }
    }
}


@Composable
private fun getFunctionalityListData(
    context: Context,
    language: String,
    sharePref: SharedPreferences
): List<ScribeItem> {
    val list = listOf(
        ScribeItem.SwitchItem(
            title = stringResource(
                R.string.app_settings_keyboard_functionality_double_space_period
            ),
            desc = stringResource(
                R.string.app_settings_keyboard_functionality_double_space_period_description
            ),
            state = sharePref.getBoolean(
                "period_on_double_tap_$language",
                false,
            ),
            onToggle = { shouldDoubleSpacePeriod ->
                PreferencesHelper.setPeriodOnSpaceBarDoubleTapPreference(
                    context,
                    language,
                    shouldDoubleSpacePeriod
                )
            }
        ),
        ScribeItem.SwitchItem(
            title = stringResource(
                R.string.app_settings_keyboard_functionality_auto_suggest_emoji
            ),
            desc = stringResource(
                R.string.app_settings_keyboard_functionality_auto_suggest_emoji_description
            ),
            state = sharePref.getBoolean(
                "emoji_suggestions_$language",
                false,
            ),
            onToggle = { shouldDoubleSpacePeriod ->
                PreferencesHelper.setEmojiAutoSuggestionsPreference(
                    context,
                    language,
                    shouldDoubleSpacePeriod
                )
            }
        )
    )

    return list
}

@Composable
private fun getLayoutListData(
    context: Context,
    language: String,
    sharePref: SharedPreferences
): List<ScribeItem> {
    val list: MutableList<ScribeItem> = mutableListOf()

    when(language) {
        "German" -> {
            list.add(
                ScribeItem.SwitchItem(
                    title = stringResource(
                        R.string.app_settings_keyboard_layout_disable_accent_characters,
                    ),
                    desc = stringResource(
                        R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                    ),
                    state = sharePref.getBoolean(
                        "disable_accent_characters_$language",
                        false,
                    ),
                    onToggle = { shouldDisableAccentCharacter ->
                        PreferencesHelper.setAccentCharacterPreference(
                            context,
                            language,
                            shouldDisableAccentCharacter
                        )
                    }
                )
            )
        }

        "Swedish" -> {
            list.add(
                ScribeItem.SwitchItem(
                    title = stringResource(
                        R.string.app_settings_keyboard_layout_disable_accent_characters,
                    ),
                    desc = stringResource(
                        R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                    ),
                    state = sharePref.getBoolean(
                        "disable_accent_characters_$language",
                        false,
                    ),
                    onToggle = { shouldDisableAccentCharacter ->
                        PreferencesHelper.setAccentCharacterPreference(
                            context,
                            language,
                            shouldDisableAccentCharacter
                        )
                    }
                )
            )
        }

        "Spanish" -> {
            list.add(
                ScribeItem.SwitchItem(
                    title = stringResource(
                        R.string.app_settings_keyboard_layout_disable_accent_characters,
                    ),
                    desc = stringResource(
                        R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                    ),
                    state = sharePref.getBoolean(
                        "disable_accent_characters_$language",
                        false,
                    ),
                    onToggle = { shouldDisableAccentCharacter ->
                        PreferencesHelper.setAccentCharacterPreference(
                            context,
                            language,
                            shouldDisableAccentCharacter
                        )
                    }
                )
            )
        }
    }

    list.add(
        ScribeItem.SwitchItem(
            title = stringResource(
                R.string.app_settings_keyboard_layout_period_and_comma
            ),
            desc = stringResource(
                R.string.app_settings_keyboard_layout_period_and_comma_description
            ),
            state = sharePref.getBoolean(
                "period_and_comma_$language",
                false,
            ),
            onToggle = {
                PreferencesHelper.setCommaAndPeriodPreference()
            }
        )
    )

    return list
}

