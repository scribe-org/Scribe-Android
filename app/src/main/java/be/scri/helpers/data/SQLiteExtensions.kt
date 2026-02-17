// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.data

import android.database.sqlite.SQLiteDatabase

fun SQLiteDatabase.tableExists(tableName: String): Boolean =
    rawQuery(
        "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'",
        null,
    ).use { it.moveToFirst() }

fun SQLiteDatabase.columnExists(
    tableName: String,
    columnName: String,
): Boolean =
    rawQuery("PRAGMA table_info($tableName)", null).use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex("name")
            do {
                if (cursor.getString(nameIndex) == columnName) {
                    return true
                }
            } while (cursor.moveToNext())
        }
        false
    }
