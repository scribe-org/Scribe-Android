package be.scri.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.ScribeBaseScreen
import be.scri.ui.theme.ScribeTypography

@SuppressLint("ComposeModifierReused")
@Composable
fun ThirdPartyScreen(
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScribeBaseScreen(
        pageTitle = stringResource(R.string.app_about_legal_third_party),
        onBackNavigation = { onBackNavigation() },
        lastPage = stringResource(R.string.app_about_title),
        modifier = modifier,
    ) {
        ThirdPartyScreenContent(
            title = stringResource(R.string.app_about_legal_third_party_caption),
            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
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

@Composable
fun ThirdPartyScreenContent(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            fontSize = ScribeTypography.headlineMedium.fontSize,
            style =
                TextStyle.Default.copy(
                    fontStyle = ScribeTypography.headlineMedium.fontStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
        )
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            content()
        }

        Spacer(
            modifier = Modifier.height(10.dp),
        )
    }
}
