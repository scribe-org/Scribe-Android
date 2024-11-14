package be.scri.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.LayoutDirection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.layoutDirection
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentMainBinding
import java.util.Locale

class MainFragment : ScribeFragment("Main") {
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
        binding.cardView.setOnClickListener {
            openKeyboardSettings()
        }
        (requireActivity() as MainActivity).setActionBarVisibility(false)
        applyUserDarkModePreference()
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                getParentFragmentManager().popBackStack()
            }
        (requireActivity() as MainActivity).setActionBarButtonVisibility(false)
        callback.isEnabled = true
        moveCorner()
        return binding.root
    }

    private fun moveCorner() {
        val cornerTriangleImageView: ImageView = binding.cornerTriangle
        if (Locale.getDefault().layoutDirection == LayoutDirection.RTL) {
            cornerTriangleImageView.scaleX = -1f
        }
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

    private fun openKeyboardSettings() {
        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_main", R.string.app_installation_app_hint)
    }
}
