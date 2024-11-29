import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
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
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

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

    val appSettingsItemList =
        listOf(
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
                },
            ),
            ScribeItem.SwitchItem(
                title = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress),
                desc = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress_description),
                state = popupOnKeypress.value,
                onToggle = { shouldPopUpOnKeypress ->
                    popupOnKeypress.value = shouldPopUpOnKeypress
                    PreferencesHelper.setShowPopupOnKeypress(context, shouldPopUpOnKeypress)
                },
            ),
        )

    val installedKeyboardList =
        getKeyboardLanguages(context).map { language ->
            ScribeItem.ClickableItem(
                title = getLocalizedLanguageName(context, language),
                desc = null,
                action = {
                    context.navigateToFragment(language)
                },
            )
        }

    LazyColumn(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(bottom = 60.dp),
    ) {
        item {
            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_settings_menu_title),
                cardItemsList = ScribeItemList(appSettingsItemList),
            )
        }

        item {
            if (isKeyboardInstalled) {
                ItemCardContainerWithTitle(
                    title = stringResource(R.string.app_settings_keyboard_title),
                    cardItemsList = ScribeItemList(installedKeyboardList),
                    isDivider = true,
                    modifier =
                        Modifier
                            .padding(top = 8.dp),
                )
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
