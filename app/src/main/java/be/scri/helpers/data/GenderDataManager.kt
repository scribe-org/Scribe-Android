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
     * @param jsonData The data contract defining gender fields.
     * @return A map of base words to their gendered variations.
     */
    fun findGenderOfWord(
        language: String,
        jsonData: DataContract?,
    ): HashMap<String, List<String>> =
        jsonData?.let { contract ->
            fileManager.getLanguageDatabase(language)?.use { db ->
                processGenderData(db, contract)
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
            else -> Log.w("GenderDataManager", "No valid gender columns found in contract.")
        }
        return genderMap
    }

    private fun hasCanonicalGender(jsonData: DataContract): Boolean =
        jsonData.genders.canonical
            .firstOrNull()
            ?.isNotEmpty() == true

    private fun hasMasculineFeminine(jsonData: DataContract): Boolean {
        val genders = jsonData.genders

        val hasMasculines = genders.masculines.isNotEmpty()
        val hasFeminines = genders.feminines.isNotEmpty()

        return hasMasculines && hasFeminines
    }

    /**
     * Queries the DB and iterates through the cursor to process noun genders.
     * This function's complexity is now reduced.
     */
    private fun processGenders(
        db: SQLiteDatabase,
        nounColumn: String?,
        genderMap: HashMap<String, List<String>>,
        genderColumn: String? = null,
        defaultGender: String? = null,
    ) {
        // Query only the columns we actually need.
        val columnsToSelect = listOfNotNull(nounColumn, genderColumn).distinct()
        if (columnsToSelect.isEmpty()) {
            Log.e("GenderDataManager", "No valid noun or gender columns provided.")
            return
        }
        val selection = columnsToSelect.joinToString(", ") { "`$it`" }

        db.rawQuery("SELECT $selection FROM nouns", null)?.use { cursor ->
            val nounIndex = cursor.getColumnIndex(nounColumn)
            val genderIndex = genderColumn?.let { cursor.getColumnIndex(it) } ?: -1

            if (nounIndex == -1) {
                Log.e("GenderDataManager", "Required noun column '$nounColumn' not found.")
                return
            }

            while (cursor.moveToNext()) {
                processGenderRow(cursor, nounIndex, genderIndex, defaultGender, genderMap)
            }
        }
    }

    /**
     * Processes a single row from the cursor to extract and map gender data.
     * This new helper function contains the logic from the old while-loop.
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
