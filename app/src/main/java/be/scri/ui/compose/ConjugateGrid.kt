package be.scri.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlin.math.min

@Composable
fun ConjugateGrid(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val conjugateOutput by viewModel.conjugateOutput.collectAsState()
    val selectedCategory by viewModel.selectedConjugationSubCategory.collectAsState()
    val language by viewModel.language.collectAsState()

    val title = conjugateOutput?.keys?.firstOrNull()
    val languageOutput = title?.let { conjugateOutput!![it] }

    val isSubSelection = selectedCategory != null
    val showCategories = !isSubSelection && (languageOutput?.containsKey(title) != true)

    val forms = if (isSubSelection) {
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
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // Standard keyboard height approx
            .background(Color(0xFFEBEBEB))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chunkedForms = forms.chunked(columns)
            for (rowForms in chunkedForms) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (form in rowForms) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (form.isNotEmpty()) {
                                        // TODO: handle category logic properly
                                        actionListener.onSuggestionClicked(form)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = form,
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
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
