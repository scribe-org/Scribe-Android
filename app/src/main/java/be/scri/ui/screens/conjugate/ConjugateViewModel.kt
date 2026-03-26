// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.conjugate

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConjugateViewModel : ViewModel() {
    private val _isDataAvailable = MutableStateFlow(false)
    val isDataAvailable: StateFlow<Boolean> = _isDataAvailable.asStateFlow()

    // Assuming we would check actual data availability via a repository or ConjugateDataManager.
    fun checkDataAvailability(hasData: Boolean) {
        _isDataAvailable.value = hasData
    }
}
