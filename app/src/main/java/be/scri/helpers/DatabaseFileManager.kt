// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Manages access to all SQLite database files.
 * Ensures DB files are copied from assets and provides read-only connections.
 * @param context The application context.
 */
class DatabaseFileManager(
    private val context: Context,
) {
    /**
     * Companion object used for logging and shared constants.
     *
     * @see DatabaseFileManager
     */
    companion object {
        private const val TAG = "DatabaseFileManager"
    }

    /**
     * Retrieves a read-only [SQLiteDatabase] instance for a specific language's data.
     * It handles copying the database from assets if it doesn't exist locally.
     *
     * @param language The language code (e.g., "DE", "FR") used to determine the database filename.
     *
     * @return An open, read-only [SQLiteDatabase] instance, or `null` on failure.
     */
    fun getLanguageDatabase(language: String): SQLiteDatabase? {
        val dbName = "${language}LanguageData.sqlite"
        return getDatabase(dbName, "data/$dbName")
    }

    /**
     * Retrieves a read-only [SQLiteDatabase] instance for the translation data.
     * It handles copying the database from assets if it doesn't exist locally.
     *
     * @return An open, read-only [SQLiteDatabase] instance, or `null` on failure.
     */
    fun getTranslationDatabase(): SQLiteDatabase? {
        val dbName = "TranslationData.sqlite"
        return getDatabase(dbName, "data/$dbName")
    }

    /**
     * A generic function to get a database. It ensures the database file exists in the app's
     * private storage (copying it from assets if necessary) and then opens a read-only connection.
     *
     * @param dbName The filename of the database (e.g., "ENLanguageData.sqlite").
     * @param assetPath The path to the database file within the app's assets folder
     * (e.g., "data/ENLanguageData.sqlite").
     *
     * @return An open, read-only [SQLiteDatabase], or `null` if copying or opening fails.
     */
    private fun getDatabase(
        dbName: String,
        assetPath: String,
    ): SQLiteDatabase? {
        val dbFile = context.getDatabasePath(dbName)

        if (!dbFile.exists()) {
            if (!copyDatabaseFromAssets(dbFile, assetPath)) {
                return null
            }
        }

        // At this point, the file should exist. Attempt to open it.
        return try {
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
            Log.e(TAG, "Failed to open database $dbName", e)
            null
        }
    }

    /**
     * Copies a database file from the app's assets folder to its internal database directory.
     * This is called when a database is accessed for the first time.
     *
     * @param dbFile The destination [File] in the app's database directory.
     * @param assetPath The path to the source file within the assets folder.
     *
     * @return true if the copy was successful, false otherwise.
     */
    private fun copyDatabaseFromAssets(
        dbFile: File,
        assetPath: String,
    ): Boolean =
        try {
            dbFile.parentFile?.mkdirs()

            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.i(TAG, "Database copied from assets to ${dbFile.path}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error copying database from assets to ${dbFile.path}", e)
            dbFile.delete()
            false
        }
}
