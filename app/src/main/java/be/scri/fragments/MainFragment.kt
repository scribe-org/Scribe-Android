package be.scri.fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentMainBinding
import be.scri.dialogs.ConfirmationAdvancedDialog

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
//        binding.scribeKey.setOnClickListener {
//            (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
//        }
        binding.keyboardSettings.setOnClickListener {
            openKeyboardSettings()
        }

        (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
        applyUserDarkModePreference()
        if (!isKeyboardEnabled()) {
            ConfirmationAdvancedDialog(
                requireActivity(),
                messageId = R.string.redirection_note,
                positive = R.string.ok,
                negative = 0,
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
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                getParentFragmentManager().popBackStack()
            }
        (requireActivity() as MainActivity).setActionBarButtonInvisible()
        callback.isEnabled = true
        return binding.root
    }

    private fun applyUserDarkModePreference() {
        val sharedPref = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode =
            resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        Log.i("my-tag", currentNightMode.toString())
        Log.i("my-tag", Configuration.UI_MODE_NIGHT_YES.toString())
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        Log.i("my-tag", isSystemDarkMode.toString())
        Log.i("my-tag", isUserDarkMode.toString())
        AppCompatDelegate.setDefaultNightMode(
            if (isUserDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            },
        )
        if (isUserDarkMode) {
            binding.keyboardMode.setImageResource(R.drawable.keyboard_dark)
        } else {
            binding.keyboardMode.setImageResource(R.drawable.keyboard_light)
        }
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
                negative = 0,
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

    private fun openKeyboardSettings() {
        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledKeyboards = inputMethodManager.enabledInputMethodList
        return enabledKeyboards.any {
            it.packageName == requireContext().packageName
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
