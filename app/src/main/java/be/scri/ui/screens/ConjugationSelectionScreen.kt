// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen

// ---------------------------------------------------------------------------
// Dummy data — replaced by real DB data once #564 / #570 are finalised
// ---------------------------------------------------------------------------

private data class DummyTenseRow(
    val label: String,
    val forms: List<String>,
)

private data class DummyTenseGroup(
    val sectionTitle: String,
    val rows: List<DummyTenseRow>,
)

private val DUMMY_CONJUGATION_DATA: List<DummyTenseGroup> =
    listOf(
        DummyTenseGroup(
            sectionTitle = "Indicative",
            rows =
                listOf(
                    DummyTenseRow("Present", listOf("I verb", "you verb", "he/she verbs", "we verb", "you verb", "they verb")),
                    DummyTenseRow("Past", listOf("I verbed", "you verbed", "he/she verbed", "we verbed", "you verbed", "they verbed")),
                    DummyTenseRow("Future", listOf("I will verb", "you will verb", "he/she will verb", "we will verb", "you will verb", "they will verb")),
                ),
        ),
        DummyTenseGroup(
            sectionTitle = "Subjunctive",
            rows =
                listOf(
                    DummyTenseRow("Present", listOf("I verb", "you verb", "he/she verb", "we verb", "you verb", "they verb")),
                    DummyTenseRow("Past", listOf("I had verbed", "you had verbed", "he/she had verbed", "we had verbed", "you had verbed", "they had verbed")),
                ),
        ),
        DummyTenseGroup(
            sectionTitle = "Imperative",
            rows =
                listOf(
                    DummyTenseRow("Present", listOf("verb!", "let's verb!", "verb!")),
                ),
        ),
    )

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

/**
 * Displays dummy conjugation tables for a selected verb.
 *
 * The header shows "[verb] ([language])" as specified in the Figma designs for issue #567.
 * Real DB data will replace [DUMMY_CONJUGATION_DATA] once #564 / #570 are finalised.
 *
 * @param verb The verb selected by the user.
 * @param languageAlias The language code (e.g. "DE", "ES").
 * @param onBackNavigation Called when the user presses the back button.
 * @param modifier Optional modifier.
 */
@Composable
fun ConjugationSelectionScreen(
    verb: String,
    languageAlias: String,
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageTitle = "$verb (${getLanguageDisplayName(languageAlias)})"
    val backLabel = stringResource(R.string.i18n_app_conjugate_title)

    ScribeBaseScreen(
        pageTitle = pageTitle,
        lastPage = backLabel,
        onBackNavigation = onBackNavigation,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimensions.PaddingMedium)
                    .verticalScroll(rememberScrollState()),
        ) {
            DUMMY_CONJUGATION_DATA.forEach { group ->
                TenseGroupSection(group = group)
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun TenseGroupSection(
    group: DummyTenseGroup,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

        Text(
            text = group.sectionTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = Dimensions.PaddingSmall),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_radius_standard)),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.ElevationSmall),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                group.rows.forEachIndexed { index, row ->
                    TenseRow(row = row)
                    if (index < group.rows.lastIndex) {
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

@Composable
private fun TenseRow(
    row: DummyTenseRow,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.PaddingMedium, vertical = Dimensions.PaddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = Dimensions.PaddingSmall),
        )
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.End,
        ) {
            row.forms.forEach { form ->
                Text(
                    text = form,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
