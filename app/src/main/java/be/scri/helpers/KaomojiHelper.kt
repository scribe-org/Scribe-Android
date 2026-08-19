// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context

const val KAOMOJI_SPEC_FILE_PATH = "kaomoji_spec.txt"
private const val PREF_RECENT_KAOMOJI_KEY = "recent_kaomojis_list"
private const val MAX_RECENT_KAOMOJI_COUNT = 20

/**
 * Data class representing a single Kaomoji emoticon with its category and display text.
 *
 * @param category The category this kaomoji belongs to (e.g. kaomoji_joy, kaomoji_sad).
 * @param kaomoji The actual kaomoji text string (e.g. ¯\_(ツ)_/¯).
 * @param name Optional human readable label/description.
 */
data class KaomojiData(
    val category: String,
    val kaomoji: String,
    val name: String = "",
)

/**
 * Parses raw kaomoji specification asset file.
 *
 * @param context The application context used to access assets.
 * @param path The path to the kaomoji spec file within assets.
 * @return A list of [KaomojiData] objects parsed from the asset file.
 */
fun parseRawKaomojiSpecsFile(
    context: Context,
    path: String = KAOMOJI_SPEC_FILE_PATH,
): List<KaomojiData> {
    val kaomojis = mutableListOf<KaomojiData>()
    var category: String? = null

    try {
        context.assets.open(path).bufferedReader().useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("#") || trimmed.isEmpty() -> continue
                    trimmed.startsWith("[") && trimmed.endsWith("]") -> {
                        category = trimmed.substring(1, trimmed.length - 1)
                    }
                    else -> {
                        val parts = trimmed.split(";")
                        if (parts.isNotEmpty() && category != null) {
                            val kaomojiText = parts[0].trim()
                            val name = if (parts.size > 1) parts[1].trim() else ""
                            if (kaomojiText.isNotEmpty()) {
                                kaomojis.add(KaomojiData(category!!, kaomojiText, name))
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("KaomojiHelper", "Error parsing kaomoji spec file: ${e.message}", e)
    }

    return kaomojis
}

/**
 * Saves a Kaomoji to the recent history list in SharedPreferences.
 */
fun saveRecentKaomoji(context: Context, kaomoji: KaomojiData) {
    try {
        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        val currentRecents = getRecentKaomojis(context).map { it.kaomoji }.toMutableList()
        currentRecents.remove(kaomoji.kaomoji)
        currentRecents.add(0, kaomoji.kaomoji)
        if (currentRecents.size > MAX_RECENT_KAOMOJI_COUNT) {
            currentRecents.subList(MAX_RECENT_KAOMOJI_COUNT, currentRecents.size).clear()
        }
        val joined = currentRecents.joinToString("\n")
        prefs.edit().putString(PREF_RECENT_KAOMOJI_KEY, joined).apply()
    } catch (e: Exception) {
        android.util.Log.e("KaomojiHelper", "Error saving recent kaomoji: ${e.message}", e)
    }
}

/**
 * Retrieves the list of recently used Kaomojis from SharedPreferences.
 */
fun getRecentKaomojis(context: Context): List<KaomojiData> {
    val result = mutableListOf<KaomojiData>()
    try {
        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        val joined = prefs.getString(PREF_RECENT_KAOMOJI_KEY, "") ?: ""
        if (joined.isNotBlank()) {
            val lines = joined.split("\n")
            for (line in lines) {
                if (line.isNotBlank()) {
                    result.add(KaomojiData("kaomoji_recent", line.trim()))
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("KaomojiHelper", "Error getting recent kaomojis: ${e.message}", e)
    }
    return result
}
