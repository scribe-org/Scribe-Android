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
import androidx.navigation.compose.rememberNavController
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.PreferencesHelper
import be.scri.navigation.SettingsNavHost
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
                val navController = rememberNavController()
                val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

                ScribeTheme(
                    useDarkTheme =
                        PreferencesHelper.getUserDarkModePreference(requireContext())
                            == AppCompatDelegate.MODE_NIGHT_YES,
                ) {
                    SettingsNavHost(
                        isUserDarkMode = isSystemDarkMode,
                        navController = navController,
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
//        checkKeyboardInstallation()
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

    override fun onResume() {
        super.onResume()
//        (activity as MainActivity).showHint("hint_shown_settings", R.string.app_settings_app_hint)
    }
}
