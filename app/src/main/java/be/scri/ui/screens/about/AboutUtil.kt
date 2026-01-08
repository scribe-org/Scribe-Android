// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.ui.RatingHelper
import be.scri.helpers.ui.ShareHelper
import be.scri.helpers.ui.ShareHelperImpl
import be.scri.helpers.ui.ShareHelperInterface
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList
import androidx.core.net.toUri

/** A centralized object that stores all external URLs used in the About screen. */
object ExternalLinks {
    const val GITHUB_SCRIBE = "https://github.com/scribe-org/Scribe-Android"
    const val GITHUB_ISSUES = "$GITHUB_SCRIBE/issues"
    const val GITHUB_RELEASES = "$GITHUB_SCRIBE/releases/"
    const val MATRIX = "https://matrix.to/%23/%23scribe_community:matrix.org"
    const val MASTODON = "https://wikis.world/@scribe"

    const val SCRIBE_WEBSITE = "https://scri.be"
}

/**
 * Builds a list of community-related external link items displayed on the About screen.
 *
 * @param context Context to launch intents for opening URLs.
 * @param onShareScribeClick Callback invoked when the "Share Scribe" item is clicked.
 * @param onWikimediaAndScribeClick Callback invoked when the Wikimedia item is clicked.
 * @return A list of [ScribeItem.ExternalLinkItem] representing community links and actions.
 */
fun buildCommunityList(
    context: Context,
    onShareScribeClick: () -> Unit,
    onWikimediaAndScribeClick: () -> Unit,
): List<ScribeItem.ExternalLinkItem> =
    listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.github_logo,
            title = R.string.i18n_app_about_community_github,
            trailingIcon = R.drawable.external_link,
            url = ExternalLinks.GITHUB_SCRIBE,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, ExternalLinks.GITHUB_SCRIBE.toUri())
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.globe,
            title = R.string.i18n_app_about_community_visit_website,
            trailingIcon = R.drawable.external_link,
            url = ExternalLinks.SCRIBE_WEBSITE,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, ExternalLinks.SCRIBE_WEBSITE.toUri())
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.share_icon,
            title = R.string.i18n_app_about_community_share_scribe,
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = { onShareScribeClick() },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.wikimedia_logo_black,
            title = R.string.i18n_app_about_community_wikimedia,
            trailingIcon = R.drawable.right_arrow,
            url = null,
            onClick = { onWikimediaAndScribeClick() },
        ),
    )

/**
 * Returns a list of legal-related item specifications such as Privacy Policy and Third-Party Licenses.
 *
 * @return A list of [LegalItemSpec] with legal info metadata.
 */
fun getLegalItemSpecs(): List<LegalItemSpec> =
    listOf(
        LegalItemSpec(
            icon = R.drawable.shield_lock,
            title = R.string.i18n__global_privacy_policy,
            destination = Destination.PrivacyPolicy,
        ),
        LegalItemSpec(
            icon = R.drawable.license_icon,
            title = R.string.i18n_app_about_legal_third_party,
            destination = Destination.ThirdPartyLicenses,
        ),
    )

/**
 * Builds a list of feedback and support-related external link items for the About screen.
 *
 * @param context Context to launch intents.
 * @param onRateScribeClick Callback invoked when user selects "Rate Scribe".
 * @param onMailClick Callback invoked when user wants to send feedback email.
 * @param onResetHintsClick Callback invoked to reset onboarding hints.
 * @return A list of [ScribeItem.ExternalLinkItem] for feedback and support options.
 */
