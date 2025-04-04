// SPDX-License-Identifier: GPL-3.0-or-later

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * A helper class to manage and process gender data from an SQLite database for a given language and JSON contract.
 */
class GenderDataManager(
    private val context: Context,
) {
    /**
     * Retrieves gender-related word mappings for a given language from the local SQLite database.
     *
     * @param language the language code (e.g., "en", "fr") used to locate the corresponding database.
     * @param jsonData the [DataContract] defining which tables and fields to extract gender mappings from.
     * @return a [HashMap] where each key is a base word and its value is a list of gendered variations.
     *
     * If the [jsonData] is `null` or the database does not exist, an empty map is returned.
     */
    fun findGenderOfWord(
        language: String,
        jsonData: DataContract?,
    ): HashMap<String, List<String>> =
        when {
            jsonData == null -> HashMap()
            !context.getDatabasePath("${language}LanguageData.sqlite").exists() -> {
                Log.e("MY-TAG", "Database file for $language does not exist.")
                HashMap()
            }

            else -> processGenderData("${language}LanguageData.sqlite", jsonData)
        }

    /**
     * Processes the gender data from the SQLite database using the schema defined in the given [DataContract].
     *
     * @param dbFileName the name of the SQLite database file containing the gender data.
     * @param contract the [DataContract] that specifies which table and fields to use for extracting gender variations.
     * @return a [HashMap] mapping base words to their gendered variants.
     *
     * The function reads the specified table and columns from the database and organizes the data
     * into a map for easy access to gender-related word forms.
     */
    private fun processGenderData(
        dbPath: String,
        jsonData: DataContract,
    ): HashMap<String, List<String>> =
        HashMap<String, List<String>>().also { genderMap ->
            context.getDatabasePath(dbPath).path.let { path ->
                SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    when {
                        hasCanonicalGender(jsonData) ->
                            processGenders(
                                db = db,
                                nounColumn = jsonData.numbers.keys.firstOrNull(),
                                genderColumn = jsonData.genders.canonical.firstOrNull(),
                                genderMap = genderMap,
                            )

                        hasMasculineFeminine(jsonData) -> {
                            processGenders(
                                db = db,
                                nounColumn = jsonData.genders.masculines.firstOrNull(),
                                genderMap = genderMap,
                                defaultGender = "masculine",
                            )
                            processGenders(
                                db = db,
                                nounColumn = jsonData.genders.feminines.firstOrNull(),
                                genderMap = genderMap,
                                defaultGender = "feminine",
                            )
                        }

                        else -> Log.e("MY-TAG", "No valid gender columns found.")
                    }
                }
            }
            Log.i("MY-TAG", "Found ${genderMap.size} gender entries")
        }

    /**
     * Checks whether the gender data contains a non-empty canonical gender entry.
     *
     * @param jsonData the [DataContract] containing gender metadata.
     * @return `true` if the canonical gender list has at least one non-empty entry; `false` otherwise.
     */
    private fun hasCanonicalGender(jsonData: DataContract): Boolean =
        jsonData.genders.canonical
            .firstOrNull()
            ?.isNotEmpty() == true

    /**
     * Determines whether both masculine and feminine gender lists are present and non-empty
     * in the given [DataContract].
     *
     * @param jsonData the [DataContract] containing gender metadata.
     * @return `true` if both masculine and feminine lists are non-empty; `false` otherwise.
     */
    private fun hasMasculineFeminine(jsonData: DataContract): Boolean =
        jsonData.genders.masculines.isNotEmpty() &&
            jsonData.genders.feminines.isNotEmpty()

    /**
     * Processes the gender data for nouns from the database and stores it in the provided gender map.
     *
     * This function reads from the "nouns" table in the database, extracting noun names and their associated genders.
     * It adds the genders to the provided [genderMap] where the noun is the key, and the list of genders is the value.
     * The function handles optional gender columns and allows setting a default gender if no specific gender is found.
     *
     * @param db the SQLite database instance to query.
     * @param nounColumn the name of the column containing the noun names.
     * @param genderMap a [HashMap] that will be populated with noun-gender pairs.
     * @param genderColumn the name of the column containing gender data (optional). If `null`, the default gender will
     * be used.
     * @param defaultGender the default gender to be used if no specific gender is found for a noun.
     */
    @Suppress("NestedBlockDepth")
    private fun processGenders(
        db: SQLiteDatabase,
        nounColumn: String?,
        genderMap: HashMap<String, List<String>>,
        genderColumn: String? = null,
        defaultGender: String? = null,
    ) {
        db.rawQuery("SELECT * FROM nouns", null)?.use { cursor ->
            val nounIndex = cursor.getColumnIndex(nounColumn)
            val genderIndex = genderColumn?.let { cursor.getColumnIndex(it) } ?: -1

            if (nounIndex == -1 || (genderColumn != null && genderIndex == -1)) {
                Log.e("MY-TAG", "Required columns not found.")
                return
            }

            while (cursor.moveToNext()) {
                cursor.getString(nounIndex)?.lowercase()?.takeUnless { it.isEmpty() }?.let { noun ->
                    val gender =
                        when {
                            genderColumn != null -> cursor.getString(genderIndex)
                            else -> defaultGender
                        }

                    if (!gender.isNullOrEmpty()) {
                        val existingGenders = genderMap[noun]?.toMutableList() ?: mutableListOf()
                        if (!existingGenders.contains(gender)) {
                            existingGenders.add(gender)
                            genderMap[noun] = existingGenders
                        }
                    }
                }
            }
        }
        Log.i("MY-TAG", genderMap["schild"].toString())
    }
}
