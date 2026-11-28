// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class ConjugateDataDownloadViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        sharedPreferencesEditor = mockk(relaxed = true)

        every { application.getSharedPreferences("scribe_conjugate_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { application.filesDir } returns File(System.getProperty("java.io.tmpdir") ?: "")
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.remove(any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initializeStates_withSavedTimestamp_setsCompleted() =
        runTest(testDispatcher) {
            every { sharedPreferences.getString("last_conjugate_update_de", null) } returns "2026-01-01"

            val viewModel = ConjugateDataDownloadViewModel(application)
            viewModel.initializeStates(listOf("german"))

            assertEquals(DownloadState.Completed, viewModel.downloadStates["german"])
        }

    @Test
    fun initializeStates_withoutSavedTimestamp_setsReady() =
        runTest(testDispatcher) {
            every { sharedPreferences.getString("last_conjugate_update_de", null) } returns null

            val viewModel = ConjugateDataDownloadViewModel(application)
            viewModel.initializeStates(listOf("german"))

            assertEquals(DownloadState.Ready, viewModel.downloadStates["german"])
        }

    @Test
    fun deleteLanguageData_clearsDatabaseAndPreference_resetsStateToReady() =
        runTest(testDispatcher) {
            every { sharedPreferences.getString("last_conjugate_update_de", null) } returns "2026-01-01"

            val viewModel = ConjugateDataDownloadViewModel(application)
            viewModel.initializeStates(listOf("german"))
            assertEquals(DownloadState.Completed, viewModel.downloadStates["german"])

            viewModel.deleteLanguageData("german", testDispatcher)
            advanceUntilIdle()

            verify { application.deleteDatabase("DEConjugateData.sqlite") }
            verify { sharedPreferencesEditor.remove("last_conjugate_update_de") }
            assertEquals(DownloadState.Ready, viewModel.downloadStates["german"])
        }
}
