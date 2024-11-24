import android.annotation.SuppressLint
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.PreferencesHelper
import be.scri.ui.screens.Dimensions

@SuppressLint("ComposeModifierMissing")
@Composable
fun SettingsScreen(
    isUserDarkMode: Boolean,
    isKeyboardInstalled: Boolean,
    onLanguageSelect: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onInstallKeyboard: () -> Unit,
) {
    val context = LocalContext.current
    val vibrateOnKeypress =
        remember {
            mutableStateOf(
                context
                    .getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                    .getBoolean("vibrate_on_keypress", false),
            )
        }
    val popupOnKeypress =
        remember {
            mutableStateOf(
                context
                    .getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                    .getBoolean("show_popup_on_keypress", false),
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = stringResource(R.string.app_settings_menu_title),
            color = colorResource(R.color.app_text_color),
            fontWeight = FontWeight.Bold,
            fontSize = Dimensions.TextSizeLarge,
            modifier = Modifier.padding(start = 20.dp, top = Dimensions.PaddingLarge, bottom = Dimensions.PaddingSmall),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(colorResource(R.color.card_view_color)),
            ) {
                item {
                    SettingItem(
                        title = stringResource(id = R.string.app_settings_menu_app_language),
                        description = stringResource(id = R.string.app_settings_menu_app_language_description),
                        onClick = onLanguageSelect,
                    )
                }
                item {
                    SwitchSettingItem(
                        title = stringResource(id = R.string.app_settings_menu_app_color_mode),
                        description = stringResource(id = R.string.app_settings_menu_app_color_mode_description),
                        isChecked = isUserDarkMode,
                        onCheckedChange = { isDarkMode ->
                            onDarkModeChange(isDarkMode)
                        },
                    )
                }
                item {
                    SwitchSettingItem(
                        title = stringResource(id = R.string.app_settings_keyboard_keypress_vibration),
                        description = stringResource(id = R.string.app_settings_keyboard_keypress_vibration_description),
                        isChecked = vibrateOnKeypress.value,
                        onCheckedChange = { shouldVibrateOnKeypress ->
                            vibrateOnKeypress.value = shouldVibrateOnKeypress
                            PreferencesHelper.setVibrateOnKeypress(context, shouldVibrateOnKeypress)
                        },
                    )
                }
                item {
                    SwitchSettingItem(
                        title = stringResource(id = R.string.app_settings_keyboard_functionality_popup_on_keypress),
                        description = stringResource(id = R.string.app_settings_keyboard_functionality_popup_on_keypress_description),
                        isChecked = popupOnKeypress.value,
                        onCheckedChange = { shouldPopUpOnKeypress ->
                            popupOnKeypress.value = shouldPopUpOnKeypress
                            PreferencesHelper.setShowPopupOnKeypress(context, shouldPopUpOnKeypress)
                        },
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 10.dp,
                        start = 10.dp,
                        end = 12.dp,
                        bottom = 10.dp,
                    ),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                languages.forEach { language ->
                    LanguageItem(
                        language = getLocalizedLanguageName(context, language),
                        onClick = {
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallKeyboardButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.PaddingLarge)
                .padding(10.dp),
        shape = RoundedCornerShape(Dimensions.PaddingLarge),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(R.color.corner_polygon_color),
            ),
    ) {
        Text(
            text = stringResource(R.string.app_settings_button_install_keyboards),
            fontSize = Dimensions.TextSizeLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.button_text_color),
            modifier = Modifier.padding(vertical = Dimensions.PaddingLarge),
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.headlineSmall,
            )
            Image(
                painter = painterResource(R.drawable.right_arrow),
                modifier = Modifier.padding(start = 8.dp),
                contentDescription = "Right Arrow",
            )
        }
        Text(
            text = description,
            fontSize = 13.sp,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.headlineSmall,
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            text = description,
            fontSize = 13.sp,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun LanguageItem(
    language: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Text(
            text = language,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun getLocalizedLanguageName(
    context: Context,
    language: String,
): String {
    val resourceId =
        when (language) {
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
