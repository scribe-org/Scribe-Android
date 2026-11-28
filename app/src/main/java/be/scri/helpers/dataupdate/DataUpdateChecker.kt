// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.SharedPreferences
import android.util.Log
import be.scri.data.model.DataVersionResponse
import kotlinx.coroutines.CancellationException

/**
 * Loads one language's data-version payload. Production code passes the Retrofit service.
 */
fun interface DataVersionSource {
    /**
     * Fetches the version map for [language], an ISO code such as `fr`.
     */
    suspend fun getDataVersion(language: String): DataVersionResponse
}

/**
 * What one pass over the downloaded languages found.
 * [checkedCount] counts the languages that were asked, and [failedCount] the ones that did not answer.
 */
data class DataUpdateCheckResult(
    val languagesWithUpdates: List<String>,
    val checkedCount: Int,
    val failedCount: Int,
) {
    /**
     * True when at least one language was asked and none of them answered.
     * That means the check itself failed, as opposed to there being nothing new.
     */
    val allFailed: Boolean
        get() = checkedCount > 0 && failedCount == checkedCount
}

/**
 * Asks Scribe-Server which already-downloaded languages have a newer data pack.
 * A language that fails to answer is skipped so one error does not hide the rest,
 * but it is counted so the caller can tell "nothing new" apart from "every check failed".
 */
class DataUpdateChecker(
    private val downloadPrefs: SharedPreferences,
    private val source: DataVersionSource,
    private val keyPrefix: String = LAST_UPDATE_PREFIX,
) {
    /**
     * Compares each downloaded language's saved timestamp with the server.
     * [keyPrefix] is `last_update_` in Keyboards and `last_conjugate_update_` in Conjugate.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun check(): DataUpdateCheckResult {
        val updates = mutableListOf<String>()
        var checked = 0
        var failed = 0
        for (code in downloadedLanguageCodes()) {
            val localUpdatedAt = downloadPrefs.getString("$keyPrefix$code", null) ?: continue
            checked++
            try {
                val response = source.getDataVersion(code)
                if (DataVersionComparator.anyVersionNewer(localUpdatedAt, response.versions)) {
                    updates.add(code)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                failed++
                Log.w(TAG, "Version check failed for $code", e)
            }
        }
        return DataUpdateCheckResult(updates, checked, failed)
    }

    private fun downloadedLanguageCodes(): List<String> =
        downloadPrefs.all.keys
            .asSequence()
            .filter { it.startsWith(keyPrefix) }
            .map { it.removePrefix(keyPrefix) }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

    private companion object {
        const val TAG = "DataUpdateChecker"
    }
}

/** SharedPreferences file where keyboard download timestamps are stored. */
const val DOWNLOAD_PREFS_NAME = "scribe_prefs"

/** Prefix for a keyboard language timestamp, followed by a lowercase ISO code. */
const val LAST_UPDATE_PREFIX = "last_update_"

/** SharedPreferences file where conjugate download timestamps are stored. */
const val CONJUGATE_DOWNLOAD_PREFS_NAME = "scribe_conjugate_prefs"

/** Prefix for a conjugate language timestamp, followed by a lowercase ISO code. */
const val CONJUGATE_LAST_UPDATE_PREFIX = "last_conjugate_update_"
