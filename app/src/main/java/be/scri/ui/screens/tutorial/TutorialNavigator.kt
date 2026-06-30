// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private fun getCurrentScribeLanguage(context: Context): String {
    val currentImeId =
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        ) ?: ""

    return when {
        currentImeId.contains("EnglishKeyboardIME") -> "en"
        currentImeId.contains("SpanishKeyboardIME") -> "es"
        currentImeId.contains("FrenchKeyboardIME") -> "fr"
        currentImeId.contains("ItalianKeyboardIME") -> "it"
        currentImeId.contains("PortugueseKeyboardIME") -> "pt"
        currentImeId.contains("RussianKeyboardIME") -> "ru"
        currentImeId.contains("SwedishKeyboardIME") -> "sv"
        else -> "de"
    }
}

/**
 * The main tutorial navigation controller.
 * Manages the flow between the tutorial home screen, individual chapters, and steps.
 * Handles forward/backward navigation and tracks the user's current position.
 *
 * @param onTutorialExit Callback when the user exits the tutorial (back to About tab).
 */
@Composable
fun TutorialNavigator(
    onTutorialExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentScreen by remember { mutableStateOf("home") }
    var currentChapterIndex by remember { mutableIntStateOf(0) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isFullTutorial by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var activeLanguageCode by remember {
        mutableStateOf(getCurrentScribeLanguage(context))
    }

    DisposableEffect(context) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    if (intent.action == Intent.ACTION_INPUT_METHOD_CHANGED) {
                        activeLanguageCode = getCurrentScribeLanguage(context)
                    }
                }
            }

        context.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED),
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val allChapters =
        remember(activeLanguageCode) {
            TutorialContent.getAllChapters(activeLanguageCode)
        }

    BackHandler {
        if (currentScreen == "home") {
            onTutorialExit()
        } else {
            when {
                currentStepIndex > 0 -> {
                    currentStepIndex--
                }
                isFullTutorial && currentChapterIndex > 0 -> {
                    currentChapterIndex--
                    val prevSteps = allChapters[currentChapterIndex].second
                    currentStepIndex = prevSteps.size - 1
                }
                else -> {
                    currentScreen = "home"
                }
            }
        }
    }

    when (currentScreen) {
        "home" -> {
            TutorialHomeScreen(
                modifier = modifier,
                onBackPress = onTutorialExit,
                onChapterSelect = { chapterIndex ->
                    currentChapterIndex = chapterIndex
                    currentStepIndex = 0
                    isFullTutorial = false
                    currentScreen = "step"
                },
                onStartFullTutorial = {
                    currentChapterIndex = 0
                    currentStepIndex = 0
                    isFullTutorial = true
                    currentScreen = "step"
                },
            )
        }
        "step" -> {
            val (chapterTitle, steps) = allChapters[currentChapterIndex]
            val step = steps[currentStepIndex]

            val isLastStepInChapter = currentStepIndex == steps.size - 1
            val isLastChapter = currentChapterIndex == allChapters.size - 1
            val isLastStep = isLastStepInChapter && (isLastChapter || !isFullTutorial)

            TutorialStepScreen(
                modifier = modifier,
                chapterTitle = chapterTitle,
                step = step,
                isLastStep = isLastStep,
                onBackPress = {
                    when {
                        currentStepIndex > 0 -> {
                            currentStepIndex--
                        }
                        isFullTutorial && currentChapterIndex > 0 -> {
                            currentChapterIndex--
                            val prevSteps = allChapters[currentChapterIndex].second
                            currentStepIndex = prevSteps.size - 1
                        }
                        else -> {
                            currentScreen = "home"
                        }
                    }
                },
                onNextPress = {
                    when {
                        !isLastStepInChapter -> {
                            currentStepIndex++
                        }
                        isFullTutorial && !isLastChapter -> {
                            currentChapterIndex++
                            currentStepIndex = 0
                        }
                        else -> {
                            currentScreen = "home"
                        }
                    }
                },
            )
        }
    }
}
