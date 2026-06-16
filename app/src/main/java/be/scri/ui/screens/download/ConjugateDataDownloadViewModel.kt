// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import be.scri.R
import be.scri.data.remote.ConjugateDynamicDbHelper
import be.scri.data.remote.RetrofitClient
import be.scri.helpers.LanguageMappingConstants
import be.scri.helpers.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

/** ViewModel to manage conjugate data download states and actions. */
class ConjugateDataDownloadViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val downloadStates = mutableStateMapOf<String, DownloadState>()
    private val downloadSemaphore = kotlinx.coroutines.sync.Semaphore(1)
    private val downloadJobs = mutableMapOf<String, Job>()
    private val prefs = getApplication<Application>().getSharedPreferences("scribe_conjugate_prefs", Context.MODE_PRIVATE)
    private val _checkUpdateState = MutableStateFlow(CheckUpdateState.Idle)
    val checkUpdateState = _checkUpdateState.asStateFlow()

    private var checkUpdateJob: Job? = null

    /**
     * Initializes the download states for the provided languages.
     *
     * @param languages A list of language keys to initialize states for.
     */
    fun initializeStates(languages: List<String>) {
        languages.forEach { key ->
            if (downloadStates.containsKey(key)) return@forEach

            val langCode =
                LanguageMappingConstants
                    .getLanguageAlias(
                        key.replaceFirstChar { it.uppercase() },
                    ).lowercase()

            // Check if a timestamp exists in SharedPreferences.
            val savedTimestamp = prefs.getString("last_conjugate_update_$langCode", null)

            if (savedTimestamp != null) {
                downloadStates[key] = DownloadState.Completed
            } else {
                downloadStates[key] = DownloadState.Ready
            }
        }

        // After initializing, check for updates on all Completed languages.
        checkAllForUpdates()
        _checkUpdateState.value = CheckUpdateState.Idle
    }

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
        val localDate = LocalDate.parse(localUpdatedAt.take(10))
        val serverDate = LocalDate.parse(serverUpdatedAt.take(10))

        return serverDate.isAfter(localDate)
    }

    /**
     * Handles the download action for conjugate data (verbs only).
     *
     * @param key The key identifying the download item.
     * @param forceDownload If true, cancels any existing download and forces a new one.
     */
    fun handleDownloadAction(
        key: String,
        forceDownload: Boolean = false,
    ) {
        val currentState = downloadStates[key] ?: DownloadState.Ready
        val displayLang = key.replaceFirstChar { it.uppercase() }
        if (forceDownload) {
            downloadJobs[key]?.cancel()
        } else {
            if (currentState == DownloadState.Downloading) {
                return
            }

            if (currentState == DownloadState.Completed) {
                val template = getApplication<Application>().getString(R.string.i18n_app_download_menu_ui_conjugate_data_already_up_to_date)
                val msg = StringUtils.formatStringWithParams(template, displayLang)
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Set to downloading before hitting the network.
        downloadStates[key] = DownloadState.Downloading

        val langCode =
            LanguageMappingConstants
                .getLanguageAlias(
                    key.replaceFirstChar { it.uppercase() },
                ).lowercase()

        val localLastUpdate = prefs.getString("last_conjugate_update_$langCode", "1970-01-01") ?: "1970-01-01"

        // Store the job so we can cancel it later if needed.
        downloadJobs[key] =
            viewModelScope.launch(Dispatchers.IO) {
                downloadSemaphore.acquire()
                try {
                    // Fetch API.
                    val response =
                        withTimeout(300_000) {
                            RetrofitClient.apiService.getData(langCode)
                        }
                    val serverLastUpdate = response.contract.updatedAt

                    // Always download when forcing, or when update is available.
                    if (forceDownload || isUpdateAvailable(localLastUpdate, serverLastUpdate)) {
                        val dbHelper = ConjugateDynamicDbHelper(getApplication(), langCode)
                        dbHelper.syncConjugateDatabase(response)

                        // Save timestamp.
                        prefs.edit().putString("last_conjugate_update_$langCode", serverLastUpdate).apply()

                        withContext(Dispatchers.Main) {
                            downloadStates[key] = DownloadState.Completed
                            val template = getApplication<Application>().getString(R.string.i18n_app_download_menu_ui_conjugate_data_download_success)
                            val msg = StringUtils.formatStringWithParams(template, displayLang)
                            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Already up to date: Skip the DB work.
                        withContext(Dispatchers.Main) {
                            downloadStates[key] = DownloadState.Completed
                            val msg = getApplication<Application>().getString(R.string.i18n_app_download_menu_ui_download_data_generic_already_up_to_date)
                            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    val template = getApplication<Application>().getString(R.string.i18n_app_download_error_network)
                    val errorMsg = StringUtils.formatStringWithParams(template, e.message ?: "")
                    updateErrorState(key, errorMsg)
                } catch (e: SQLiteException) {
                    val template = getApplication<Application>().getString(R.string.i18n_app_download_error_database)
                    val errorMsg = StringUtils.formatStringWithParams(template, e.message ?: "")
                    updateErrorState(key, errorMsg)
                } catch (e: HttpException) {
                    val template = getApplication<Application>().getString(R.string.i18n_app_download_error_server)
                    val errorMsg = StringUtils.formatStringWithParams(template, e.code().toString())
                    updateErrorState(key, errorMsg)
                } catch (e: TimeoutCancellationException) {
                    val errorMsg = getApplication<Application>().getString(R.string.i18n_app_download_error_timeout)
                    updateErrorState(key, errorMsg)
                    throw e
                } finally {
                    // Clean up the job reference when done.
                    downloadSemaphore.release()
                    downloadJobs.remove(key)
                }
            }
    }

    /**
     * Handles the "All languages" download action by initiating downloads for all languages that are not already completed or downloading.
     */
    fun handleDownloadAllLanguages() {
        val toDownload =
            downloadStates.keys.filter { key ->
                downloadStates[key] != DownloadState.Completed && downloadStates[key] != DownloadState.Downloading
            }
        toDownload.forEach { key ->
            handleDownloadAction(key)
        }
    }

    /**
     * Checks for available updates using the data version API.
     * Sets state to Update if server has newer data.
     *
     * @param key The key identifying the download item.
     */
    private suspend fun checkForUpdates(key: String) {
        val currentState = downloadStates[key] ?: DownloadState.Ready
        if (currentState == DownloadState.Downloading) return

        val langCode =
            LanguageMappingConstants
                .getLanguageAlias(key.replaceFirstChar { it.uppercase() })
                .lowercase()

        val localLastUpdate = prefs.getString("last_conjugate_update_$langCode", "1970-01-01") ?: "1970-01-01"

        try {
            val response = RetrofitClient.apiService.getDataVersion(langCode)

            val hasUpdate =
                response.versions.values.any { serverDate ->
                    isUpdateAvailable(localLastUpdate, serverDate)
                }

            withContext(Dispatchers.Main) {
                downloadStates[key] =
                    if (hasUpdate) DownloadState.Update else DownloadState.Completed
            }
        } catch (e: IOException) {
            Log.w("ConjugateDownloadVM", "Network error while checking updates for $key: ${e.message}")
        } catch (e: HttpException) {
            Log.w("ConjugateDownloadVM", "Server error while checking updates for $key: ${e.code()}")
        } catch (e: SQLiteException) {
            Log.w("ConjugateDownloadVM", "Database error while checking updates for $key: ${e.message}")
        }
    }

    /**
     * Checks all languages for updates.
     */
    private fun checkAllForUpdates() {
        downloadStates.keys.forEach { key ->
            // Only check languages that have been downloaded before.
            if (downloadStates[key] == DownloadState.Completed) {
                viewModelScope.launch { checkForUpdates(key) }
            }
        }
    }

    /**
     * Checks for new data updates for all completed languages.
     */
    fun checkForNewData() {
        checkUpdateJob?.cancel()

        val keysToCheck = downloadStates.keys.filter { downloadStates[it] == DownloadState.Completed }

        if (keysToCheck.isEmpty()) {
            _checkUpdateState.value = CheckUpdateState.Idle
            return
        }

        _checkUpdateState.value = CheckUpdateState.Checking

        checkUpdateJob =
            viewModelScope.launch {
                coroutineScope {
                    keysToCheck.forEach { key -> launch { checkForUpdates(key) } }
                }
                _checkUpdateState.value = CheckUpdateState.Done
            }
    }

    /**
     * Cancels the ongoing check for available updates.
     */
    fun cancelCheckForNewData() {
        checkUpdateJob?.cancel()
        checkUpdateJob = null
        _checkUpdateState.value = CheckUpdateState.Idle
    }

    /**
     * Updates the error state for a given key and shows a toast message.
     *
     * @param key The key identifying the download item.
     * @param message The error message to display in the toast.
     */
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

    /**
     * Cancels all ongoing downloads.
     */
    override fun onCleared() {
        super.onCleared()
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
    }
}
