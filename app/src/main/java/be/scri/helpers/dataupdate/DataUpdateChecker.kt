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
 * Asks Scribe-Server which already-downloaded languages have a newer data pack.
 * Languages that fail to answer are skipped so one error does not hide the rest.
 */
class DataUpdateChecker(
    private val downloadPrefs: SharedPreferences,
    private val source: DataVersionSource,
    private val keyPrefix: String = LAST_UPDATE_PREFIX,
) {
    /**
     * ISO codes whose saved timestamp is older than the server.
     * [keyPrefix] is `last_update_` in Keyboards and `last_conjugate_update_` in Conjugate.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun languagesWithUpdates(): List<String> {
        val updates = mutableListOf<String>()
        for (code in downloadedLanguageCodes()) {
            val localUpdatedAt = downloadPrefs.getString("$keyPrefix$code", null) ?: continue
            try {
                val response = source.getDataVersion(code)
                if (DataVersionComparator.anyVersionNewer(localUpdatedAt, response.versions)) {
                    updates.add(code)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.w(TAG, "Version check failed for $code", e)
            }
        }
        return updates
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
