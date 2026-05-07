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
 * Helper class for managing dynamic SQLite databases for conjugate data (verbs only).
 * It creates only the verbs table and inserts verb data according to the provided DataResponse.
 */
class ConjugateDynamicDbHelper(
    context: Context,
    language: String,
) : SQLiteOpenHelper(context, "${language.uppercase()}ConjugateData.sqlite", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        // Tables are created dynamically via syncConjugateDatabase from API contract.
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        old: Int,
        new: Int,
    ) {
        // Dynamic schema updates are handled via syncConjugateDatabase.
    }

    /**
     * Synchronizes the conjugate database schema and data based on the provided DataResponse.
     * Only creates the verbs table and inserts verb data, ignoring other data types.
     * @param response The data response containing the contract and data to be inserted.
     */
    fun syncConjugateDatabase(response: DataResponse) {
        val db = writableDatabase
        try {
            db.beginTransaction()

            // Check if verbs table exists in the contract
            val verbsColumns = response.contract.fields["verbs"]
            if (verbsColumns != null) {
                // Create verbs table
                val colDefinition = verbsColumns.keys.joinToString(", ") { "$it TEXT" }
                db.execSQL("DROP TABLE IF EXISTS verbs")
                db.execSQL(
                    "CREATE TABLE verbs " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, $colDefinition)",
                )

                // Insert verbs data
                val verbsData = response.data["verbs"]
                if (verbsData != null) {
                    val cv = ContentValues()
                    verbsData.forEach { row ->
                        cv.clear()
                        row.forEach { (key, value) ->
                            cv.put(key, value?.toString() ?: "")
                        }
                        db.insert("verbs", null, cv)
                    }
                    Log.i("CONJUGATE_DB", "Successfully synced ${verbsData.size} verb records for ${response.language}")
                } else {
                    Log.w("CONJUGATE_DB", "No verbs data found in response for ${response.language}")
                }
            } else {
                Log.e("CONJUGATE_DB", "No verbs table found in contract for ${response.language}")
            }

            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            Log.e("CONJUGATE_DB", "Error during conjugate database sync: ${e.message}")
            throw e
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}
