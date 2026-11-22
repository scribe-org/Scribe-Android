// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import DataContract
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.test.core.app.ApplicationProvider
import be.scri.helpers.data.ConjugateDataManager
import be.scri.helpers.data.ContractDataLoader
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class ConjugationSelectionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var contractDataLoader: ContractDataLoader
    private lateinit var conjugateDataManager: ConjugateDataManager
    private var databaseExists: (String) -> Boolean = { true }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        contractDataLoader = mockk()
        conjugateDataManager = mockk()
        databaseExists = { true }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        val viewModel =
            ConjugationSelectionViewModel(
                application = application,
                contractDataLoader = contractDataLoader,
                conjugateDataManager = conjugateDataManager,
                ioDispatcher = testDispatcher,
                databaseExists = databaseExists,
            )

        assertEquals(ConjugationSelectionUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun loadConjugations_transitionsToDataState_whenDataExists() {
        val contract = mockk<DataContract>()
        val mockData =
            mapOf(
                "Present" to
                    mapOf(
                        "Simple" to listOf("ich" to "esse"),
                    ),
            )

        coEvery { contractDataLoader.loadOrDownloadContract("DE") } returns contract
        every { conjugateDataManager.getStructuredConjugations("DE", contract, "essen") } returns mockData

        val viewModel =
            ConjugationSelectionViewModel(
                application = application,
                contractDataLoader = contractDataLoader,
                conjugateDataManager = conjugateDataManager,
                ioDispatcher = testDispatcher,
                databaseExists = databaseExists,
            )

        viewModel.loadConjugations("essen", "DE")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ConjugationSelectionUiState.Data)
        assertEquals(mockData, (state as ConjugationSelectionUiState.Data).conjugationData)
    }

    @Test
    fun loadConjugations_transitionsToEmptyState_whenVerbHasNoRows() {
        val contract = mockk<DataContract>()
        coEvery { contractDataLoader.loadOrDownloadContract("DE") } returns contract
        every { conjugateDataManager.getStructuredConjugations("DE", contract, "nonexistent") } returns null

        val viewModel =
            ConjugationSelectionViewModel(
                application = application,
                contractDataLoader = contractDataLoader,
                conjugateDataManager = conjugateDataManager,
                ioDispatcher = testDispatcher,
                databaseExists = databaseExists,
            )

        viewModel.loadConjugations("nonexistent", "DE")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ConjugationSelectionUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun loadConjugations_transitionsToNoDataState_whenDatabaseDoesNotExist() {
        coEvery { contractDataLoader.loadOrDownloadContract("DE") } returns mockk()

        val viewModel =
            ConjugationSelectionViewModel(
                application = application,
                contractDataLoader = contractDataLoader,
                conjugateDataManager = conjugateDataManager,
                ioDispatcher = testDispatcher,
                databaseExists = { false },
            )

        viewModel.loadConjugations("essen", "DE")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ConjugationSelectionUiState.NoData, viewModel.uiState.value)
    }

    @Test
    fun loadConjugations_transitionsToErrorState_whenContractFails() {
        coEvery { contractDataLoader.loadOrDownloadContract("DE") } returns null

        val viewModel =
            ConjugationSelectionViewModel(
                application = application,
                contractDataLoader = contractDataLoader,
                conjugateDataManager = conjugateDataManager,
                ioDispatcher = testDispatcher,
                databaseExists = databaseExists,
            )

        viewModel.loadConjugations("essen", "DE")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ConjugationSelectionUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun loadConjugations_cancelsPreviousJob_whenNewLoadStarts() {
        val contract = mockk<DataContract>()
        coEvery { contractDataLoader.loadOrDownloadContract(any()) } returns contract
        every { conjugateDataManager.getStructuredConjugations("DE", contract, "slowVerb") } answers {
            Thread.sleep(100)
            mapOf("Slow" to emptyMap())
        }
        every { conjugateDataManager.getStructuredConjugations("DE", contract, "fastVerb") } returns
            mapOf("Fast" to emptyMap())

        val viewModel =
            ConjugationSelectionViewModel(
                application = application,
                contractDataLoader = contractDataLoader,
                conjugateDataManager = conjugateDataManager,
                ioDispatcher = testDispatcher,
                databaseExists = databaseExists,
            )

        viewModel.loadConjugations("slowVerb", "DE")
        viewModel.loadConjugations("fastVerb", "DE")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ConjugationSelectionUiState.Data)
        val data = (state as ConjugationSelectionUiState.Data).conjugationData
        assertTrue(data.containsKey("Fast"))
        assertTrue(!data.containsKey("Slow"))
    }

    @Test
    fun defaultFactoryConstructsViewModel() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        val viewModel =
            ViewModelProvider(
                owner,
                ViewModelProvider.AndroidViewModelFactory.getInstance(app),
            )[ConjugationSelectionViewModel::class.java]
        assertNotNull(viewModel)
    }
}
