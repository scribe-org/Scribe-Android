package be.scri.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentLanguageSettingsBinding
import be.scri.helpers.CustomAdapter
import be.scri.models.SwitchItem

class LanguageSettingsFragment : Fragment() {
    private var _binding: FragmentLanguageSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
                if (viewpager.currentItem == 3) {
                    viewpager.setCurrentItem(3, true)
                    frameLayout.visibility = View.GONE
                } else {
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }

                (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.app_about_title)
            }
        })
        _binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val language = arguments?.getString("LANGUAGE_EXTRA") ?: return
        (requireActivity() as MainActivity).supportActionBar?.title = language
        (requireActivity() as MainActivity).showFragmentContainer()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
        setupRecyclerView(language)
    }

    private fun setupRecyclerView(language: String) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = CustomAdapter(getRecyclerViewData(language), requireContext())
    }

    private fun getRecyclerViewData(language: String): List<SwitchItem> {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return listOf(
            SwitchItem(
                isChecked = sharedPref.getBoolean("period_on_double_tap_$language", false),
                title = "Double space Periods",
                action = { enablePeriodOnSpaceBarDoubleTap(language) },
                action2 = { disablePeriodOnSpaceBarDoubleTap(language) }
            )
        )
    }

    private fun enablePeriodOnSpaceBarDoubleTap(language: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", true)
        editor.apply()
    }

    private fun disablePeriodOnSpaceBarDoubleTap(language: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", false)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
