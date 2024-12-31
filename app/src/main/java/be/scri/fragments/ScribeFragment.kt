/**
 * Handles displaying information like app hints and other fragments.
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

import android.util.Log
import androidx.fragment.app.Fragment
import be.scri.R
import be.scri.activities.MainActivity

abstract class ScribeFragment(
    val fragmentName: String,
) : Fragment() {
    override fun onPause() {
        super.onPause()
        (activity as MainActivity).hideHint()
    }

    protected fun loadOtherFragment(
        fragment: Fragment,
        pageName: String?,
    ) {
        try {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            if (pageName != null) {
                fragmentTransaction.replace(R.id.fragment_container, fragment, pageName)
            } else {
                fragmentTransaction.replace(R.id.fragment_container, fragment)
            }
            fragmentTransaction.addToBackStack(pageName)
            fragmentTransaction.commit()
        } catch (e: IllegalStateException) {
            Log.e("${fragmentName}Fragment", "Failed to load fragment", e)
        }
    }
}
