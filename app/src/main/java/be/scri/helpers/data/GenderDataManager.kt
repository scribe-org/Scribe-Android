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
     * Gets gender-related word mappings for a language.
     * @param language The language code (e.g., "DE", "FR").
     * @param contract The data contract defining gender fields.
     * @return A map of base words to their gendered variations.
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
     * Processes gender data from the DB using a contract.
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

    private fun hasCanonicalGender(contract: DataContract): Boolean =
        contract.genders.canonical
            .firstOrNull()
            ?.isNotEmpty() == true

    private fun hasMasculineFeminine(contract: DataContract): Boolean {
        val masculineList = contract.genders.masculines
        val feminineList = contract.genders.feminines

        val hasMasculine = masculineList.isNotEmpty()
        val hasFeminine = feminineList.isNotEmpty()

        return hasMasculine && hasFeminine
    }

    /**
     * Queries the DB and iterates through the cursor to process noun genders.
     * This function is now more defensive against invalid column names from the contract.
     */
    private fun processGenders(
        db: SQLiteDatabase,
        nounColumn: String?,
        genderMap: HashMap<String, List<String>>,
        genderColumn: String? = null,
        defaultGender: String? = null,
    ) {
        // Ensure we have a valid noun column to proceed
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
     * Processes a single row from the cursor to extract and map gender data.
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
