// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.data

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import be.scri.data.remote.SqlIdentifier

fun SQLiteDatabase.tableExists(tableName: String): Boolean =
    rawQuery(
        "SELECT name FROM sqlite_master WHERE type='table' AND LOWER(name) = LOWER(?)",
        arrayOf(tableName),
    ).use { it.moveToFirst() }

fun SQLiteDatabase.columnExists(
    tableName: String,
    columnName: String,
): Boolean =
    rawQuery("PRAGMA table_info($tableName)", null).use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex("name")
            do {
                if (cursor.getString(nameIndex) == columnName) {
                    return true
                }
            } while (cursor.moveToNext())
        }
        false
    }

fun SQLiteDatabase.getTableColumns(tableName: String): List<String> {
    if (!SqlIdentifier.isValid(tableName)) return emptyList()
    return rawQuery("PRAGMA table_info(${SqlIdentifier.quote(tableName)})", null).use { cursor ->
        val columns = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex != -1) {
                do {
                    cursor.getString(nameIndex)?.let { columns.add(it) }
                } while (cursor.moveToNext())
            }
        }
        columns
    }
}

fun SQLiteDatabase.getInfinitiveColumnName(): String? {
    val candidates = listOf("infinitive", "verb", "activeInfinitive", "simplePresent")
    for (candidate in candidates) {
        if (columnExists("verbs", candidate)) {
            return candidate
        }
    }
    return null
}

/**
 * Reads the lowercase profane words from the `profanity` table, splitting alternative spellings ("a | b").
 *
 * @return The set of profane words, or an empty set if the table does not exist.
 */
fun SQLiteDatabase.getProfanity(): Set<String> {
    if (!tableExists("profanity") || !columnExists("profanity", "profanity")) return emptySet()
    val profanity = mutableSetOf<String>()
    try {
        rawQuery("SELECT profanity FROM profanity", null).use { cursor ->
            while (cursor.moveToNext()) {
                cursor
                    .getString(0)
                    ?.split("|")
                    ?.map { it.trim().lowercase() }
                    ?.filterTo(profanity) { it.isNotEmpty() }
            }
        }
    } catch (e: SQLiteException) {
        return emptySet()
    }
    return profanity
}
