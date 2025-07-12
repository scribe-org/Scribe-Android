// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import be.scri.helpers.DatabaseFileManager

class PluralFormsManager(private val fileManager: DatabaseFileManager) {
    private val pluralCache = mutableMapOf<String, List<String>>()
    private val germanPlurals = setOf("leben", "daten", "schmerzen")
    private val englishSingulars = setOf("news", "physics", "mathematics")

    fun isAlreadyPlural(language: String, word: String, jsonData: DataContract?): Boolean {
        if (word.isEmpty()) return false
        
        return try {
            when (language.uppercase()) {
                "DE" -> word.endsWith("en", true) || word.lowercase() in germanPlurals
                "EN" -> word.endsWith("s", true) && word.lowercase() !in englishSingulars
                "ES" -> word.endsWith("s", true) || word.endsWith("es", true)
                "FR" -> word.endsWith("x", true) || word.endsWith("s", true)
                else -> false
            } || getCachedPluralForms(language, jsonData)?.any { 
                it.equals(word, true) 
            } == true
        } catch (e: Exception) {
            Log.e("PluralForms", "Error checking plural for $word", e)
            false
        }
    }

    fun getPluralRepresentation(
        language: String, 
        jsonData: DataContract?, 
        noun: String
    ): Map<String, String?> {
        return jsonData?.numbers?.let { numbers ->
            numbers.keys.firstOrNull()?.let { singularCol ->
                numbers.values.firstOrNull()?.let { pluralCol ->
                    fileManager.getLanguageDatabase(language)?.use { db ->
                        db.rawQuery(
                            "SELECT `$singularCol`, `$pluralCol` FROM nouns WHERE `$singularCol` = ? COLLATE NOCASE",
                            arrayOf(noun)
                        ).use { cursor -> 
                            if (cursor.moveToFirst()) {
                                mapOf(cursor.getString(0) to cursor.getString(1))
                            } else {
                                emptyMap()
                            }
                        }
                    }
                }
            }
        } ?: emptyMap()
    }

    private fun getCachedPluralForms(
        language: String, 
        jsonData: DataContract?
    ): List<String>? {
        val cacheKey = "$language-${jsonData?.numbers?.values?.hashCode()}"
        return pluralCache.getOrPut(cacheKey) {
            jsonData?.numbers?.values?.toList()?.takeIf { it.isNotEmpty() }?.let { forms ->
                fileManager.getLanguageDatabase(language)?.use { db ->
                    val result = mutableListOf<String>()
                    val query = "SELECT ${forms.joinToString(", ") { "`$it`" }} FROM nouns"
                    
                    db.rawQuery(query, null).use { cursor ->
                        if (cursor.moveToFirst()) {
                            do {
                                forms.indices.forEach { i ->
                                    cursor.getString(i)?.takeIf { it.isNotBlank() }?.let { 
                                        result.add(it) 
                                    }
                                }
                            } while (cursor.moveToNext())
                        }
                    }
                    result
                }
            } ?: emptyList()
        }.takeIf { it.isNotEmpty() }
    }
}