package be.scri.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentLanguageSettingsBinding
import be.scri.helpers.CustomAdapter
import be.scri.helpers.PreferencesHelper
import be.scri.models.SwitchItem

@Suppress("LongMethod")
class LanguageSettingsFragment : Fragment() {
    private var _binding: FragmentLanguageSettingsBinding? = null
    val binding get() = _binding!!

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
//        val mainActivity = requireActivity() as MainActivity
//        mainActivity.setActionBarButtonFunction(
//            ACTION_BAR_BUTTON_INDEX,
//            R.string.app_settings_title,
//        )
//        val callback =
//            requireActivity().onBackPressedDispatcher.addCallback(this) {
//                viewpager.setCurrentItem(ACTION_BAR_BUTTON_INDEX, true)
//                (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
//                (requireActivity() as MainActivity).setActionBarVisibility(false)
//            }
//        (requireActivity() as MainActivity).setActionBarVisibility(true)
//        (requireActivity() as MainActivity)
//            .supportActionBar
//            ?.customView
//            ?.findViewById<Button>(R.id.button)
//            ?.text = getString(R.string.app_settings_title)
//
//        callback.isEnabled = true
//    }

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
        setupRecyclerView(language)
    }

    private fun setupRecyclerView(language: String) {
        binding.functionalityRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.functionalityRecyclerView.adapter =
            CustomAdapter(
                getFunctionalityRecyclerViewData(language),
                requireContext(),
            )

        binding.layoutRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.layoutRecyclerView.adapter = CustomAdapter(getLayoutRecyclerViewData(language), requireContext())
    }

    private fun getFunctionalityRecyclerViewData(language: String): List<SwitchItem> {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val list = mutableListOf<SwitchItem>()
        list.add(
            SwitchItem(
                isChecked = sharedPref.getBoolean("period_on_double_tap_$language", false),
                title = getString(R.string.app_settings_keyboard_functionality_double_space_period),
                description = getString(R.string.app_settings_keyboard_functionality_double_space_period_description),
                action = {
                    PreferencesHelper.setPeriodOnSpaceBarDoubleTapPreference(requireContext(), language, true)
                },
                action2 = {
                    PreferencesHelper.setPeriodOnSpaceBarDoubleTapPreference(requireContext(), language, false)
                },
            ),
        )
        list.add(
            SwitchItem(
                isChecked = sharedPref.getBoolean("emoji_suggestions_$language", true),
                title = getString(R.string.app_settings_keyboard_functionality_auto_suggest_emoji),
                description = getString(R.string.app_settings_keyboard_functionality_auto_suggest_emoji_description),
                action = {
                    PreferencesHelper.setEmojiAutoSuggestionsPreference(requireContext(), language, true)
                },
                action2 = {
                    PreferencesHelper.setEmojiAutoSuggestionsPreference(requireContext(), language, false)
                },
            ),
        )
        return list
    }

    private fun getLayoutRecyclerViewData(language: String): List<SwitchItem> {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val list = mutableListOf<SwitchItem>()
        when (language) {
            "German" -> {
                list.add(
                    SwitchItem(
                        isChecked =
                            sharedPref.getBoolean(
                                "disable_accent_character_$language",
                                false,
                            ),
                        title =
                            getString(
                                R.string.app_settings_keyboard_layout_disable_accent_characters,
                            ),
                        description =
                            getString(
                                R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                            ),
                        action = {
                            PreferencesHelper.setAccentCharacterPreference(requireContext(), language, true)
                        },
                        action2 = {
                            PreferencesHelper.setAccentCharacterPreference(requireContext(), language, false)
                        },
                    ),
                )
            }

            "Swedish" -> {
                list.add(
                    SwitchItem(
                        isChecked =
                            sharedPref.getBoolean(
                                "disable_accent_character_$language",
                                false,
                            ),
                        title =
                            getString(
                                R.string.app_settings_keyboard_layout_disable_accent_characters,
                            ),
                        description =
                            getString(
                                R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                            ),
                        action = {
                            PreferencesHelper.setAccentCharacterPreference(requireContext(), language, true)
                        },
                        action2 = {
                            PreferencesHelper.setAccentCharacterPreference(requireContext(), language, false)
                        },
                    ),
                )
            }

            "Spanish" -> {
                list.add(
                    SwitchItem(
                        isChecked =
                            sharedPref.getBoolean(
                                "disable_accent_character_$language",
                                false,
                            ),
                        title =
                            getString(
                                R.string.app_settings_keyboard_layout_disable_accent_characters,
                            ),
                        description =
                            getString(
                                R.string.app_settings_keyboard_layout_disable_accent_characters_description,
                            ),
                        action = {
                            PreferencesHelper.setAccentCharacterPreference(requireContext(), language, true)
                        },
                        action2 = {
                            PreferencesHelper.setAccentCharacterPreference(requireContext(), language, false)
                        },
                    ),
                )
            }
        }

        list.add(
            SwitchItem(
                isChecked = sharedPref.getBoolean("period_and_comma_$language", false),
                title = getString(R.string.app_settings_keyboard_layout_period_and_comma),
                description = getString(R.string.app_settings_keyboard_layout_period_and_comma_description),
                action = { PreferencesHelper.setCommaAndPeriodPreference() },
                action2 = { PreferencesHelper.setCommaAndPeriodPreference() },
            ),
        )
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getLanguageStringFromi18n(language: String): Int {
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
