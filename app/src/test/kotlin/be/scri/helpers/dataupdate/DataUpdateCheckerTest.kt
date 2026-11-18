// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.SharedPreferences
import be.scri.data.model.DataVersionResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

class DataUpdateCheckerTest {
    @Test
    fun onlyStoredLanguagesAreCheckedAndOneFailureDoesNotHideAnUpdate() =
        runTest {
            val prefs =
                prefsWith(
                    mapOf(
                        "${LAST_UPDATE_PREFIX}fr" to "2024-01-01",
                        "${LAST_UPDATE_PREFIX}de" to "2024-06-01",
                        "${LAST_UPDATE_PREFIX}es" to "2024-01-01",
                        "${LAST_UPDATE_PREFIX}" to "2024-01-01",
                        "unrelated" to "ignore-me",
                    ),
                )
            val requested = mutableListOf<String>()
            val source =
                DataVersionSource { language ->
                    requested.add(language)
                    if (language == "es") throw IOException("offline")
                    val serverDate = if (language == "fr") "2025-01-01" else "2020-01-01"
                    DataVersionResponse(language, mapOf("verbs" to serverDate))
                }

            val updates = DataUpdateChecker(prefs, source).languagesWithUpdates()

            assertEquals(listOf("fr"), updates)
            assertEquals(setOf("fr", "de", "es"), requested.toSet())
        }

    @Test
    fun nothingDownloadedMakesNoRequest() =
        runTest {
            val prefs = prefsWith(emptyMap())
            val source =
                DataVersionSource {
                    error("no language should be requested")
                }

            val updates = DataUpdateChecker(prefs, source).languagesWithUpdates()

            assertTrue(updates.isEmpty())
        }

    @Test
    fun conjugatePrefixDoesNotReadKeyboardTimestamps() =
        runTest {
            val prefs =
                prefsWith(
                    mapOf(
                        "${LAST_UPDATE_PREFIX}fr" to "2020-01-01",
                        "${CONJUGATE_LAST_UPDATE_PREFIX}de" to "2020-01-01",
                    ),
                )
            val requested = mutableListOf<String>()
            val source =
                DataVersionSource { language ->
                    requested.add(language)
                    DataVersionResponse(language, mapOf("verbs" to "2025-01-01"))
                }

            val updates =
                DataUpdateChecker(prefs, source, CONJUGATE_LAST_UPDATE_PREFIX).languagesWithUpdates()

            assertEquals(listOf("de"), updates)
            assertEquals(listOf("de"), requested)
        }

    private fun prefsWith(entries: Map<String, String>): SharedPreferences {
        val prefs = mockk<SharedPreferences>()
        every { prefs.all } returns entries
        entries.forEach { (key, value) ->
            every { prefs.getString(key, null) } returns value
        }
        return prefs
    }
}
