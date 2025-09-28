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
     * Retrieves a comprehensive map of conjugation data for a specific verb in a given language.
     * The returned map is structured by tense/mood, then by conjugation type (e.g., "Indicative Present").
     *
     * @param language The language code (e.g., "EN", "SV") to determine the correct database.
     * @param jsonData The data contract for the language, which defines the structure of conjugations.
     * @param word The specific verb to look up conjugations for.
     * @return A nested map where the outer key is the tense group title
     * (e.g., "Indicative"), the inner key is the
     * conjugation category title (e.g., "Present"), and the value is a collection of the conjugated forms.
     */
    fun getTheConjugateLabels(
        language: String,
        jsonData: DataContract?,
        word: String,
    ): MutableMap<String, MutableMap<String, Collection<String>>>? {
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
        return if (finalOutput.isEmpty() || finalOutput.values.all { it.isEmpty() || it.values.all { forms -> forms.all { it.isEmpty() } } }) {
            null
        } else {
            finalOutput
        }
    }

    /**
     * Extracts a unique set of all conjugation form keys (e.g., "1ps", "2ps", "participle")
     * from the data contract.
     *
     * @param jsonData The data contract containing the conjugation structure.
     * @param word The base word, which is also added to the set.
     * @return A `Set` of unique strings representing all possible conjugation form identifiers.
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
     * Retrieves the specific conjugated form of a word from the database.
     *
     * @param word The base word (verb) to look up.
     * @param form The specific conjugation form identifier (e.g., "1ps", "past_participle").
     * @param language The language code to select the correct database.
     * @return The conjugated word as a [String], or an empty string if not found.
     */
    private fun getTheValueForTheConjugateWord(
        word: String,
        form: String?,
        language: String,
    ): String {
        if (form.isNullOrEmpty()) return ""
        return fileManager.getLanguageDatabase(language)?.use { db ->
            getVerbCursor(db, word, language)?.use { cursor ->
                getConjugatedValueFromCursor(cursor, form, language)
            }
        } ?: ""
    }

    /**
     * Extracts a conjugated value from a database cursor for a given form.
     * It handles both simple column lookups and complex forms that require parsing.
     *
     * @param cursor The database cursor positioned at the correct row for the verb.
     * @param form The form identifier, which can be a simple column name or a complex string.
     * @return The conjugated value, or an empty string on failure.
     */
    private fun getConjugatedValueFromCursor(
        cursor: Cursor,
        form: String,
        language: String,
    ): String =
        if (form.contains("[")) {
            parseComplexForm(cursor, form, language)
        } else {
            try {
                cursor.getString(cursor.getColumnIndexOrThrow(form))
            } catch (e: IllegalArgumentException) {
                Log.e("ConjugateDataManager", "Simple form column not found: '$form'", e)
                ""
            }
        }

    /**
     * Parses a complex conjugation form that includes an auxiliary part in brackets,
     * such as "[have] past_participle". It combines the auxiliary word with the value
     * from the specified database column.
     *
     * @param cursor The database cursor positioned at the correct row.
     * @param form The complex form string to parse.
     * @return The combined string (e.g., "have walked"), or an empty string on failure.
     */
    private fun parseComplexForm(
        cursor: Cursor,
        form: String,
        language: String,
    ): String {
        val bracketRegex = Regex("""\[(.*?)]""")
        val match = bracketRegex.find(form) ?: return ""

        val auxiliaryWords = match.groupValues[1]
        val dbColumnName = form.replace(bracketRegex, "").trim()

        return try {
            val verbPart = cursor.getString(cursor.getColumnIndexOrThrow(dbColumnName))
            val words = auxiliaryWords.split(Regex("\\s+"))
            val verbType = cursor.getString(cursor.getColumnIndexOrThrow(words.last()))
            val db = fileManager.getLanguageDatabase(language = language)

            val wordPart1 = words.firstOrNull()
            var auxResult = ""
            wordPart1?.let {
                val auxCursor =
                    db?.rawQuery(
                        "SELECT $wordPart1 FROM verbs WHERE wdLexemeId = ?",
                        arrayOf(verbType),
                    )
                if (auxCursor?.moveToFirst() == true) {
                    auxResult = auxCursor.getString(0)
                }
                auxCursor?.close()
            }

            val result = "$auxResult $verbPart".trim()
            Log.d("DEBUG", "Returning: $result")
            result
        } catch (e: IllegalArgumentException) {
            Log.e("ConjugateDataManager", "Column '$dbColumnName' not found", e)
            ""
        }
    }

    /**
     * Creates and returns a database cursor pointing to the requested verb's data row.
     * Note: Handles a special case for Swedish ("SV") where the key column is 'verb' instead of 'infinitive'.
     *
     * @param db The SQLite database instance to query.
     * @param word The verb to search for.
     * @param language The language code, used for special query conditions.
     * @return A [Cursor] positioned at the verb's row, or null if the verb is not found.
     * The caller is responsible for closing the cursor.
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
