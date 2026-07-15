// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.helpers.DatabaseFileManager
import be.scri.helpers.data.ConjugateDataManager
import be.scri.helpers.data.ContractDataLoader
import be.scri.ui.common.ScribeBaseScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

/**
 * Displays conjugation tables for a selected verb using a beautiful grid-based layout.
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
    val context = LocalContext.current
    val pageTitle = "$verb (${getLanguageDisplayName(languageAlias)})"
    val backLabel = stringResource(R.string.i18n_app_conjugate_title)

    var conjugationData by remember(verb, languageAlias) {
        mutableStateOf<Map<String, Map<String, List<Pair<String, String>>>>?>(null)
    }

    LaunchedEffect(verb, languageAlias) {
        withContext(Dispatchers.IO) {
            val loader = ContractDataLoader(context)
            val contract = loader.loadContract(languageAlias)
            if (contract != null) {
                val fileManager = DatabaseFileManager(context)
                val manager = ConjugateDataManager(fileManager)

                val structuredData = mutableMapOf<String, MutableMap<String, List<Pair<String, String>>>>()
                contract.conjugations.values.forEach { tenseGroup ->
                    val categories = mutableMapOf<String, List<Pair<String, String>>>()
                    tenseGroup.tenses.values.forEach { conjugationCategory ->
                        val pairs =
                            conjugationCategory.tenseForms.values
                                .map { form ->
                                    val resolvedForm = manager.getTheValueForTheConjugateWord(verb.lowercase(), form.value, languageAlias)
                                    form.label to resolvedForm
                                }.filter { it.second.isNotEmpty() }
                        if (pairs.isNotEmpty()) {
                            categories[conjugationCategory.tenseTitle] = pairs
                        }
                    }
                    if (categories.isNotEmpty()) {
                        structuredData[tenseGroup.sectionTitle] = categories
                    }
                }
                conjugationData = structuredData
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedTenseGroup by remember { mutableStateOf<String?>(null) }
    val tenseGroupsList = conjugationData?.keys?.toList() ?: emptyList()

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
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            Text(
                text = stringResource(R.string.i18n_app_conjugate_choose_conjugation_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = Dimensions.PaddingMedium),
            )

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimensions.PaddingLarge)
                        .animateContentSize(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropdown Tense Selector (padded on sides)
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(35.dp)
                                    .background(
                                        color = Color(0xFFFFA000),
                                        shape = RoundedCornerShape(8.dp),
                                    ).clickable { expanded = !expanded }
                                    .padding(horizontal = Dimensions.PaddingMedium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = selectedTenseGroup ?: stringResource(R.string.i18n_app_conjugate_choose_conjugation_select_tense),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_tab_rounded),
                                contentDescription = stringResource(R.string.i18n_app_accessibility_expand_tenses),
                                colorFilter = ColorFilter.tint(Color.Black),
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f),
                        ) {
                            DropdownMenuItem(
                                text = { Text("All tenses") },
                                onClick = {
                                    selectedTenseGroup = null
                                    expanded = false
                                },
                            )
                            tenseGroupsList.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group) },
                                    onClick = {
                                        selectedTenseGroup = group
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }

                    // Display Conjugation Tense Groups flush inside this single Card
                    val dataToShow = conjugationData
                    Crossfade(
                        targetState = dataToShow,
                        label = "loadingTransition",
                    ) { currentData ->
                        if (currentData != null) {
                            if (currentData.isEmpty()) {
                                // Empty State
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.i18n_app_conjugate_data_not_present_in_wikidata),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                            } else {
                                Crossfade(
                                    targetState = selectedTenseGroup,
                                    label = "tenseGroupTransition",
                                ) { currentTenseGroup ->
                                    val filteredGroups =
                                        if (currentTenseGroup != null) {
                                            currentData.filterKeys { it == currentTenseGroup }
                                        } else {
                                            currentData
                                        }

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        filteredGroups.entries.forEachIndexed { groupIndex, (groupTitle, categories) ->
                                            TenseGroupGridSection(groupTitle = groupTitle, categories = categories)
                                        }
                                        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                                    }
                                }
                            }
                        } else {
                            // Loading Placeholder
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.i18n_app_conjugate_loading_tables),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun TenseGroupGridSection(
    groupTitle: String,
    categories: Map<String, List<Pair<String, String>>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
    ) {
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

        // Single Card containing BOTH the gray header bar and the conjugation cell grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Header with gray background bar inside the Card (rounded top corners)
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = groupTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Divider line between Header and the first row of cells
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                )

                // 2. Grid Cells for categories
                val showSubHeaders = categories.size > 1
                categories.entries.forEachIndexed { catIndex, (categoryTitle, forms) ->
                    if (showSubHeaders) {
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
                        )
                    }

                    val chunkedForms = forms.chunked(2)
                    chunkedForms.forEachIndexed { rowIndex, pair ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (pair.size > 1) {
                                // Left Cell
                                val left = pair[0]
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                ) {
                                    ConjugationCell(label = left.first, form = left.second)
                                }

                                // Vertical Divider
                                Box(
                                    modifier =
                                        Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                )

                                // Right Cell
                                val right = pair[1]
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                ) {
                                    ConjugationCell(label = right.first, form = right.second)
                                }
                            } else {
                                // Only 1 form in this row -> span full width, no vertical divider.
                                val single = pair[0]
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(),
                                ) {
                                    ConjugationCell(label = single.first, form = single.second)
                                }
                            }
                        }

                        // Horizontal Divider (between cell rows).
                        if (rowIndex < chunkedForms.lastIndex) {
                            Spacer(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            )
                        }
                    }

                    // Horizontal Divider (between categories).
                    if (catIndex < categories.size - 1) {
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
private fun ConjugationCell(
    label: String,
    form: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .clickable {
                    clipboardManager.setText(AnnotatedString(form))
                    Toast
                        .makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                        .show()
                }.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Image(
                painter = painterResource(id = R.drawable.ic_clipboard_vector),
                contentDescription = stringResource(R.string.i18n_app_accessibility_copy_conjugation),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                modifier =
                    Modifier
                        .size(18.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = form,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
