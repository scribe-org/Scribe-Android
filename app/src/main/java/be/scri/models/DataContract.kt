// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data structure to hold various linguistic data for the Scribe keyboard.
 *
 * This includes information such as gender classifications, verb conjugations, and number representations
 * for different languages, allowing for processing of language-specific grammar rules.
 */
@Serializable
data class DataContract(
    val numbers: Map<String, String>,
    val genders: Genders,
    val conjugations: Map<Int, TenseGroup>,
    val translations: Translations,
)

/**
 * Represents different grammatical gender groupings.
 */
@Serializable
data class Genders(
    val canonical: List<String>,
    val feminines: List<String>,
    val masculines: List<String>,
    val commons: List<String>,
    val neuters: List<String>,
)

/**
 * Represents a tense group (e.g., "Präsens", "Preterite", etc.)
 */
@Serializable
data class TenseGroup(
    val sectionTitle: String = "",
    val tenses: Map<Int, ConjugationCategory>,
)

/**
 * Represents a sub-group of conjugations within a tense (e.g., regular/irregular variations).
 */
@Serializable
data class ConjugationCategory(
    val tenseTitle: String = "",
    val tenseForms: Map<Int, TenseForm>,
)

@Serializable
data class TenseForm(
    val label: String,
    val value: String,
)

/**
 * Represents the structure of translations for different word types.
 */
@Serializable
data class Translations(
    val wordType: WordType,
)

/**
 * Represents the various parts of speech and their associated display values and section titles.
 */
@Serializable
data class WordType(
    val sectionTitle: String,
    val adjective: WordTypeEntry,
    val adverb: WordTypeEntry,
    val article: WordTypeEntry,
    val conjunction: WordTypeEntry,
    val noun: WordTypeEntry,
    val postposition: WordTypeEntry,
    val preposition: WordTypeEntry,
    @SerialName("proper_noun") val properNoun: WordTypeEntry,
    val pronoun: WordTypeEntry,
    val verb: WordTypeEntry,
)

/**
 * Represents the display value and section title for a specific part of speech.
 */
@Serializable
data class WordTypeEntry(
    val displayValue: String,
    val sectionTitle: String,
)
