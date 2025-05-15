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
    val conjugations: Map<String, TenseGroup>
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
    val title: String = "",
    val conjugationTypes: Map<String, ConjugationCategory>
)

/**
 * Represents a sub-group of conjugations within a tense (e.g., regular/irregular variations).
 */
@Serializable
data class ConjugationCategory(
    val title: String = "",
    val conjugationForms: Map<String, String> = emptyMap()
)
