// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.util.Log
import java.io.FileOutputStream

/**
 * A helper class to facilitate loading and managing database files for different languages in the Scribe keyboard.
 * This class is responsible for loading language-specific SQLite database files from assets into the app's
 * internal storage.
 *
 * @param context The context used to access the app's resources and file system.
 */
class DatabaseFileManager(
    private val context: Context,
) {
    /**
     * Loads a database file for a specific language.
     * <p>
     * The method checks if the database file for the given language already exists in the app's internal
     * storage. If it doesn't exist, it copies the corresponding database file from the assets folder to the
     * internal storage.
     * </p>
     *
     * @param language The language for which the database file is being loaded.
     */
    fun loadDatabaseFile(language: String) {
        val databaseName = "${language}LanguageData.sqlite"
        val dbFile = context.getDatabasePath(databaseName)
        Log.i("ALPHA", "Loaded Database")

        if (!dbFile.exists()) {
            context.assets.open("data/$databaseName").use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }
            }
        }
    }
}