fun feedbackAndSupportList(
    context: Context,
    onRateScribeClick: () -> Unit,
    onMailClick: () -> Unit,
    onResetHintsClick: () -> Unit,
): List<ScribeItem.ExternalLinkItem> =
    listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.star,
            title = R.string.i18n_app_about_feedback_rate_scribe,
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = { onRateScribeClick() },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.bug_report_icon,
            title = R.string.i18n_app_about_feedback_bug_report,
            trailingIcon = R.drawable.external_link,
            url = ExternalLinks.GITHUB_ISSUES,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ExternalLinks.GITHUB_ISSUES))
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.mail_icon,
            title = R.string.i18n_app_about_feedback_send_email,
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = { onMailClick() },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.bookmark_icon,
            title = R.string.i18n_app_about_feedback_version,
            trailingIcon = R.drawable.external_link,
            url = ExternalLinks.GITHUB_RELEASES,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ExternalLinks.GITHUB_RELEASES))
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.light_bulb_icon,
            title = R.string.i18n_app_about_feedback_reset_app_hints,
            trailingIcon = R.drawable.counter_clockwise_icon,
            url = null,
            onClick = { onResetHintsClick() },
        ),
    )

/**
 * Data class representing legal item specification metadata.
 *
 * @property icon Resource ID of the icon displayed.
 * @property title Resource ID of the title string.
 * @property destination The destination screen or action associated with this item.
 */
data class LegalItemSpec(
    val icon: Int,
    val title: Int,
    val destination: Destination,
)

/**
 * Enum representing navigation destinations for legal items.
 */
enum class Destination {
    PrivacyPolicy,
    ThirdPartyLicenses,
}

/**
 * Utility object providing helper functions and data for the About screen.
 */
object AboutUtil {
    /**
     * Instance of [ShareHelperInterface] used for sharing actions.
     * Initialized with the concrete implementation [ShareHelperImpl].
     */
    var shareHelper: ShareHelperInterface = ShareHelperImpl()

    /**
     * Shares the Scribe app via the system's share sheet.
     *
     * @param context Context used to launch the sharing intent.
     */
    fun onShareScribeClick(context: Context) {
        shareHelper.shareScribe(context)
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
     * Returns a memoized list of community links for the About screen.
     *
     * @param onWikimediaAndScribeClick Callback invoked when Wikimedia link is clicked.
     * @param onShareScribeClick Callback invoked when Share Scribe link is clicked.
     * @param context Android context to open URLs.
     *
     * @return A [ScribeItemList] wrapping community external links.
     */
    @Composable
    fun getCommunityList(
        onWikimediaAndScribeClick: () -> Unit,
        onShareScribeClick: () -> Unit,
        context: Context,
    ): ScribeItemList =
        remember {
            ScribeItemList(
                items = buildCommunityList(context, onShareScribeClick, onWikimediaAndScribeClick),
            )
        }

    /**
     * Returns a memoized list of feedback and support items for the About screen.
     *
     * @param onRateScribeClick Callback for "Rate Scribe" action.
     * @param onMailClick Callback to open email intent.
     * @param onResetHintsClick Callback to reset onboarding hints.
     * @param context Android context used to launch external intents.
     *
     * @return A [ScribeItemList] wrapping feedback and support options.
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
                    feedbackAndSupportList(
                        context,
                        onRateScribeClick,
                        onMailClick,
                        onResetHintsClick,
                    ),
            )
        }

    /**
     * Returns a memoized list of legal items for the About screen.
     *
     * @param onPrivacyPolicyClick Callback invoked when Privacy Policy is selected.
     * @param onThirdPartyLicensesClick Callback invoked when Third-Party Licenses is selected.
     *
     * @return A [ScribeItemList] wrapping legal information items.
     */
    @Composable
    fun getLegalListItems(
        onPrivacyPolicyClick: () -> Unit,
        onThirdPartyLicensesClick: () -> Unit,
    ): ScribeItemList =
        remember {
            val items =
                getLegalItemSpecs().map { spec ->
                    val clickHandler =
                        when (spec.destination) {
                            Destination.PrivacyPolicy -> onPrivacyPolicyClick
                            Destination.ThirdPartyLicenses -> onThirdPartyLicensesClick
                        }

                    ScribeItem.ExternalLinkItem(
                        leadingIcon = spec.icon,
                        title = spec.title,
                        trailingIcon = R.drawable.right_arrow,
                        url = null,
                        onClick = clickHandler,
                    )
                }

            ScribeItemList(items = items)
        }
}
