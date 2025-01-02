/**
 * This files handles the state and business logic for the settings screen.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package be.scri.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.scri.helpers.PreferencesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    fun setVibrateOnKeypress(
        context: Context,
        value: Boolean,
    ) {
        _vibrateOnKeypress.value = value
        PreferencesHelper.setVibrateOnKeypress(context, value)
    }

    fun setPopupOnKeypress(
        context: Context,
        value: Boolean,
    ) {
        _popupOnKeypress.value = value
        PreferencesHelper.setShowPopupOnKeypress(context, value)
    }

    fun setLightDarkMode(value: Boolean) {
        _isUserDarkMode.value = value
    }
}
