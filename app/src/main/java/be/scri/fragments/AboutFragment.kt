package be.scri.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import be.scri.BuildConfig
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentAboutBinding
import be.scri.extensions.addCustomItemDecoration
import be.scri.helpers.CustomAdapter
import be.scri.helpers.HintUtils
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper
import be.scri.models.ItemsViewModel
import be.scri.ui.screens.AboutScreen
import be.scri.ui.screens.LanguageSettingsScreen
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
                        onWikimediaAndScribeClick = { loadOtherFragment(WikimediaScribeFragment(), "WikimediaScribePage") },
                        onPrivacyPolicyClick = { loadOtherFragment(PrivacyPolicyFragment(), null) },
                        onThirdPartyLicensesClick = { loadOtherFragment(ThirdPartyFragment(), null) },
                        onRateScribeClick = { RatingHelper.rateScribe(requireContext(), activity as MainActivity) },
                        onMailClick = { ShareHelper.sendEmail(requireContext()) },
                        onResetHintsClick = ::resetHints,
                        context = requireContext()
                    )
                }
            }
        }
    }

//    override fun onViewCreated(
//        view: View,
//        savedInstanceState: Bundle?,
//    ) {
//        super.onViewCreated(view, savedInstanceState)
//        setupRecyclerViews()
//    }
//
//    private fun setupRecyclerViews() {
//        val recyclerView1 = binding.recycleView2
//        recyclerView1.layoutManager = LinearLayoutManager(context)
//        recyclerView1.adapter = CustomAdapter(getFirstRecyclerViewData(), requireContext())
//        recyclerView1.suppressLayout(true)
//        recyclerView1.apply {
//            addCustomItemDecoration(requireContext())
//        }
//        val recyclerView2 = binding.recycleView
//        recyclerView2.layoutManager = LinearLayoutManager(context)
//        recyclerView2.adapter = CustomAdapter(getSecondRecyclerViewData(), requireContext())
//        recyclerView2.suppressLayout(true)
//        recyclerView2.apply {
//            addCustomItemDecoration(requireContext())
//        }
//        val recyclerView3 = binding.recycleView3
//        recyclerView3.layoutManager = LinearLayoutManager(context)
//        recyclerView3.adapter = CustomAdapter(getThirdRecyclerViewData(), requireContext())
//        recyclerView3.suppressLayout(true)
//        recyclerView3.apply {
//            addCustomItemDecoration(requireContext())
//        }
//    }
//
//    private fun getFirstRecyclerViewData(): List<Any> =
//        listOf(
//            ItemsViewModel(
//                image = R.drawable.github_logo,
//                text = ItemsViewModel.Text(R.string.app_about_community_github),
//                image2 = R.drawable.external_link,
//                url = "https://github.com/scribe-org/Scribe-Android",
//                activity = null,
//                action = null,
//            ),
//            ItemsViewModel(
//                image = R.drawable.matrix_icon,
//                text = ItemsViewModel.Text(R.string.app_about_community_matrix),
//                image2 = R.drawable.external_link,
//                url = "https://matrix.to/%23/%23scribe_community:matrix.org",
//                activity = null,
//                action = null,
//            ),
//            ItemsViewModel(
//                image = R.drawable.mastodon_svg_icon,
//                text = ItemsViewModel.Text(R.string.app_about_community_mastodon),
//                image2 = R.drawable.external_link,
//                url = "https://wikis.world/@scribe",
//                activity = null,
//                action = null,
//            ),
//            ItemsViewModel(
//                image = R.drawable.share_icon,
//                text = ItemsViewModel.Text(R.string.app_about_community_share_scribe),
//                image2 = R.drawable.external_link,
//                url = null,
//                activity = null,
//                action = ({ ShareHelper.shareScribe(requireContext()) }),
//            ),
//            ItemsViewModel(
//                image = R.drawable.wikimedia_logo_black,
//                text = ItemsViewModel.Text(R.string.app_about_community_wikimedia),
//                image2 = R.drawable.right_arrow,
//                url = null,
//                activity = null,
//                action = ({ loadOtherFragment(WikimediaScribeFragment(), "WikimediaScribePage") }),
//            ),
//        )
//
//    private fun getSecondRecyclerViewData(): List<Any> =
//        listOf(
//            ItemsViewModel(
//                image = R.drawable.star,
//                text = ItemsViewModel.Text(R.string.app_about_feedback_rate_scribe),
//                image2 = R.drawable.external_link,
//                url = null,
//                activity = null,
//                action = ({ RatingHelper.rateScribe(requireContext(), activity as MainActivity) }),
//            ),
//            ItemsViewModel(
//                image = R.drawable.bug_report_icon,
//                text = ItemsViewModel.Text(R.string.app_about_feedback_bug_report),
//                image2 = R.drawable.external_link,
//                url = "https://github.com/scribe-org/Scribe-Android/issues",
//                activity = null,
//                action = null,
//            ),
//            ItemsViewModel(
//                image = R.drawable.mail_icon,
//                text = ItemsViewModel.Text(R.string.app_about_feedback_email),
//                image2 = R.drawable.external_link,
//                url = null,
//                activity = null,
//                action = ({ ShareHelper.sendEmail(requireContext()) }),
//            ),
//            ItemsViewModel(
//                image = R.drawable.bookmark_icon,
//                text = ItemsViewModel.Text(R.string.app_about_feedback_version, BuildConfig.VERSION_NAME),
//                image2 = R.drawable.external_link,
//                url = "https://github.com/scribe-org/Scribe-Android/releases/",
//                activity = null,
//                action = null,
//            ),
//            ItemsViewModel(
//                image = R.drawable.light_bulb_icon,
//                text = ItemsViewModel.Text(R.string.app_about_feedback_app_hints),
//                image2 = R.drawable.counter_clockwise_icon,
//                url = null,
//                activity = null,
//                action = ::resetHints,
//            ),
//        )
//
//    private fun getThirdRecyclerViewData(): List<ItemsViewModel> =
//        listOf(
//            ItemsViewModel(
//                image = R.drawable.shield_lock,
//                text = ItemsViewModel.Text(R.string.app_about_legal_privacy_policy),
//                image2 = R.drawable.right_arrow,
//                url = null,
//                activity = null,
//                action = ({ loadOtherFragment(PrivacyPolicyFragment(), null) }),
//            ),
//            ItemsViewModel(
//                image = R.drawable.license_icon,
//                text = ItemsViewModel.Text(R.string.app_about_legal_third_party),
//                image2 = R.drawable.right_arrow,
//                url = null,
//                activity = null,
//                action = ({ loadOtherFragment(ThirdPartyFragment(), null) }),
//            ),
//        )

    private fun resetHints() {
        HintUtils.resetHints(requireContext())
        (activity as MainActivity).showHint("hint_shown_about", R.string.app_about_app_hint)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHint("hint_shown_about", R.string.app_about_app_hint)
    }
}
