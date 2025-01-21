// SPDX-License-Identifier: AGPL-3.0-or-later

/**
 * A helper to facilitate database calls for Scribe keyboard commands.
 */

package be.scri.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(
    context: Context,
) : SQLiteOpenHelper(
        context,
        null,
        null,
        DATABASE_VERSION,
    ) {
    private val dbManagers = DatabaseManagers(context)

    companion object {
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // No operation for now
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        // No operation for now
    }

    fun loadDatabase(language: String) {
        dbManagers.fileManager.loadDatabaseFile(language)
    }

    fun getRequiredData(language: String): DataContract? =
        dbManagers.contractLoader.loadContract(
            language,
        )

    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> =
        dbManagers.emojiManager.getEmojiKeywords(
            language,
        )

    fun findGenderOfWord(language: String): HashMap<String, List<String>> =
        dbManagers.genderManager.findGenderOfWord(
            language,
            getRequiredData(language),
        )

    fun checkIfWordIsPlural(language: String): List<String>? =
        dbManagers.pluralManager.checkIfWordIsPlural(
            language,
            getRequiredData(language),
        )
}
