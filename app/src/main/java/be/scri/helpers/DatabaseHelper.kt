// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import ContractDataLoader
import DataContract
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * A helper class to facilitate database calls for Scribe keyboard commands.
 * This class handles interactions with the database, including loading the database
 * for a specific language and querying the required data related to words, emoji keywords,
 * gender, plural forms, and case annotations.
 *
 * @param context The context used to access the app's resources and database.
 */
@Suppress("TooManyFunctions")
class DatabaseHelper(
    private val context: Context,
) : SQLiteOpenHelper(
        context,
        null,
        null,
        DATABASE_VERSION,
    ) {
    private val contractLoader = ContractDataLoader(context)
    private val fileManager = DatabaseFileManager(context)
    private var emojiMaxLen = 0

    /**
     * Companion object for DatabaseHelper.
     * Contains constants used throughout the class.
     */
    companion object {
        private const val DATABASE_VERSION = 1
        private const val TAG = "ScribeDBHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // No operation
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        // No operation
    }

    /**
     * Loads the database file for a specific language.
     * This ensures the SQLite file is copied from assets to the app's database directory if needed.
     *
     * @param language The two-letter language code (e.g., "DE", "FR").
     */
    fun loadDatabase(language: String) {
        fileManager.loadDatabaseFile(language)
    }

    private fun openDbFor(language: String): SQLiteDatabase? {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        return if (dbFile.exists()) {
            try {
                SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            } catch (e: SQLiteException) {
                Log.e(TAG, "Failed to open database for $language", e)
                null
            }
        } else {
            Log.e(TAG, "Database file not found for language: $language")
            null
        }
    }

    /**
     * Retrieves the data contract for a given language.
     * The contract defines the structure and metadata of the language's database tables.
     *
     * @param language The two-letter language code.
     * @return A [DataContract] object for the language, or null if it cannot be loaded.
     */
    fun getLanguageContract(language: String): DataContract? = contractLoader.loadContract(language)

    /**
     * Fetches a map of words to their associated emoji keywords for a specific language.
     * Also calculates the maximum length of an emoji keyword during this process.
     *
     * @param language The two-letter language code.
     * @return A [HashMap] where keys are words and values are a list of corresponding emoji keywords.
     */
    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> {
        val emojiMap = HashMap<String, MutableList<String>>()
        try {
            openDbFor(language)?.use { db ->
                val query =
                    """
                    SELECT word,
                           emoji_keyword_0,
                           emoji_keyword_1,
                           emoji_keyword_2
                    FROM emoji_keywords
                    """.trimIndent()

                db.rawQuery(query, null)?.use { cursor ->
                    this.emojiMaxLen = processEmojiCursor(cursor, emojiMap)
                }
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "SQLite error fetching emoji keywords for $language", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid argument fetching emoji keywords for $language", e)
        }

        return emojiMap
    }

    /**
     * Finds the grammatical gender for nouns in a specific language.
     * Uses the data contract to find noun and gender columns dynamically.
     *
     * @param language The two-letter language code.
     * @param contract The [DataContract] for the language, which defines table and column names.
     * @return [HashMap] of lowercase nouns to their gender list, or empty if contract/columns are missing.
     */
    fun findGenderOfWord(
        language: String,
        contract: DataContract?,
    ): HashMap<String, List<String>> {
        if (contract == null) return hashMapOf()

        return try {
            openDbFor(language)?.use { db ->
                val (wordColumn, genderColumn) = findNounAndGenderColumns(db, contract)
                if (wordColumn == null || genderColumn == null) {
                    Log.e(TAG, "Required columns not found in 'nouns' table for $language.")
                    return@use hashMapOf()
                }

                val query = "SELECT `$wordColumn`, `$genderColumn` FROM nouns"
                db.rawQuery(query, null)?.use { cursor ->
                    processGenderCursor(cursor, wordColumn, genderColumn)
                } ?: hashMapOf()
            } ?: hashMapOf()
        } catch (e: SQLiteException) {
            Log.e(TAG, "SQLite error fetching gender for $language", e)
            hashMapOf()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid argument fetching gender for $language", e)
            hashMapOf()
        }
    }

    /**
     * Retrieves a list of all plural noun forms from the database for a given language.
     *
     * @param language The two-letter language code.
     * @param contract The [DataContract] for the language, used to find the plural column name.
     * @return A list of plural words, or null if the operation fails or the plural column is not found.
     */
    fun checkIfWordIsPlural(
        language: String,
        contract: DataContract?,
    ): List<String>? {
        if (contract == null) return null

        return try {
            openDbFor(language)?.use { db ->
                val pluralColumn = findPluralColumn(db, contract)
                if (pluralColumn == null) {
                    Log.e(TAG, "Plural column not found for language $language")
                    return@use null
                }
                fetchPluralList(db, pluralColumn)
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error fetching plural words for $language", e)
            null
        }
    }

    /**
     * Finds the grammatical cases governed by prepositions for certain languages (e.g., "DE", "RU").
     *
     * @param language The two-letter language code. This function currently only supports "DE" and "RU".
     * @return A [HashMap] where keys are prepositions and values are a list of cases they can take.
     */
    fun findCaseAnnnotationForPreposition(language: String): HashMap<String, MutableList<String>> {
        if (language !in listOf("DE", "RU")) return hashMapOf()

        return try {
            openDbFor(language)?.use { db ->
                val query = "SELECT preposition, grammaticalCase FROM prepositions"
                db.rawQuery(query, null)?.use { cursor ->
                    processPrepositionCursor(cursor)
                } ?: hashMapOf()
            } ?: hashMapOf()
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error fetching preposition cases for $language", e)
            hashMapOf()
        }
    }

    /**
     * Finds the plural form of a specific singular noun.
     *
     * @param language The two-letter language code.
     * @param contract The [DataContract] for the language.
     * @param noun The singular noun to find the plural for.
     *@return Map of singular nouns to their plurals, or empty if not found.
     */
    fun getPluralRepresentation(
        language: String,
        contract: DataContract?,
        noun: String,
    ): Map<String, String?> {
        if (contract == null) return emptyMap()

        return try {
            openDbFor(language)?.use { db ->
                val (singularColumn, pluralColumn) = findSingularAndPluralColumns(db, contract)

                if (singularColumn == null || pluralColumn == null) {
                    Log.e(TAG, "Plural/singular columns not found for $language")
                    return@use emptyMap()
                }

                fetchSingularToPluralMap(db, singularColumn, pluralColumn, noun)
            } ?: emptyMap()
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error fetching plural representation for $noun in $language", e)
            emptyMap()
        }
    }

    // --- Private Helper Functions for Cursor Processing ---

    /**
     * Executes the query to find the plural form of a specific singular noun.
     * @return A map with the singular noun as key and plural as value, or an empty map.
     */
    private fun fetchSingularToPluralMap(
        db: SQLiteDatabase,
        singularColumn: String,
        pluralColumn: String,
        noun: String,
    ): Map<String, String?> {
        val query =
            """
            SELECT `$singularColumn`, `$pluralColumn`
            FROM nouns
            WHERE `$singularColumn` = ?
            COLLATE NOCASE
            """.trimIndent()

        return db.rawQuery(query, arrayOf(noun))?.use { cursor ->
            if (cursor.moveToFirst()) {
                mapOf(cursor.getString(0) to cursor.getString(1))
            } else {
                emptyMap()
            }
        } ?: emptyMap()
    }

    private fun processEmojiCursor(
        cursor: Cursor,
        emojiMap: HashMap<String, MutableList<String>>,
    ): Int {
        val wordIndex = cursor.getColumnIndexOrThrow("word")
        val emojiIndices =
            listOf("emoji_keyword_0", "emoji_keyword_1", "emoji_keyword_2")
                .map { cursor.getColumnIndex(it) }
                .filter { it != -1 }
        var maxLen = 0
        while (cursor.moveToNext()) {
            val word = cursor.getString(wordIndex)
            maxLen = maxOf(maxLen, word.length)
            val emojis =
                emojiIndices
                    .mapNotNull { index -> cursor.getString(index)?.takeIf { it.isNotBlank() } }
                    .toMutableList()
            if (emojis.isNotEmpty()) {
                emojiMap[word] = emojis
            }
        }
        return maxLen
    }

    private fun processGenderCursor(
        cursor: Cursor,
        wordColumn: String,
        genderColumn: String,
    ): HashMap<String, List<String>> {
        val genderMap = HashMap<String, List<String>>()
        val wordIndex = cursor.getColumnIndexOrThrow(wordColumn)
        val genderIndex = cursor.getColumnIndexOrThrow(genderColumn)
        while (cursor.moveToNext()) {
            val word = cursor.getString(wordIndex)
            val gender = cursor.getString(genderIndex)
            if (word != null && gender != null) {
                genderMap[word.lowercase()] = listOf(gender)
            }
        }
        return genderMap
    }

    private fun findPluralColumn(
        db: SQLiteDatabase,
        contract: DataContract,
    ): String? =
        db.rawQuery("SELECT * FROM nouns LIMIT 1", null)?.use { cursor ->
            contract.numbers.values
                .firstOrNull()
                ?.takeIf { it in cursor.columnNames }
        }

    private fun fetchPluralList(
        db: SQLiteDatabase,
        pluralColumn: String,
    ): List<String> {
        val pluralList = mutableListOf<String>()
        val query = "SELECT `$pluralColumn` FROM nouns"
        db.rawQuery(query, null)?.use { cursor ->
            val pluralIndex = cursor.getColumnIndex(pluralColumn)
            if (pluralIndex == -1) return@use
            while (cursor.moveToNext()) {
                cursor.getString(pluralIndex)?.takeIf { it.isNotBlank() }?.let { pluralList.add(it) }
            }
        }
        return pluralList
    }

    private fun processPrepositionCursor(cursor: Cursor): HashMap<String, MutableList<String>> {
        val caseMap = HashMap<String, MutableList<String>>()
        val prepIndex = cursor.getColumnIndex("preposition")
        val caseIndex = cursor.getColumnIndex("grammaticalCase")

        if (prepIndex == -1 || caseIndex == -1) return caseMap

        while (cursor.moveToNext()) {
            val prep = cursor.getString(prepIndex)
            val case = cursor.getString(caseIndex)
            if (prep != null && case != null) {
                caseMap.getOrPut(prep) { mutableListOf() }.add(case)
            }
        }
        return caseMap
    }

    private fun findSingularAndPluralColumns(
        db: SQLiteDatabase,
        contract: DataContract,
    ): Pair<String?, String?> =
        db.rawQuery("SELECT * FROM nouns LIMIT 1", null)?.use {
            val sCol =
                contract.numbers.keys
                    .firstOrNull()
                    ?.takeIf { col -> col in it.columnNames }
            val pCol =
                contract.numbers.values
                    .firstOrNull()
                    ?.takeIf { col -> col in it.columnNames }
            sCol to pCol
        } ?: (null to null)

    /**
     * Finds the relevant noun and gender column names from the database schema.
     * @param db The SQLiteDatabase instance to query for column names.
     * @param contract The DataContract containing schema information.
     * @return A Pair containing the found word column and gender column, or nulls if not found.
     */
    private fun findNounAndGenderColumns(
        db: SQLiteDatabase,
        contract: DataContract,
    ): Pair<String?, String?> {
        val columnNames =
            db.rawQuery("SELECT * FROM nouns LIMIT 1", null)?.use {
                it.columnNames
            } ?: return null to null

        val possibleWordColumns =
            listOf(
                "nominativeSingular",
                "singular",
                "nominativeIndefiniteSingular",
            )
        val wordColumn = possibleWordColumns.firstOrNull { it in columnNames }
        val genderColumn = contract.genders.canonical.firstOrNull { it in columnNames }

        return wordColumn to genderColumn
    }

    /**
     * Gets the maximum length of any emoji keyword found for the last processed language.
     * This value is calculated and cached during the [getEmojiKeywords] call.
     *
     * @return The maximum length of an emoji keyword as an [Int].
     */
    fun getEmojiMaxKeywordLength(): Int = emojiMaxLen

    /**
     * Retrieves the translation for a given word.
     * This method delegates the request to the [be.scri.helpers.keyboardDBHelper.TranslationDataManager].
     *
     * @param language The two-letter language code of the source language.
     * @param word The word to be translated.
     * @return The translated word as a [String].
     */
    fun getTranslationSourceAndDestination(
        language: String,
        word: String,
    ): String =
        be.scri.helpers.keyboardDBHelper.TranslationDataManager(context).getTranslationDataForAWord(
            be.scri.helpers.keyboardDBHelper
                .TranslationDataManager(context)
                .getSourceAndDestinationLanguage(language),
            word,
        )

    /**
     * Retrieves detailed conjugation data for a given verb.
     * This method delegates the request to the [keyboardDBHelper.ConjugateDataManager].
     *
     * @param language The two-letter language code.
     * @param word The verb to conjugate.
     * @return A nested map containing the full conjugation data.
     */
    fun getConjugateData(
        language: String,
        word: String,
    ): MutableMap<String, MutableMap<String, Collection<String>>> =
        be.scri.helpers.keyboardDBHelper.ConjugateDataManager(context).getTheConjugateLabels(
            language,
            getLanguageContract(language),
            word,
        )

    /**
     * Retrieves the set of conjugation headings (e.g., "present", "past tense") for a given verb.
     * This method delegates the request to the [be.scri.helpers.keyboardDBHelper.ConjugateDataManager].
     *
     * @param language The two-letter language code.
     * @param word The verb for which to get conjugation labels.
     * @return A [Set] of strings representing the conjugation headings.
     */
    fun getConjugateLabels(
        language: String,
        word: String,
    ): Set<String> =
        be.scri.helpers.keyboardDBHelper.ConjugateDataManager(context).extractConjugateHeadings(
            getLanguageContract(language),
            word,
        )
}
