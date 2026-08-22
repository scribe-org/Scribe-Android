// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConjugateViewModelTest {
    private lateinit var application: Application
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @BeforeTest
    fun setUp() {
        application = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        sharedPreferencesEditor = mockk(relaxed = true)

        every { application.getSharedPreferences("scribe_conjugate_search_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.remove(any()) } returns sharedPreferencesEditor
        every { sharedPreferences.getString("recently_conjugated_list", null) } returns null
    }

    @Test
    fun testInitialState() {
        val viewModel = ConjugateViewModel(application)
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
        assertTrue(viewModel.recentlyConjugated.value.isEmpty())
    }

    @Test
    fun testSearchQueryChangedToEmptyClearsResults() {
        val viewModel = ConjugateViewModel(application)
        viewModel.onSearchQueryChanged("test")
        viewModel.onSearchQueryChanged("")
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun testClearSearchQuery() {
        val viewModel = ConjugateViewModel(application)
        viewModel.onSearchQueryChanged("test")
        viewModel.clearSearchQuery()
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun testOnVerbSelectedAddsToRecentlyConjugatedAndClearsSearch() {
        val viewModel = ConjugateViewModel(application)
        val result = ConjugateSearchResult("essere", "IT")
        viewModel.onSearchQueryChanged("ess")
        viewModel.onVerbSelected(result)

        assertEquals("", viewModel.searchQuery.value)
        assertEquals(1, viewModel.recentlyConjugated.value.size)
        assertEquals(result, viewModel.recentlyConjugated.value[0])

        verify {
            sharedPreferencesEditor.putString("recently_conjugated_list", "essere,IT")
            sharedPreferencesEditor.apply()
        }
    }

    @Test
    fun testClearAllRecentlyConjugated() {
        val viewModel = ConjugateViewModel(application)
        val result = ConjugateSearchResult("essere", "IT")
        viewModel.onVerbSelected(result)
        viewModel.clearAllRecentlyConjugated()

        assertTrue(viewModel.recentlyConjugated.value.isEmpty())
        verify {
            sharedPreferencesEditor.remove("recently_conjugated_list")
            sharedPreferencesEditor.apply()
        }
    }

    @Test
    fun testLoadRecentlyConjugated() {
        every { sharedPreferences.getString("recently_conjugated_list", null) } returns "essere,IT;parler,FR"

        val testViewModel = ConjugateViewModel(application)
        val list = testViewModel.recentlyConjugated.value
        assertEquals(2, list.size)
        assertEquals(ConjugateSearchResult("essere", "IT"), list[0])
        assertEquals(ConjugateSearchResult("parler", "FR"), list[1])
    }
}
