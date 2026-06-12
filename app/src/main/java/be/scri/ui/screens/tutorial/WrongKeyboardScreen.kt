// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen displayed when the user has a non-Scribe keyboard active during the tutorial.
 * Prompts the user to press the globe button to switch to a Scribe keyboard.
 *
 * The screen includes a focused text input field that automatically requests focus
 * on launch, ensuring the system keyboard appears so the user can tap the globe icon
 * to switch keyboards. Without this field, the keyboard would never appear and the
 * user would be stuck on this screen.
 *
 * @param onBackPress Callback when the back button is pressed.
 * @param modifier Modifier for this composable.
 */
@Composable
fun WrongKeyboardScreen(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val headerColor = MaterialTheme.colorScheme.onBackground

    var userInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus the input field when the screen appears so the keyboard pops up.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        // Top navigation bar
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBackPress() }.padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = headerColor,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Quick tutorial",
                    color = headerColor,
                    fontSize = 16.sp,
                )
            }
        }

        // Title
        Text(
            text = "Non-Scribe keyboard",
            color = headerColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.padding(8.dp))

        // Instruction card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                // Instruction text
                Text(
                    text = "Press the \uD83C\uDF10 button to select a Scribe keyboard.",
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )

                Spacer(modifier = Modifier.padding(8.dp))

                HorizontalDivider(
                    color = dividerColor,
                )

                // Hidden input field that brings up the keyboard.
                // The user types nothing here — it just exists to trigger the IME
                // so the globe icon is accessible for switching keyboards.
                BasicTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .focusRequester(focusRequester),
                    textStyle =
                        TextStyle(
                            color = textColor,
                            fontSize = 16.sp,
                        ),
                    cursorBrush = SolidColor(textColor),
                    singleLine = true,
                )

                HorizontalDivider(
                    color = dividerColor,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
