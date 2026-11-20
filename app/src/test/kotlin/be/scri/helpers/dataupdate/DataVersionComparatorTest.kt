// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataVersionComparatorTest {
    @Test
    fun newerDayIsAnUpdate() {
        assertTrue(DataVersionComparator.isUpdateAvailable("2024-01-01", "2024-01-02"))
    }

    @Test
    fun sameDayIsNotAnUpdateEvenWhenTheClockTimeDiffers() {
        assertFalse(DataVersionComparator.isUpdateAvailable("2024-01-01T01:00:00Z", "2024-01-01T23:59:59Z"))
    }

    @Test
    fun olderDayIsNotAnUpdate() {
        assertFalse(DataVersionComparator.isUpdateAvailable("2024-06-01", "2024-01-01"))
    }

    @Test
    fun nullBlankAndUnparseableServerDatesAreNotUpdates() {
        assertFalse(DataVersionComparator.isUpdateAvailable("2024-01-01", null))
        assertFalse(DataVersionComparator.isUpdateAvailable("2024-01-01", ""))
        assertFalse(DataVersionComparator.isUpdateAvailable("2024-01-01", "not-a-date"))
        assertFalse(DataVersionComparator.isUpdateAvailable("not-a-date", "2024-06-01"))
    }

    @Test
    fun oneNewerEntryInAMixedMapIsAnUpdate() {
        val versions =
            mapOf(
                "nouns" to "2024-01-01",
                "verbs" to null,
                "emoji" to "yesterday",
                "prepositions" to "2024-08-01",
            )
        assertTrue(DataVersionComparator.anyVersionNewer("2024-02-01", versions))
    }

    @Test
    fun mapWithNoNewerEntryIsNotAnUpdate() {
        val versions = mapOf("nouns" to "2024-01-01", "verbs" to null)
        assertFalse(DataVersionComparator.anyVersionNewer("2024-06-01", versions))
    }
}
