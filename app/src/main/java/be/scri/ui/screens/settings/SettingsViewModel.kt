// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * This files handles the state and business logic for the settings screen.
 */
class SettingsViewModel(
    context: Context,
) : ViewModel() {
    private val _languages = MutableStateFlow<List<String>>(emptyList())
    val languages: StateFlow<List<String>> = _languages

    private val _isKeyboardInstalled = MutableStateFlow(false)
    val isKeyboardInstalled: StateFlow<Boolean> = _isKeyboardInstalled

    private val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val _vibrateOnKeypress = MutableStateFlow(sharedPrefs.getBoolean("vibrate_on_keypress", false))
    val vibrateOnKeypress: StateFlow<Boolean> = _vibrateOnKeypress

    private val _popupOnKeypress = MutableStateFlow(sharedPrefs.getBoolean("show_popup_on_keypress", false))
    val popupOnKeypress: StateFlow<Boolean> = _popupOnKeypress

    private val _isUserDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val isUserDarkMode: StateFlow<Boolean> = _isUserDarkMode

    init {
        viewModelScope.launch {
            refreshSettings(context)
        }
    }

    fun refreshSettings(context: Context) {
        _languages.value = SettingsUtil.getKeyboardLanguages(context)
        _isKeyboardInstalled.value = SettingsUtil.checkKeyboardInstallation(context)
    }

    fun setLightDarkMode(value: Boolean) {
        _isUserDarkMode.value = value
    }
}
