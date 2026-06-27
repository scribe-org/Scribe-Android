// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToConjugationSelection: (String, String) -> Unit = { _, _ -> },
    viewModel: ConjugateViewModel = viewModel(),
) {
    val localConfiguration = LocalConfiguration.current
    val scrollState = rememberScrollState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val displayResults by viewModel.displayResults.collectAsState()
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
            if (searchQuery.isEmpty()) {
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
            } else {
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            }

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

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimensions.PaddingMedium)
                        .animateContentSize(),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (searchQuery.isEmpty()) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                    ),
                border =
                    if (searchQuery.isEmpty()) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    } else {
                        null
                    },
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Search Bar Row (Always at the exact same composition tree location!)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = Dimensions.PaddingMedium),
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
                                        val first = displayResults.firstOrNull()
                                        if (first != null) {
                                            viewModel.onVerbSelected(first)
                                            onNavigateToConjugationSelection(first.verb, first.languageAlias)
                                        }
                                    },
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        // Thin divider between Search Bar and Results
                        Spacer(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        )

                        if (displayResults.isNotEmpty()) {
                            displayResults.forEachIndexed { index, result ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onVerbSelected(result)
                                                onNavigateToConjugationSelection(result.verb, result.languageAlias)
                                            }.padding(Dimensions.PaddingMedium),
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
                                if (index < displayResults.lastIndex) {
                                    Spacer(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                    )
                                }
                            }
                        } else {
                            val downloaded = viewModel.getDownloadedLanguages()
                            val noResultsMessage =
                                if (downloaded.isEmpty()) {
                                    "No results found. Please download verb data first."
                                } else {
                                    "No results found in ${viewModel.getDownloadedLanguagesFormatted()}"
                                }
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(Dimensions.PaddingMedium),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = noResultsMessage,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            if (searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
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

                // Direct link to ConjugationSelectionScreen (issue #567).
                // Andrew requested a link on the Conjugate tab while search-based
                // navigation (#570) is not yet wired up.
                Text(
                    text = stringResource(R.string.i18n_app_conjugate_choose_conjugation_title),
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
                            .clickable { onNavigateToConjugationSelection("verb", "DE") },
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .padding(Dimensions.PaddingMedium)
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.i18n_app_conjugate_choose_conjugation_select_tense),
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
                }

                // Header 3: Recently conjugated — only shown when real items exist.
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
                        Column(modifier = Modifier.fillMaxWidth()) {
                            recentlyConjugated.forEachIndexed { index, item ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onVerbSelected(item)
                                                onNavigateToConjugationSelection(item.verb, item.languageAlias)
                                            }.padding(Dimensions.PaddingMedium),
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
