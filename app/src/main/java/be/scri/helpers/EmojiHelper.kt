// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import be.scri.R

private var cachedEmojiData: MutableList<EmojiData>? = null

const val EMOJI_SPEC_FILE_PATH = "emoji_spec.txt"

/**
 * Reads the emoji spec file and returns a parsed list of EmojiData.
 *
 * @param context The application context used to access assets.
 * @param path The path to the emoji spec file within assets.
 * @return A mutable list of [EmojiData] objects parsed from the file.
 */
fun parseRawEmojiSpecsFile(
    context: Context,
    path: String,
): MutableList<EmojiData> {
    val emojis = mutableListOf<EmojiData>()
    var emojiEditorList: MutableList<String>? = null
    var category: String? = null

    fun commitEmojiEditorList() {
        emojiEditorList?.let {
            val base = it.first()
            val variants = it.drop(1)
            emojis.add(EmojiData(category ?: "none", base, variants))
        }
        emojiEditorList = null
    }

    context.assets.open(path).bufferedReader().useLines { lines ->
        for (line in lines) {
            when {
                line.startsWith("#") -> { }
                line.startsWith("[") -> {
                    commitEmojiEditorList()
                    category = line.replace("[", "").replace("]", "")
                }
                line.trim().isEmpty() -> continue
                else -> {
                    if (!line.startsWith("\t")) {
                        commitEmojiEditorList()
                    }
                    val data = line.split(";")
                    if (data.size == 3) {
                        val emoji = data[0].trim()
                        if (emojiEditorList != null) {
                            emojiEditorList!!.add(emoji)
                        } else {
                            emojiEditorList = mutableListOf(emoji)
                        }
                    }
                }
            }
        }
        commitEmojiEditorList()
    }

    cachedEmojiData = emojis
    return emojis
}

/**
 * Data class representing a single emoji with its category and skin tone variants.
 *
 * @param category The category this emoji belongs to.
 * @param emoji The base emoji character string.
 * @param variants The list of skin tone or other variants.
 */
data class EmojiData(
    val category: String,
    val emoji: String,
    val variants: List<String>,
)

/**
 * Returns the drawable resource ID for a given emoji category icon.
 *
 * @param category The category name from the emoji spec file.
 * @return The drawable resource ID for the category icon.
 */
fun getCategoryIconRes(category: String): Int =
    when (category) {
        "smileys_emotion" -> R.drawable.ic_emoji_smileys
        "people_body" -> R.drawable.ic_emoji_people
        "animals_nature" -> R.drawable.ic_emoji_animals
        "food_drink" -> R.drawable.ic_emoji_food
        "travel_places" -> R.drawable.ic_emoji_travel
        "activities" -> R.drawable.ic_emoji_activities
        "objects" -> R.drawable.ic_emoji_objects
        "symbols" -> R.drawable.ic_emoji_symbols
        "flags" -> R.drawable.ic_emoji_flags
        else -> R.drawable.ic_emoji_vector
    }
