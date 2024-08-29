package be.scri.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentSettingsBinding
import be.scri.extensions.config
import be.scri.helpers.CustomAdapter
import be.scri.models.SwitchItem
import be.scri.models.TextItem

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            getParentFragmentManager().popBackStack()
        }
        callback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        setupRecyclerView2()
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.app_settings_title)

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
        return listOf(
            TextItem(R.string.app_settings_appSettings_appLanguage, image = R.drawable.right_arrow, action = ::selectLanguage),
            SwitchItem("Dark mode", isChecked = sharedPref.getBoolean("dark_mode", false), action = ::darkMode, action2 = ::lightMode),
            SwitchItem("Vibrate on Keypress", isChecked = requireContext().config.vibrateOnKeypress, action = ::enableVibrateOnKeypress, action2 = ::disableVibrateOnKeypress),
            SwitchItem("Show a popup on keypress", isChecked = requireContext().config.showPopupOnKeypress, action = ::enableShowPopupOnKeypress, action2 = ::disableShowPopupOnKeypress)
        )
    }

    private fun selectLanguage() {
        val packageName = requireActivity().packageName
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
    }

    private fun getRecyclerViewElements(): MutableList<TextItem> {
        val languages = setupKeyboardLanguage()
        val list = mutableListOf<TextItem>()
        for (language in languages) {
            val localizeLanguage: Int = when (language) {
                "English" -> R.string._global_english
                "French" -> R.string._global_french
                "German" -> R.string._global_german
                "Russian" -> R.string._global_russian
                "Spanish" -> R.string._global_spanish
                "Italian" -> R.string._global_italian
                "Portuguese" -> R.string._global_portuguese
                else -> 0
            }
            list.add(
                TextItem(
                    text = localizeLanguage,
                    image = R.drawable.right_arrow,
                    action = { loadLanguageSettingsFragment(language) },
                    language = language
                )
            )
        }
        return list
    }

    private fun loadLanguageSettingsFragment(language: String) {
        val fragment = LanguageSettingsFragment().apply {
            arguments = Bundle().apply {
                putString("LANGUAGE_EXTRA", language)
            }
        }
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.app_about_wikimedia)
    }


    private fun setupKeyboardLanguage(): MutableList<String> {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        val result = mutableListOf<String>()

        for (inputMethod in enabledInputMethods) {
            when (inputMethod.serviceName) {
                "be.scri.services.EnglishKeyboardIME" -> result.add("English")
                "be.scri.services.GermanKeyboardIME" -> result.add("German")
                "be.scri.services.RussianKeyboardIME" -> result.add("Russian")
                "be.scri.services.SpanishKeyboardIME" -> result.add("Spanish")
                "be.scri.services.FrenchKeyboardIME" -> result.add("French")
                "be.scri.services.ItalianKeyboardIME" -> result.add("Italian")
                "be.scri.services.PortugueseKeyboardIME" -> result.add("Portuguese")
            }
        }
        return result
    }

    private fun lightMode() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("dark_mode", false)
        editor.apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        requireActivity().recreate()
    }

    private fun darkMode() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("dark_mode", true)
        editor.apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        requireActivity().recreate()
    }

    private fun enableVibrateOnKeypress() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("vibrate_on_keypress", true)
        editor.apply()
        requireActivity().config.vibrateOnKeypress = true
    }

    private fun disableVibrateOnKeypress() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("vibrate_on_keypress", false)
        editor.apply()
        requireActivity().config.vibrateOnKeypress = false
    }

    private fun enableShowPopupOnKeypress() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("show_popup_on_keypress", true)
        editor.apply()
        requireActivity().config.showPopupOnKeypress = true
    }

    private fun disableShowPopupOnKeypress() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("show_popup_on_keypress", false)
        editor.apply()
        requireActivity().config.showPopupOnKeypress = false
    }

    private fun setupItemVisibility() {
        binding.btnInstall.visibility = View.INVISIBLE
        binding.selectLanguage.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView2()
    }
}
