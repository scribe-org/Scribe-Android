// SPDX-License-Identifier: GPL-3.0-or-later

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * A helper class to manage and query plural forms of
 * words from an SQLite database based on the provided language and JSON contract.
 */
class PluralFormsManager(
    private val context: Context,
) {
    /**
     * Checks if a word is plural by querying the database for plural forms.
     *
     * This function checks if the `numbers` data in the provided [jsonData] is valid and non-empty.
     * It retrieves the plural forms from the JSON data and queries the database to check for matching plural forms.
     *
     * @param language the language code (e.g., "en" for English) used to locate the corresponding database file.
     * @param jsonData the [DataContract] containing language-specific data, including plural forms.
     * @return a list of plural forms for the word, or `null` if the `numbers` data is null or empty.
     */
    fun checkIfWordIsPlural(
        language: String,
        jsonData: DataContract?,
    ): List<String>? {
        if (jsonData?.numbers?.values.isNullOrEmpty()) {
            Log.e("MY-TAG", "JSON data for 'numbers' is null or empty.")
            return null
        }

        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        val pluralForms = jsonData!!.numbers.values.toList()
        Log.d("MY-TAG", "Plural Forms: $pluralForms")

        return queryPluralForms(dbFile.path, pluralForms)
    }

    /**
     * Queries the plural representation of a given noun based on the provided language and JSON data.
     *
     * This function checks if the `numbers` data in the provided [jsonData] is valid and non-empty. It retrieves
     * the plural and singular forms from the JSON data and queries the database to find the corresponding plural form
     * for the given noun.
     *
     * @param language the language code (e.g., "en" for English) used to locate the corresponding database file.
     * @param jsonData the [DataContract] containing language-specific data, including singular and plural forms.
     * @param noun the noun for which the plural representation is being queried.
     * @return a map containing the plural forms of the noun, or an empty map if the `numbers` data is null or empty.
     */
    fun queryPluralRepresentation(
        language: String,
        jsonData: DataContract?,
        noun: String,
    ): Map<String, String?> {
        if (jsonData?.numbers?.values.isNullOrEmpty()) {
            Log.e("MY-TAG", "JSON data for 'numbers' is null or empty.")
            return mapOf()
        }

        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        val pluralForms = jsonData!!.numbers.values.toList()
        val singularForms = jsonData.numbers.keys.toList()

        return queryPluralForms(dbFile.path, pluralForms, singularForms, noun)
    }

    /**
     * Queries the database for plural forms of nouns based on the provided plural form values.
     *
     * This function opens the database located at [dbPath] and searches for entries in the `nouns` table.
     * It checks each noun against the provided [pluralForms] list, adding any matching plural forms
     * to the result list. The database is opened in read-only mode, and the query is processed using a cursor.
     *
     * @param dbPath the file path to the SQLite database containing noun data.
     * @param pluralForms the list of plural forms to match against nouns in the database.
     * @return a list of plural forms found in the database that match the provided [pluralForms].
     */
    private fun queryPluralForms(
        dbPath: String,
        pluralForms: List<String>,
    ): List<String> {
        val result = mutableListOf<String>()
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        db.use { database ->
            database.rawQuery("SELECT * FROM nouns", null)?.use { cursor ->
                processPluralFormsCursor(cursor, pluralForms, result)
            }
        }
        db.close()
        return result
    }

    /**
     * Queries the database for plural forms of a noun based on the provided singular forms and plural forms.
     *
     * This function builds a dynamic SQL query to search for nouns in the database that match the provided
     * [singularForms].
     * For each singular noun found, the function attempts to find the corresponding plural form from the [pluralForms]
     * list.
     * The results are mapped as a pair of plural forms and their corresponding plural representations.
     *
     * @param dbPath the file path to the SQLite database that contains noun data.
     * @param pluralForms the list of plural forms that are being matched against the database entries.
     * @param singularForms the list of singular noun forms used to query the database.
     * @param noun the specific noun for which plural forms are being queried.
     * @return a map where the keys are plural forms from [pluralForms], and the values are the corresponding plural
     *         forms found in the database. If no match is found, the value will be `null` for that plural form.
     */
    private fun queryPluralForms(
        dbPath: String,
        pluralForms: List<String>,
        singularForms: List<String>,
        noun: String,
    ): Map<String, String?> {
        val result = mutableListOf<String>()
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        val queryBuilder = StringBuilder("SELECT * FROM nouns WHERE")
        val placeholders = singularForms.joinToString("OR ") { "$it = ?" }
        queryBuilder.append(" $placeholders;")
        val selectionArgs = Array(singularForms.size) { noun }

        db.use { database ->
            database.rawQuery(queryBuilder.toString(), selectionArgs)?.use { cursor ->
                processPluralFormsCursor(cursor, pluralForms, result)
            }
        }
        db.close()
        return pluralForms.zip(result).toMap()
    }

    /**
     * Processes the cursor containing noun data and extracts plural forms.
     *
     * This function iterates over the cursor, which contains data from the `nouns` table in the database.
     * For each row in the cursor, it extracts the relevant plural forms and adds them to the provided [result] list.
     * The plural forms to match against are provided in the [pluralForms] list.
     *
     * If the cursor is empty, a warning message is logged indicating that no data was found.
     *
     * @param cursor the cursor containing the results from a query on the `nouns` table.
     * @param pluralForms the list of plural forms to be matched against the noun data.
     * @param result the mutable list to store the plural forms that are found.
     */
    private fun processPluralFormsCursor(
        cursor: Cursor,
        pluralForms: List<String>,
        result: MutableList<String>,
    ) {
        if (!cursor.moveToFirst()) {
            Log.w("MY-TAG", "Cursor is empty, no data found in 'nouns' table.")
            return
        }

        do {
            addPluralForms(cursor, pluralForms, result)
        } while (cursor.moveToNext())
    }

    /**
     * Adds plural forms from the cursor to the result list based on the provided plural form names.
     *
     * This function checks for the presence of columns corresponding to the plural forms in the cursor.
     * For each plural form name in the [pluralForms] list, it looks for the corresponding column in the cursor.
     * If the column exists, its value is added to the [result] list. If the column is not found, an error is logged.
     *
     * @param cursor the cursor containing noun data, which should have columns corresponding to plural forms.
     * @param pluralForms the list of plural form names that correspond to columns in the cursor.
     * @param result the mutable list to store the plural form values retrieved from the cursor.
     */
    private fun addPluralForms(
        cursor: Cursor,
        pluralForms: List<String>,
        result: MutableList<String>,
    ) {
        pluralForms.forEach { pluralForm ->
            val columnIndex = cursor.getColumnIndex(pluralForm)
            if (columnIndex != -1) {
                cursor.getString(columnIndex)?.let { result.add(it) }
            } else {
                Log.e("MY-TAG", "Column '$pluralForm' not found in the database.")
            }
        }
    }
}
