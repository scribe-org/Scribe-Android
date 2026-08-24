// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConjugateGrid(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val conjugateOutput by viewModel.conjugateOutput.collectAsState()
    val selectedCategory by viewModel.selectedConjugationSubCategory.collectAsState()

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val bgColor = if (isDarkMode) Color(0xFF282828) else Color(0xFFEBEBEB)
    val cardBg = if (isDarkMode) Color(0xFF404040) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    val title = conjugateOutput?.keys?.firstOrNull()
    val languageOutput = title?.let { conjugateOutput!![it] }

    val isSubSelection = selectedCategory != null
    val showCategories = !isSubSelection && (languageOutput?.containsKey(title) != true)

    val forms =
        if (isSubSelection) {
            languageOutput?.get(selectedCategory)?.toList() ?: emptyList()
        } else if (showCategories) {
            languageOutput?.map { (_, values) ->
                if (values.size == 1) values.first() else values.joinToString(" / ")
            } ?: emptyList()
        } else {
            languageOutput?.get(title)?.toList() ?: emptyList()
        }

    val columns = if (isSubSelection || (forms.size <= 2)) 1 else 2

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(250.dp) // Standard keyboard height approx
                .background(bgColor)
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chunkedForms = forms.chunked(columns)
            for (rowForms in chunkedForms) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (form in rowForms) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(cardBg, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (form.isNotEmpty()) {
                                            actionListener.onSuggestionClicked(form)
                                        }
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = form,
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                    }
                    // Fill remaining space if odd number of items in a row
                    if (rowForms.size < columns) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
