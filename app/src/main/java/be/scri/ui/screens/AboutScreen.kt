package be.scri.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import be.scri.BuildConfig
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.fragments.PrivacyPolicyFragment
import be.scri.fragments.ThirdPartyFragment
import be.scri.fragments.WikimediaScribeFragment
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper
import be.scri.models.ItemsViewModel
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AboutScreen(
    onWikimediaAndScribeClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onThirdPartyLicensesClick: () -> Unit,
    onRateScribeClick: () -> Unit,
    onMailClick: () -> Unit,
    onResetHintsClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val communityList = ScribeItemList(
        items = getCommunityList(
            onWikimediaAndScribeClick = onWikimediaAndScribeClick
        )
    )

    val feedbackAndSupportList = ScribeItemList(
        items = getFeedbackAndSupportList(
            onRateScribeClick = onRateScribeClick,
            onMailClick = onMailClick,
            onResetHintsClick = onResetHintsClick
        )
    )

    val legalItemsList = ScribeItemList(
        items = getLegalListItems(
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onThirdPartyLicensesClick = onThirdPartyLicensesClick
        )
    )

    Scaffold(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ItemCardContainerWithTitle(
                    title = stringResource(R.string.community_title),
                    cardItemsList = communityList,
                    isDivider = true,
                )
            }
            item {
                ItemCardContainerWithTitle(
                    title = stringResource(R.string.app_about_feedback_title),
                    cardItemsList = feedbackAndSupportList,
                    isDivider = true,
                )
            }

            item {
                ItemCardContainerWithTitle(
                    title = stringResource(R.string.app_about_legal_title),
                    cardItemsList = legalItemsList,
                    isDivider = true
                )
            }
        }
    }
}


@Composable
fun getCommunityList(
    onWikimediaAndScribeClick: () -> Unit
): List<ScribeItem.ExternalLinkItem> {
    return listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.github_logo,
            title = stringResource(R.string.app_about_community_github),
            trailingIcon = R.drawable.external_link,
            url = "https://github.com/scribe-org/Scribe-Android",
            onClick = {  }
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.matrix_icon,
            title = stringResource(R.string.app_about_community_matrix),
            trailingIcon = R.drawable.external_link,
            url = "https://matrix.to/%23/%23scribe_community:matrix.org",
            onClick = { },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.mastodon_svg_icon,
            title = stringResource(R.string.app_about_community_mastodon),
            trailingIcon = R.drawable.external_link,
            url = "https://wikis.world/@scribe",
            onClick = { },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.share_icon,
            title = stringResource(R.string.app_about_community_share_scribe),
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = { },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.wikimedia_logo_black,
            title = stringResource(R.string.app_about_community_wikimedia),
            trailingIcon = R.drawable.right_arrow,
            url = null,
            onClick = {
                onWikimediaAndScribeClick()
            }
        ),
    )
}

@Composable
fun getFeedbackAndSupportList(
    onRateScribeClick: () -> Unit,
    onMailClick: () -> Unit,
    onResetHintsClick: () -> Unit,

): List<ScribeItem.ExternalLinkItem> {
    return listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.star,
            title = stringResource(R.string.app_about_feedback_rate_scribe),
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = { onMailClick() },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.bug_report_icon,
            title = stringResource(R.string.app_about_feedback_bug_report),
            trailingIcon = R.drawable.external_link,
            url = "https://github.com/scribe-org/Scribe-Android/issues",
            onClick = { },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.mail_icon,
            title = stringResource(R.string.app_about_feedback_email),
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = { onMailClick() },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.bookmark_icon,
            title = stringResource(R.string.app_about_feedback_version, BuildConfig.VERSION_NAME),
            trailingIcon = R.drawable.external_link,
            url = "https://github.com/scribe-org/Scribe-Android/releases/",
            onClick = { },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.light_bulb_icon,
            title = stringResource(R.string.app_about_feedback_app_hints),
            trailingIcon = R.drawable.counter_clockwise_icon,
            url = null,
            onClick = { onResetHintsClick() },
        ),
    )
}

@Composable
fun getLegalListItems(
    onPrivacyPolicyClick: () -> Unit,
    onThirdPartyLicensesClick: () -> Unit
): List<ScribeItem.ExternalLinkItem> {
    return listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.shield_lock,
            title = stringResource(R.string.app_about_legal_privacy_policy),
            trailingIcon = R.drawable.right_arrow,
            url = null,
            onClick = { onPrivacyPolicyClick() },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.license_icon,
            title = stringResource(R.string.app_about_legal_third_party),
            trailingIcon = R.drawable.right_arrow,
            url = null,
            onClick = { onThirdPartyLicensesClick() },
        ),
    )
}
