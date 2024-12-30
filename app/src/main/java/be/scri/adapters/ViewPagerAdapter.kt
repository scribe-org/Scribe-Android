/**
 * Provides a ViewPager2 adapter for managing fragments in the main activity, including Main, Settings, and About fragments.
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
package be.scri.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import be.scri.activities.MainActivity
import be.scri.fragments.AboutFragment
import be.scri.fragments.MainFragment
import be.scri.fragments.SettingsFragment

class ViewPagerAdapter(
    fragment: MainActivity,
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> MainFragment()
            1 -> SettingsFragment()
            2 -> AboutFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
}
