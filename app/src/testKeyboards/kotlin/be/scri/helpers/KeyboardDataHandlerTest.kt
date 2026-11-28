// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import be.scri.helpers.data.AutocompletionDataManager
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class KeyboardDataHandlerTest {
    private val context = mockk<Context>(relaxed = true)
    private val autocompletionManager = mockk<AutocompletionDataManager>(relaxed = true)
    private val dbManagers = mockk<DatabaseManagers>(relaxed = true)
    private lateinit var dataHandler: KeyboardDataHandler

    @Before
    fun setUp() {
        dataHandler = KeyboardDataHandler()
        every { dbManagers.autocompletionManager } returns autocompletionManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun initialState_isNotInitialized() {
        assertFalse(dataHandler.isInitialized)
        assertNull(dataHandler.dataContract)
        assertNull(dataHandler.emojiKeywords)
        assertNull(dataHandler.pluralWords)
    }

    @Test
    fun getPluralRepresentation_whenNullOrEmpty_returnsNull() {
        assertNull(dataHandler.getPluralRepresentation("en", null))
        assertNull(dataHandler.getPluralRepresentation("en", ""))
    }

    @Test
    fun getPluralRepresentation_whenAlreadyPlural_returnsAlreadyPluralMsg() {
        dataHandler.pluralWords = setOf("books")
        assertEquals(ALREADY_PLURAL_MSG, dataHandler.getPluralRepresentation("en", "books"))
    }
}
