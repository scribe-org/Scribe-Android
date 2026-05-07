// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Defines all tutorial chapters and their steps.
 * Each chapter contains one or more interactive steps that guide the user
 * through a specific Scribe feature.
 */
object TutorialContent {
    /**
     * Chapter 1: Noun Annotation.
     * Teaches users about gender tags that appear when typing nouns.
     */
    val nounAnnotationSteps =
        listOf(
            TutorialStep(
                instruction =
                    "Write the word \"Vater\". Notice the word suggestions " +
                        "that appear on the keyboard's top bar.\n\n" +
                        "Then, press space. You will see the word's gender " +
                        "tag on the keyboard's top bar \u2013 in this case, \"M\" for Maskulin.",
                expectedWord = "Vater",
            ),
            TutorialStep(
                instruction =
                    "Now write the word \"Mutter\" and then press space. " +
                        "The gender tag will be \"F\", for Feminin.",
                expectedWord = "Mutter",
            ),
        )

    /**
     * Chapter 2: Word Translation.
     * Teaches users how to use the Translate command via the Scribe key.
     */
    val wordTranslationSteps =
        listOf(
            TutorialStep(
                instruction =
                    "Let's translate! Tap the \u27A1 Scribe key on the top-left " +
                        "corner of your keyboard, and select \u00DCbersetzen.\n\n" +
                        "Then write the word you want to translate, press \u25B6, " +
                        "and the translation will be returned to you.",
                hint = "If your second language is not German, change the language in your keyboard.",
                requiresValidation = false,
            ),
        )

    val verbConjugationSteps =
        listOf(
            TutorialStep(
                instruction =
                    "On to the verbs. Tap the \u27A1 Scribe key on the top-left " +
                        "corner of your keyboard, and select Konjugieren.\n\n" +
                        "Write the verb you want to conjugate, press \u25B6, and " +
                        "you will see a table with all the verb tenses. Select " +
                        "the one you need and it will be inserted!",
                hint = "If your second language is not German, change the language in your keyboard.",
                requiresValidation = false,
            ),
        )

    val nounPluralsSteps =
        listOf(
            TutorialStep(
                instruction =
                    "Finding the plural of a noun with Scribe is easy. Tap " +
                        "the \u27A1 Scribe key on the top-left corner of your " +
                        "keyboard, and select Plural.\n\n" +
                        "Then write the noun you want the plural for, press " +
                        "\u25B6, and the plural will be returned to you.",
                hint = "If your second language is not German, change the language in your keyboard.",
                requiresValidation = false,
            ),
        )

    /** Returns all chapters as a list of pairs (title, steps). */
    fun getAllChapters(): List<Pair<String, List<TutorialStep>>> =
        listOf(
            "Noun annotation" to nounAnnotationSteps,
            "Word translation" to wordTranslationSteps,
            "Verb conjugation" to verbConjugationSteps,
            "Noun plurals" to nounPluralsSteps,
        )
}

/**
 * The main tutorial navigation controller.
 * Manages the flow between the tutorial home screen, individual chapters, and steps.
 * Handles forward/backward navigation and tracks the user's current position.
 *
 * @param onTutorialExit Callback when the user exits the tutorial (back to About tab).
 */
@Composable
fun TutorialNavigator(onTutorialExit: () -> Unit) {
    var currentScreen by remember { mutableStateOf("home") }
    var currentChapterIndex by remember { mutableIntStateOf(0) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isFullTutorial by remember { mutableStateOf(false) }

    val allChapters = TutorialContent.getAllChapters()

    when (currentScreen) {
        "home" -> {
            TutorialHomeScreen(
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
                chapterTitle = chapterTitle,
                step = step,
                isLastStep = isLastStep,
                showQuickTutorialHeader = !isFullTutorial && currentStepIndex == 0,
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
                onClosePress = {
                    currentScreen = "home"
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
