// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * A helper to facilitate loading and
 * managing database files for different languages in the Scribe keyboard.
 *
 */

package be.scri.helpers

import android.content.Context
import android.util.Log
import java.io.FileOutputStream

class DatabaseFileManager(
    private val context: Context,
) {
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
