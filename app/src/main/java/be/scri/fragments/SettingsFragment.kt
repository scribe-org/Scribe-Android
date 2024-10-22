package be.scri.fragments

import CustomDividerItemDecoration
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentSettingsBinding
import be.scri.extensions.config
import be.scri.helpers.CustomAdapter
import be.scri.helpers.PreferencesHelper
import be.scri.models.SwitchItem
import be.scri.models.TextItem

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private var isDecorationSet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                getParentFragmentManager().popBackStack()
            }
        (requireActivity() as MainActivity).setActionBarTitle(R.string.app_settings_title)
        (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
        (requireActivity() as MainActivity).setActionBarButtonInvisible()
        callback.isEnabled = true
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.app_settings_title)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        setupRecyclerView2()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        for (inputMethod in enabledInputMethods) {
            if (inputMethod.packageName == "be.scri.debug") {
                setupItemVisibility()
            }
        }

        binding.btnInstall.setOnClickListener {
            Intent(ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }
    }

    private fun setupRecycleView() {
        val recyclerView = binding.recyclerViewSettings
        recyclerView.adapter = CustomAdapter(getFirstRecyclerViewData(), requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.suppressLayout(true)
    }

    private fun getFirstRecyclerViewData(): List<Any> {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        return listOf(
            TextItem(
                R.string.app_settings_menu_app_language,
                image = R.drawable.right_arrow,
                description = getString(R.string.app_settings_menu_app_language_description),
                action = ::selectLanguage,
            ),
            SwitchItem(
                getString(R.string.app_settings_menu_app_color_mode),
                description = getString(R.string.app_settings_menu_app_color_mode_description),
                isChecked = sharedPref.getBoolean("dark_mode", isUserDarkMode),
                action = ({ setLightDarkMode(isDarkMode = true) }),
                action2 = ({ setLightDarkMode(isDarkMode = false) }),
            ),
            SwitchItem(
                getString(R.string.app_settings_keyboard_keypress_vibration),
                description = getString(R.string.app_settings_keyboard_keypress_vibration_description),
                isChecked = requireContext().config.vibrateOnKeypress,
                action = ({
                    PreferencesHelper.setVibrateOnKeypress(requireContext(), shouldVibrateOnKeypress = true)
                }),
                action2 = ({
                    PreferencesHelper.setVibrateOnKeypress(requireContext(), shouldVibrateOnKeypress = false)
                }),
            ),
            SwitchItem(
                getString(R.string.app_settings_keyboard_functionality_popup_on_keypress),
                description = getString(R.string.app_settings_keyboard_functionality_popup_on_keypress_description),
                isChecked = requireContext().config.showPopupOnKeypress,
                action = ({
                    PreferencesHelper.setShowPopupOnKeypress(requireContext(), shouldShowPopupOnKeypress = true)
                }),
                action2 = ({
                    PreferencesHelper.setShowPopupOnKeypress(requireContext(), shouldShowPopupOnKeypress = false)
                }),
            ),
        )
    }

    private fun selectLanguage() {
        val packageName = requireActivity().packageName
        val intent: Intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(ACTION_APP_LOCALE_SETTINGS)
            } else {
                Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    putExtra("android.intent.extra.SHOW_FRAGMENT", "com.android.settings.localepicker.LocaleListEditor")
                }
            }

        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun setupRecyclerView2() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        for (inputMethod in enabledInputMethods) {
            if (inputMethod.packageName == "be.scri.debug") {
                setupItemVisibility()
            }
        }
        val recyclerView = binding.recyclerView2
        val adapter = CustomAdapter(getRecyclerViewElements(), requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.suppressLayout(true)

        if (!isDecorationSet) {
            isDecorationSet = true
            recyclerView.apply {
                addCustomItemDecoration()
            }
        }
    }

    private fun RecyclerView.addCustomItemDecoration() {
        val itemDecoration =
            CustomDividerItemDecoration(
                drawable = getDrawable(requireContext(), R.drawable.rv_divider)!!,
                width = 1,
                marginLeft = 50,
                marginRight = 50,
            )
        addItemDecoration(itemDecoration)
    }

    private fun getRecyclerViewElements(): MutableList<TextItem> {
        val languages = setupKeyboardLanguage()
        val list = mutableListOf<TextItem>()
        for (language in languages) {
            val localizeLanguage: Int =
                when (language) {
                    "English" -> R.string.app__global_english
                    "French" -> R.string.app__global_french
                    "German" -> R.string.app__global_german
                    "Russian" -> R.string.app__global_russian
                    "Spanish" -> R.string.app__global_spanish
                    "Italian" -> R.string.app__global_italian
                    "Portuguese" -> R.string.app__global_portuguese
                    "Swedish" -> R.string.app__global_swedish
                    else -> 0
                }
            list.add(
                TextItem(
                    text = localizeLanguage,
                    image = R.drawable.right_arrow,
                    action = { loadLanguageSettingsFragment(language) },
                    language = language,
                ),
            )
        }
        return list
    }

    private fun loadLanguageSettingsFragment(language: String) {
        val fragment =
            LanguageSettingsFragment().apply {
                arguments =
                    Bundle().apply {
                        putString("LANGUAGE_EXTRA", language)
                    }
            }
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment, "LanguageFragment")
        fragmentTransaction.addToBackStack("LanguageFragment")
        fragmentTransaction.commit()
    }

    private fun setupKeyboardLanguage(): MutableList<String> {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        val result = mutableListOf<String>()

        for (inputMethod in enabledInputMethods) {
            Log.i("MY-TAG", inputMethod.serviceName)
            when (inputMethod.serviceName) {
                "be.scri.services.EnglishKeyboardIME" -> result.add("English")
                "be.scri.services.GermanKeyboardIME" -> result.add("German")
                "be.scri.services.RussianKeyboardIME" -> result.add("Russian")
                "be.scri.services.SpanishKeyboardIME" -> result.add("Spanish")
                "be.scri.services.FrenchKeyboardIME" -> result.add("French")
                "be.scri.services.ItalianKeyboardIME" -> result.add("Italian")
                "be.scri.services.PortugueseKeyboardIME" -> result.add("Portuguese")
                "be.scri.services.SwedishKeyboardIME" -> result.add("Swedish")
            }
        }
        return result
    }

    private fun setLightDarkMode(isDarkMode: Boolean) {
        PreferencesHelper.setLightDarkModePreference(requireContext(), isDarkMode)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        requireActivity().recreate()
    }

    private fun setupItemVisibility() {
        binding.btnInstall.visibility = View.INVISIBLE
        binding.selectLanguage.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_settings", R.string.app_settings_app_hint)
        setupRecyclerView2()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).hideHint()
    }
}
