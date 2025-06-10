// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import DataContract
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
    context: Context,
) : SQLiteOpenHelper(
        context,
        null,
        null,
        DATABASE_VERSION,
    ) {
    private val dbManagers = DatabaseManagers(context)

    /**
     * The database version of the application.
     */
    companion object {
        private const val DATABASE_VERSION = 1
    }

    /**
     * Creates the database tables. Currently, this method does nothing as no database schema is defined.
     *
     * @param db The database to be created.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // No operation for now
    }

    /**
     * Handles database upgrades. Currently, this method does nothing as there is no upgrade strategy defined.
     *
     * @param db The database to be upgraded.
     * @param oldVersion The old version of the database.
     * @param newVersion The new version of the database.
     */
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        // No operation for now
    }

    /**
     * Loads the database for a specific language.
     * This method checks if the required database file is available and loads it if necessary.
     *
     * @param language The language for which the database is being loaded.
     */
    fun loadDatabase(language: String) {
        dbManagers.fileManager.loadDatabaseFile(language)
    }

    /**
     * Retrieves the required data for a specific language.
     *
     * @param language The language for which the data is being fetched.
     * @return The [DataContract] containing the data for the specified language, or null if not found.
     */
    private fun getRequiredData(language: String): DataContract? =
        dbManagers.contractLoader.loadContract(
            language,
        )

    /**
     * Retrieves the emoji keywords for a specific language.
     *
     * @param language The language for which the emoji keywords are being fetched.
     * @return A [HashMap] mapping emoji keywords to their corresponding list of keywords.
     */
    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> =
        dbManagers.emojiManager.getEmojiKeywords(
            language,
        )

    /**
     * Finds the gender of words for a given language.
     *
     * @param language The language for which the gender of words is being determined.
     * @return A [HashMap] mapping words to their corresponding gender.
     */
    fun findGenderOfWord(language: String): HashMap<String, List<String>> =
        dbManagers.genderManager.findGenderOfWord(
            language,
            getRequiredData(language),
        )

    /**
     * Checks if a word is plural in a given language.
     *
     * @param language The language for which the plural form of the word is being checked.
     * @return A list of words that are considered plural, or null if not found.
     */
    fun checkIfWordIsPlural(language: String): List<String>? =
        dbManagers.pluralManager.checkIfWordIsPlural(
            language,
            getRequiredData(language),
        )

    /**
     * Retrieves the maximum length of emoji keywords.
     *
     * @return The maximum length of an emoji keyword.
     */
    fun getEmojiMaxKeywordLength(): Int = dbManagers.emojiManager.maxKeywordLength

    /**
     * Finds the case annotations for prepositions in a given language.
     * This method is only applicable for German (DE) and Russian (RU).
     *
     * @param language The language for which case annotations for prepositions are being fetched.
     * @return A [HashMap] mapping prepositions to their corresponding case annotations.
     */
    fun findCaseAnnnotationForPreposition(language: String): HashMap<String, MutableList<String>> =
        if (language != "DE" && language != "RU") {
            hashMapOf()
        } else {
            dbManagers.prepositionManager.getCaseAnnotations(
                language,
            )
        }

    /**
     * Retrieves the plural representation of a noun for a specific language.
     *
     * @param language The language for which the plural representation is being fetched.
     * @param noun The noun for which the plural form is being queried.
     * @return A [Map] containing the singular and plural forms of the noun.
     */
    fun getPluralRepresentation(
        language: String,
        noun: String,
    ): Map<String, String?> =
        dbManagers.pluralManager.queryPluralRepresentation(
            language,
            getRequiredData(language),
            noun,
        )

    /**
     * Retrieves the translation of a given word between the source and destination languages.
     *
     * This function determines the source and destination language ISO codes based on the provided
     * language name, and then fetches the translation of the specified word using those language codes.
     *
     * @param language The language name (e.g., "english") to determine the source and destination languages.
     * @param word The word whose translation is to be fetched.
     * @return The translation of the given word in the destination language,
     * or an empty string if no translation is found.
     */
    fun getTranslationSourceAndDestination(
        language: String,
        word: String,
    ): String {
        val sourceAndDestination = dbManagers.translationDataManager.getSourceAndDestinationLanguage(language)
        return dbManagers.translationDataManager.getTranslationDataForAWord(sourceAndDestination, word)
    }

    fun getConjugateData(language: String) {
        Log.i("alpha", "The data contract is ${getRequiredData(language)}")
        dbManagers.conjugateDataManager.getTheConjugateLabels(language, getRequiredData(language))
    }
}
