// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.appcomponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

/**
 * A composable button that prompts the user to install keyboards.
 *
 * @param onClick The callback to invoke when the button is clicked.
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
fun InstallKeyboardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingSmallXL)
                .shadow(Dimensions.ElevationSmall, RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard))),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius_standard)),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Text(
            text = stringResource(R.string.i18n_app_settings_button_install_keyboards),
            fontSize = Dimensions.TextSizeExtraLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(vertical = Dimensions.PaddingLarge),
        )
    }
}

/**
 * Defines commonly used dimensions for the Settings screen UI.
 * Includes padding, text sizes, and elevation values.
 */
object Dimensions {
    val PaddingLarge = 20.dp
    val PaddingSmallXL = 12.dp

    val TextSizeExtraLarge = 24.sp

    val ElevationSmall = 4.dp
}
