import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import be.scri.R
import be.scri.fragments.LanguageSettingsFragment
import be.scri.helpers.PreferencesHelper
import be.scri.ui.common.components.ItemsCardContainer
import be.scri.ui.model.ScribeItem
import be.scri.ui.model.ScribeItemList

@Composable
fun SettingsScreen(
    isUserDarkMode: Boolean,
    isKeyboardInstalled: Boolean,
    onLanguageSelect: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onInstallKeyboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var languages by remember(lifecycleOwner) {
        mutableStateOf(getKeyboardLanguages(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    languages = getKeyboardLanguages(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

    val appSettingsItemList = listOf(
        ScribeItem.ClickableItem(
            title = stringResource(R.string.app_settings_menu_app_language),
            desc = stringResource(R.string.app_settings_menu_app_language_description),
            action = onLanguageSelect,
        ),
        ScribeItem.SwitchItem(
            title = stringResource(R.string.app_settings_menu_app_color_mode),
            desc = stringResource(R.string.app_settings_menu_app_color_mode_description),
            state = isUserDarkMode,
            onToggle = { isUserDarkMode1 ->
                onDarkModeChange(isUserDarkMode1)
            },
        ),
        ScribeItem.SwitchItem(
            title = stringResource(R.string.app_settings_keyboard_keypress_vibration),
            desc = stringResource(R.string.app_settings_keyboard_keypress_vibration_description),
            state = vibrateOnKeypress.value,
            onToggle = { shouldVibrateOnKeypress ->
                vibrateOnKeypress.value = shouldVibrateOnKeypress
                PreferencesHelper.setVibrateOnKeypress(context, shouldVibrateOnKeypress)
            }
        ),
        ScribeItem.SwitchItem(
            title = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress),
            desc = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress_description),
            state = popupOnKeypress.value,
            onToggle = { shouldPopUpOnKeypress ->
                popupOnKeypress.value = shouldPopUpOnKeypress
                PreferencesHelper.setShowPopupOnKeypress(context, shouldPopUpOnKeypress)
            }
        )
    )


    LazyColumn(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.app_settings_menu_title),
                color = colorResource(R.color.app_text_color),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier =
                    Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp,
                    ),
            )
        }

        item {
            ItemsCardContainer(
                cardItemsList = ScribeItemList(
                    items = appSettingsItemList
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )
        }

        item {
            if (isKeyboardInstalled) {
                Text(
                    text = stringResource(id = R.string.app_settings_keyboard_title),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp,
                            ),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column {
                        languages.forEachIndexed { index, language ->
                            LanguageItem(
                                language = getLocalizedLanguageName(context, language),
                                onClick = { context.navigateToFragment(language) },
                                isLastElement = index == languages.size - 1,
                            )
                        }
                    }
                }
            } else {
                InstallKeyboardButton(onInstallKeyboard)
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
                .padding(vertical = 16.dp)
                .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(R.color.corner_polygon_color),
            ),
    ) {
        Text(
            text = stringResource(R.string.app_settings_button_install_keyboards),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.button_text_color),
            modifier = Modifier.padding(vertical = 8.dp),
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
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.bodyMedium,
            )
            Image(
                painter = painterResource(R.drawable.right_arrow),
                modifier =
                    Modifier
                        .padding(start = 6.dp)
                        .size(14.dp),
                contentDescription = "Right Arrow",
            )
        }
        Text(
            text = description,
            fontSize = 12.sp,
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
    val checkedThumbColor = colorResource(R.color.switch_thumb_selector_color_true)
    val uncheckedThumbColor = colorResource(R.color.switch_thumb_selector_color_false)
    val checkedTrackColor = colorResource(R.color.switch_selector_color)
    val uncheckedTrackColor = colorResource(R.color.switch_selector_color_false)

    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                interactionSource = null,
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier =
                    Modifier
                        .width(51.dp)
                        .height(31.dp),
                thumbContent = {
                    Box(
                        modifier =
                            Modifier
                                .size(27.dp)
                                .background(
                                    if (isChecked) {
                                        colorResource(R.color.switch_thumb_selector_color_true)
                                    } else {
                                        colorResource(R.color.switch_thumb_selector_color_false)
                                    },
                                    shape = CircleShape,
                                ),
                    )
                },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = checkedThumbColor,
                        uncheckedThumbColor = uncheckedThumbColor,
                        checkedTrackColor = checkedTrackColor,
                        uncheckedTrackColor = uncheckedTrackColor,
                        uncheckedBorderColor = Color.Transparent,
                    ),
            )
        }
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun LanguageItem(
    language: String,
    onClick: (String) -> Unit,
    isLastElement: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = { onClick(language) })
                .padding(bottom = 15.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
        ) {
            Text(
                text = language,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.bodyMedium,
            )

            Image(
                painter = painterResource(R.drawable.right_arrow),
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .size(16.dp),
                contentDescription = "Right Arrow",
            )
        }
        if (!isLastElement) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(top = 8.dp),
            )
        }
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

fun Context.navigateToFragment(language: String) {
    val fragmentTransaction = (this as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()

    val fragment =
        LanguageSettingsFragment().apply {
            arguments =
                Bundle().apply {
                    putString("LANGUAGE_EXTRA", language)
                }
        }
    fragmentTransaction?.replace(R.id.fragment_container, fragment)
    fragmentTransaction?.addToBackStack(null)
    fragmentTransaction?.commit()
}
