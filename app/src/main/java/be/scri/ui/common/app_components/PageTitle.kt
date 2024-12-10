package be.scri.ui.common.app_components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun PageTitle(
    pageTitle: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = pageTitle,
        fontSize = 16.sp,
        color = colorResource(R.color.app_text_color),
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}
