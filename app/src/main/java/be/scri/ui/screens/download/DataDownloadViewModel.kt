// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteException
import android.widget.Toast
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import be.scri.data.remote.DynamicDbHelper
import be.scri.data.remote.RetrofitClient
import be.scri.helpers.LanguageMappingConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

/** ViewModel to manage data download states and actions. */
class DataDownloadViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val downloadStates = mutableStateMapOf<String, DownloadState>()
    private val prefs = getApplication<Application>().getSharedPreferences("scribe_prefs", Context.MODE_PRIVATE)

    /**
     * Checks if an update is available by comparing local and server update timestamps.
     *
     * @param localUpdatedAt The last update timestamp stored locally.
     * @param serverUpdatedAt The last update timestamp from the server.
     * @return True if an update is available, false otherwise.
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
        // Prevent double clicks.
        val currentState = downloadStates[key] ?: DownloadState.Ready
        if (currentState == DownloadState.Downloading || currentState == DownloadState.Completed) {
            // If already up to date, tell the user and stop.
            if (currentState == DownloadState.Completed) {
                Toast.makeText(getApplication(), "$key data is already up to date", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Set to downloading before hitting the network.
        downloadStates[key] = DownloadState.Downloading

        val langCode =
            LanguageMappingConstants
                .getLanguageAlias(
                    key.replaceFirstChar { it.uppercase() },
                ).lowercase()

        val localLastUpdate = prefs.getString("last_update_$langCode", "1970-01-01") ?: "1970-01-01"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch API.
                val response = RetrofitClient.apiService.getData(langCode)
                val serverLastUpdate = response.contract.updatedAt

                if (isUpdateAvailable(localLastUpdate, serverLastUpdate)) {
                    // Sync to SQLite.
                    val dbHelper = DynamicDbHelper(getApplication(), langCode)
                    dbHelper.syncDatabase(response)

                    // Save timestamp.
                    prefs.edit().putString("last_update_$langCode", serverLastUpdate).apply()

                    withContext(Dispatchers.Main) {
                        downloadStates[key] = DownloadState.Completed
                        Toast.makeText(getApplication(), "Download finished!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Already up to date: Skip the DB work.
                    withContext(Dispatchers.Main) {
                        downloadStates[key] = DownloadState.Completed
                        Toast.makeText(getApplication(), "Already up to date!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                updateErrorState(key, "Network Error: ${e.message}")
            } catch (e: SQLiteException) {
                updateErrorState(key, "Database Error: ${e.message}")
            } catch (e: HttpException) {
                updateErrorState(key, "Server Error: ${e.code()}")
            }
        }
    }

    private suspend fun updateErrorState(
        key: String,
        message: String,
    ) {
        withContext(Dispatchers.Main) {
            // Reset status so user can retry.
            downloadStates[key] = DownloadState.Ready
            Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show()
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
