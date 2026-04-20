// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen displayed when the user has a non-Scribe keyboard active during the tutorial.
 * Prompts the user to press the globe button to switch to a Scribe keyboard.
 *
 * @param onBackPressed Callback when the back button is pressed.
 * @param onClosePressed Callback when the close (X) button is pressed.
 */
@Composable
fun WrongKeyboardScreen(
    onBackPressed: () -> Unit,
    onClosePressed: () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) TutorialColors.darkBackground else TutorialColors.lightBackground
    val cardBackground = if (isDarkTheme) TutorialColors.cardBackgroundDark else TutorialColors.cardBackgroundLight
    val textColor = if (isDarkTheme) TutorialColors.textPrimaryDark else TutorialColors.textPrimary

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        // Top navigation bar
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onClosePressed) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close tutorial",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Title
        Text(
            text = "Non-Scribe keyboard",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Instruction card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "Press the 🌐 button to select a Scribe keyboard.",
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
