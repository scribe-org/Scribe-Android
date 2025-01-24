// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The about screen that displays the privacy policy for the application.
 */

package be.scri.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.theme.ScribeTypography

@Composable
fun PrivacyPolicyScreen(
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScribeBaseScreen(
        pageTitle = stringResource(R.string.privacy_policy),
        onBackNavigation = { onBackNavigation() },
        lastPage = stringResource(R.string.app_about_title),
        modifier = modifier,
    ) {
        PrivacyPolicyScreenContent(
            title = stringResource(R.string.app_about_legal_privacy_policy_caption),
            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
                ),
        ) {
            Text(
                text = stringResource(id = R.string.app_about_legal_privacy_policy_text),
                fontSize = ScribeTypography.bodyMedium.fontSize,
                style =
                    TextStyle.Default.copy(
                        fontStyle = ScribeTypography.bodyMedium.fontStyle,
                    ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
fun PrivacyPolicyScreenContent(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            fontSize = ScribeTypography.headlineMedium.fontSize,
            style =
                TextStyle.Default.copy(
                    fontStyle = ScribeTypography.headlineMedium.fontStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
        )
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            content()
        }

        Spacer(
            modifier = Modifier.height(10.dp),
        )
    }
}
