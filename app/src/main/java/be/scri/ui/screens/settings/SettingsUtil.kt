package be.scri.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.PreferencesHelper
import be.scri.ui.models.ScribeItem

object SettingsUtil {
    fun checkKeyboardInstallation(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        return imm.enabledInputMethodList.any {
                it.packageName == "be.scri.debug"
            }
    }

    fun setLightDarkMode(isDarkMode: Boolean, context: Context) {
        PreferencesHelper.setLightDarkModePreference(context, isDarkMode)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
        )
    }

    fun showSettingsHint(context: Context) {
        (context as MainActivity).showHint("hint_shown_settings", R.string.app_settings_app_hint)
    }

    fun selectLanguage(context: Context) {
        val packageName = context.packageName
        val intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(ACTION_APP_LOCALE_SETTINGS)
            } else {
                Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    putExtra(
                        "android.intent.extra.SHOW_FRAGMENT",
                        "com.android.settings.localepicker.LocaleListEditor",
                    )
                }
            }
        intent.data = Uri.fromParts("package", packageName, null)
        context.startActivity(intent)
    }

    fun navigateToKeyboardSettings(context: Context) {
        Intent(ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    fun getKeyboardLanguages(context: Context): List<String> {
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

    fun getLocalizedLanguageName(
        context: Context,
        language: String,
    ): Int {
        return when (language) {
            "English" -> R.string.app__global_english
            "French" -> R.string.app__global_french
            "German" -> R.string.app__global_german
            "Russian" -> R.string.app__global_russian
            "Spanish" -> R.string.app__global_spanish
            "Italian" -> R.string.app__global_italian
            "Portuguese" -> R.string.app__global_portuguese
            "Swedish" -> R.string.app__global_swedish
            else -> return R.string.language
        }
    }

    @Composable
    fun getAppSettingsItemList(context: Context): List<ScribeItem> =
        remember {
            listOf(
                ScribeItem.ClickableItem(
                    title = R.string.app_settings_menu_app_language,
                    desc = stringResource(R.string.app_settings_menu_app_language_description),
                    action = {
                        SettingsUtil.selectLanguage(context)
                    },
                ),
                ScribeItem.SwitchItem(
                    title = R.string.app_settings_menu_app_color_mode,
                    desc = stringResource(R.string.app_settings_menu_app_color_mode_description),
                    state = isUserDarkMode,
                    onToggle = { isUserDarkMode1 ->
                        SettingsUtil.setLightDarkMode(isUserDarkMode1, context)
                    },
                ),
                ScribeItem.SwitchItem(
                    title = R.string.app_settings_keyboard_keypress_vibration,
                    desc = stringResource(R.string.app_settings_keyboard_keypress_vibration_description),
                    state = vibrateOnKeypress.value,
                    onToggle = { shouldVibrateOnKeypress ->
                        vibrateOnKeypress.value = shouldVibrateOnKeypress
                        PreferencesHelper.setVibrateOnKeypress(context, shouldVibrateOnKeypress)
                    },
                ),
                ScribeItem.SwitchItem(
                    title = R.string.app_settings_keyboard_functionality_popup_on_keypress,
                    desc = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress_description),
                    state = popupOnKeypress.value,
                    onToggle = { shouldPopUpOnKeypress ->
                        popupOnKeypress.value = shouldPopUpOnKeypress
                        PreferencesHelper.setShowPopupOnKeypress(context, shouldPopUpOnKeypress)
                    },
                ),
            )
        }
}
