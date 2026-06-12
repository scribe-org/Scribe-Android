// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

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
 * @param onNextPress Callback when the Next/Finish button is pressed.
 * @param modifier Modifier for this composable.
 * @param isLastStep Whether this is the final step in the entire tutorial.
 */
@Composable
fun TutorialStepScreen(
    chapterTitle: String,
    step: TutorialStep,
    onBackPress: () -> Unit,
    onNextPress: () -> Unit,
    modifier: Modifier = Modifier,
    isLastStep: Boolean = false,
) {
    val context = LocalContext.current
    var isScribeActive by remember { mutableStateOf(isScribeKeyboardActive(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isScribeActive = isScribeKeyboardActive(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            isScribeActive = isScribeKeyboardActive(context)
        }
    }

    if (!isScribeActive) {
        WrongKeyboardScreen(
            onBackPress = onBackPress,
        )
        return
    }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val headerColor = MaterialTheme.colorScheme.onBackground
    val successColor = if (isDarkTheme) Color(0xFF08A045) else Color(0xFF9BC53D)
    val errorColor = Color(0xFFE53935)

    var userInput by remember(step) { mutableStateOf("") }

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

        // Chapter title
        Text(
            text = chapterTitle,
            color = headerColor,
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
                        color = Color.Transparent,
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
                        color = textSecondaryColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text input field
                HorizontalDivider(
                    color = dividerColor,
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
                    color = dividerColor,
                )

                // Validation feedback
                when (validationState) {
                    InputValidationState.CORRECT -> {
                        if (step.requiresValidation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = step.successMessage,
                                color = successColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    InputValidationState.INCORRECT -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorText,
                            color = errorColor,
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
                    containerColor = primaryColor,
                    contentColor = if (isDarkTheme) Color.White else Color.Black,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f),
                    disabledContentColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.5f),
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
