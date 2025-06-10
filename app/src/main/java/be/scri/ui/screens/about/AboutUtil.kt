// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

/**
 * A centralized object that stores all external URLs used in the About screen.
 */
object ExternalLinks {
    const val GITHUB_SCRIBE = "https://github.com/scribe-org/Scribe-Android"
    const val GITHUB_ISSUES = "$GITHUB_SCRIBE/issues"
    const val GITHUB_RELEASES = "$GITHUB_SCRIBE/releases/"
    const val MATRIX = "https://matrix.to/%23/%23scribe_community:matrix.org"
    const val MASTODON = "https://wikis.world/@scribe"
}

/** This file provide utility functions for the about page */
object AboutUtil {
    /**
     * Shares the Scribe app via the system's share sheet.
     *
     * @param context Context used to launch the sharing intent.
     */
    fun onShareScribeClick(context: Context) {
        ShareHelper.shareScribe(context)
    }

    /**
     * Opens the app's Play Store page for rating.
     *
     * @param context Context used to launch the rating flow (must be a MainActivity instance).
     */
    fun onRateScribeClick(context: Context) {
        RatingHelper.rateScribe(context, context as MainActivity)
    }

    /**
     * Launches the email app to send feedback about Scribe.
     *
     * @param context Context used to send the email intent.
     */
    fun onMailClick(context: Context) {
        ShareHelper.sendEmail(context)
    }

    /**
     * Returns a list of community links and actions for the About screen, such as GitHub, Matrix,
     * or Mastodon. The list is memoized with [remember].
     *
     * @param onWikimediaAndScribeClick Callback invoked when the Wikimedia item is clicked.
     * @param onShareScribeClick Callback for the share Scribe action.
     * @param context Android context used to open URLs.
     * @return A [ScribeItemList] representing community-related actions.
     */
    @Composable
    fun getCommunityList(
        onWikimediaAndScribeClick: () -> Unit,
        onShareScribeClick: () -> Unit,
        context: Context,
    ): ScribeItemList =
        remember {
            ScribeItemList(
                items =
                    listOf(
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.github_logo,
                            title = R.string.app_about_community_github,
                            trailingIcon = R.drawable.external_link,
                            url = ExternalLinks.GITHUB_SCRIBE,
                            onClick = {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            ExternalLinks.GITHUB_SCRIBE,
                                        ),
                                    )
                                context.startActivity(intent)
                            },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.matrix_icon,
                            title = R.string.app_about_community_matrix,
                            trailingIcon = R.drawable.external_link,
                            url =
                                ExternalLinks.MATRIX,
                            onClick = {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            ExternalLinks.MATRIX,
                                        ),
                                    )
                                context.startActivity(intent)
                            },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.mastodon_svg_icon,
                            title = R.string.app_about_community_mastodon,
                            trailingIcon = R.drawable.external_link,
                            url = ExternalLinks.MASTODON,
                            onClick = {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            ExternalLinks.MASTODON,
                                        ),
                                    )
                                context.startActivity(intent)
                            },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.share_icon,
                            title = R.string.app_about_community_share_scribe,
                            trailingIcon = R.drawable.external_link,
                            url = null,
                            onClick = { onShareScribeClick() },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.wikimedia_logo_black,
                            title = R.string.app_about_community_wikimedia,
                            trailingIcon = R.drawable.right_arrow,
                            url = null,
                            onClick = { onWikimediaAndScribeClick() },
                        ),
                    ),
            )
        }

    /**
     * Returns a list of feedback and support options for the About screen.
     *
     * @param onRateScribeClick Callback for initiating app rating.
     * @param onMailClick Callback to open an email intent.
     * @param onResetHintsClick Callback to reset onboarding hints.
     * @param context Android context used to open external URLs.
     * @return A [ScribeItemList] with support-related options.
     */
    @Composable
    fun getFeedbackAndSupportList(
        onRateScribeClick: () -> Unit,
        onMailClick: () -> Unit,
        onResetHintsClick: () -> Unit,
        context: Context,
    ): ScribeItemList =
        remember {
            ScribeItemList(
                items =
                    listOf(
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.star,
                            title = R.string.app_about_feedback_rate_scribe,
                            trailingIcon = R.drawable.external_link,
                            url = null,
                            onClick = { onRateScribeClick() },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.bug_report_icon,
                            title = R.string.app_about_feedback_bug_report,
                            trailingIcon = R.drawable.external_link,
                            url = ExternalLinks.GITHUB_ISSUES,
                            onClick = {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            ExternalLinks.GITHUB_ISSUES,
                                        ),
                                    )
                                context.startActivity(intent)
                            },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.mail_icon,
                            title = R.string.app_about_feedback_email,
                            trailingIcon = R.drawable.external_link,
                            url = null,
                            onClick = { onMailClick() },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.bookmark_icon,
                            title = R.string.app_about_feedback_version,
                            //                    , BuildConfig.VERSION_NAME,
                            trailingIcon = R.drawable.external_link,
                            url =
                                ExternalLinks.GITHUB_RELEASES,
                            onClick = {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            ExternalLinks.GITHUB_RELEASES,
                                        ),
                                    )
                                context.startActivity(intent)
                            },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.light_bulb_icon,
                            title = R.string.app_about_feedback_app_hints,
                            trailingIcon = R.drawable.counter_clockwise_icon,
                            url = null,
                            onClick = { onResetHintsClick() },
                        ),
                    ),
            )
        }

    /**
     * Returns a list of legal-related items like privacy policy and licenses.
     *
     * @param onPrivacyPolicyClick Callback invoked when the Privacy Policy is selected.
     * @param onThirdPartyLicensesClick Callback for opening the licenses screen.
     * @return A [ScribeItemList] with legal information items.
     */
    @Composable
    fun getLegalListItems(
        onPrivacyPolicyClick: () -> Unit,
        onThirdPartyLicensesClick: () -> Unit,
    ): ScribeItemList =
        remember {
            ScribeItemList(
                items =
                    listOf(
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.shield_lock,
                            title = R.string.app_about_legal_privacy_policy,
                            trailingIcon = R.drawable.right_arrow,
                            url = null,
                            onClick = { onPrivacyPolicyClick() },
                        ),
                        ScribeItem.ExternalLinkItem(
                            leadingIcon = R.drawable.license_icon,
                            title = R.string.app_about_legal_third_party,
                            trailingIcon = R.drawable.right_arrow,
                            url = null,
                            onClick = { onThirdPartyLicensesClick() },
                        ),
                    ),
            )
        }
}
