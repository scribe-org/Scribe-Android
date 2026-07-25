package be.scri.helpers.clipboard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC")
    suspend fun getAll(): List<ClipboardItem>

    @Query("SELECT * FROM clipboard_items WHERE text = :text LIMIT 1")
    suspend fun getByText(text: String): ClipboardItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clipboard_items")
    suspend fun deleteAll()

    @Query("UPDATE clipboard_items SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePinned(
        id: Long,
        pinned: Boolean,
    )

    @Query("SELECT COUNT(*) FROM clipboard_items WHERE isPinned = 0")
    suspend fun getUnpinnedCount(): Int

    @Query("SELECT * FROM clipboard_items WHERE isPinned = 0 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getOldestUnpinned(): ClipboardItem?

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0 AND timestamp < :expiryTime")
    suspend fun deleteExpired(expiryTime: Long)

    @Query("UPDATE clipboard_items SET timestamp = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(
        id: Long,
        timestamp: Long,
    )
}
