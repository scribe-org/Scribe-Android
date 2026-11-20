// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Compares the saved language-data timestamp with dates from `data-version/{lang}`.
 * A value that cannot be parsed is not treated as an update.
 */
object DataVersionComparator {
    private const val DATE_LENGTH = 10

    /**
     * Returns true when [serverUpdatedAt] is a later calendar day than [localUpdatedAt].
     * Both values are compared on their first ten characters (`yyyy-MM-dd`).
     */
    fun isUpdateAvailable(
        localUpdatedAt: String,
        serverUpdatedAt: String?,
    ): Boolean {
        val localDate = parseDate(localUpdatedAt) ?: return false
        val serverDate = parseDate(serverUpdatedAt) ?: return false
        return serverDate.isAfter(localDate)
    }

    /**
     * Returns true when any entry in [serverVersions] is newer than [localUpdatedAt].
     */
    fun anyVersionNewer(
        localUpdatedAt: String,
        serverVersions: Map<String, String?>,
    ): Boolean = serverVersions.values.any { serverDate -> isUpdateAvailable(localUpdatedAt, serverDate) }

    private fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value.take(DATE_LENGTH))
        } catch (ignored: DateTimeParseException) {
            null
        }
    }
}
