// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context

private const val PREFS_NAME = "recent_emojis"
private const val KEY_RECENT = "recent_emoji_list"
private const val MAX_RECENT = 30

fun recordRecentEmoji(
    context: Context,
    emoji: String,
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current =
        prefs
            .getString(KEY_RECENT, "")!!
            .split(",")
            .filter { it.isNotBlank() }
            .toMutableList()
    current.remove(emoji)
    current.add(0, emoji)
    while (current.size > MAX_RECENT) current.removeAt(current.lastIndex)
    prefs.edit().putString(KEY_RECENT, current.joinToString(",")).apply()
}

fun getRecentEmojis(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_RECENT, "")!!.split(",").filter { it.isNotBlank() }
}
