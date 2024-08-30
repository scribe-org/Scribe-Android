package be.scri.fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import be.scri.R
import be.scri.databinding.FragmentMainBinding
import be.scri.dialogs.ConfirmationAdvancedDialog

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.scribeKey.setOnClickListener {
            (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }
        applyUserDarkModePreference()
        if (!isKeyboardEnabled()) {
            ConfirmationAdvancedDialog(
                requireActivity(),
                messageId = R.string.redirection_note,
                positive = R.string.ok,
                negative = 0
            ) { success ->
                if (success) {
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                } else {
                    requireActivity().finish()
                }
            }
        }
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            getParentFragmentManager().popBackStack()
        }
        callback.isEnabled = true
        return binding.root
    }

    private fun applyUserDarkModePreference() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)



        AppCompatDelegate.setDefaultNightMode(
            if (isUserDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        if (isUserDarkMode != (currentNightMode == Configuration.UI_MODE_NIGHT_YES)) {
            requireActivity().recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isKeyboardEnabled()) {
            ConfirmationAdvancedDialog(
                requireActivity(),
                messageId = R.string.redirection_note,
                positive = R.string.ok,
                negative = 0
            ) { success ->
                if (success) {
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                } else {
                    requireActivity().finish()
                }
            }
        }

    }

    private fun isKeyboardEnabled(): Boolean {
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledKeyboards = inputMethodManager.enabledInputMethodList
        return enabledKeyboards.any {
            it.packageName == requireContext().packageName
        }
    }
}
