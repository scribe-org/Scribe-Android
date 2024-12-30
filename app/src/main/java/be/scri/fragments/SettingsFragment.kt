/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/**
 * Class of methods to manage Scribe's behaviors.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.scri.fragments

import SettingsScreen
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.PreferencesHelper
import be.scri.ui.theme.ScribeTheme

class SettingsFragment : ScribeFragment("Settings") {
    private var isKeyboardInstalled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        setupBackPress()
        setupActionBar()

        return ComposeView(requireContext()).apply {
            setContent {
                val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

                ScribeTheme(
                    useDarkTheme =
                        PreferencesHelper.getUserDarkModePreference(requireContext())
                            == AppCompatDelegate.MODE_NIGHT_YES,
                ) {
                    SettingsScreen(
                        onLanguageSelect = ::selectLanguage,
                        onDarkModeChange = ::setLightDarkMode,
                        onInstallKeyboard = ::navigateToKeyboardSettings,
                        isKeyboardInstalled = isKeyboardInstalled,
                        isUserDarkMode = isSystemDarkMode,
                    )
                }
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        checkKeyboardInstallation()
    }

    private fun setupBackPress() {
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                parentFragmentManager.popBackStack()
            }
        callback.isEnabled = true
    }

    private fun setupActionBar() {
        (requireActivity() as MainActivity).apply {
            setActionBarTitle(R.string.app_settings_title)
            setActionBarVisibility(false)
            setActionBarButtonVisibility(false)
            supportActionBar?.title = getString(R.string.app_settings_title)
        }
    }

    private fun checkKeyboardInstallation() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        isKeyboardInstalled =
            imm.enabledInputMethodList.any {
                it.packageName == "be.scri.debug"
            }
    }

    private fun selectLanguage() {
        val packageName = requireActivity().packageName
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
        startActivity(intent)
    }

    private fun setLightDarkMode(isDarkMode: Boolean) {
        PreferencesHelper.setLightDarkModePreference(requireContext(), isDarkMode)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
        )
        requireActivity().recreate()
    }

    private fun navigateToKeyboardSettings() {
        Intent(ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_settings", R.string.app_settings_app_hint)
        checkKeyboardInstallation()
    }
}
