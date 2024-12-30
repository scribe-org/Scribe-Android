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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.PreferencesHelper
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.theme.ScribeTheme
import com.google.android.material.bottomnavigation.BottomNavigationView

class ThirdPartyFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                viewpager.setCurrentItem(2, true)
                (requireActivity() as MainActivity).setActionBarVisibility(false)
            }
        (requireActivity() as MainActivity).setActionBarButtonFunction(2, R.string.app_about_title)
        (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_legal_privacy_policy)
        (requireActivity() as MainActivity).setActionBarButtonVisibility(true)
        (requireActivity() as MainActivity).setActionBarVisibility(true)
        (requireActivity() as MainActivity)
            .supportActionBar
            ?.customView
            ?.findViewById<Button>(R.id.button)
            ?.text = getString(R.string.app_about_title)
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.showFragmentContainer()
        mainActivity.setActionBarTitle(R.string.app_about_legal_third_party)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val viewpager = mainActivity.findViewById<ViewPager2>(R.id.view_pager)
                    val frameLayout = mainActivity.findViewById<ViewGroup>(R.id.fragment_container)
                    mainActivity.setActionBarTitle(R.string.app_about_title)
                    mainActivity.setActionBarButtonVisibility(false)
                    mainActivity.setActionBarVisibility(false)

                    if (viewpager.currentItem == 2) {
                        viewpager.setCurrentItem(2, true)
                        frameLayout.visibility = View.GONE
                    } else {
                        if (parentFragmentManager.backStackEntryCount > 0) {
                            parentFragmentManager.popBackStack()
                        } else {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            },
        )

        return ComposeView(requireContext()).apply {
            setContent {
                ScribeTheme(
                    useDarkTheme =
                        PreferencesHelper.getUserDarkModePreference(requireContext())
                            == AppCompatDelegate.MODE_NIGHT_YES,
                ) {
                    ThirdPartyScreen(
                        bottomSpacerHeight =
                            mainActivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).height,
                    )
                }
            }
        }
    }
}
