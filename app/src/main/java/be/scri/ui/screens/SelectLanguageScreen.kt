package be.scri.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import androidx.core.content.edit

@Composable
fun SelectTranslationSourceLanguageScreen(
    currentLanguage: String,
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    var selectedLanguage by remember {
        mutableStateOf(sharedPref.getString("translation_source_$currentLanguage", "English") ?: "English")
    }

    val scrollState = rememberScrollState()
    val options = listOf("English", "German", "French", "Spanish", "Italian", "Russian", "Portuguese", "Swedish")
        .filterNot { it == getDisplayLanguageName(currentLanguage) }
    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_settings_keyboard_translation_select_source_title),
        lastPage = stringResource(id = getLanguageStringFromi18n(currentLanguage)), 
        onBackNavigation = onBackNavigation,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = stringResource(R.string.app_settings_keyboard_translation_select_source_caption),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = option
                                    sharedPref.edit { putString("translation_source_$currentLanguage", option) }
                                }
                                .padding(vertical = 5.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = getDisplayLanguageName(option),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = (option == selectedLanguage),
                                onClick = {
                                    selectedLanguage = option
                                    sharedPref.edit { putString("translation_source_$currentLanguage", option) }
                                }
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
private fun getDisplayLanguageName(language: String): String {
    return when (language) {
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
}
