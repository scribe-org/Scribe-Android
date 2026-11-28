// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

class DataUpdateCalendarTest {
    @Test
    fun newSlotStaysInsideTheSecondWeek() {
        repeat(40) { seed ->
            val slot = DataUpdateCalendar.newSlot(Random(seed))
            assertTrue(slot.dayOfMonth in DataUpdateCalendar.FIRST_DAY..DataUpdateCalendar.LAST_DAY)
            assertTrue(slot.minuteOfDay in 0 until MINUTES_PER_DAY)
        }
    }

    @Test
    fun futureSlotThisMonthIsKept() {
        val now = LocalDateTime.of(2026, 2, 1, 0, 0)
        val next = DataUpdateCalendar.nextOccurrence(now, DataCheckSlot(dayOfMonth = 8, minuteOfDay = 0))
        assertEquals(LocalDateTime.of(2026, 2, 8, 0, 0), next)
    }

    @Test
    fun pastSlotRollsToNextMonth() {
        val now = LocalDateTime.of(2026, 5, 20, 8, 0)
        val next = DataUpdateCalendar.nextOccurrence(now, DataCheckSlot(dayOfMonth = 9, minuteOfDay = 30))
        assertEquals(LocalDateTime.of(2026, 6, 9, 0, 30), next)
    }

    @Test
    fun decemberRollsIntoJanuary() {
        val now = LocalDateTime.of(2026, 12, 14, 23, 59)
        val next = DataUpdateCalendar.nextOccurrence(now, DataCheckSlot(dayOfMonth = 14, minuteOfDay = 23 * 60 + 50))
        assertEquals(LocalDateTime.of(2027, 1, 14, 23, 50), next)
    }

    @Test
    fun februaryUsesTheSameDayNextMonth() {
        val now = LocalDateTime.of(2026, 2, 20, 12, 0)
        val next = DataUpdateCalendar.nextOccurrence(now, DataCheckSlot(dayOfMonth = 10, minuteOfDay = 15 * 60))
        assertEquals(LocalDateTime.of(2026, 3, 10, 15, 0), next)
    }

    @Test
    fun delayUntilTheSlotIsNeverZeroOrNegative() {
        val samples =
            listOf(
                LocalDateTime.of(2026, 5, 9, 0, 30),
                LocalDateTime.of(2026, 5, 20, 8, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
            )
        val slot = DataCheckSlot(dayOfMonth = 9, minuteOfDay = 30)
        samples.forEach { now ->
            val delay = DataUpdateCalendar.delayUntilNextSlot(now, slot)
            assertFalse(delay.isNegative || delay.isZero)
        }
    }

    @Test
    fun batterySaverBeforeThe21stWaitsTwelveHours() {
        val now = LocalDateTime.of(2026, 5, 20, 23, 0)
        val delay = DataUpdateCalendar.batterySaverDelay(now, DataCheckSlot(dayOfMonth = 8, minuteOfDay = 0))
        assertEquals(Duration.ofHours(DataUpdateCalendar.POSTPONE_HOURS), delay)
    }

    @Test
    fun batterySaverOnOrAfterThe21stUsesNextMonthsSlot() {
        val now = LocalDateTime.of(2026, 5, 21, 0, 0)
        val slot = DataCheckSlot(dayOfMonth = 10, minuteOfDay = 0)
        val delay = DataUpdateCalendar.batterySaverDelay(now, slot)
        val expected = Duration.between(now, LocalDateTime.of(2026, 6, 10, 0, 0))
        assertEquals(expected, delay)
    }

    private companion object {
        const val MINUTES_PER_DAY = 24 * 60
    }
}
