package be.scri.ui.screens

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
fun PrivacyPolicyScreen(
    bottomSpacerHeight: Int,
    modifier: Modifier = Modifier,
) {
    MainActivityComposeScreen(
        R.string.app_about_legal_privacy_policy_caption,
        modifier,
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
}
