package be.scri.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.PreferencesHelper
import be.scri.ui.screens.SettingsScreen
import be.scri.ui.theme.ScribeTheme

class SettingsFragment : ScribeFragment("Settings") {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                ScribeTheme(
                    useDarkTheme =
                        PreferencesHelper.getUserDarkModePreference(requireContext())
                            == AppCompatDelegate.MODE_NIGHT_YES,
                ) {
                    SettingsScreen()
                }
            }
        }

    private fun setLightDarkMode(isDarkMode: Boolean) {
        PreferencesHelper.setLightDarkModePreference(requireContext(), isDarkMode)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
        )
        requireActivity().recreate()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_settings", R.string.app_settings_app_hint)
    }
}
