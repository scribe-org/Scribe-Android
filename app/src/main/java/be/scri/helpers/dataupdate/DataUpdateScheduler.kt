// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import be.scri.helpers.PreferencesHelper
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Owns the single background check named [WORK_NAME].
 * Turning the switch on replaces any pending run with an immediate check.
 * That check appends the next monthly run when it finishes.
 */
object DataUpdateScheduler {
    const val WORK_NAME = "scribe_data_update_check"
    const val KEY_USER_INITIATED = "user_initiated"

    private const val RETRY_BACKOFF_MINUTES = 15L

    /**
     * Saves [enabled] and either cancels the check or runs one now.
     */
    fun onToggle(
        context: Context,
        enabled: Boolean,
    ) {
        PreferencesHelper.setRegularlyCheckForDataUpdates(context, enabled)
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        DataUpdateCalendar.ensureSlot(context)
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request(Duration.ZERO, userInitiated = true))
    }

    /**
     * If the switch is on and no check is waiting, queue the next monthly slot.
     * Does not start a check just because the app was opened.
     */
    fun ensureScheduled(context: Context) {
        if (!PreferencesHelper.getRegularlyCheckForDataUpdates(context)) return
        DataUpdateCalendar.ensureSlot(context)
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request(DataUpdateCalendar.delayUntilNextSlot(context), userInitiated = false),
        )
    }

    /**
     * Adds the next run after the one that is currently executing.
     * Append is required here: replace would cancel the running check.
     */
    fun enqueueFollowUp(
        context: Context,
        delay: Duration,
        userInitiated: Boolean,
    ) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request(delay, userInitiated),
        )
    }

    private fun request(
        delay: Duration,
        userInitiated: Boolean,
    ): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<DataUpdateCheckWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            ).setInitialDelay(delay)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, RETRY_BACKOFF_MINUTES, TimeUnit.MINUTES)
            .setInputData(workDataOf(KEY_USER_INITIATED to userInitiated))
            .build()
}
