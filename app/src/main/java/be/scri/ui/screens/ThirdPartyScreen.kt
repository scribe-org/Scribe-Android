package be.scri.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.components.MainActivityComposeScreen
import be.scri.ui.theme.ScribeTypography

@Composable
fun ThirdPartyScreen(
    modifier: Modifier = Modifier,
) {
    MainActivityComposeScreen(R.string.app_about_legal_third_party_caption, modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.app_about_legal_third_party_text),
                fontSize = ScribeTypography.bodyMedium.fontSize,
                style =
                    TextStyle.Default.copy(
                        fontStyle = ScribeTypography.bodyMedium.fontStyle,
                    ),
            )
            Text(
                text = stringResource(id = R.string.app_about_legal_third_party_entry_simple_keyboard),
                fontSize = ScribeTypography.bodyMedium.fontSize,
                style =
                    TextStyle.Default.copy(
                        fontStyle = ScribeTypography.bodyMedium.fontStyle,
                    ),
            )
        }
    }
}
