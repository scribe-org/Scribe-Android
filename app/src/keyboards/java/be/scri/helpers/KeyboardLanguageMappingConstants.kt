// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portuguese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables

/**
 * Object containing keyboard-specific constant mappings related to language-specific UI elements.
 */
object KeyboardLanguageMappingConstants {
    val translatePlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.TRANSLATE_KEY_LBL,
            "ES" to ESInterfaceVariables.TRANSLATE_KEY_LBL,
            "DE" to DEInterfaceVariables.TRANSLATE_KEY_LBL,
            "IT" to ITInterfaceVariables.TRANSLATE_KEY_LBL,
            "FR" to FRInterfaceVariables.TRANSLATE_KEY_LBL,
            "PT" to PTInterfaceVariables.TRANSLATE_KEY_LBL,
            "RU" to RUInterfaceVariables.TRANSLATE_KEY_LBL,
            "SV" to SVInterfaceVariables.TRANSLATE_KEY_LBL,
        )

    val conjugatePlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.CONJUGATE_KEY_LBL,
            "ES" to ESInterfaceVariables.CONJUGATE_KEY_LBL,
            "DE" to DEInterfaceVariables.CONJUGATE_KEY_LBL,
            "IT" to ITInterfaceVariables.CONJUGATE_KEY_LBL,
            "FR" to FRInterfaceVariables.CONJUGATE_KEY_LBL,
            "PT" to PTInterfaceVariables.CONJUGATE_KEY_LBL,
            "RU" to RUInterfaceVariables.CONJUGATE_KEY_LBL,
            "SV" to SVInterfaceVariables.CONJUGATE_KEY_LBL,
        )

    val pluralPlaceholder =
        mapOf(
            "EN" to ENInterfaceVariables.PLURAL_KEY_LBL,
            "ES" to ESInterfaceVariables.PLURAL_KEY_LBL,
            "DE" to DEInterfaceVariables.PLURAL_KEY_LBL,
            "IT" to ITInterfaceVariables.PLURAL_KEY_LBL,
            "FR" to FRInterfaceVariables.PLURAL_KEY_LBL,
            "PT" to PTInterfaceVariables.PLURAL_KEY_LBL,
            "RU" to RUInterfaceVariables.PLURAL_KEY_LBL,
            "SV" to SVInterfaceVariables.PLURAL_KEY_LBL,
        )

    /**
     * Maps a keyboard language alias to its localized emoji category header labels,
     * keyed by the category name used in the emoji spec file.
     * Categories with a blank translation are omitted so callers can fall back
     * to the default (English/string resource) label.
     */
    val emojiCategoryHeaders =
        mapOf(
            "EN" to
                mapOf(
                    "smileys_emotion" to ENInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to ENInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to ENInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to ENInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to ENInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to ENInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to ENInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to ENInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to ENInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "ES" to
                mapOf(
                    "smileys_emotion" to ESInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to ESInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to ESInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to ESInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to ESInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to ESInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to ESInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to ESInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to ESInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "DE" to
                mapOf(
                    "smileys_emotion" to DEInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to DEInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to DEInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to DEInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to DEInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to DEInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to DEInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to DEInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to DEInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "IT" to
                mapOf(
                    "smileys_emotion" to ITInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to ITInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to ITInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to ITInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to ITInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to ITInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to ITInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to ITInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to ITInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "FR" to
                mapOf(
                    "smileys_emotion" to FRInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to FRInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to FRInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to FRInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to FRInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to FRInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to FRInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to FRInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to FRInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "PT" to
                mapOf(
                    "smileys_emotion" to PTInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to PTInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to PTInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to PTInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to PTInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to PTInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to PTInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to PTInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to PTInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "RU" to
                mapOf(
                    "smileys_emotion" to RUInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to RUInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to RUInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to RUInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to RUInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to RUInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to RUInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to RUInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to RUInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
            "SV" to
                mapOf(
                    "smileys_emotion" to SVInterfaceVariables.SMILEYS_EMOTIONS_EMOJI_HEADER,
                    "people_body" to SVInterfaceVariables.PEOPLE_BODY_EMOJI_HEADER,
                    "animals_nature" to SVInterfaceVariables.ANIMALS_NATURE_EMOJI_HEADER,
                    "food_drink" to SVInterfaceVariables.FOOD_DRINK_EMOJI_HEADER,
                    "travel_places" to SVInterfaceVariables.TRAVEL_PLACES_EMOJI_HEADER,
                    "activities" to SVInterfaceVariables.ACTIVITIES_EMOJI_HEADER,
                    "objects" to SVInterfaceVariables.OBJECTS_EMOJI_HEADER,
                    "symbols" to SVInterfaceVariables.SYMBOLS_EMOJI_HEADER,
                    "flags" to SVInterfaceVariables.FLAGS_EMOJI_HEADER,
                ),
        ).mapValues { (_, headers) -> headers.filterValues { it.isNotBlank() } }
}
