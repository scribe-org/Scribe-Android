import android.annotation.SuppressLint
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@SuppressLint("ComposeModifierMissing")
@Composable
fun SettingsScreen(
    onLanguageSelect: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onInstallKeyboard: () -> Unit,
    isKeyboardInstalled: Boolean,
    defaultDarkMode: Boolean
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isUserDarkMode by remember { mutableStateOf(defaultDarkMode) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_settings_menu_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 26.dp,
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 10.dp
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    SettingItem(
                        title = stringResource(id = R.string.app_settings_menu_app_language),
                        description = stringResource(id = R.string.app_settings_menu_app_language_description),
                        onClick = onLanguageSelect
                    )
                    SwitchSettingItem(
                        title = stringResource(id = R.string.app_settings_menu_app_color_mode),
                        description = stringResource(id = R.string.app_settings_menu_app_color_mode_description),
                        isChecked = isUserDarkMode,
                        onCheckedChange = { isDarkMode ->
                            isUserDarkMode = isDarkMode
                            onDarkModeChange(isDarkMode)
                        }
                    )
                }
            }

            if (isKeyboardInstalled) {
                KeyboardLanguagesSection()
            } else {
                InstallKeyboardButton(onInstallKeyboard)
            }
        }
    }
}

@Composable
private fun KeyboardLanguagesSection() {
    val context = LocalContext.current
    val languages = remember { getKeyboardLanguages(context) }

    Column {
        Text(
            text = stringResource(id = R.string.app_settings_keyboard_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    start = 10.dp,
                    end = 12.dp,
                    bottom = 10.dp
                ),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                languages.forEach { language ->
                    LanguageItem(
                        language = getLocalizedLanguageName(context, language),
                        onClick = { /* Navigate to language settings */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallKeyboardButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(
                start = 13.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_settings_button_install_keyboards),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingItem(title: String, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun LanguageItem(language: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = language,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getLocalizedLanguageName(context: Context, language: String): String {
    val resourceId = when (language) {
        "English" -> R.string.app__global_english
        "French" -> R.string.app__global_french
        "German" -> R.string.app__global_german
        "Russian" -> R.string.app__global_russian
        "Spanish" -> R.string.app__global_spanish
        "Italian" -> R.string.app__global_italian
        "Portuguese" -> R.string.app__global_portuguese
        "Swedish" -> R.string.app__global_swedish
        else -> return language
    }
    return context.getString(resourceId)
}

private fun getKeyboardLanguages(context: Context): List<String> {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.enabledInputMethodList.mapNotNull { inputMethod ->
        when (inputMethod.serviceName) {
            "be.scri.services.EnglishKeyboardIME" -> "English"
            "be.scri.services.GermanKeyboardIME" -> "German"
            "be.scri.services.RussianKeyboardIME" -> "Russian"
            "be.scri.services.SpanishKeyboardIME" -> "Spanish"
            "be.scri.services.FrenchKeyboardIME" -> "French"
            "be.scri.services.ItalianKeyboardIME" -> "Italian"
            "be.scri.services.PortugueseKeyboardIME" -> "Portuguese"
            "be.scri.services.SwedishKeyboardIME" -> "Swedish"
            else -> null
        }
    }
}
