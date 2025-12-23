// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import java.time.LocalDate

private const val PLACEBO_SERVER_UPDATED_AT = "2025-01-10"
private const val PLACEBO_LOCAL_UPDATED_AT = "2025-01-01"

/** ViewModel to manage data download states and actions. */
class DataDownloadViewModel : ViewModel() {
    val downloadStates = mutableStateMapOf<String, DownloadState>()

    /**
     * @return true if server data is newer than local data.
     */
    private fun isUpdateAvailable(
        localUpdatedAt: String,
        serverUpdatedAt: String,
    ): Boolean {
        val localDate = LocalDate.parse(localUpdatedAt)
        val serverDate = LocalDate.parse(serverUpdatedAt)

        return serverDate.isAfter(localDate)
    }

    /**
     * Handles the download action based on the current state.
     *
     * @param key The key identifying the download item.
     */
    fun handleDownloadAction(key: String) {
        val currentState = downloadStates[key] ?: DownloadState.Ready
        downloadStates[key] =
            when (currentState) {
                DownloadState.Ready -> DownloadState.Downloading
                DownloadState.Downloading -> DownloadState.Completed
                DownloadState.Completed ->
                    if (isUpdateAvailable(PLACEBO_LOCAL_UPDATED_AT, PLACEBO_SERVER_UPDATED_AT)) {
                        DownloadState.Update
                    } else {
                        DownloadState.Completed
                    }
                DownloadState.Update -> DownloadState.Downloading
            }
    }
}

/**
 * Represents the state of the download button.
 */
enum class DownloadState {
    Ready,
    Downloading,
    Completed,
    Update,
}
