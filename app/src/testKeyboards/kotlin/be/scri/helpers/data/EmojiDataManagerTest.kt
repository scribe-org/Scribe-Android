// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiDataManagerTest {
    @Test
    fun `getEmojiKeywords lowercases keys and populates map correctly`() {
        val fileManager = mockk<DatabaseFileManager>()
        val db = mockk<SQLiteDatabase>(relaxed = true)
        val maxCursor = mockk<Cursor>(relaxed = true)
        val dataCursor = mockk<Cursor>(relaxed = true)

        val tableCheckCursor = mockk<Cursor>(relaxed = true)

        every { fileManager.getLanguageDatabase("EN") } returns db
        every {
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='emoji_keywords'", null)
        } returns tableCheckCursor
        every { tableCheckCursor.moveToFirst() } returns true

        every { db.rawQuery("SELECT MAX(LENGTH(word)) FROM emoji_keywords", null) } returns maxCursor
        every { maxCursor.moveToFirst() } returns true
        every { maxCursor.getInt(0) } returns 5

        every { db.rawQuery("SELECT * FROM emoji_keywords", null) } returns dataCursor
        every { dataCursor.getColumnIndex("word") } returns 0
        every { dataCursor.getColumnIndex("emoji_keyword_0") } returns 1
        every { dataCursor.getColumnIndex("emoji_keyword_1") } returns 2
        every { dataCursor.getColumnIndex("emoji_keyword_2") } returns -1

        every { dataCursor.moveToFirst() } returns true

        var rowNum = 0
        every { dataCursor.moveToNext() } answers {
            rowNum++
            rowNum < 2
        }

        every { dataCursor.getString(0) } answers {
            if (rowNum == 0) "Laugh" else "CRY"
        }
        every { dataCursor.getString(1) } answers {
            if (rowNum == 0) "😂" else "😢"
        }
        every { dataCursor.getString(2) } answers {
            if (rowNum == 0) "😄" else ""
        }

        val emojiDataManager = EmojiDataManager(fileManager)
        val keywordsMap = emojiDataManager.getEmojiKeywords("EN")

        assertEquals(5, emojiDataManager.maxKeywordLength)

        assertTrue(keywordsMap.containsKey("laugh"))
        assertEquals(listOf("😂", "😄"), keywordsMap["laugh"])

        assertTrue(keywordsMap.containsKey("cry"))
        assertEquals(listOf("😢"), keywordsMap["cry"])
    }
}
