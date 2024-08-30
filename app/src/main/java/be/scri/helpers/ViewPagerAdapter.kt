package be.scri.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import be.scri.activities.MainActivity
import be.scri.fragments.AboutFragment
import be.scri.fragments.MainFragment
import be.scri.fragments.SettingsFragment

class ViewPagerAdapter(fragment: MainActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainFragment()
            1 -> SettingsFragment()
            2 -> AboutFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
