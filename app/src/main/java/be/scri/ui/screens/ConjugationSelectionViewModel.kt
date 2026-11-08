// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import be.scri.helpers.DatabaseFileManager
import be.scri.helpers.data.ConjugateDataManager
import be.scri.helpers.data.ContractDataLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConjugationSelectionUiState {
    data object Loading : ConjugationSelectionUiState

    data object Empty : ConjugationSelectionUiState

    data object NoData : ConjugationSelectionUiState

    data object Error : ConjugationSelectionUiState

    data class Data(
        val conjugationData: Map<String, Map<String, List<Pair<String, String>>>>,
    ) : ConjugationSelectionUiState
}

class ConjugationSelectionViewModel
    @JvmOverloads
    constructor(
        application: Application,
        private val contractDataLoader: ContractDataLoader = ContractDataLoader(application),
        private val conjugateDataManager: ConjugateDataManager = ConjugateDataManager(DatabaseFileManager(application)),
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        private val databaseExists: (String) -> Boolean = { alias ->
            application.getDatabasePath("${alias.uppercase()}ConjugateData.sqlite").exists()
        },
    ) : AndroidViewModel(application) {
        private val _uiState = MutableStateFlow<ConjugationSelectionUiState>(ConjugationSelectionUiState.Loading)

        val uiState: StateFlow<ConjugationSelectionUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null

        fun loadConjugations(
            verb: String,
            languageAlias: String,
        ) {
            loadJob?.cancel()
            _uiState.value = ConjugationSelectionUiState.Loading
            loadJob =
                viewModelScope.launch(ioDispatcher) {
                    if (!databaseExists(languageAlias)) {
                        _uiState.value = ConjugationSelectionUiState.NoData
                        return@launch
                    }
                    val contract = contractDataLoader.loadOrDownloadContract(languageAlias)
                    if (contract == null) {
                        _uiState.value = ConjugationSelectionUiState.Error
                        return@launch
                    }
                    val data = conjugateDataManager.getStructuredConjugations(languageAlias, contract, verb)
                    _uiState.value =
                        if (data == null) {
                            ConjugationSelectionUiState.Empty
                        } else {
                            ConjugationSelectionUiState.Data(data)
                        }
                }
        }
    }
