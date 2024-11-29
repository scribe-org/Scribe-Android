package be.scri.fragments

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentLanguageSettingsBinding
import be.scri.helpers.CustomAdapter
import be.scri.helpers.PreferencesHelper
import be.scri.models.SwitchItem
import be.scri.ui.screens.LanguageSettingsScreen
import be.scri.ui.screens.ThirdPartyScreen
import be.scri.ui.theme.ScribeTheme

@Suppress("LongMethod")
class LanguageSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setActionBarButtonFunction(
            ACTION_BAR_BUTTON_INDEX,
            R.string.app_settings_title,
        )
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                viewpager.setCurrentItem(ACTION_BAR_BUTTON_INDEX, true)
                (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                (requireActivity() as MainActivity).setActionBarVisibility(false)
            }
        (requireActivity() as MainActivity).setActionBarVisibility(true)
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
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                    val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
                    (requireActivity() as MainActivity).setActionBarVisibility(false)
                    if (viewpager.currentItem == ACTION_BAR_BUTTON_INDEX) {
                        viewpager.setCurrentItem(ACTION_BAR_BUTTON_INDEX, true)
                        frameLayout.visibility = View.GONE
                        (requireActivity() as MainActivity).setActionBarVisibility(false)
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

        return ComposeView(requireContext()).apply {
            setContent {
                ScribeTheme(
                    useDarkTheme =
                    PreferencesHelper.getUserDarkModePreference(requireContext())
                        == AppCompatDelegate.MODE_NIGHT_YES,
                ) {
                    LanguageSettingsScreen(
                        language = arguments?.getString("LANGUAGE_EXTRA") ?: "",
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
        val language = arguments?.getString("LANGUAGE_EXTRA") ?: return
        val titleInt = getLanguageStringFromi18n(language)
        (requireActivity() as MainActivity).setActionBarTitle(titleInt)
        (requireActivity() as MainActivity).showFragmentContainer()
        (requireActivity() as MainActivity).setActionBarButtonVisibility(true)
        val mainActivity = requireActivity() as MainActivity
        val actionBarButtonIndex = ACTION_BAR_BUTTON_INDEX
        val titleResId = R.string.app_settings_title
        mainActivity.setActionBarButtonFunction(
            actionBarButtonIndex,
            titleResId,
        )
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            (requireActivity() as MainActivity).setActionBarButtonVisibility(false)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getLanguageStringFromi18n(language: String): Int {
        val languageMap =
            mapOf(
                "German" to R.string.app__global_german,
                "French" to R.string.app__global_french,
                "Spanish" to R.string.app__global_spanish,
                "Italian" to R.string.app__global_italian,
                "Russian" to R.string.app__global_russian,
                "Portuguese" to R.string.app__global_portuguese,
                "Swedish" to R.string.app__global_swedish,
            )
        return languageMap[language] ?: R.string.app__global_english
    }

    companion object {
        private const val ACTION_BAR_BUTTON_INDEX = 3
    }
}
