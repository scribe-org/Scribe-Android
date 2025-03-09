// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The Select Languages subpage is for selecting the translation source language.
 */
package be.scri.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen

@Composable
fun SelectTranslationSourceLanguageScreen(
    currentLanguage: String,
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    var selectedLanguage =
        remember {
            mutableStateOf(sharedPref.getString("translation_source_$currentLanguage", "English") ?: "English")
        }

    val scrollState = rememberScrollState()
    val options =
        listOf("English", "German", "French", "Spanish", "Italian", "Russian", "Portuguese", "Swedish")
            .filterNot { it == getDisplayLanguageName(currentLanguage) }
    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_settings_keyboard_translation_select_source_title),
        lastPage = stringResource(id = getLanguageStringFromi18n(currentLanguage)),
        onBackNavigation = onBackNavigation,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Text(
                text = stringResource(R.string.app_settings_keyboard_translation_select_source_caption),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage.value = option
                                        sharedPref.edit { putString("translation_source_$currentLanguage", option) }
                                    }.padding(vertical = 5.dp, horizontal = 8.dp),
                        ) {
                            Text(
                                text = getDisplayLanguageName(option),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = (option == selectedLanguage.value),
                                onClick = {
                                    selectedLanguage.value = option
                                    sharedPref.edit { putString("translation_source_$currentLanguage", option) }
                                },
                            )
                        }

                        if (index < options.lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getDisplayLanguageName(language: String): String =
    when (language) {
        "English" -> stringResource(R.string.app__global_english)
        "German" -> stringResource(R.string.app__global_german)
        "French" -> stringResource(R.string.app__global_french)
        "Spanish" -> stringResource(R.string.app__global_spanish)
        "Italian" -> stringResource(R.string.app__global_italian)
        "Russian" -> stringResource(R.string.app__global_russian)
        "Portuguese" -> stringResource(R.string.app__global_portuguese)
        "Swedish" -> stringResource(R.string.app__global_swedish)
        else -> language
    }
