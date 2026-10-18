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
) : SQLiteOpenHelper(context, "${language.uppercase()}LanguageData.sqlite", null, 1) {
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

            val createdTables = mutableSetOf<String>()

            response.contract.fields.forEach { (tableName, columns) ->
                if (!SqlIdentifier.isValid(tableName) || columns.keys.any { !SqlIdentifier.isValid(it) }) {
                    Log.w("SCRIBE_DB", "Skipping table with an unsupported name: $tableName")
                    return@forEach
                }
                val colDefinition = columns.keys.joinToString(", ") { "${SqlIdentifier.quote(it)} TEXT" }
                db.execSQL("DROP TABLE IF EXISTS ${SqlIdentifier.quote(tableName)}")
                db.execSQL(
                    "CREATE TABLE ${SqlIdentifier.quote(tableName)} " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, $colDefinition)",
                )
                createdTables.add(tableName)
            }

            response.data.forEach { (tableName, rows) ->
                if (tableName !in createdTables) {
                    Log.w("SCRIBE_DB", "Skipping data for a table that was not created: $tableName")
                    return@forEach
                }
                val cv = ContentValues()
                rows.forEach { row ->
                    cv.clear()
                    row.forEach { (key, value) ->
                        if (SqlIdentifier.isValid(key)) {
                            cv.put(key, value?.toString() ?: "")
                        }
                    }
                    db.insert(tableName, null, cv)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            Log.e("SCRIBE_DB", "Error during insert: ${e.message}")
            throw e
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}
