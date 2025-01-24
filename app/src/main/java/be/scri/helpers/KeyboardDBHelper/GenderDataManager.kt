import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class GenderDataManager(
    private val context: Context,
) {
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

    private fun hasCanonicalGender(jsonData: DataContract): Boolean =
        jsonData.genders.canonical
            .firstOrNull()
            ?.isNotEmpty() == true

    private fun hasMasculineFeminine(jsonData: DataContract): Boolean =
        jsonData.genders.masculines.isNotEmpty() &&
            jsonData.genders.feminines.isNotEmpty()

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
