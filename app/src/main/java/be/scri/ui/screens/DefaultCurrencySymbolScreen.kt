// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.helpers.PreferencesHelper
import be.scri.ui.common.ScribeBaseScreen

/**
 * The Select Languages subpage is for selecting the translation source language.
 */
@Composable
fun DefaultCurrencySymbolScreen(
    currentLanguage: String,
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val selectedSymbol =
        remember {
            mutableStateOf(PreferencesHelper.getDefaultCurrencyName(context, currentLanguage))
        }
    val scrollState = rememberScrollState()
    val symbolMap =
        mapOf(
            "Dollar" to "$",
            "Euro" to "€",
            "Pound" to "£",
            "Rouble" to "₽",
            "Rupee" to "₹",
            "Won" to "₩",
            "Yen" to "¥",
        )

    // Show ALL currencies, don't filter any out.
    val options = symbolMap.keys.toList()

    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_settings_keyboard_layout_default_currency),
        lastPage = stringResource(id = getLanguageStringFromi18n(currentLanguage)),
        onBackNavigation = onBackNavigation,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Text(
                text = stringResource(R.string.app_settings_keyboard_layout_default_currency_caption),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSymbol.value = option
                                        PreferencesHelper.setDefaultCurrencySymbol(context, currentLanguage, option)
                                    }.padding(vertical = 5.dp, horizontal = 8.dp),
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = symbolMap[option] ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            RadioButton(
                                selected = (option == selectedSymbol.value),
                                onClick = {
                                    selectedSymbol.value = option
                                    PreferencesHelper.setDefaultCurrencySymbol(context, currentLanguage, option)
                                },
                            )
                        }

                        if (index < options.lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}
