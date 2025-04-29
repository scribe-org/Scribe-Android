package be.scri.helpers.keyboardDBHelper

import DataContract
import android.content.Context
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
    ): MutableMap<String, List<String>> {
        var result: MutableList<String>
        val conjugationIds = jsonData?.conjugations?.keys
        val result1: MutableMap<String, List<String>> = mutableMapOf()
        if (conjugationIds != null) {
            for (id in conjugationIds) {
                val intId = id.toIntOrNull() ?: continue

                val conjugation = jsonData.conjugations[id]
                val persons =
                    listOf(
                        "firstPerson" to conjugation?.firstPerson,
                        "secondPerson" to conjugation?.secondPerson,
                        "thirdPersonSingular" to conjugation?.thirdPersonSingular,
                        "firstPersonPlural" to conjugation?.firstPersonPlural,
                        "secondPersonPlural" to conjugation?.secondPersonPlural,
                        "thirdPersonPlural" to conjugation?.thirdPersonPlural,
                    )

                Log.i("ALPHA", "Conjugation $intId: title is ${conjugation?.title}")
                Log.i("ALPHA", "The word is $word")
                result = mutableListOf()
                for ((label, personMap) in persons) {
                    val personValue = personMap?.values?.firstOrNull()
                    val resultValue =
                        getTheValueForTheConjugateWord(
                            word,
                            personValue,
                            language = language,
                        )
                    Log.i("ALPHA", "Conjugation $intId: $label is $personValue")
                    Log.i("ALPHA", "Conjugation $intId: $label value is $resultValue")

                    result.add(resultValue)
                    result1[conjugation?.title!!] = result
                }
            }
        }
        extractConjugateHeadings(jsonData)
        result1["word"] = listOf(word)
        Log.i("MY-TAG", "the result1 is $result1")
        return result1
    }

    /**
     * Extracts all unique conjugation headings from the provided data contract.
     *
     * @param jsonData The data contract containing conjugation information
     * @return A set of unique conjugation heading strings
     */
    fun extractConjugateHeadings(jsonData: DataContract?): Set<String> {
        val conjugationIds = jsonData?.conjugations?.values

        val keys =
            conjugationIds
                ?.flatMap { conjugation ->
                    listOfNotNull(
                        conjugation.firstPerson?.keys,
                        conjugation.secondPerson?.keys,
                        conjugation.thirdPersonSingular?.keys,
                        conjugation.firstPersonPlural?.keys,
                        conjugation.secondPersonPlural?.keys,
                        conjugation.thirdPersonPlural?.keys,
                    ).flatten()
                }?.toSet() ?: emptySet()
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
        val dbPath = context.getDatabasePath("${language}LanguageData.sqlite")

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open("data/${language}LanguageData.sqlite").use { inputStream ->
                FileOutputStream(dbPath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        val db = SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
        var result = ""

        db.use { database ->
            val verbQuery =
                if (language == "SV") {
                    "SELECT * FROM verbs WHERE verb = ?"
                } else {
                    "SELECT * FROM verbs WHERE infinitive = ?"
                }

            database.rawQuery(verbQuery, arrayOf(word)).use { verbCursor ->
                if (verbCursor.moveToFirst()) {
                    if (form != null) {
                        try {
                            result = verbCursor.getString(verbCursor.getColumnIndexOrThrow(form))
                        } catch (e: IllegalArgumentException) {
                            if (language != "EN") {
                                val bracketRegex = Regex("""\[(.*?)\]""")
                                val wordsRegex = Regex("""\b(\w+)\s+(\w+)\b""")

                                val bracketPart = bracketRegex.find(form)?.groupValues?.get(1)
                                val matchResult = wordsRegex.find(bracketPart ?: "")

                                val fallbackFields = mutableListOf<String>()

                                if (matchResult != null) {
                                    val (firstWord, secondWord) = matchResult.destructured
                                    fallbackFields.add(firstWord)
                                    fallbackFields.add(secondWord)
                                    Log.i("MY-TAG", "Parsed words: $firstWord, $secondWord")
                                }
                                val rest = form.replace(bracketRegex, "").trim()
                                if (rest.isNotEmpty()) {
                                    fallbackFields.add(rest)
                                }
                                Log.i("MY-TAG", "The fallback fields are $fallbackFields")
                                database.rawQuery("SELECT ${fallbackFields[1]} FROM verbs WHERE infinitive = ?", arrayOf(word)).use { auxiliaryVerbCursor ->
                                    if (auxiliaryVerbCursor.moveToFirst()) {
                                        fallbackFields[1] = auxiliaryVerbCursor.getString(auxiliaryVerbCursor.getColumnIndexOrThrow(fallbackFields[1]))
                                    }
                                }
                                fallbackFields[2] = verbCursor.getString(verbCursor.getColumnIndexOrThrow(fallbackFields[2]))

                                database
                                    .rawQuery(
                                        "SELECT ${fallbackFields[0]} FROM verbs WHERE wdLexemeID = ? ",
                                        arrayOf(fallbackFields[1]),
                                    ).use { formCursor ->
                                        if (formCursor.moveToFirst()) {
                                            fallbackFields[1] = formCursor.getString(formCursor.getColumnIndexOrThrow(fallbackFields[0]))
                                        }
                                    }

                                result = fallbackFields[1] + " " + fallbackFields[2]
                            } else {
                                val bracketRegex = Regex("""\[(.*?)\]""")
                                val wordsRegex = Regex("""\b(\w+)\s+(\w+)\b""")

                                val bracketPart = bracketRegex.find(form)?.groupValues?.get(1)
                                val matchResult = wordsRegex.find(bracketPart ?: "")

                                val fallbackFields = mutableListOf<String>()

                                if (matchResult != null) {
                                    val (firstWord, secondWord) = matchResult.destructured
                                    fallbackFields.add(firstWord)
                                    fallbackFields.add(secondWord)
                                    Log.i("MY-TAG", "Parsed words: $firstWord, $secondWord")
                                }
                                val rest = form.replace(bracketRegex, "").trim()
                                if (rest.isNotEmpty()) {
                                    fallbackFields.add(rest)
                                }
                                Log.i("MY-TAG", "The fallback fields are $fallbackFields")

                                database
                                    .rawQuery(
                                        "SELECT ${fallbackFields[0]} FROM verbs WHERE infinitive = ?",
                                        arrayOf(fallbackFields[1]),
                                    ).use { auxiliaryVerbCursor ->
                                        if (auxiliaryVerbCursor.moveToFirst()) {
                                            fallbackFields[1] = auxiliaryVerbCursor.getString(auxiliaryVerbCursor.getColumnIndexOrThrow(fallbackFields[0]))
                                        }
                                    }

                                fallbackFields[2] = verbCursor.getString(verbCursor.getColumnIndexOrThrow(fallbackFields[2]))

                                Log.i("MY-TAG", "The updated fallback fields are $fallbackFields")

                                result = fallbackFields[1] + " " + fallbackFields[2]
                            }
                        }
                    }
                }
            }
        }
        return result
    }
}
