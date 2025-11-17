// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import be.scri.helpers.DatabaseFileManager
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipboardDataManagerTest {

    private lateinit var clipboardManager: ClipboardDataManager
    private lateinit var mockFileManager: DatabaseFileManager
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockFileManager = mockk(relaxed = true)
        clipboardManager = ClipboardDataManager(mockFileManager, context)

        // Ensure a clean DB state before each test
        clipboardManager.clearAll()
    }

    @Test
    fun insertClip_WithValidText_InsertsSuccessfully() {
        // Arrange
        val testText = "Hello from test"

        // Act
        val id = clipboardManager.insertClip(testText)
        val items = clipboardManager.getLatest(5)

        // Assert
        assertTrue("insert returned id should be > 0", id > 0)
        assertTrue("latest should contain inserted text", items.any { it.text == testText })
    }

    @Test
    fun insertClip_WithDuplicateText_DoesNotInsertDuplicate() {
        // Arrange
        val testText = "duplicate text"

        // Act
        val id1 = clipboardManager.insertClip(testText)
        val id2 = clipboardManager.insertClip(testText) // should be prevented as consecutive duplicate
        val items = clipboardManager.getLatest(10)

        // Assert
        assertTrue("first insert should succeed", id1 > 0)
        assertEquals("second insert (duplicate) should return -1", -1L, id2)
        assertEquals("there should be exactly one item with that text", 1, items.count { it.text == testText })
    }

    @Test
    fun pinToggle_TogglesItemPinStatus() {
        // Arrange
        val id = clipboardManager.insertClip("test item")
        val items = clipboardManager.getLatest(1)
        val itemId = items.first().id

        // Act
        clipboardManager.pinToggle(itemId, true)
        val pinnedItems = clipboardManager.getPinned()

        // Assert
        assertTrue("pinned list should contain the item", pinnedItems.any { it.id == itemId && it.isPinned })
    }

    @Test
    fun deleteOlderThan_RemovesExpiredItems() {
        // Arrange
        clipboardManager.clearAll()
        clipboardManager.insertClip("old item")
        // ensure a measurable timestamp gap
        Thread.sleep(60)
        val cutoffTime = System.currentTimeMillis()
        Thread.sleep(60)
        clipboardManager.insertClip("new item")

        // Act
        clipboardManager.deleteOlderThan(cutoffTime)
        val items = clipboardManager.getLatest(10)

        // Assert
        assertEquals("only one recent item should remain", 1, items.size)
        assertEquals("remaining item should be 'new item'", "new item", items.first().text)
    }
}
