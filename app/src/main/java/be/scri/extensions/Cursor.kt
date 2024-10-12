package be.scri.extensions

import android.database.Cursor

fun Cursor.getIntValue(key: String) = getInt(getColumnIndexOrThrow(key))

fun Cursor.getIntValueOrNull(key: String) = if (isNull(getColumnIndexOrThrow(key))) null else getInt(getColumnIndexOrThrow(key))
