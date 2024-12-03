package be.scri.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.theme.ScribeTypography

@Composable
fun PrivacyPolicyScreen(
    bottomSpacerHeight: Int,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize(), contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(id = R.string.app_about_legal_privacy_policy_caption),
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
                Text(
                    text = stringResource(id = R.string.app_about_legal_privacy_policy_text),
                    fontSize = ScribeTypography.bodyMedium.fontSize,
                    style =
                        TextStyle.Default.copy(
                            fontStyle = ScribeTypography.bodyMedium.fontStyle,
                        ),
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets(bottom = bottomSpacerHeight),
                ),
            )
        }
    }
}
