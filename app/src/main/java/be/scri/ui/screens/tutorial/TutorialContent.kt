// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

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
    fun getNounAnnotationSteps(languageCode: String): List<TutorialStep> {
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
                    "Write the word \"$motherWord\". Notice the word suggestions " +
                        "that appear on the keyboard's top bar.\n\n" +
                        "Then, press space. You will see the word's gender " +
                        "tag on the keyboard's top bar \u2013 in this case, \"$motherTag\" for $motherGender.",
                expectedWord = motherWord,
            ),
            TutorialStep(
                instruction =
                    "Now write the word \"$fatherWord\" and then press space. " +
                        "The gender tag will be \"$fatherTag\", for $fatherGender.",
                expectedWord = fatherWord,
            ),
        )
    }

    /**
     * Chapter 2: Word Translation.
     * Teaches users how to use the Translate command via the Scribe key.
     */
    fun wordTranslationSteps(languageCode: String): List<TutorialStep> {
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
                    "Let's translate! Tap the pencil-like Scribe key on the top-left " +
                        "corner of your keyboard, and select $translation.\n\n" +
                        "Then write the word you want to translate, press \u25B6, " +
                        "and the translation will be returned to you.",
                requiresValidation = false,
            ),
        )
    }

    fun verbConjugationSteps(languageCode: String): List<TutorialStep> {
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
                    "On to the verbs. Tap the pencil-like Scribe key on the top-left " +
                        "corner of your keyboard, and select $conjugation.\n\n" +
                        "Write the verb you want to conjugate, press \u25B6, and " +
                        "you will see a table with all the verb tenses. Select " +
                        "the one you need and it will be inserted!",
                requiresValidation = false,
            ),
        )
    }

    fun nounPluralsSteps(languageCode: String): List<TutorialStep> {
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
                    "Finding the plural of a noun with Scribe is easy. Tap " +
                        "the \u27A1 Scribe key on the top-left corner of your " +
                        "keyboard, and select $plural.\n\n" +
                        "Then write the noun you want the plural for, press " +
                        "\u25B6, and the plural will be returned to you.",
                requiresValidation = false,
            ),
        )
    }

    /** Returns all chapters as a list of pairs (title, steps). */
    fun getAllChapters(languageCode: String = "de"): List<Pair<String, List<TutorialStep>>> =
        listOf(
            "Noun annotation" to getNounAnnotationSteps(languageCode),
            "Word translation" to wordTranslationSteps(languageCode),
            "Verb conjugation" to verbConjugationSteps(languageCode),
            "Noun plurals" to nounPluralsSteps(languageCode),
        )
}
