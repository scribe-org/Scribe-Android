// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TranslationDataManagerTest {
    private lateinit var context: Context
    private lateinit var fileManager: DatabaseFileManager
    private lateinit var db: SQLiteDatabase
    private lateinit var manager: TranslationDataManager

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        fileManager = mockk(relaxed = true)
        db = mockk(relaxed = true)

        manager = TranslationDataManager(context, fileManager)
    }

    private fun createCursorWithResult(result: String): Cursor =
        mockk<Cursor>(relaxed = true) {
            every { moveToFirst() } returns true
            every { getColumnIndexOrThrow(any()) } returns 0
            every { getString(0) } returns result
        }

    private fun createEmptyCursor(): Cursor =
        mockk<Cursor>(relaxed = true) {
            every { moveToFirst() } returns false
        }

    @Test
    fun `finds translation using lowercase variant when exact capitalized match fails`() {
        every { fileManager.getTranslationDatabase() } returns db

        // First query (exact "Book") fails, second query (lowercase "book") succeeds.
        every { db.rawQuery(any(), any()) } returnsMany
            listOf(
                createEmptyCursor(),
                createCursorWithResult("livre"),
            )

        val result = manager.getTranslationDataForAWord("en" to "fr", "Book")

        // Should recapitalize the result since input was capitalized.
        assertEquals("Livre", result)
    }

    @Test
    fun `does not recapitalize when input is lowercase`() {
        every { fileManager.getTranslationDatabase() } returns db

        // Exact match succeeds immediately.
        every { db.rawQuery(any(), any()) } returns createCursorWithResult("livre")

        val result = manager.getTranslationDataForAWord("en" to "fr", "book")

        // Input was lowercase, so output stays lowercase.
        assertEquals("livre", result)
    }

    @Test
    fun `returns empty string when no variant matches`() {
        every { fileManager.getTranslationDatabase() } returns db

        // All queries fail.
        every { db.rawQuery(any(), any()) } returns createEmptyCursor()

        val result = manager.getTranslationDataForAWord("en" to "fr", "nonexistent")

        assertEquals("", result)
    }

    @Test
    fun `German capitalized input matches exact`() {
        every { fileManager.getTranslationDatabase() } returns db

        // User types "Buch", database has "Buch" - direct match.
        every { db.rawQuery(any(), any()) } returns createCursorWithResult("book")

        val result = manager.getTranslationDataForAWord("de" to "en", "Buch")

        assertEquals("book", result)
    }

    @Test
    fun `German source tries canonical capitalization as fallback`() {
        every { fileManager.getTranslationDatabase() } returns db

        // Simulate: exact "buch" fails, lowercase "buch" fails, canonical "Buch" succeeds.
        every { db.rawQuery(any(), any()) } returnsMany
            listOf(
                createEmptyCursor(),
                createEmptyCursor(),
                createCursorWithResult("book"),
            )

        val result = manager.getTranslationDataForAWord("de" to "en", "buch")

        assertEquals("book", result)
    }

    @Test
    fun `German capitalized verb finds translation via lowercase fallback`() {
        every { fileManager.getTranslationDatabase() } returns db

        // User types "Laufen" (capitalized verb), but database has "laufen" (lowercase).
        every { db.rawQuery(any(), any()) } returnsMany
            listOf(
                createEmptyCursor(),
                createCursorWithResult("run"),
            )

        val result = manager.getTranslationDataForAWord("de" to "en", "Laufen")

        // German doesn't recapitalize, returns result as-is.
        assertEquals("run", result)
    }

    @Test
    fun `returns original word when source and destination are the same`() {
        // No database call should happen.
        val result = manager.getTranslationDataForAWord("en" to "en", "Book")

        assertEquals("Book", result)
        verify(exactly = 0) { fileManager.getTranslationDatabase() }
    }

    @Test
    fun `returns empty string when database is unavailable`() {
        every { fileManager.getTranslationDatabase() } returns null

        val result = manager.getTranslationDataForAWord("en" to "fr", "book")

        assertEquals("", result)
    }

    @Test
    fun `returns original word when source language is null`() {
        val result = manager.getTranslationDataForAWord(null to "fr", "Book")

        assertEquals("Book", result)
    }

    @Test
    fun `returns original word when destination language is null`() {
        val result = manager.getTranslationDataForAWord("en" to null, "Book")

        assertEquals("Book", result)
    }
}
