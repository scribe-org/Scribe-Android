// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Represents the validation state of the user's input in a tutorial step.
 */
enum class InputValidationState {
    /** No input yet. */
    EMPTY,

    /** User typed the correct word. */
    CORRECT,

    /** User typed the wrong word. */
    INCORRECT,
}

/**
 * A single step within a tutorial chapter.
 *
 * @property instruction The instructional text shown to the user.
 * @property expectedWord The word the user needs to type to pass this step.
 * @property hint An optional hint about switching keyboard language.
 * @property successMessage The message shown when the user types correctly.
 * @property errorMessage The message shown when the user types incorrectly.
 * @property requiresValidation Whether this step requires the user to type a specific word.
 */
data class TutorialStep(
    val instruction: String,
    val expectedWord: String = "",
    val hint: String = "If your second language is not German, change the language in your keyboard.",
    val successMessage: String = "Great! Press Next to continue.",
    val errorMessage: String = "",
    val requiresValidation: Boolean = true,
)

/**
 * Checks whether the currently active keyboard is a Scribe keyboard.
 *
 * @param context The application context.
 * @return true if the active input method belongs to the Scribe package, false otherwise.
 */
fun isScribeKeyboardActive(context: Context): Boolean {
    val currentInputMethod =
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        )
    return currentInputMethod?.contains("be.scri") == true
}

/**
 * The reusable tutorial step screen component (Screens 1.1-4.0 from Figma).
 * This is the interactive lesson screen used by all tutorial chapters.
 * It displays an instruction, a text input field, validates the user's input,
 * and shows success/error feedback.
 *
 * If the user does not have a Scribe keyboard active, it shows the
 * WrongKeyboardScreen instead, prompting them to switch.
 *
 * @param chapterTitle The title of the current chapter (e.g., "Noun annotation").
 * @param step The [TutorialStep] data for the current step.
 * @param onBackPress Callback when the back button is pressed.
 * @param onClosePress Callback when the close (X) button is pressed.
 * @param onNextPress Callback when the Next/Finish button is pressed.
 * @param modifier Modifier for this composable.
 * @param isLastStep Whether this is the final step in the entire tutorial.
 * @param showQuickTutorialHeader Whether to show "Quick tutorial" back link instead of back arrow.
 */
@Composable
fun TutorialStepScreen(
    chapterTitle: String,
    step: TutorialStep,
    onBackPress: () -> Unit,
    onClosePress: () -> Unit,
    onNextPress: () -> Unit,
    modifier: Modifier = Modifier,
    isLastStep: Boolean = false,
    showQuickTutorialHeader: Boolean = false,
) {
    val context = LocalContext.current
    val isScribeActive = isScribeKeyboardActive(context)

    if (!isScribeActive) {
        WrongKeyboardScreen(
            onBackPress = onBackPress,
            onClosePress = onClosePress,
        )
        return
    }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) TutorialColors.darkBackground else TutorialColors.lightBackground
    val cardBackground = if (isDarkTheme) TutorialColors.cardBackgroundDark else TutorialColors.cardBackgroundLight
    val textColor = if (isDarkTheme) TutorialColors.textPrimaryDark else TutorialColors.textPrimary

    var userInput by remember { mutableStateOf("") }

    val validationState =
        when {
            !step.requiresValidation -> InputValidationState.CORRECT
            userInput.isEmpty() -> InputValidationState.EMPTY
            userInput.trim().equals(step.expectedWord, ignoreCase = false) -> InputValidationState.CORRECT
            else -> InputValidationState.INCORRECT
        }

    val errorText =
        if (step.errorMessage.isNotEmpty()) {
            step.errorMessage
        } else {
            "Not quite! Try writing ${step.expectedWord}."
        }

    Column(
        modifier =
            modifier
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
            IconButton(onClick = onBackPress) {
                if (showQuickTutorialHeader) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                        Text(
                            text = "Quick tutorial",
                            color = Color.White,
                            fontSize = 14.sp,
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            IconButton(onClick = onClosePress) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close tutorial",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Chapter title
        Text(
            text = chapterTitle,
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
                    .padding(horizontal = 20.dp)
                    .border(
                        width = 2.dp,
                        color = TutorialColors.accentYellow,
                        shape = RoundedCornerShape(12.dp),
                    ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                // Instruction text
                Text(
                    text = step.instruction,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Language hint
                Row(
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "\uD83C\uDF10 ",
                        fontSize = 14.sp,
                    )
                    Text(
                        text = step.hint,
                        color = if (isDarkTheme) TutorialColors.textSecondaryDark else TutorialColors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text input field
                HorizontalDivider(
                    color = if (isDarkTheme) TutorialColors.dividerDark else TutorialColors.dividerLight,
                )

                BasicTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                    textStyle =
                        TextStyle(
                            color = textColor,
                            fontSize = 16.sp,
                        ),
                    cursorBrush = SolidColor(textColor),
                    singleLine = true,
                )

                HorizontalDivider(
                    color = if (isDarkTheme) TutorialColors.dividerDark else TutorialColors.dividerLight,
                )

                // Validation feedback
                when (validationState) {
                    InputValidationState.CORRECT -> {
                        if (step.requiresValidation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = step.successMessage,
                                color = TutorialColors.successGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    InputValidationState.INCORRECT -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorText,
                            color = TutorialColors.errorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    InputValidationState.EMPTY -> {
                        // No feedback when empty.
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next / Finish button
        Button(
            onClick = onNextPress,
            enabled = validationState == InputValidationState.CORRECT,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = TutorialColors.accentYellow,
                    contentColor = Color.White,
                    disabledContainerColor = TutorialColors.accentYellow.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f),
                ),
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
        ) {
            Text(
                text = if (isLastStep) "Finish tutorial" else "Next",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
