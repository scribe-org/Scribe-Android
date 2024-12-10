package be.scri.ui.common.app_components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.ui.theme.ScribeTypography

@Composable
fun PageTitle(
    pageTitle: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = pageTitle,
        fontSize = ScribeTypography.headlineLarge.fontSize,
        style =
        TextStyle.Default.copy(
            fontStyle = ScribeTypography.headlineMedium.fontStyle,
            fontWeight = FontWeight.Bold,
        ),
        modifier = modifier
    )
}
