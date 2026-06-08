// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen

/**
 * The conjugate page of the application with details for downloading conjugation data.
 */
@Composable
fun ConjugateScreen(
    onNavigateToDownloadData: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConjugateViewModel = viewModel(),
) {
    val context = LocalContext.current
    val localConfiguration = LocalConfiguration.current
    val scrollState = rememberScrollState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentlyConjugated by viewModel.recentlyConjugated.collectAsState()

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

            // Header 1: Conjugate verbs
            Text(
                text = stringResource(R.string.i18n_app_conjugate_verbs_search_title),
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

            // Search Bar
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                        ).border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                        ).padding(horizontal = Dimensions.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_search_vector),
                    contentDescription = "Search",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.size(Dimensions.IconSize),
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    textStyle =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.i18n_app_conjugate_verbs_search_placeholder),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                if (searchQuery.isNotEmpty()) {
                    Image(
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = "Clear",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        modifier =
                            Modifier
                                .size(Dimensions.IconSize)
                                .clickable { viewModel.clearSearchQuery() },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Image(
                    painter = painterResource(id = R.drawable.play_button),
                    contentDescription = "Play button",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier =
                        Modifier
                            .width(21.dp)
                            .height(18.dp)
                            .clickable {
                                if (searchResults.isNotEmpty()) {
                                    viewModel.onVerbSelected(searchResults.first())
                                }
                            },
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            if (searchQuery.isNotEmpty()) {
                // Search suggestion container
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimensions.PaddingMedium),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.ElevationSmall),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.PaddingSmall),
                    ) {
                        if (searchResults.isEmpty()) {
                            Text(
                                text = "No verbs found",
                                modifier = Modifier.padding(Dimensions.PaddingMedium),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.MEDIUM),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            searchResults.forEachIndexed { index, result ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.onVerbSelected(result) }
                                            .padding(Dimensions.PaddingMedium),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${result.verb} (${getLanguageDisplayName(result.languageAlias)})",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.right_arrow),
                                        contentDescription = "Right Arrow",
                                        modifier =
                                            Modifier
                                                .size(Dimensions.IconSize)
                                                .alpha(Alpha.HIGH),
                                    )
                                }
                                if (index < searchResults.lastIndex) {
                                    Spacer(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // normal screen flow when not searching

                // Header 2: Verb data
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
                            Modifier
                                .padding(Dimensions.PaddingMedium)
                                .fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.i18n_app_download_menu_option_conjugate_download_data),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Image(
                                painter = painterResource(R.drawable.right_arrow),
                                contentDescription = "Right Arrow",
                                modifier =
                                    Modifier
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

                // Header 3: Recently conjugated
                if (recentlyConjugated.isNotEmpty()) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = Dimensions.PaddingLarge, bottom = Dimensions.PaddingSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.i18n_app_conjugate_recently_conjugated_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                        )

                        Row(
                            modifier =
                                Modifier
                                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(4.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.clearAllRecentlyConjugated() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Clear all ✕",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            recentlyConjugated.forEachIndexed { index, item ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(Dimensions.PaddingMedium)
                                            .clickable {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Conjugating ${item.verb} (${getLanguageDisplayName(item.languageAlias)})...",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                            },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${item.verb} (${getLanguageDisplayName(item.languageAlias)})",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.right_arrow),
                                        contentDescription = "Right Arrow",
                                        modifier =
                                            Modifier
                                                .size(Dimensions.IconSize)
                                                .alpha(Alpha.HIGH),
                                    )
                                }
                                if (index < recentlyConjugated.lastIndex) {
                                    Spacer(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns the native/endonym name of a language alias.
 */
fun getLanguageDisplayName(alias: String): String =
    when (alias.uppercase()) {
        "EN" -> "English"
        "FR" -> "Français"
        "DE" -> "Deutsch"
        "IT" -> "Italiano"
        "PT" -> "Português"
        "RU" -> "Русский"
        "ES" -> "Español"
        "SV" -> "Svenska"
        else -> alias
    }
