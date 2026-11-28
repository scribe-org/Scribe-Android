// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.Context
import androidx.core.content.edit
import be.scri.helpers.PreferencesHelper
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * The day and minute, stored once per install, when a monthly data check should run.
 * The day is always in the second week of the month.
 */
data class DataCheckSlot(
    val dayOfMonth: Int,
    val minuteOfDay: Int,
)

/**
 * Picks and remembers a monthly check slot, and computes how long to wait for it.
 */
object DataUpdateCalendar {
    const val FIRST_DAY = 8
    const val LAST_DAY = 14
    const val GIVE_UP_DAY = 21
    const val POSTPONE_HOURS = 12L

    private const val KEY_DAY = "data_check_day_of_month"
    private const val KEY_MINUTE = "data_check_minute_of_day"
    private const val UNSET_DAY = 0
    private const val UNSET_MINUTE = -1
    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR

    /**
     * Returns the saved slot, or creates one and saves it.
     */
    fun ensureSlot(
        context: Context,
        random: Random = Random.Default,
    ): DataCheckSlot {
        val prefs = context.getSharedPreferences(PreferencesHelper.SCRIBE_PREFS, Context.MODE_PRIVATE)
        val day = prefs.getInt(KEY_DAY, UNSET_DAY)
        val minute = prefs.getInt(KEY_MINUTE, UNSET_MINUTE)
        if (day in FIRST_DAY..LAST_DAY && minute in 0 until MINUTES_PER_DAY) {
            return DataCheckSlot(day, minute)
        }
        val slot = newSlot(random)
        prefs.edit {
            putInt(KEY_DAY, slot.dayOfMonth)
            putInt(KEY_MINUTE, slot.minuteOfDay)
        }
        return slot
    }

    /**
     * A random day from the 8th through the 14th, and a random minute of that day.
     */
    fun newSlot(random: Random): DataCheckSlot =
        DataCheckSlot(
            dayOfMonth = random.nextInt(FIRST_DAY, LAST_DAY + 1),
            minuteOfDay = random.nextInt(MINUTES_PER_DAY),
        )

    /**
     * Delay from [now] until the next time [slot] occurs. Never zero or negative.
     */
    fun delayUntilNextSlot(
        now: LocalDateTime,
        slot: DataCheckSlot,
    ): Duration = Duration.between(now, nextOccurrence(now, slot))

    /**
     * Delay until the next saved slot, creating the slot if this install does not have one yet.
     */
    fun delayUntilNextSlot(
        context: Context,
        now: LocalDateTime = LocalDateTime.now(),
    ): Duration = delayUntilNextSlot(now, ensureSlot(context))

    /**
     * While battery saver is on before the 21st, wait twelve hours. On the 21st or later, wait
     * until next month's slot instead of checking outside the intended window.
     */
    fun batterySaverDelay(
        now: LocalDateTime,
        slot: DataCheckSlot,
    ): Duration =
        if (now.dayOfMonth >= GIVE_UP_DAY) {
            delayUntilNextSlot(now, slot)
        } else {
            Duration.ofHours(POSTPONE_HOURS)
        }

    /**
     * Same rule as [batterySaverDelay], using the slot saved for this install.
     */
    fun batterySaverDelay(
        context: Context,
        now: LocalDateTime = LocalDateTime.now(),
    ): Duration = batterySaverDelay(now, ensureSlot(context))

    /**
     * The next local date-time at which [slot] occurs strictly after [now].
     */
    fun nextOccurrence(
        now: LocalDateTime,
        slot: DataCheckSlot,
    ): LocalDateTime {
        val thisMonth =
            now
                .withDayOfMonth(slot.dayOfMonth)
                .withHour(slot.minuteOfDay / MINUTES_PER_HOUR)
                .withMinute(slot.minuteOfDay % MINUTES_PER_HOUR)
                .withSecond(0)
                .withNano(0)
        return if (thisMonth.isAfter(now)) thisMonth else thisMonth.plusMonths(1)
    }
}
