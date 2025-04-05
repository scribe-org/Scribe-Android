// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.keyboardDBHelper

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * A helper class to manage preposition data and extract case annotations from an SQLite database for a given language.
 */
class PrepositionDataManager(
    private val context: Context,
) {
    /**
     * Opens a read-only SQLite database for the specified language.
     *
     * This function opens a database file named `<language>LanguageData.sqlite` from the app's internal storage.
     * It returns an [SQLiteDatabase] object that allows for querying the database in read-only mode.
     *
     * @param language the language code (e.g., "en", "de") used to locate the corresponding database file.
     * @return an [SQLiteDatabase] object representing the open database.
     * @throws SQLiteException if the database cannot be opened, for example if the file does not exist or is corrupted.
     */
    fun openDatabase(language: String): SQLiteDatabase {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    /**
     * Extracts the preposition and its associated case annotation from the provided cursor.
     *
     * This function retrieves the preposition (at column index 1) and its case annotation (at column index 2)
     * from the given [Cursor]. The preposition and case annotation are returned as a pair.
     *
     * @param cursor the [Cursor] containing the data, assumed to have at least two columns:
     *               - Column 1: the preposition
     *               - Column 2: the case annotation.
     * @return a [Pair] where the first element is the preposition (a [String]),
     *         and the second element is the case annotation (a [String]).
     */
    fun extractPrepositionCase(cursor: Cursor): Pair<String, String> {
        val preposition = cursor.getString(1)
        val caseAnnotation = cursor.getString(2)
        return preposition to caseAnnotation
    }

    /**
     * Processes a [Cursor] to extract prepositions and their associated case annotations.
     * The data is stored in a [HashMap], where the key is the preposition and the value is a list
     * of associated case annotations. If a preposition appears more than once, its case annotations
     * are accumulated in the list.
     *
     * This function expects that the cursor contains at least two columns:
     * - Column 1: Preposition (a [String]).
     * - Column 2: Case annotation (a [String]).
     *
     * The [Cursor] is iterated over, and the [extractPrepositionCase] function is used to extract
     * the preposition and case annotation for each row. The result is stored in the map, with the
     * preposition as the key and a list of case annotations as the value.
     *
     * @param cursor The [Cursor] containing preposition and case annotation data.
     *               It is assumed to have two columns:
     *               - The first column contains the preposition (a [String]).
     *               - The second column contains the case annotation (a [String]).
     * @return A [HashMap] where each key is a preposition and the value is a list of associated
     *         case annotations.
     */
    fun processCursor(cursor: Cursor): HashMap<String, MutableList<String>> {
        val result = HashMap<String, MutableList<String>>()
        if (cursor.moveToFirst()) {
            do {
                val (preposition, caseAnnotation) = extractPrepositionCase(cursor)
                if (result.containsKey(preposition)) {
                    result[preposition]?.add(caseAnnotation)
                } else {
                    result[preposition] = mutableListOf(caseAnnotation)
                }
            } while (cursor.moveToNext())
        }
        return result
    }

    /**
     * Retrieves case annotations associated with prepositions from the database for a specified language.
     * The case annotations are stored in a [HashMap], where the key is the preposition, and the value
     * is a list of associated case annotations.
     *
     * This function opens the database for the specified language, queries the `PREPOSITIONS` table
     * to fetch all prepositions and their corresponding case annotations, and processes the data using
     * the [processCursor] function. The resulting [HashMap] is returned, containing the prepositions as
     * keys and the case annotations as values.
     *
     * @param language The language code for the database from which the case annotations are to be retrieved.
     * @return A [HashMap] where each key is a preposition, and the value is a list of case annotations
     *         associated with that preposition.
     */
    fun getCaseAnnotations(language: String): HashMap<String, MutableList<String>> {
        val db = openDatabase(language)
        val result = HashMap<String, MutableList<String>>()

        db.use { database ->
            database.rawQuery("SELECT * FROM PREPOSITIONS", null)?.use { cursor ->
                result.putAll(processCursor(cursor))
            }
        }
        Log.i("MY-TAG", " These are the case annotations ${result["in"]}")
        return result
    }
}
