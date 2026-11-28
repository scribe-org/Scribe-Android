// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.util.Log
import kotlinx.coroutines.CancellationException

/**
 * What the worker should tell WorkManager after one run.
 */
enum class DataUpdateRunOutcome {
    /** Nothing more to do for this run. The next monthly check is queued when one is wanted. */
    DONE,

    /** Every check failed and attempts are left, so WorkManager should run it again with backoff. */
    RETRY,
}

/**
 * Which later run the worker should queue.
 */
enum class DataUpdateFollowUp {
    /** The saved day of the second week, next time it comes round. */
    NEXT_SLOT,

    /** Twelve hours on, or next month's slot once past the 21st, because battery saver was on. */
    AFTER_BATTERY_SAVER,
}

/**
 * The decisions of one data-version check, kept free of Android classes so they can be unit tested.
 * [DataUpdateCheckWorker] supplies the real implementations.
 *
 * - A run that finds the switch off does nothing.
 * - A scheduled run waits out battery saver. A run from the switch does not, because the user just asked for it.
 * - A run that cannot post notifications skips the network checks, since a result could never be shown.
 *   It queues nothing: opening the app queues the next check again through [DataUpdateScheduler.ensureScheduled].
 * - If every language check fails, the run is retried until [MAX_ATTEMPTS] runs have been made,
 *   then it gives up and falls through to the next monthly slot.
 *   Some languages failing is not a failed run: updates that were found are still reported.
 */
class DataUpdateCheckRunner(
    private val isEnabled: () -> Boolean,
    private val isBatterySaverOn: () -> Boolean,
    private val canPostNotifications: () -> Boolean,
    private val check: suspend () -> DataUpdateCheckResult,
    private val notify: (List<String>) -> Unit,
    private val scheduleFollowUp: (DataUpdateFollowUp) -> Unit,
) {
    /**
     * Runs one check. [runAttemptCount] is WorkManager's zero-based count of earlier attempts.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun run(
        userInitiated: Boolean,
        runAttemptCount: Int,
    ): DataUpdateRunOutcome {
        if (!isEnabled()) return DataUpdateRunOutcome.DONE

        if (!userInitiated && isBatterySaverOn()) {
            scheduleFollowUp(DataUpdateFollowUp.AFTER_BATTERY_SAVER)
            return DataUpdateRunOutcome.DONE
        }

        if (!canPostNotifications()) {
            Log.i(TAG, "Notifications are not allowed. Skipping the data check.")
            return DataUpdateRunOutcome.DONE
        }

        val result =
            try {
                check()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.w(TAG, "Data update check failed", e)
                null
            }

        if (result == null || result.allFailed) {
            if (runAttemptCount + 1 < MAX_ATTEMPTS) return DataUpdateRunOutcome.RETRY
            Log.w(TAG, "Giving up after $MAX_ATTEMPTS attempts. Waiting for the next monthly check.")
        } else if (result.languagesWithUpdates.isNotEmpty() && isEnabled()) {
            try {
                notify(result.languagesWithUpdates)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.w(TAG, "Could not post the update notification", e)
            }
        }

        if (isEnabled()) scheduleFollowUp(DataUpdateFollowUp.NEXT_SLOT)
        return DataUpdateRunOutcome.DONE
    }

    companion object {
        /** Total runs, including the first, before a failing check waits for the next monthly slot. */
        const val MAX_ATTEMPTS = 3

        private const val TAG = "DataUpdateCheckRunner"
    }
}
