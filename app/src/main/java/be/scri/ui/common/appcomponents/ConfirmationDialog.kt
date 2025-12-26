// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.common.appcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import be.scri.R
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot

/**
 * A confirmation dialog with customizable text and actions.
 *
 * @param text The main text to display in the dialog.
 * @param textChange The text for the change button.
 * @param textConfirm The text for the confirm button.
 * @param modifier Modifier for layout and styling.
 * @param onConfirm Callback triggered when the confirm button is clicked.
 * @param onChange Callback triggered when the change button is clicked.
 * @param onDismiss Callback triggered when the dialog is dismissed.
 */
@Composable
fun ConfirmationDialog(
    text: String,
    textChange: String,
    textConfirm: String,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {},
    onChange: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val context = LocalContext.current
    val isUserDarkMode = remember { getIsDarkModeOrNot(context.applicationContext) }

    ConfirmationDialogContent(
        text = text,
        textConfirm = textConfirm,
        textChange = textChange,
        onConfirm = onConfirm,
        onChange = onChange,
        isUserDarkMode = isUserDarkMode,
        modifier = modifier.padding(top = 24.dp),
        onDismiss = onDismiss,
    )
}

/**
 * The content of the confirmation dialog.
 *
 * @param text The main text to display in the dialog.
 * @param textConfirm The text for the confirm button.
 * @param textChange The text for the change button.
 * @param isUserDarkMode Whether the dark mode is enabled.
 * @param modifier Modifier for layout and styling.
 * @param onConfirm Callback triggered when the confirm button is clicked.
 * @param onChange Callback triggered when the change button is clicked.
 * @param onDismiss Callback triggered when the dialog is dismissed.
 */
@Composable
fun ConfirmationDialogContent(
    text: String,
    onConfirm: () -> Unit,
    onChange: () -> Unit,
    isUserDarkMode: Boolean,
    textConfirm: String,
    textChange: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val buttonConfirmColor: Color
    val buttonChangeColor: Color
    val buttonTextColor: Color
    val textFontSize = 16.sp
    val buttonPadding = 8.dp
    if (isUserDarkMode) {
        buttonConfirmColor = colorResource(R.color.dark_scribe_blue)
        buttonChangeColor = colorResource(R.color.dark_special_key_color)
        buttonTextColor = Color.White
    } else {
        buttonConfirmColor = colorResource(R.color.light_scribe_blue)
        buttonChangeColor = colorResource(R.color.light_special_key_color)
        buttonTextColor = Color.Black
    }

    val shadowColor = colorResource(R.color.light_key_shadow_color)

    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth(0.95f)
                    .padding(horizontal = 4.dp)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        shadowColor,
                                    ),
                            ),
                        shape = RoundedCornerShape(10.dp),
                    ),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_vector),
                            contentDescription = "Confirmation",
                            tint = Color(0xFFFDAD0D),
                            modifier =
                                Modifier
                                    .padding(start = 0.dp, end = 10.dp)
                                    .size(60.dp),
                        )

                        Text(
                            text = text,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = textFontSize,
                            style =
                                MaterialTheme.typography.bodyMedium.merge(
                                    TextStyle(lineHeight = 1.5.em),
                                ),
                            modifier = Modifier.weight(0.85f),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .padding(end = 2.dp)
                                    .background(Color.Transparent)
                                    .drawBehind {
                                        drawRoundRect(
                                            color = shadowColor,
                                            topLeft = Offset.Zero.copy(y = size.height - 16.dp.toPx()),
                                            size = size.copy(height = 14.dp.toPx()),
                                            cornerRadius = CornerRadius(8.dp.toPx()),
                                        )
                                    },
                        ) {
                            Button(
                                onClick = onChange,
                                colors =
                                    ButtonColors(
                                        containerColor = buttonChangeColor,
                                        contentColor = buttonTextColor,
                                        disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                        disabledContentColor = Color.White,
                                    ),
                                contentPadding = PaddingValues(buttonPadding),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = textChange,
                                    fontSize = textFontSize,
                                    modifier = Modifier,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier =
                                Modifier
                                    .background(Color.Transparent)
                                    .drawBehind {
                                        drawRoundRect(
                                            color = shadowColor,
                                            topLeft = Offset.Zero.copy(y = size.height - 16.dp.toPx()),
                                            size = size.copy(height = 14.dp.toPx()),
                                            cornerRadius = CornerRadius(8.dp.toPx()),
                                        )
                                    },
                        ) {
                            Button(
                                onClick = onConfirm,
                                colors =
                                    ButtonColors(
                                        containerColor = buttonConfirmColor,
                                        contentColor = buttonTextColor,
                                        disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                        disabledContentColor = Color.White,
                                    ),
                                contentPadding = PaddingValues(buttonPadding),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = textConfirm,
                                    fontSize = textFontSize,
                                    modifier = Modifier,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
