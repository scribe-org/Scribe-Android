package be.scri.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.theme.ScribeTheme
import be.scri.ui.theme.ScribeTypography

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ThirdPartyScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.app_about_legal_third_party_caption),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                fontSize = ScribeTypography.headlineMedium.fontSize,
                style =
                    TextStyle.Default.copy(
                        fontStyle = ScribeTypography.headlineMedium.fontStyle,
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
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
    }
}

@Preview
@Composable
fun ThirdPartyScreenPreviewDark() {
    ScribeTheme(useDarkTheme = true) {
        ThirdPartyScreen()
    }
}

@Preview
@Composable
fun ThirdPartyScreenPreviewLight() {
    ScribeTheme(useDarkTheme = false) {
        ThirdPartyScreen()
    }
}
