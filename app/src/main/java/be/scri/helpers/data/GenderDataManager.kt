// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import be.scri.helpers.DatabaseFileManager

/**
 * Manages and processes gender data from the database.
 * @param fileManager The central manager for database file access.
 */
class GenderDataManager(
    private val fileManager: DatabaseFileManager,
) {
    /**
     * Retrieves a map of words and their associated grammatical genders for a given language.
     *
     * @param language The language code (e.g., "DE", "FR") to select the correct database.
     * @param contract The data contract for the language, which defines the gender-related database columns.
     *
     * @return A [HashMap] where keys are lowercase nouns and values are a list of their gender(s) (e.g., "masculine").
     */
    fun findGenderOfWord(
        language: String,
        contract: DataContract?,
    ): HashMap<String, List<String>> =
        contract?.let {
            fileManager.getLanguageDatabase(language)?.use { db ->
                processGenderData(db, it)
            }
        } ?: hashMapOf()

    /**
     * The main processing function that dispatches to the correct gender-handling logic
     * based on the structure defined in the [DataContract].
     *
     * @param db The SQLite database instance.
     * @param contract The data contract defining how gender is stored for this language.
     *
     * @return A [HashMap] of nouns to their genders.
     */
    private fun processGenderData(
        db: SQLiteDatabase,
        contract: DataContract,
    ): HashMap<String, List<String>> {
        val genderMap = HashMap<String, List<String>>()

        when {
            hasCanonicalGender(contract) ->
                processGenders(
                    db = db,
                    nounColumn = contract.numbers.keys.firstOrNull(),
                    genderColumn = contract.genders.canonical.firstOrNull(),
                    genderMap = genderMap,
                )
            hasMasculineFeminine(contract) -> {
                processGenders(
                    db = db,
                    nounColumn = contract.genders.masculines.firstOrNull(),
                    genderMap = genderMap,
                    defaultGender = "masculine",
                )
                processGenders(
                    db = db,
                    nounColumn = contract.genders.feminines.firstOrNull(),
                    genderMap = genderMap,
                    defaultGender = "feminine",
                )
            }
            else -> Log.w("GenderDataManager", "No valid gender columns found in contract for language.")
        }
        return genderMap
    }

    /**
     * Checks if the data contract defines a single, canonical gender column.
     *
     * @param contract The data contract to check.
     *
     * @return true if a canonical gender column is specified, false otherwise.
     */
    private fun hasCanonicalGender(contract: DataContract): Boolean =
        contract.genders.canonical
            .firstOrNull()
            ?.isNotEmpty() == true

    /**
     * Checks if the data contract defines separate columns for masculine and feminine genders.
     *
     * @param contract The data contract to check.
     *
     * @return true if both masculine and feminine columns are specified, false otherwise.
     */
    private fun hasMasculineFeminine(contract: DataContract): Boolean {
        val masculineList = contract.genders.masculines
        val feminineList = contract.genders.feminines

        val hasMasculine = masculineList.isNotEmpty()
        val hasFeminine = feminineList.isNotEmpty()

        return hasMasculine && hasFeminine
    }

    /**
     * Queries the `nouns` table for gender information and populates the gender map.
     * It is defensive and will not proceed if the columns specified in the contract do not exist in the table.
     *
     * @param db The SQLite database to query.
     * @param nounColumn The name of the column containing the noun.
     * @param genderMap The map to populate with results.
     * @param genderColumn The name of the column containing the gender information (optional).
     * @param defaultGender A default gender to assign if `genderColumn` is not provided (optional).
     */
    private fun processGenders(
        db: SQLiteDatabase,
        nounColumn: String?,
        genderMap: HashMap<String, List<String>>,
        genderColumn: String? = null,
        defaultGender: String? = null,
    ) {
        if (nounColumn.isNullOrEmpty()) {
            Log.e("GenderDataManager", "No valid noun column provided in contract.")
            return
        }

        val columnsToSelect = listOfNotNull(nounColumn, genderColumn).distinct()

        db.rawQuery("SELECT * FROM nouns LIMIT 1", null).use { tempCursor ->
            for (column in columnsToSelect) {
                if (tempCursor.getColumnIndex(column) == -1) {
                    Log.e(
                        "GenderDataManager",
                        "Column '$column' specified in the data contract was NOT FOUND in the 'nouns' table" +
                            " Skipping this gender processing step to prevent a crash.",
                    )
                    return
                }
            }
        }

        val selection = columnsToSelect.joinToString(", ") { "`$it`" }

        db.rawQuery("SELECT $selection FROM nouns", null).use { cursor ->
            val nounIndex = cursor.getColumnIndex(nounColumn)
            // genderIndex will be valid because we checked it above.
            val genderIndex = genderColumn?.let { cursor.getColumnIndex(it) } ?: -1

            while (cursor.moveToNext()) {
                processGenderRow(cursor, nounIndex, genderIndex, defaultGender, genderMap)
            }
        }
    }

    /**
     * Processes a single row from the gender query cursor, extracting the noun and its
     * gender, and adds the entry to the provided map.
     *
     * @param cursor The database cursor, positioned at the row to process.
     * @param nounIndex The column index for the noun.
     * @param genderIndex The column index for the gender, or -1 if not applicable.
     * @param defaultGender A fallback gender to use if `genderIndex` is -1.
     * @param genderMap The map to which the noun/gender pair will be added.
     */
    private fun processGenderRow(
        cursor: Cursor,
        nounIndex: Int,
        genderIndex: Int,
        defaultGender: String?,
        genderMap: HashMap<String, List<String>>,
    ) {
        val noun = cursor.getString(nounIndex)?.lowercase()?.takeIf { it.isNotEmpty() } ?: return

        val gender =
            if (genderIndex != -1) {
                cursor.getString(genderIndex)
            } else {
                defaultGender
            }

        if (!gender.isNullOrEmpty()) {
            @Suppress("UNCHECKED_CAST")
            val list = genderMap.getOrPut(noun) { mutableListOf() } as MutableList<String>
            if (!list.contains(gender)) {
                list.add(gender)
            }
        }
    }
}
