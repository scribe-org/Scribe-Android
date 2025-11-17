package be.scri.helpers.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager

/**
 * Data class representing a clipboard entry
 */
data class ClipboardItem(
    val id: Long,
    val text: String,
    val timestampMs: Long,
    val isPinned: Boolean
)

class ClipboardDataManager(
    @Suppress("UnusedPrivateProperty")
    private val fileManager: DatabaseFileManager,
    private val context: Context
) {
    companion object {
        private const val DB_FILENAME = "clipboard_history.db"
        private const val TABLE = "clipboard_items"

        private const val COL_ID = "id"
        private const val COL_TEXT = "text"
        private const val COL_TIMESTAMP = "timestamp_ms"
        private const val COL_PINNED = "is_pinned"

        private const val MAX_CLIP_LENGTH = 4096
    }

    /** Open or create writable DB. */
    private fun openDb(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_FILENAME)
        if (!dbFile.exists()) dbFile.parentFile?.mkdirs()
        return SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    }

    /** Check Table exists or not. */
    private fun ensureTableExists(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TEXT TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_PINNED INTEGER NOT NULL DEFAULT 0
            );
            """.trimIndent()
        )
    }

    /** Get last copied text to avoid duplicates. */
    private fun getMostRecentText(): String? {
        val db = openDb()
        try {
            ensureTableExists(db)
            val cursor = db.rawQuery(
                "SELECT $COL_TEXT FROM $TABLE ORDER BY $COL_TIMESTAMP DESC LIMIT 1",
                null
            )
            return cursor.use { if (it.moveToFirst()) it.getString(0) else null }
        } finally {
            db.close()
        }
    }

    /** Insert new clipboard item and also skip the duplicates. */
    fun insertClip(rawText: String, pinned: Boolean = false): Long {
        val text = rawText.take(MAX_CLIP_LENGTH)

        // prevent consecutive duplicates
        if (getMostRecentText() == text) return -1L

        val db = openDb()
        try {
            ensureTableExists(db)
            val cv = ContentValues().apply {
                put(COL_TEXT, text)
                put(COL_TIMESTAMP, System.currentTimeMillis())
                put(COL_PINNED, if (pinned) 1 else 0)
            }
            return db.insert(TABLE, null, cv)
        } finally {
            db.close()
        }
    }

    /** Get the latest N clipboard entries */
    fun getLatest(limit: Int = 50): List<ClipboardItem> {
        val db = openDb()
        try {
            ensureTableExists(db)
            val cursor = db.rawQuery(
                "SELECT $COL_ID, $COL_TEXT, $COL_TIMESTAMP, $COL_PINNED FROM $TABLE " +
                    "ORDER BY $COL_TIMESTAMP DESC LIMIT ?",
                arrayOf(limit.toString())
            )
            return cursor.use {
                val items = mutableListOf<ClipboardItem>()
                while (it.moveToNext()) {
                    items.add(
                        ClipboardItem(
                            id = it.getLong(0),
                            text = it.getString(1),
                            timestampMs = it.getLong(2),
                            isPinned = it.getInt(3) == 1
                        )
                    )
                }
                items
            }
        } finally {
            db.close()
        }
    }

    /** Get only pinned items */
    fun getPinned(): List<ClipboardItem> {
        val db = openDb()
        try {
            ensureTableExists(db)
            val cursor = db.rawQuery(
                "SELECT $COL_ID, $COL_TEXT, $COL_TIMESTAMP, $COL_PINNED FROM $TABLE " +
                    "WHERE $COL_PINNED = 1 ORDER BY $COL_TIMESTAMP DESC",
                null
            )
            return cursor.use {
                val out = mutableListOf<ClipboardItem>()
                while (it.moveToNext()) {
                    out.add(
                        ClipboardItem(
                            id = it.getLong(0),
                            text = it.getString(1),
                            timestampMs = it.getLong(2),
                            isPinned = it.getInt(3) == 1
                        )
                    )
                }
                out
            }
        } finally {
            db.close()
        }
    }

    /** Toggle pin state to pin or unpin */
    fun pinToggle(id: Long, pinned: Boolean) {
        val db = openDb()
        try {
            ensureTableExists(db)
            val cv = ContentValues().apply { put(COL_PINNED, if (pinned) 1 else 0) }
            db.update(TABLE, cv, "$COL_ID = ?", arrayOf(id.toString()))
        } finally {
            db.close()
        }
    }

    /** Deleting a specific clip. */
    fun deleteClip(id: Long) {
        val db = openDb()
        try {
            ensureTableExists(db)
            db.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
        } finally {
            db.close()
        }
    }

    /** Clear all the things. */
    fun clearAll() {
        val db = openDb()
        try {
            ensureTableExists(db)
            db.execSQL("DELETE FROM $TABLE")
        } finally {
            db.close()
        }
    }

    /** Keep the latest N non-pinned items */
    fun trimToKeep(keep: Int = 50) {
        val db = openDb()
        try {
            ensureTableExists(db)
            db.execSQL(
                """
                DELETE FROM $TABLE
                WHERE $COL_ID IN (
                    SELECT $COL_ID FROM $TABLE
                    WHERE $COL_PINNED = 0
                    ORDER BY $COL_TIMESTAMP DESC
                    LIMIT -1 OFFSET $keep
                );
                """.trimIndent()
            )
        } finally {
            db.close()
        }
    }

    /** Delete unpinned items older than the cutoff time */
    fun deleteOlderThan(cutoffMillis: Long) {
        val db = openDb()
        try {
            ensureTableExists(db)
            db.delete(
                TABLE,
                "$COL_PINNED = 0 AND $COL_TIMESTAMP < ?",
                arrayOf(cutoffMillis.toString())
            )
        } finally {
            db.close()
        }
    }
}
