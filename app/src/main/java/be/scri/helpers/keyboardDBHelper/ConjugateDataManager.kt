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
        var result: MutableList<String>
        val conjugationIds = jsonData?.conjugations?.keys
        val result1: MutableMap<String, List<String>> = mutableMapOf()
        Log.i("ISSUE-123","The conjugate data is ${jsonData?.conjugations}")
        val conjugateForms: MutableMap<String , Collection<String>> = mutableMapOf()
        val finalOutput: MutableMap <String , MutableMap<String,Collection<String>>> = mutableMapOf()
        for (i in jsonData?.conjugations?.keys!!) {
            val title = jsonData?.conjugations?.get(i)?.title
            Log.i("MY-TAG","The keys for the task are ${jsonData?.conjugations?.keys}")
            val label1 = jsonData.conjugations.get(i)
            val label2 = label1?.conjugationTypes
            val keys = label2?.keys
            val conjugateForms: MutableMap<String , Collection<String>> = mutableMapOf()
            for (key in keys!!) {
                val conjugationType = label2[key]
                val formTitle = conjugationType?.title ?: continue
                val forms = conjugationType.conjugationForms.values.map { form ->
                    getTheValueForTheConjugateWord(
                        word = word,
                        form = form,
                        language = language
                    )
                }
                conjugateForms[formTitle] = forms
            }
            if (title != null) {
                finalOutput[title] = conjugateForms
            }
            Log.i("CONJUGATE-ISSUE","The conjugate forms are $conjugateForms")
        }
        Log.i("CONJUGATE-ISSUE","The final output is $finalOutput")
        return finalOutput
    }

    /**
     * Extracts all unique conjugation headings from the provided data contract.
     *
     * @param jsonData The data contract containing conjugation information
     * @return A set of unique conjugation heading strings
     */
    fun extractConjugateHeadings(jsonData: DataContract?): Set<String> {
        val conjugationIds = jsonData?.conjugations?.values
        val keys = setOf("HI","HI","HI","HI")
//        val keys =
//            conjugationIds
//                ?.flatMap { conjugation ->
//                    listOfNotNull(
//                        conjugation.firstPerson?.keys,
//                        conjugation.secondPerson?.keys,
//                        conjugation.thirdPersonSingular?.keys,
//                        conjugation.firstPersonPlural?.keys,
//                        conjugation.secondPersonPlural?.keys,
//                        conjugation.thirdPersonPlural?.keys,
//                    ).flatten()
//                }?.toSet() ?: emptySet()
        Log.i("BETA-TAG", "The conjugate labels are $keys")
        return keys
    }

    /**
     * Retrieves the conjugated form of a word based on the provided form pattern.
     *
     * @param word The base verb form
     * @param form The pattern describing how to conjugate the verb
     * @param language The language code (e.g., "EN", "SV")
     * @return The conjugated form of the word
     */
    private fun getTheValueForTheConjugateWord(
        word: String,
        form: String?,
        language: String,
    ): String {
        var result = ""
        val db = openDatabase(language)

        db?.use { database ->
            val verbCursor = getVerbCursor(database, word, language)

            if (verbCursor != null && form != null) {
                result =
                    try {
                        verbCursor.getString(verbCursor.getColumnIndexOrThrow(form))
                    } catch (e: IllegalArgumentException) {
                        Log.w("ConjugateDataManager", "Form column not found: $form", e)
                        resolveFallbackForm(database, verbCursor, word, form, language)
                    }
            }
        }

        return result
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
        return if (cursor.moveToFirst()) cursor else null
    }
    private fun resolveFallbackForm(
        db: SQLiteDatabase,
        verbCursor: Cursor,
        word: String,
        form: String,
        language: String,
    ): String {
        var fallbackFields: MutableList<String> = mutableListOf()
        var resultList : MutableList<String> = mutableListOf()
        if (language!= "EN") {
            val bracketRegex = Regex("""\[(.*?)\]""")
            val wordsRegex = Regex("""\b(\w+)\s+(\w+)\b""")
            val bracketPart = bracketRegex.find(form)?.groupValues?.get(1) ?: ""
            val (first, second) = wordsRegex.find(bracketPart)?.destructured ?: return ""
            val rest = form.replace(bracketRegex, "").trim()
            fallbackFields = mutableListOf(first, second, rest)
        }
        else {
            val bracketRegex = Regex("""\[(.*?)]""")
            val bracketContent = bracketRegex.find(form)?.groupValues?.get(1) ?: ""
            val (first, second) = bracketContent.split(" ").let {
                when (it.size) {
                    0 -> "" to ""
                    1 -> it[0] to ""
                    else -> it[0] to it[1]
                }
            }
            val rest = form.replace(bracketRegex, "").trim()
            resultList =  mutableListOf(first, second, rest)
        }
        return if (language != "EN") {
            resolveFallbackNonEnglish(db, verbCursor, word, fallbackFields)
        } else {
            resolveFallbackEnglish( verbCursor, resultList )
        }
    }

    private fun resolveFallbackNonEnglish(
        db: SQLiteDatabase,
        verbCursor: Cursor,
        word: String,
        fields: MutableList<String>,
    ): String {
        db.rawQuery("SELECT ${fields[1]} FROM verbs WHERE infinitive = ?", arrayOf(word)).use {
            if (it.moveToFirst()) {
                fields[1] = it.getString(it.getColumnIndexOrThrow(fields[1]))
            }
        }

        fields[2] = verbCursor.getString(verbCursor.getColumnIndexOrThrow(fields[2]))

        db.rawQuery("SELECT ${fields[0]} FROM verbs WHERE wdLexemeID = ?", arrayOf(fields[1])).use {
            if (it.moveToFirst()) {
                fields[1] = it.getString(it.getColumnIndexOrThrow(fields[0]))
            }
        }

        return "${fields[1]} ${fields[2]}"
    }

    private fun resolveFallbackEnglish(
        verbCursor: Cursor,
        fields: MutableList<String>,
    ): String {

        fields[2] = verbCursor.getString(verbCursor.getColumnIndexOrThrow(fields[2]))
        Log.i("CONJUGATE-ISSUE","The fields are $fields")
        return "${fields[0]} ${fields[1]} ${fields[2]}"
    }
}
