package be.scri.ui.screens.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import be.scri.BuildConfig
import be.scri.R
import be.scri.helpers.HintUtils
import be.scri.navigation.Screen
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.common.components.ItemCardContainerWithTitle
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList
import be.scri.ui.screens.WikimediaScreen
import be.scri.ui.screens.about.AboutUtil.getCommunityList
import be.scri.ui.screens.about.AboutUtil.getFeedbackAndSupportList
import be.scri.ui.screens.about.AboutUtil.getLegalListItems

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
    val scrollState = rememberScrollState()

    val communityList = getCommunityList(
        onWikimediaAndScribeClick = {
            onWikiClick()
        },
        onShareScribeClick = { AboutUtil.onShareScribeClick(context) },
        context = context,
    )

    val feedbackAndSupportList = getFeedbackAndSupportList(
        onRateScribeClick = { AboutUtil.onRateScribeClick(context) },
        onMailClick = { AboutUtil.onMailClick(context) },
        onResetHintsClick = {
            HintUtils.resetHints(context)
            resetHints()
        },
        context = context,
    )

    val legalItemsList = getLegalListItems(
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onThirdPartyLicensesClick = onThirdPartyLicensesClick,
    )

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_about_title),
        onBackNavigation = {},
        modifier = modifier
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

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

