/**
 * The about screen that displays the privacy policy for the application.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
        bottomSpacerHeight,
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
