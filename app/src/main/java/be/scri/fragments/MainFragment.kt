/**
 * Handles displaying the installation screen and related app content.
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

package be.scri.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.PreferencesHelper
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.theme.ScribeTheme

class MainFragment : ScribeFragment("Main") {
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
                    InstallationScreen()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_main", R.string.app_installation_app_hint)
    }

    fun getDarkModePreference(context: Context): Boolean {
        val mode = PreferencesHelper.getUserDarkModePreference(context)
        return mode == AppCompatDelegate.MODE_NIGHT_YES
    }
}
