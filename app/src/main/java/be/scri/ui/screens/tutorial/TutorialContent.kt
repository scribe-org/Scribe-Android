// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import android.content.Context
import be.scri.R

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
    fun getNounAnnotationSteps(languageCode: String, context: Context): List<TutorialStep> {
        val (fatherWord, fatherTag, fatherGender) =
            when (languageCode) {
                "en" -> Triple("father", "M", "Masculine")
                "es" -> Triple("padre", "M", "Masculino")
                "fr" -> Triple("père", "M", "Masculin")
                "it" -> Triple("padre", "M", "Maschile")
                "pt" -> Triple("pai", "M", "Masculino")
                "ru" -> Triple("отец", "M", "Мужской")
                "sv" -> Triple("far", "C", "Common")
                else -> Triple("Vater", "M", "Maskulin")
            }

        val (motherWord, motherTag, motherGender) =
            when (languageCode) {
                "en" -> Triple("mother", "F", "Feminine")
                "es" -> Triple("madre", "F", "Femenino")
                "fr" -> Triple("mère", "F", "Féminin")
                "it" -> Triple("madre", "F", "Femminile")
                "pt" -> Triple("mãe", "F", "Feminino")
                "ru" -> Triple("мать", "F", "Женский")
                "sv" -> Triple("mor", "C", "Common")
                else -> Triple("Mutter", "F", "Feminin")
            }

        return listOf(
            TutorialStep(
                instruction =
                    context.getString(R.string.i18n_app_keyboard_tutorial_noun_annotation_instruction_1)
                        .replace("{mother_word}", motherWord)
                        .replace("{mother_tag}", motherTag)
                        .replace("{mother_gender}", motherGender),
                expectedWord = motherWord,
            ),
            TutorialStep(
                instruction =
                    context.getString(R.string.i18n_app_keyboard_tutorial_noun_annotation_instruction_2)
                        .replace("{father_word}", fatherWord)
                        .replace("{father_tag}", fatherTag)
                        .replace("{father_gender}", fatherGender),
                expectedWord = fatherWord,
            ),
        )
    }

    /**
     * Chapter 2: Word Translation.
     * Teaches users how to use the Translate command via the Scribe key.
     */
    fun wordTranslationSteps(languageCode: String, context: Context): List<TutorialStep> {
        val translation =
            when (languageCode) {
                "en" -> "Translate"
                "es" -> "Traducir"
                "fr" -> "Traduire"
                "it" -> "Tradurre"
                "pt" -> "Traduzir"
                "ru" -> "Перевести"
                "sv" -> "Översätt"
                else -> "Übersetzen"
            }
        return listOf(
            TutorialStep(
                instruction =
                    context.getString(R.string.i18n_app_keyboard_tutorial_word_translation_instruction)
                        .replace("{translate}", translation),
                requiresValidation = false,
            ),
        )
    }

    fun verbConjugationSteps(languageCode: String, context: Context): List<TutorialStep> {
        val conjugation =
            when (languageCode) {
                "en" -> "Conjugate"
                "es" -> "Conjugar"
                "fr" -> "Conjuguer"
                "it" -> "Coniugare"
                "pt" -> "Conjugar"
                "ru" -> "Спрягать"
                "sv" -> "Konjugera"
                else -> "Konjugieren"
            }
        return listOf(
            TutorialStep(
                instruction =
                    context.getString(R.string.i18n_app_keyboard_tutorial_verb_conjugation_instruction)
                        .replace("{conjugate}", conjugation),
                requiresValidation = false,
            ),
        )
    }

    fun nounPluralsSteps(languageCode: String, context: Context): List<TutorialStep> {
        val plural =
            when (languageCode) {
                "en" -> "Plural"
                "es" -> "Plural"
                "fr" -> "Pluriel"
                "it" -> "Plurale"
                "pt" -> "Plural"
                "ru" -> "Множ-ое"
                "sv" -> "Plural"
                else -> "Plural"
            }
        return listOf(
            TutorialStep(
                instruction =
                    context.getString(R.string.i18n_app_keyboard_tutorial_noun_plurals_instruction)
                        .replace("{plural}", plural),
                requiresValidation = false,
            ),
        )
    }

    /** Returns all chapters as a list of pairs (title, steps). */
    fun getAllChapters(languageCode: String = "de", context: Context): List<Pair<String, List<TutorialStep>>> =
        listOf(
            context.getString(R.string.i18n_app_keyboard_tutorial_noun_annotation) to getNounAnnotationSteps(languageCode, context),
            context.getString(R.string.i18n_app_keyboard_tutorial_word_translation) to wordTranslationSteps(languageCode, context),
            context.getString(R.string.i18n_app_keyboard_tutorial_verb_conjugation) to verbConjugationSteps(languageCode, context),
            context.getString(R.string.i18n_app_keyboard_tutorial_noun_plurals) to nounPluralsSteps(languageCode, context),
        )
}
