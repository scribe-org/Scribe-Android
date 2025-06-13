// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import be.scri.helpers.DatabaseFileManager

/**
 * Manages verb conjugation data by interfacing with SQLite databases.
 * @param fileManager The central manager for database file access.
 */
class ConjugateDataManager(
    private val fileManager: DatabaseFileManager,
) {
    /**
     * Gets conjugation labels and values for a given verb.
     * @param language The language code (e.g., "EN", "SV").
     * @param jsonData The data contract for the language.
     * @param word The verb to get conjugations for.
     * @return A map of conjugation titles to their forms.
     */
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
        return finalOutput
    }

    /**
     * Extracts all unique conjugation headings from a data contract.
     * @param jsonData The data contract containing conjugation info.
     * @param word The base word to include in the headings.
     * @return A set of unique conjugation heading strings.
     */
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

    /**
     * Retrieves the conjugated form of a word.
     */
    private fun getTheValueForTheConjugateWord(
        word: String,
        form: String?,
        language: String,
    ): String {
        if (form.isNullOrEmpty()) return ""
        return fileManager.getLanguageDatabase(language)?.use { db ->
            getVerbCursor(db, word, language)?.use { cursor ->
                getConjugatedValueFromCursor(cursor, form)
            }
        } ?: ""
    }

    /**
     * Extracts the conjugated value from a database cursor.
     */
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
                Log.e("ConjugateDataManager", "Simple form column not found: '$form'", e)
                ""
            }
        }

    /**
     * Parses a complex conjugation form like auxiliary verb`.
     */
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
            Log.e("ConjugateDataManager", "Complex form column '$dbColumnName' not found", e)
            ""
        }
    }

    /**
     * Gets a cursor pointing to the requested verb's data.
     */
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
