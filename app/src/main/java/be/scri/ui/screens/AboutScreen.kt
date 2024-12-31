/**
 * The about page of the application with links to the community as well as sub pages for detailed descriptions.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package be.scri.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import be.scri.BuildConfig
import be.scri.R
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AboutScreen(
    onWikimediaAndScribeClick: () -> Unit,
    onShareScribeClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onThirdPartyLicensesClick: () -> Unit,
    onRateScribeClick: () -> Unit,
    onMailClick: () -> Unit,
    onResetHintsClick: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val communityList =
        ScribeItemList(
            items =
                getCommunityList(
                    onWikimediaAndScribeClick = onWikimediaAndScribeClick,
                    onShareScribeClick = onShareScribeClick,
                    context = context,
                ),
        )

    val feedbackAndSupportList =
        ScribeItemList(
            items =
                getFeedbackAndSupportList(
                    onRateScribeClick = onRateScribeClick,
                    onMailClick = onMailClick,
                    onResetHintsClick = onResetHintsClick,
                    context = context,
                ),
        )

    val legalItemsList =
        ScribeItemList(
            items =
                getLegalListItems(
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    onThirdPartyLicensesClick = onThirdPartyLicensesClick,
                ),
        )

    Scaffold(
        modifier =
            modifier
                .background(color = MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ItemCardContainerWithTitle(
                title = stringResource(R.string.community_title),
                cardItemsList = communityList,
                isDivider = true,
            )

            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_about_feedback_title),
                cardItemsList = feedbackAndSupportList,
                isDivider = true,
            )

            ItemCardContainerWithTitle(
                title = stringResource(R.string.app_about_legal_title),
                cardItemsList = legalItemsList,
                isDivider = true,
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun getCommunityList(
    onWikimediaAndScribeClick: () -> Unit,
    onShareScribeClick: () -> Unit,
    context: Context,
): List<ScribeItem.ExternalLinkItem> =
    listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.github_logo,
            title = stringResource(R.string.app_about_community_github),
            trailingIcon = R.drawable.external_link,
            url = "https://github.com/scribe-org/Scribe-Android",
            onClick = {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://github.com/scribe-org/Scribe-Android",
                        ),
                    )
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.matrix_icon,
            title = stringResource(R.string.app_about_community_matrix),
            trailingIcon = R.drawable.external_link,
            url = "https://matrix.to/%23/%23scribe_community:matrix.org",
            onClick = {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://matrix.to/%23/%23scribe_community:matrix.org",
                        ),
                    )
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.mastodon_svg_icon,
            title = stringResource(R.string.app_about_community_mastodon),
            trailingIcon = R.drawable.external_link,
            url = "https://wikis.world/@scribe",
            onClick = {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://wikis.world/@scribe",
                        ),
                    )
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.share_icon,
            title = stringResource(R.string.app_about_community_share_scribe),
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = {
                onShareScribeClick()
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.wikimedia_logo_black,
            title = stringResource(R.string.app_about_community_wikimedia),
            trailingIcon = R.drawable.right_arrow,
            url = null,
            onClick = {
                onWikimediaAndScribeClick()
            },
        ),
    )

@Composable
fun getFeedbackAndSupportList(
    onRateScribeClick: () -> Unit,
    onMailClick: () -> Unit,
    onResetHintsClick: () -> Unit,
    context: Context,
): List<ScribeItem.ExternalLinkItem> =
    listOf(
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.star,
            title = stringResource(R.string.app_about_feedback_rate_scribe),
            trailingIcon = R.drawable.external_link,
            url = null,
            onClick = {
                onRateScribeClick()
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.bug_report_icon,
            title = stringResource(R.string.app_about_feedback_bug_report),
            trailingIcon = R.drawable.external_link,
            url = "https://github.com/scribe-org/Scribe-Android/issues",
            onClick = {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://github.com/scribe-org/Scribe-Android/issues",
                        ),
                    )
                context.startActivity(intent)
            },
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
            onClick = {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://github.com/scribe-org/Scribe-Android/releases/",
                        ),
                    )
                context.startActivity(intent)
            },
        ),
        ScribeItem.ExternalLinkItem(
            leadingIcon = R.drawable.light_bulb_icon,
            title = stringResource(R.string.app_about_feedback_app_hints),
            trailingIcon = R.drawable.counter_clockwise_icon,
            url = null,
            onClick = { onResetHintsClick() },
        ),
    )

@Composable
fun getLegalListItems(
    onPrivacyPolicyClick: () -> Unit,
    onThirdPartyLicensesClick: () -> Unit,
): List<ScribeItem.ExternalLinkItem> =
    listOf(
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
