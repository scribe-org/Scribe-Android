// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")

package be.scri.ui.conjugation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import be.scri.repository.KeyboardLanguageCodes
import be.scri.repository.VerbConjugationRepository
import be.scri.repository.VerbConjugationRow
import be.scri.repository.VerbConjugationSession
import be.scri.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConjugationViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = VerbConjugationRepository(application.applicationContext)

    private var session: VerbConjugationSession? = null

    private var loadJob: Job? = null

    private val _rows = MutableLiveData<List<VerbConjugationRow>>(emptyList())
    val rows: LiveData<List<VerbConjugationRow>> = _rows

    private val _tenseOptions =
        MutableLiveData<List<String>>(
            listOf(getApplication<Application>().getString(R.string.app_conjugate_filter_all)),
        )
    val tenseOptions: LiveData<List<String>> = _tenseOptions

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _verbNotFound = MutableLiveData(false)
    val verbNotFound: LiveData<Boolean> = _verbNotFound

    private val _loadError = MutableLiveData(false)
    val loadError: LiveData<Boolean> = _loadError

    private val _selectedTensePosition = MutableLiveData(0)
    val selectedTensePosition: LiveData<Int> = _selectedTensePosition

    fun loadVerb(
        displayLanguage: String,
        lemma: String,
        allTensesLabel: String,
    ) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _isLoading.value = true
                _verbNotFound.value = false
                _loadError.value = false
                var newSession: VerbConjugationSession? = null
                try {
                    val alias = KeyboardLanguageCodes.toDbAlias(displayLanguage)
                    newSession =
                        withContext(Dispatchers.IO) {
                            repository.openSession(alias, lemma.trim())
                        }
                    ensureActive()
                    session?.close()
                    session = null
                    if (newSession == null) {
                        _rows.value = emptyList()
                        _tenseOptions.value = listOf(allTensesLabel)
                        _selectedTensePosition.value = 0
                        _verbNotFound.value = true
                        return@launch
                    }
                    session = newSession
                    val distinct = newSession.distinctTenses()
                    _tenseOptions.value = listOf(allTensesLabel) + distinct
                    _selectedTensePosition.value = 0
                    _rows.value = newSession.queryRows(null)
                } catch (e: CancellationException) {
                    if (newSession != null && session != newSession) {
                        newSession.close()
                    }
                    throw e
                } catch (e: Exception) {
                    Log.e("ConjugationViewModel", "loadVerb failed", e)
                    if (newSession != null && session != newSession) {
                        runCatching { newSession.close() }
                    }
                    _loadError.value = true
                } finally {
                    _isLoading.value = false
                }
            }
    }

    /**
     * Runs on the main thread (spinner callbacks). Keeps all reads on the same session instance as
     * [loadVerb] assigns, avoiding use-after-close races with background loads.
     */
    fun applyTenseFilter(
        spinnerPosition: Int,
        allTensesLabel: String,
    ) {
        if (_isLoading.value == true) return
        val s = session ?: return
        val options = _tenseOptions.value ?: return
        if (spinnerPosition !in options.indices) return
        _selectedTensePosition.value = spinnerPosition
        val selected = options[spinnerPosition]
        val tenseArg =
            if (spinnerPosition == 0 || selected == allTensesLabel) {
                null
            } else {
                selected
            }
        _rows.value = s.queryRows(tenseArg)
    }

    fun resetResultsForLanguageChange(allTensesLabel: String) {
        loadJob?.cancel()
        session?.close()
        session = null
        _rows.value = emptyList()
        _tenseOptions.value = listOf(allTensesLabel)
        _selectedTensePosition.value = 0
        _verbNotFound.value = false
        _loadError.value = false
    }

    fun consumeVerbNotFoundEvent() {
        _verbNotFound.value = false
    }

    fun consumeLoadErrorEvent() {
        _loadError.value = false
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        session?.close()
        session = null
    }
}
