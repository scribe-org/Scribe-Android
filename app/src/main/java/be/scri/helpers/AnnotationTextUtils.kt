// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import be.scri.R
import be.scri.helpers.LanguageMappingConstants.nounAnnotationConversionDict
import be.scri.helpers.LanguageMappingConstants.prepAnnotationConversionDict

/**
 * Utility object for handling the display text and color of annotations
 * related to noun cases, genders, and types.
 *
 * This object provides functions to map raw annotation strings (e.g., "genitive case", "masculine")
 * to user-friendly display text and corresponding color resources. It also handles
 * language-specific conversions for abbreviations of cases and genders.
 */
object AnnotationTextUtils {
    /**
     * Maps a case annotation string (e.g., "genitive case") to a displayable text and color.
     *
     * @param nounType The case annotation string.
     *
     * @return A pair containing the color resource ID and the display text.
     */
    fun handleTextForCaseAnnotation(
        nounType: String,
        language: String,
        context: Context,
    ): Pair<Int, String> {
        val color = R.color.annotateOrange
        val suggestionMap =
            mapOf(
                "genitive case" to Pair(color, processValuesForPreposition(language, "Gen")),
                "accusative case" to Pair(color, processValuesForPreposition(language, "Acc")),
                "dative case" to Pair(color, processValuesForPreposition(language, "Dat")),
                "locative case" to Pair(color, processValuesForPreposition(language, "Loc")),
                "Prepositional case" to Pair(color, processValuesForPreposition(language, "Pre")),
                "Instrumental case" to Pair(color, processValuesForPreposition(language, "Ins")),
            )
        return suggestionMap[nounType] ?: Pair(R.color.transparent, context.getString(R.string.suggestion))
    }

    /**
     * Maps a noun type string (e.g., "masculine") to a displayable text and color.
     *
     * @param nounType The noun type or gender string.
     *
     * @return A pair containing the color resource ID and the display text.
     */
    fun handleColorAndTextForNounType(
        nounType: String,
        language: String,
        context: Context,
    ): Pair<Int, String> {
        val suggestionMap =
            mapOf(
                "PL" to Pair(R.color.annotateOrange, "PL"),
                "neuter" to Pair(R.color.annotateGreen, processValueForNouns(language, "N")),
                "common of two genders" to Pair(R.color.annotatePurple, processValueForNouns(language, "C")),
                "common" to Pair(R.color.annotatePurple, processValueForNouns(language, "C")),
                "masculine" to Pair(R.color.annotateBlue, processValueForNouns(language, "M")),
                "feminine" to Pair(R.color.annotateRed, processValueForNouns(language, "F")),
            )
        return suggestionMap[nounType] ?: Pair(R.color.transparent, context.getString(R.string.suggestion))
    }

    /**
     * Processes a noun gender abbreviation for display, converting it based on language-specific conventions.
     *
     * @param language The current keyboard language.
     * @param text The gender abbreviation (e.g., "M", "F", "N").
     *
     * @return The language-specific display text (e.g., "М" for Russian masculine).
     */
    fun processValueForNouns(
        language: String,
        text: String,
    ): String = nounAnnotationConversionDict[language]?.get(text) ?: text

    /**
     * Processes a preposition case abbreviation for display, converting it based on language-specific conventions.
     *
     * @param language The current keyboard language.
     * @param text The case abbreviation (e.g., "Acc", "Dat").
     *
     * @return The language-specific display text (e.g., "Akk" for German accusative).
     */
    fun processValuesForPreposition(
        language: String,
        text: String,
    ): String = prepAnnotationConversionDict[language]?.get(text) ?: text
}
