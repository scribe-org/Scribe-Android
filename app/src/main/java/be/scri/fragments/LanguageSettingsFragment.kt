package be.scri.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
        (requireActivity() as MainActivity).setActionBarButtonFunction(3, R.string.app_settings_title)
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                viewpager.setCurrentItem(3, true)
                (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
            }
        (requireActivity() as MainActivity).setActionBarLayoutMargin()
        (requireActivity() as MainActivity)
            .supportActionBar
            ?.customView
            ?.findViewById<Button>(R.id.button)
            ?.text = getString(R.string.app_settings_title)

        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                    val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
                    (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
                    if (viewpager.currentItem == 3) {
                        viewpager.setCurrentItem(3, true)
                        frameLayout.visibility = View.GONE
                        (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
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
            },
        )
        _binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val language = arguments?.getString("LANGUAGE_EXTRA") ?: return
        val titleInt = getLanguageStringFromi18n(language)
        (requireActivity() as MainActivity).setActionBarTitle(titleInt)
        (requireActivity() as MainActivity).showFragmentContainer()
        (requireActivity() as MainActivity).setActionBarButtonVisible()
        (requireActivity() as MainActivity).setActionBarButtonFunction(3, R.string.app_settings_title)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            (requireActivity() as MainActivity).setActionBarButtonInvisible()
            parentFragmentManager
                .beginTransaction()
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
                title = "Double space periods",
                action = { enablePeriodOnSpaceBarDoubleTap(language) },
                action2 = { disablePeriodOnSpaceBarDoubleTap(language) },
            ),
            SwitchItem(
                isChecked = sharedPref.getBoolean("autosuggest_emojis_$language", true),
                title = "Autosuggest emojis",
                action = { enableEmojiAutosuggestions(language) },
                action2 = { disableEmojiAutosuggestions(language) },
            ),
        )
    }

    private fun enablePeriodOnSpaceBarDoubleTap(language: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", true)
        editor.apply()
        Toast.makeText(requireContext(), "$language Period on Double Tap of Space Bar on ", Toast.LENGTH_SHORT).show()
    }

    private fun disablePeriodOnSpaceBarDoubleTap(language: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", false)
        editor.apply()
        Toast.makeText(requireContext(), "$language Period on Double Tap of Space Bar on ", Toast.LENGTH_SHORT).show()
    }

    private fun enableEmojiAutosuggestions(language: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("emoji_suggestions_$language", true)
        editor.apply()
        Toast.makeText(requireContext(), "$language Emoji Autosuggestions on", Toast.LENGTH_SHORT).show()
    }

    private fun disableEmojiAutosuggestions(language: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("emoji_suggestions_$language", false)
        editor.apply()
        Toast.makeText(requireContext(), "$language Emoji Autosuggestions off", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getLanguageStringFromi18n(language: String): Int {
        when (language) {
            "German" -> return R.string._global_german
            "French" -> return R.string._global_french
            "Spanish" -> return R.string._global_spanish
            "Italian" -> return R.string._global_italian
            "Russian" -> return R.string._global_russian
            "Portuguese" -> return R.string._global_portuguese
            "Swedish" -> return R.string._global_swedish
            else -> return R.string._global_english
        }
    }
}
