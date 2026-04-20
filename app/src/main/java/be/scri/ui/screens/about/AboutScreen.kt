// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.about

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.AppFlavor
import be.scri.helpers.FlavorProvider
import be.scri.helpers.ui.HintUtils
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.screens.about.AboutUtil.getCommunityList
import be.scri.ui.screens.about.AboutUtil.getFeedbackAndSupportList
import be.scri.ui.screens.about.AboutUtil.getLegalListItems
import be.scri.ui.screens.tutorial.TutorialNavigator

/**
 * The about page of the application with links to the community as well as sub pages for detailed descriptions.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AboutScreen(
    onPrivacyPolicyClick: () -> Unit,
    onThirdPartyLicensesClick: () -> Unit,
    onWikiClick: () -> Unit,
    resetHints: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier,
) {
    var showTutorial by remember { mutableStateOf(false) }

    if (showTutorial) {
        TutorialNavigator(
            onTutorialExit = { showTutorial = false }
        )
        return
    }

    val isConjugateApp = FlavorProvider.get() == AppFlavor.CONJUGATE
    val scrollState = rememberScrollState()

    val communityList =
        getCommunityList(
            onWikimediaAndScribeClick = {
                onWikiClick()
            },
            onShareScribeClick = { AboutUtil.onShareScribeClick(context, isConjugateApp) },
            context = context,
            isConjugateApp = isConjugateApp,
        )

    val feedbackAndSupportList =
        getFeedbackAndSupportList(
            onRateScribeClick = { AboutUtil.onRateScribeClick(context) },
            onMailClick = { AboutUtil.onMailClick(context) },
            onResetHintsClick = {
                HintUtils.resetHints(context)
                resetHints()
            },
            context = context,
            isConjugateApp = isConjugateApp,
        )

    val legalItemsList =
        getLegalListItems(
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onThirdPartyLicensesClick = onThirdPartyLicensesClick,
        )

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.i18n_app_about_title),
        onBackNavigation = {},
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tutorial button
            Button(
                onClick = { showTutorial = true },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5A623),
                        contentColor = Color.White,
                    ),
                shape = RoundedCornerShape(12.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
            ) {
                Text(
                    text = "Start full tutorial",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            ItemCardContainerWithTitle(
                title = stringResource(R.string.i18n_app_about_community_title),
                cardItemsList = communityList,
                isDivider = true,
            )

            ItemCardContainerWithTitle(
                title = stringResource(R.string.i18n_app_about_feedback_title),
                cardItemsList = feedbackAndSupportList,
                isDivider = true,
            )

            ItemCardContainerWithTitle(
                title = stringResource(R.string.i18n__global_legal),
                cardItemsList = legalItemsList,
                isDivider = true,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
