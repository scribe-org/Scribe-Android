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
     * Gets a read-only database for a specific language.
     * @param language The language code (e.g., "DE", "FR").
     * @return An open, read-only SQLiteDatabase, or null on failure.
     */
    fun getLanguageDatabase(language: String): SQLiteDatabase? {
        val dbName = "${language}LanguageData.sqlite"
        return getDatabase(dbName, "data/$dbName")
    }

    /**
     * Gets a read-only database for translations.
     * @return An open, read-only SQLiteDatabase, or null on failure.
     */
    fun getTranslationDatabase(): SQLiteDatabase? {
        val dbName = "TranslationData.sqlite"
        return getDatabase(dbName, "data/$dbName")
    }

    /**
     * Ensures a database file exists and opens it.
     * @param dbName The name of the database file.
     * @param assetPath The path to the file within the assets folder.
     * @return An open, read-only SQLiteDatabase, or null on failure.
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
     * Copies a database file from the assets folder to the app's database directory.
     * This logic is extracted to reduce nesting in the getDatabase function.
     *
     * @param dbFile The destination file path.
     * @param assetPath The path to the file within the assets folder.
     * @return True if the copy was successful, false otherwise.
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
