// SPDX-License-Identifier: GPL-3.0-or-later

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
import androidx.lifecycle.viewmodel.compose.viewModel
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.appcomponents.ConfirmationDialog
import be.scri.ui.screens.download.DataDownloadViewModel

/**
 * The Select Languages subpage is for selecting the translation source language.
 */
@Composable
fun SelectTranslationSourceLanguageScreen(
    currentLanguage: String,
    onBackNavigation: () -> Unit,
    onNavigateToDownloadData: () -> Unit,
    modifier: Modifier = Modifier,
    downloadViewModel: DataDownloadViewModel = viewModel(),
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    var savedLanguage =
        remember {
            mutableStateOf(sharedPref.getString("translation_source_$currentLanguage", "English") ?: "English")
        }
    var selectedLanguage = remember { mutableStateOf(savedLanguage.value) }
    var showDialog = remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val options =
        listOf("English", "French", "German", "Italian", "Portuguese", "Russian", "Spanish", "Swedish")
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
                                        showDialog.value = true
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
                                    showDialog.value = true
                                },
                            )
                            if (showDialog.value) {
                                ConfirmationDialog(
                                    text =
                                        "You’ve changed your source translation language. " +
                                            "Would you like to download new data so that you can translate " +
                                            "from ${selectedLanguage.value}?",
                                    textConfirm = "Download data",
                                    textChange = "Keep ${savedLanguage.value}",
                                    onConfirm = {
                                        // User confirmed - save the new selection permanently.
                                        savedLanguage.value = selectedLanguage.value
                                        sharedPref.edit { putString("translation_source_$currentLanguage", selectedLanguage.value) }
                                        showDialog.value = false
                                        downloadViewModel.handleDownloadAction(savedLanguage.value)
                                        onNavigateToDownloadData()
                                    },
                                    onChange = {
                                        // User cancelled - revert back to old selection.
                                        selectedLanguage.value = savedLanguage.value
                                        showDialog.value = false
                                    },
                                    onDismiss = {
                                        // Dialog dismissed - revert back to old selection.
                                        selectedLanguage.value = savedLanguage.value
                                        showDialog.value = false
                                    },
                                )
                            }
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
        "French" -> stringResource(R.string.app__global_french)
        "German" -> stringResource(R.string.app__global_german)
        "Italian" -> stringResource(R.string.app__global_italian)
        "Portuguese" -> stringResource(R.string.app__global_portuguese)
        "Russian" -> stringResource(R.string.app__global_russian)
        "Spanish" -> stringResource(R.string.app__global_spanish)
        "Swedish" -> stringResource(R.string.app__global_swedish)
        else -> language
    }
