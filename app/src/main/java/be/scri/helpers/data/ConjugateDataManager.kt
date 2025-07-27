// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import be.scri.helpers.DatabaseFileManager

class ConjugateDataManager(
    private val fileManager: DatabaseFileManager,
    private val context: Context,
) {
    private val TAG = "ConjugateLog"

    fun getTheConjugateLabels(
        language: String,
        jsonData: DataContract?,
        word: String,
    ): MutableMap<String, MutableMap<String, Collection<String>>> {
        val finalOutput: MutableMap<String, MutableMap<String, Collection<String>>> = mutableMapOf()
        jsonData?.conjugations?.values?.forEach { tenseGroup ->
            val conjugateForms: MutableMap<String, Collection<String>> = mutableMapOf()
            tenseGroup.conjugationTypes.values.forEach { conjugationCategory ->
                val forms =
                    conjugationCategory.conjugationForms.values.map { form ->
                        getTheValueForTheConjugateWord(word, form, language)
                    }
                conjugateForms[conjugationCategory.title] = forms
            }
            finalOutput[tenseGroup.title] = conjugateForms
        }
        Log.d(TAG, "Conjugations for '$word': $finalOutput")

        val autoSuggestionManager = AutoSuggestionDataManager(context, language)

        // We need the auto-suggestion database to get a cursor that processSuggestionRow understands.
        val autoSuggestionDb: SQLiteDatabase? = fileManager.getLanguageDatabase(language)

        if (autoSuggestionDb != null) {
            val columnsToSelectForSuggestions =
                listOf(
                    "autosuggestion_0",
                    "autosuggestion_1",
                    "autosuggestion_2",
                )
            val selectionForSuggestions = columnsToSelectForSuggestions.joinToString(", ") { "`$it`" }
            val queryForSuggestions = "SELECT $selectionForSuggestions FROM ${"autosuggestions"} WHERE ${"word"} = ? COLLATE NOCASE"

            var autoSuggestionCursor: Cursor? = null
            try {
                autoSuggestionCursor = autoSuggestionDb.rawQuery(queryForSuggestions, arrayOf(word))

                if (autoSuggestionCursor.moveToFirst()) {
                    // Here's the direct call to processSuggestionRow with the auto-suggestion cursor.
                    val suggestionsFromDirectCall = autoSuggestionManager.processSuggestionRow(autoSuggestionCursor)
                    Log.d(TAG, "Direct call to processSuggestionRow for '$word': $suggestionsFromDirectCall")
                } else {
                    Log.d(TAG, "No auto-suggestion cursor results for '$word' from direct query.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during direct call to processSuggestionRow: ${e.message}", e)
            } finally {
                autoSuggestionCursor?.close()
            }
        } else {
            Log.e(TAG, "Failed to get auto-suggestion database for language: $language")
        }

        return finalOutput
    }

    fun extractConjugateHeadings(
        jsonData: DataContract?,
        word: String,
    ): Set<String> {
        val allFormKeys = mutableSetOf<String>()
        jsonData?.conjugations?.values?.forEach { tenseGroup ->
            tenseGroup.conjugationTypes.values.forEach { conjugationCategory ->
                allFormKeys.addAll(conjugationCategory.conjugationForms.keys)
            }
        }
        allFormKeys.add(word)
        return allFormKeys
    }

    private fun getTheValueForTheConjugateWord(
        word: String,
        form: String?,
        language: String,
    ): String {
        if (form.isNullOrEmpty()) return ""
        val result =
            fileManager.getLanguageDatabase(language)?.use { db ->
                getVerbCursor(db, word, language)?.use { cursor ->
                    getConjugatedValueFromCursor(cursor, form)
                }
            } ?: ""
        Log.d(TAG, "Fetched conjugated form for word '$word' with form '$form' in language '$language': '$result'")
        return result
    }

    private fun getConjugatedValueFromCursor(
        cursor: Cursor,
        form: String,
    ): String =
        if (form.contains("[")) {
            parseComplexForm(cursor, form)
        } else {
            try {
                cursor.getString(cursor.getColumnIndexOrThrow(form))
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Simple form column not found: '$form'", e)
                ""
            }
        }

    private fun parseComplexForm(
        cursor: Cursor,
        form: String,
    ): String {
        val bracketRegex = Regex("""\[(.*?)]""")
        val match = bracketRegex.find(form) ?: return ""

        val auxiliaryWords = match.groupValues[1]
        val dbColumnName = form.replace(bracketRegex, "").trim()
        return try {
            val verbPart = cursor.getString(cursor.getColumnIndexOrThrow(dbColumnName))
            "$auxiliaryWords $verbPart".trim()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Complex form column '$dbColumnName' not found", e)
            ""
        }
    }

    private fun getVerbCursor(
        db: SQLiteDatabase,
        word: String,
        language: String,
    ): Cursor? {
        val query =
            if (language == "SV") {
                "SELECT * FROM verbs WHERE verb = ?"
            } else {
                "SELECT * FROM verbs WHERE infinitive = ?"
            }
        val cursor = db.rawQuery(query, arrayOf(word))
        return if (cursor.moveToFirst()) {
            cursor
        } else {
            cursor.close()
            null
        }
    }
}
