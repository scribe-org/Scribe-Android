// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import be.scri.data.remote.SqlIdentifier
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
     * @param yamlData The data contract for the language, which defines the structure of conjugations.
     * @param word The specific verb to look up conjugations for.
     *
     * @return A nested map where the outer key is the tense group title
     * (e.g., "Indicative"), the inner key is the
     * conjugation category title (e.g., "Present"), and the value is a collection of the conjugated forms.
     */
    fun getTheConjugateLabels(
        language: String,
        yamlData: DataContract?,
        word: String,
    ): MutableMap<String, MutableMap<String, Collection<String>>>? {
        val db = fileManager.getConjugateDatabase(language) ?: return null
        return db.use { database ->
            if (!database.tableExists("verbs")) {
                return@use null
            }

            val rowData = getVerbRowData(database, word) ?: return@use null

            val finalOutput: MutableMap<String, MutableMap<String, Collection<String>>> = mutableMapOf()
            yamlData?.verbs?.conjugations?.values?.forEach { tenseGroup ->
                val conjugateForms: MutableMap<String, Collection<String>> = mutableMapOf()
                tenseGroup.tenses.values.forEach { conjugationCategory ->
                    val forms =
                        conjugationCategory.tenseForms.values.map { form ->
                            resolveConjugatedValue(rowData, form.value, database)
                        }
                    conjugateForms[conjugationCategory.tenseTitle] = forms
                }
                finalOutput[tenseGroup.sectionTitle] = conjugateForms
            }
            if (finalOutput.isEmpty() || finalOutput.values.all { it.isEmpty() || it.values.all { forms -> forms.all { it.isEmpty() } } }) {
                null
            } else {
                finalOutput
            }
        }
    }

    /**
     * Retrieves structured conjugation data mapped to form labels for UI display.
     *
     * @param language The language code (e.g., "EN", "DE").
     * @param yamlData The data contract defining the conjugation structure.
     * @param word The verb to conjugate.
     *
     * @return A structured map of tense groups to categories and (label, form) pairs, or null if not found.
     */
    fun getStructuredConjugations(
        language: String,
        yamlData: DataContract?,
        word: String,
    ): Map<String, Map<String, List<Pair<String, String>>>>? {
        val db = fileManager.getConjugateDatabase(language) ?: return null
        return db.use { database ->
            if (!database.tableExists("verbs")) {
                return@use null
            }

            val rowData = getVerbRowData(database, word) ?: return@use null

            val structuredData = mutableMapOf<String, MutableMap<String, List<Pair<String, String>>>>()
            yamlData?.verbs?.conjugations?.values?.forEach { tenseGroup ->
                val categories = mutableMapOf<String, List<Pair<String, String>>>()
                tenseGroup.tenses.values.forEach { conjugationCategory ->
                    val pairs =
                        conjugationCategory.tenseForms.values
                            .map { form ->
                                val resolvedForm = resolveConjugatedValue(rowData, form.value, database)
                                form.label to resolvedForm
                            }.filter { it.second.isNotEmpty() }
                    if (pairs.isNotEmpty()) {
                        categories[conjugationCategory.tenseTitle] = pairs
                    }
                }
                if (categories.isNotEmpty()) {
                    structuredData[tenseGroup.sectionTitle] = categories
                }
            }
            if (structuredData.isEmpty()) null else structuredData
        }
    }

    /**
     * Extracts a unique set of all conjugation form keys (e.g., "1ps", "2ps", "participle")
     * from the data contract.
     *
     * @param yamlData The data contract containing the conjugation structure.
     * @param word The base word, which is also added to the set.
     *
     * @return A `Set` of unique strings representing all possible conjugation form identifiers.
     */
    fun extractConjugateHeadings(
        yamlData: DataContract?,
        word: String,
    ): Set<String> {
        val allFormKeys = mutableSetOf<String>()
        yamlData?.verbs?.conjugations?.values?.forEach { tenseGroup ->
            tenseGroup.tenses.values.forEach { conjugationCategory ->
                conjugationCategory.tenseForms.values.forEach { form ->
                    allFormKeys.add(form.label)
                }
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
     *
     * @return The conjugated word as a [String], or an empty string if not found.
     */
    fun getTheValueForTheConjugateWord(
        word: String,
        form: String?,
        language: String,
    ): String {
        if (form.isNullOrEmpty()) return ""
        return fileManager.getConjugateDatabase(language)?.use { db ->
            if (!db.tableExists("verbs")) {
                return ""
            }

            val rowData = getVerbRowData(db, word) ?: return ""
            resolveConjugatedValue(rowData, form, db)
        } ?: ""
    }

    /**
     * Retrieves the verb row from the database using case-insensitive matching,
     * prefix-stripping fallback, and inflected form fallback.
     */
    private fun getVerbRowData(
        db: SQLiteDatabase,
        word: String,
    ): Map<String, String>? {
        val cleanWord = word.trim()
        if (cleanWord.isEmpty()) return null

        val candidateColumns =
            listOf("infinitive", "verb", "activeInfinitive", "simplePresent")
                .filter { db.columnExists("verbs", it) }

        // 1. Primary case-insensitive search across candidate infinitive columns
        for (col in candidateColumns) {
            val cursor =
                db.rawQuery(
                    "SELECT * FROM verbs WHERE LOWER(${SqlIdentifier.quote(col)}) = LOWER(?) LIMIT 1",
                    arrayOf(cleanWord),
                )
            val row = cursorToMap(cursor)
            if (row != null) return row
        }

        // 2. Fallback: try prefix-stripped form if user typed e.g. "to go", "se laver", "sich freuen"
        val stripped =
            when {
                cleanWord.startsWith("to ", ignoreCase = true) -> cleanWord.substring(3).trim()
                cleanWord.startsWith("se ", ignoreCase = true) -> cleanWord.substring(3).trim()
                cleanWord.startsWith("sich ", ignoreCase = true) -> cleanWord.substring(5).trim()
                cleanWord.startsWith("s'", ignoreCase = true) -> cleanWord.substring(2).trim()
                else -> null
            }
        if (!stripped.isNullOrEmpty()) {
            for (col in candidateColumns) {
                val cursor =
                    db.rawQuery(
                        "SELECT * FROM verbs WHERE LOWER(${SqlIdentifier.quote(col)}) = LOWER(?) LIMIT 1",
                        arrayOf(stripped),
                    )
                val row = cursorToMap(cursor)
                if (row != null) return row
            }
        }

        // 3. Fallback: search if user entered an inflected form in any verb column
        return findRowByInflectedForm(db, cleanWord)
    }

    /**
     * Searches across all columns of the `verbs` table to find the matching verb row for an inflected form.
     */
    private fun findRowByInflectedForm(
        db: SQLiteDatabase,
        word: String,
    ): Map<String, String>? {
        val allColumns = db.getTableColumns("verbs")
        val validColumns =
            allColumns.filter {
                it != "id" && it != "wdLexemeId" && it != "auxiliaryVerb" && SqlIdentifier.isValid(it)
            }
        if (validColumns.isEmpty()) return null

        val conditions =
            validColumns.joinToString(" OR ") {
                "LOWER(${SqlIdentifier.quote(it)}) = LOWER(?)"
            }
        val args = Array(validColumns.size) { word }
        return try {
            val cursor = db.rawQuery("SELECT * FROM verbs WHERE $conditions LIMIT 1", args)
            cursorToMap(cursor)
        } catch (e: SQLiteException) {
            Log.w("ConjugateDataManager", "Fallback inflected search failed: ${e.message}")
            null
        }
    }

    private fun cursorToMap(cursor: Cursor): Map<String, String>? =
        cursor.use {
            if (!it.moveToFirst()) return@use null
            val map = mutableMapOf<String, String>()
            val columnNames = it.columnNames
            for (i in columnNames.indices) {
                map[columnNames[i]] = it.getString(i) ?: ""
            }
            map
        }

    /**
     * Resolves a conjugated form value from the cached row data, supporting complex auxiliary parsing.
     */
    private fun resolveConjugatedValue(
        rowData: Map<String, String>,
        form: String?,
        db: SQLiteDatabase,
    ): String {
        if (form.isNullOrEmpty()) return ""
        return if (form.contains("[") || form.contains("{")) {
            parseComplexFormFromRow(rowData, form, db)
        } else {
            getValueWithFallback(rowData, form)
        }
    }

    private fun getValueWithFallback(
        rowData: Map<String, String>,
        form: String,
    ): String {
        rowData[form]?.let { if (it.isNotEmpty()) return it }

        val fallbacks =
            listOf(
                "combined" + form.replaceFirstChar { it.uppercase() },
                form.replace("presentParticiple", "combinedPresentParticiple"),
                form.replace("pastParticiple", "combinedPastParticiple"),
            )
        for (fallback in fallbacks) {
            val value = rowData[fallback]
            if (!value.isNullOrEmpty()) return value
        }
        return rowData[form] ?: ""
    }

    /**
     * Parses a complex conjugation form that includes an auxiliary part in brackets,
     * reusing the existing open database connection.
     */
    private fun parseComplexFormFromRow(
        rowData: Map<String, String>,
        form: String,
        db: SQLiteDatabase,
    ): String {
        val bracketRegex = Regex("""[\[{](.*?)[]}]""")
        val match = bracketRegex.find(form) ?: return ""

        val auxiliaryWords = match.groupValues[1]
        val dbColumnName = form.replace(bracketRegex, "").trim()

        val verbPart = getValueWithFallback(rowData, dbColumnName)
        if (verbPart.isEmpty()) {
            return ""
        }

        // Try dynamic auxiliary lookup (e.g. German style: [form auxiliary_column])
        return try {
            val words = auxiliaryWords.split(Regex("\\s+"))
            val auxColumn = words.last()

            val verbType = rowData[auxColumn]
            if (verbType.isNullOrEmpty()) {
                return "$auxiliaryWords $verbPart".trim()
            }

            val targetForm = words.first()
            if (!SqlIdentifier.isValid(targetForm)) {
                return "$auxiliaryWords $verbPart".trim()
            }

            val quotedTarget = SqlIdentifier.quote(targetForm)
            var auxResult = ""

            try {
                db
                    .rawQuery(
                        "SELECT $quotedTarget FROM verbs WHERE wdLexemeId = ? LIMIT 1",
                        arrayOf(verbType),
                    ).use { auxCursor ->
                        if (auxCursor.moveToFirst()) {
                            auxResult = auxCursor.getString(0) ?: ""
                        }
                    }
            } catch (e: SQLiteException) {
                Log.w("ConjugateDataManager", "Auxiliary query by wdLexemeId failed: ${e.message}")
            }

            if (auxResult.isEmpty()) {
                try {
                    db
                        .rawQuery(
                            "SELECT $quotedTarget FROM verbs WHERE infinitive = ? LIMIT 1",
                            arrayOf(verbType),
                        ).use { auxCursor2 ->
                            if (auxCursor2.moveToFirst()) {
                                auxResult = auxCursor2.getString(0) ?: ""
                            }
                        }
                } catch (e: SQLiteException) {
                    Log.w("ConjugateDataManager", "Auxiliary query by infinitive failed: ${e.message}")
                }
            }

            if (auxResult.isNotEmpty()) {
                "$auxResult $verbPart".trim()
            } else {
                "$auxiliaryWords $verbPart".trim()
            }
        } catch (e: IllegalArgumentException) {
            Log.w("ConjugateDataManager", "Dynamic lookup failed: ${e.message}")
            "$auxiliaryWords $verbPart".trim()
        } catch (e: NoSuchElementException) {
            Log.w("ConjugateDataManager", "Dynamic lookup failed: ${e.message}")
            "$auxiliaryWords $verbPart".trim()
        } catch (e: SQLiteException) {
            Log.e("ConjugateDataManager", "Database error in dynamic lookup", e)
            "$auxiliaryWords $verbPart".trim()
        }
    }
}
