// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.clipboard

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClipboardRepository(
    context: Context,
) {
    private val clipboardDao = ClipboardDatabase.getDatabase(context).clipboardDao()

    companion object {
        private const val MAX_ITEMS = 25
    }

    private suspend fun pruneExpiredItems() {
        val oneHourAgo = System.currentTimeMillis() - 3600000L
        clipboardDao.deleteExpired(oneHourAgo)
    }

    suspend fun getAllItems(): List<ClipboardItem> =
        withContext(Dispatchers.IO) {
            pruneExpiredItems()
            clipboardDao.getAll()
        }

    suspend fun insertItem(text: String): Long =
        withContext(Dispatchers.IO) {
            pruneExpiredItems()
            if (text.isBlank()) return@withContext -1L

            // Check if item already exists — just bump its timestamp, don't re-insert
            val existing = clipboardDao.getByText(text)
            if (existing != null) {
                clipboardDao.updateTimestamp(existing.id, System.currentTimeMillis())
                return@withContext existing.id
            }

            // Limit size by evicting oldest unpinned if total count exceeds MAX_ITEMS
            val allItems = clipboardDao.getAll()
            if (allItems.size >= MAX_ITEMS) {
                val oldestUnpinned = clipboardDao.getOldestUnpinned()
                if (oldestUnpinned != null) {
                    clipboardDao.deleteById(oldestUnpinned.id)
                }
            }

            val newItem = ClipboardItem(text = text)
            clipboardDao.insert(newItem)
        }

    suspend fun deleteItem(id: Long) =
        withContext(Dispatchers.IO) {
            clipboardDao.deleteById(id)
        }

    suspend fun togglePin(
        id: Long,
        isPinned: Boolean,
    ) = withContext(Dispatchers.IO) {
        clipboardDao.updatePinned(id, !isPinned)
    }

    suspend fun clearAll() =
        withContext(Dispatchers.IO) {
            clipboardDao.deleteAll()
        }
}
