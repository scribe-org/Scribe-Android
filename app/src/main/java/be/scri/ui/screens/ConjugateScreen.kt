// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen

/**
 * The conjugate page of the application with details for downloading conjugation data.
 */
@Composable
fun ConjugateScreen(
    onNavigateToDownloadData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localConfiguration = LocalConfiguration.current
    val scrollState = rememberScrollState()

    val dynamicSpacing = localConfiguration.screenHeightDp.dp * 0.1f
    ScribeBaseScreen {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = Dimensions.PaddingMedium)
                    .padding(vertical = Dimensions.PaddingLarge)
                    .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(dynamicSpacing))

            Image(
                painter = painterResource(id = R.drawable.scribe_logo),
                contentDescription = stringResource(R.string.app_launcher_name),
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .width(248.dp)
                        .height(122.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(R.string.i18n_app_download_menu_option_conjugate_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier =
                    Modifier
                        .padding(
                            start = 4.dp,
                            top = Dimensions.PaddingLarge,
                            bottom = Dimensions.PaddingSmall,
                        ).align(Alignment.Start),
            )
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimensions.PaddingSmall)
                        .clickable {
                            onNavigateToDownloadData()
                        },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier.Companion
                            .padding(Dimensions.PaddingMedium)
                            .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.i18n_app_download_menu_option_conjugate_download_data_start),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Image(
                            painter = painterResource(R.drawable.right_arrow),
                            contentDescription = "Right Arrow",
                            modifier =
                                Modifier.Companion
                                    .size(Dimensions.IconSize)
                                    .alpha(Alpha.HIGH),
                        )
                    }
                    Text(
                        text = stringResource(R.string.i18n_app_download_menu_option_conjugate_description),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.MEDIUM),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
