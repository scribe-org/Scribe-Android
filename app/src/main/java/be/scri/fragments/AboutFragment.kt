package be.scri.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.HintUtils
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper
import be.scri.ui.screens.AboutScreen
import be.scri.ui.theme.ScribeTheme

class AboutFragment : ScribeFragment("About") {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                getParentFragmentManager().popBackStack()
            }
        callback.isEnabled = true
        (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_title)
        (requireActivity() as MainActivity).setActionBarVisibility(false)
        (requireActivity() as MainActivity).setActionBarButtonVisibility(false)

        return ComposeView(requireContext()).apply {
            setContent {
                ScribeTheme(
                    useDarkTheme =
                        PreferencesHelper.getUserDarkModePreference(requireContext())
                            == AppCompatDelegate.MODE_NIGHT_YES,
                ) {
                    AboutScreen(
                        onWikimediaAndScribeClick = {
                            loadOtherFragment(WikimediaScribeFragment(), "WikimediaScribePage")
                        },
                        onShareScribeClick = { ShareHelper.shareScribe(requireContext()) },
                        onPrivacyPolicyClick = { loadOtherFragment(PrivacyPolicyFragment(), null) },
                        onThirdPartyLicensesClick = { loadOtherFragment(ThirdPartyFragment(), null) },
                        onRateScribeClick = {
                            RatingHelper.rateScribe(requireContext(), activity as MainActivity)
                        },
                        onMailClick = { ShareHelper.sendEmail(requireContext()) },
                        onResetHintsClick = ::resetHints,
                        context = requireContext(),
                    )
                }
            }
        }
    }

    private fun resetHints() {
        HintUtils.resetHints(requireContext())
        (activity as MainActivity).showHint("hint_shown_about", R.string.app_about_app_hint)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_about", R.string.app_about_app_hint)
    }
}
