// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

/**
 * Defines all tutorial chapters and their steps.
 * Each chapter contains one or more interactive steps that guide the user
 * through a specific Scribe feature.
 */
object TutorialContent {

    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "es" -> "Spanish"
            "fr" -> "French"
            "it" -> "Italian"
            "pt" -> "Portuguese"
            "ru" -> "Russian"
            "sv" -> "Swedish"
            else -> "German"
        }
    }

    /**
     * Chapter 1: Noun Annotation.
     * Teaches users about gender tags that appear when typing nouns.
     */
    fun getNounAnnotationSteps(languageCode: String): List<TutorialStep> {
        val language = getLanguageName(languageCode)
        val (fatherWord, fatherTag, fatherGender) = when (languageCode) {
            "en" -> Triple("father", "M", "Masculine")
            "es" -> Triple("padre", "M", "Masculino")
            "fr" -> Triple("père", "M", "Masculin")
            "it" -> Triple("padre", "M", "Maschile")
            "pt" -> Triple("pai", "M", "Masculino")
            "ru" -> Triple("отец", "M", "Мужской")
            "sv" -> Triple("far", "C", "Common")
            else -> Triple("Vater", "M", "Maskulin")
        }

        val (motherWord, motherTag, motherGender) = when (languageCode) {
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
                    "Write the word \"$fatherWord\". Notice the word suggestions " +
                        "that appear on the keyboard's top bar.\n\n" +
                        "Then, press space. You will see the word's gender " +
                        "tag on the keyboard's top bar \u2013 in this case, \"$fatherTag\" for $fatherGender.",
                hint = "If your second language is not $language, change the language in your keyboard.",
                expectedWord = fatherWord,
            ),
            TutorialStep(
                instruction =
                    "Now write the word \"$motherWord\" and then press space. " +
                        "The gender tag will be \"$motherTag\", for $motherGender.",
                hint = "If your second language is not $language, change the language in your keyboard.",
                expectedWord = motherWord,
            ),
        )
    }

    /**
     * Chapter 2: Word Translation.
     * Teaches users how to use the Translate command via the Scribe key.
     */
    fun wordTranslationSteps(languageCode: String): List<TutorialStep>{
        val language = getLanguageName(languageCode)
        return listOf(
            TutorialStep(
                instruction =
                    "Let's translate! Tap the \u27A1 Scribe key on the top-left " +
                        "corner of your keyboard, and select \u00DCbersetzen.\n\n" +
                        "Then write the word you want to translate, press \u25B6, " +
                        "and the translation will be returned to you.",
                hint = "If your second language is not $language, change the language in your keyboard.",
                requiresValidation = false,
            ),
        )}

    fun verbConjugationSteps(languageCode: String) : List<TutorialStep>{
        val language = getLanguageName(languageCode)
        return listOf(
            TutorialStep(
                instruction =
                    "On to the verbs. Tap the \u27A1 Scribe key on the top-left " +
                        "corner of your keyboard, and select Konjugieren.\n\n" +
                        "Write the verb you want to conjugate, press \u25B6, and " +
                        "you will see a table with all the verb tenses. Select " +
                        "the one you need and it will be inserted!",
                hint = "If your second language is not $language, change the language in your keyboard.",
                requiresValidation = false,
            ),
        )}

    fun nounPluralsSteps (languageCode: String) : List<TutorialStep> {
        val language = getLanguageName(languageCode)
        return listOf(
            TutorialStep(
                instruction =
                    "Finding the plural of a noun with Scribe is easy. Tap " +
                        "the \u27A1 Scribe key on the top-left corner of your " +
                        "keyboard, and select Plural.\n\n" +
                        "Then write the noun you want the plural for, press " +
                        "\u25B6, and the plural will be returned to you.",
                hint = "If your second language is not $language, change the language in your keyboard.",
                requiresValidation = false,
            ),
        )}

    /** Returns all chapters as a list of pairs (title, steps). */
    fun getAllChapters(languageCode: String = "de"): List<Pair<String, List<TutorialStep>>> =
        listOf(
            "Noun annotation" to getNounAnnotationSteps(languageCode),
            "Word translation" to wordTranslationSteps(languageCode),
            "Verb conjugation" to verbConjugationSteps(languageCode),
            "Noun plurals" to nounPluralsSteps(languageCode),
        )
}
