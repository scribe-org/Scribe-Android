// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.keyboardDBHelper

import DataContract
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream

/**
 * Manages conjugation data for different languages by interfacing with SQLite databases.
 * This class handles retrieving and processing verb conjugation information.
 *
 * @property context The Android application context used to access assets and databases
 */
class ConjugateDataManager(
    private val context: Context,
) {
    /**
     * Retrieves conjugation labels and their corresponding values for a given word.
     *
     * @param language The language code (e.g., "EN", "SV")
     * @param jsonData The data contract containing conjugation information
     * @param word The verb to get conjugations for
     * @return A map of conjugation titles to their corresponding person forms
     */
    fun getTheConjugateLabels(
        language: String,
        jsonData: DataContract?,
        word: String,
    ): MutableMap<String, MutableMap<String, Collection<String>>> {
        // This function is okay, no changes needed here.
        val finalOutput: MutableMap<String, MutableMap<String, Collection<String>>> = mutableMapOf()
        jsonData?.conjugations?.values?.forEach { tenseGroup ->
            val conjugateForms: MutableMap<String, Collection<String>> = mutableMapOf()
            tenseGroup.conjugationTypes.values.forEach { conjugationCategory ->
                val forms =
                    conjugationCategory.conjugationForms.values.map { form ->
                        getTheValueForTheConjugateWord(
                            word = word,
                            form = form,
                            language = language,
                        )
                    }
                conjugateForms[conjugationCategory.title] = forms
            }
            finalOutput[tenseGroup.title] = conjugateForms
        }
        return finalOutput
    }

    /**
     * Extracts all unique conjugation headings from the provided data contract.
     *
     * @param jsonData The data contract containing conjugation information
     * @return A set of unique conjugation heading strings
     */
    fun extractConjugateHeadings(
        jsonData: DataContract?,
        word: String,
    ): Set<String> {
        // This function is okay, no changes needed here.
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
     * Retrieves the conjugated form of a word based on the provided form pattern.
     * This function is refactored to reduce nesting depth.
     */
    private fun getTheValueForTheConjugateWord(
        word: String,
        form: String?,
        language: String,
    ): String {
        if (form.isNullOrEmpty()) {
            return ""
        }

        // Chain `use` blocks and return the result.
        // If the database or cursor cannot be opened, the chain results in null.
        // The elvis operator `?: ""` handles this case by returning an empty string.
        return openDatabase(language)?.use { database ->
            getVerbCursor(database, word, language)?.use { cursor ->
                getConjugatedValueFromCursor(cursor, form)
            }
        } ?: ""
    }

    /**
     * Extracts the conjugated value from a database cursor based on the form.
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
     * Parses a complex conjugation form.
     */
    private fun parseComplexForm(
        cursor: Cursor,
        form: String,
    ): String {
        val bracketRegex = Regex("""\[(.*?)]""")
        val match = bracketRegex.find(form)

        if (match != null) {
            val auxiliaryWords = match.groupValues[1]
            val dbColumnName = form.replace(bracketRegex, "").trim()

            return try {
                val verbPart = cursor.getString(cursor.getColumnIndexOrThrow(dbColumnName))
                "$auxiliaryWords $verbPart".trim()
            } catch (e: IllegalArgumentException) {
                Log.e(
                    "ConjugateDataManager",
                    "Complex form column not found: '$dbColumnName' in template '$form'",
                    e,
                )
                ""
            }
        }
        return ""
    }

    private fun openDatabase(language: String): SQLiteDatabase? {
        val dbPath = context.getDatabasePath("${language}LanguageData.sqlite")
        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open("data/${language}LanguageData.sqlite").use { inputStream ->
                FileOutputStream(dbPath).use { it.write(inputStream.readBytes()) }
            }
        }
        return SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
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
