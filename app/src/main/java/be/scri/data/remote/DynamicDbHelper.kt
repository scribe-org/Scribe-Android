// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.remote

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import be.scri.data.model.DataResponse

/**
 * Helper class for managing dynamic SQLite databases based on language.
 * It creates tables and inserts data according to the provided DataResponse.
 */
class DynamicDbHelper(
    context: Context,
    language: String,
) : SQLiteOpenHelper(context, "${language}LanguageData.sqlite", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        // Tables are created dynamically via syncDatabase from API contract.
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        old: Int,
        new: Int,
    ) {
        // Dynamic schema updates are handled via syncDatabase.
    }

    /**
     * Synchronizes the database schema and data based on the provided DataResponse.
     * @param response The data response containing the contract and data to be inserted.
     */
    fun syncDatabase(response: DataResponse) {
        val db = writableDatabase
        try {
            db.beginTransaction()

            response.contract.fields.forEach { (tableName, columns) ->
                val colDefinition = columns.keys.joinToString(", ") { "$it TEXT" }
                db.execSQL("DROP TABLE IF EXISTS $tableName")
                db.execSQL(
                    "CREATE TABLE $tableName " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, $colDefinition)",
                )
            }

            response.data.forEach { (tableName, rows) ->
                val cv = ContentValues()
                rows.forEach { row ->
                    cv.clear()
                    row.forEach { (key, value) ->
                        cv.put(key, value?.toString() ?: "")
                    }
                    db.insert(tableName, null, cv)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            Log.e("SCRIBE_DB", "Error during insert: ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}
