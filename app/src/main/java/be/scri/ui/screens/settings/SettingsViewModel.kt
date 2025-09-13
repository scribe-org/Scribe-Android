// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** This files handles the state and business logic for the settings screen. */
class SettingsViewModel(
    context: Context,
) : ViewModel() {
    private val _languages = MutableStateFlow<List<String>>(emptyList())
    val languages: StateFlow<List<String>> = _languages

    private val _isKeyboardInstalled = MutableStateFlow(false)
    val isKeyboardInstalled: StateFlow<Boolean> = _isKeyboardInstalled

    private val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val _vibrateOnKeypress =
        MutableStateFlow(sharedPrefs.getBoolean("vibrate_on_keypress", false))
    val vibrateOnKeypress: StateFlow<Boolean> = _vibrateOnKeypress

    private val _popupOnKeypress =
        MutableStateFlow(sharedPrefs.getBoolean("show_popup_on_keypress", false))
    val popupOnKeypress: StateFlow<Boolean> = _popupOnKeypress

    private val _isUserDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val isUserDarkMode: StateFlow<Boolean> = _isUserDarkMode

    private val _holdForAltKeys = MutableStateFlow(sharedPrefs.getBoolean("hold_for_alt_keys", false))
    val holdForAltKeys: StateFlow<Boolean> = _holdForAltKeys

    init {
        viewModelScope.launch { refreshSettings(context) }
    }

    /**
     * Refreshes the settings by reloading the current list of available keyboard languages and
     * checking whether the custom keyboard is installed.
     *
     * @param context The context used to access system services.
     */
    fun refreshSettings(context: Context) {
        _languages.value = SettingsUtil.getKeyboardLanguages(context)
        _isKeyboardInstalled.value = SettingsUtil.checkKeyboardInstallation(context)
    }

    /**
     * Updates the UI theme mode based on the user's preference.
     *
     * @param value True to enable dark mode, false for light mode.
     */
    fun setLightDarkMode(value: Boolean) {
        _isUserDarkMode.value = value
    }
}
