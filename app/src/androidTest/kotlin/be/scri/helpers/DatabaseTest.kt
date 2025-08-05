// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import DataContract
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.inputmethod.InputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.helpers.data.PluralFormsManager
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var mockIME: GeneralKeyboardIME
    private lateinit var mockInputConnection: InputConnection
    private lateinit var keyHandler: KeyHandler

    @Before
    fun setUp() {
        mockIME = mockk(relaxed = true)
        mockInputConnection = mockk(relaxed = true)

        every { mockIME.currentInputConnection } returns mockInputConnection
        every { mockIME.keyboard } returns mockk(relaxed = true)
        every { mockIME.currentState } returns ScribeState.IDLE

        keyHandler = KeyHandler(mockIME)
    }

    @Test
    fun testPluralCommandGeneratesCorrectOutput() {
        // Arrange
        val testWord = "cat"
        val expectedResult = mapOf("cat" to "cats")

        val mockDatabaseFileManager = mockk<DatabaseFileManager>()
        val mockDatabase = mockk<SQLiteDatabase>()
        val mockDataContract = mockk<DataContract>()
        val mockCursor = mockk<Cursor>(relaxed = true)

        every { mockDatabaseFileManager.getLanguageDatabase("EN") } returns mockDatabase
        every { mockDataContract.numbers } returns mapOf("singular" to "plural")
        every {
            mockDatabase.rawQuery(
                "SELECT `singular`, `plural` FROM nouns WHERE `singular` = ? COLLATE NOCASE",
                arrayOf(testWord),
            )
        } returns mockCursor

        // Mock: cursor behavior
        every { mockDatabase.close() } returns Unit
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getColumnIndex("singular") } returns 0
        every { mockCursor.getString(0) } returns "cat"
        every { mockCursor.getColumnIndex("plural") } returns 1
        every { mockCursor.getString(1) } returns "cats"

        // Act
        val pluralManager = PluralFormsManager(mockDatabaseFileManager)
        val result = pluralManager.getPluralRepresentation("EN", mockDataContract, testWord)

        // Assert
        assertEquals(expectedResult, result)
    }
}
