// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.Context
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import be.scri.data.remote.RetrofitClient
import be.scri.helpers.AppFlavor
import be.scri.helpers.FlavorProvider
import be.scri.helpers.PreferencesHelper

/**
 * Runs one data-version check through [DataUpdateCheckRunner], which holds the rules for
 * battery saver, missing notification permission and the retry limit.
 */
class DataUpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val outcome =
            DataUpdateCheckRunner(
                isEnabled = { PreferencesHelper.getRegularlyCheckForDataUpdates(context) },
                isBatterySaverOn = { isBatterySaverOn(context) },
                canPostNotifications = { NotificationManagerCompat.from(context).areNotificationsEnabled() },
                check = { buildChecker(context).check() },
                notify = { languages -> DataUpdateNotifier.show(context, languages) },
                scheduleFollowUp = { followUp ->
                    val delay =
                        when (followUp) {
                            DataUpdateFollowUp.NEXT_SLOT -> DataUpdateCalendar.delayUntilNextSlot(context)
                            DataUpdateFollowUp.AFTER_BATTERY_SAVER -> DataUpdateCalendar.batterySaverDelay(context)
                        }
                    DataUpdateScheduler.enqueueFollowUp(context, delay, userInitiated = false)
                },
            ).run(
                userInitiated = inputData.getBoolean(DataUpdateScheduler.KEY_USER_INITIATED, false),
                runAttemptCount = runAttemptCount,
            )
        return when (outcome) {
            DataUpdateRunOutcome.DONE -> Result.success()
            DataUpdateRunOutcome.RETRY -> Result.retry()
        }
    }

    private fun buildChecker(context: Context): DataUpdateChecker {
        val conjugate = FlavorProvider.get() == AppFlavor.CONJUGATE
        val prefsName = if (conjugate) CONJUGATE_DOWNLOAD_PREFS_NAME else DOWNLOAD_PREFS_NAME
        val keyPrefix = if (conjugate) CONJUGATE_LAST_UPDATE_PREFIX else LAST_UPDATE_PREFIX
        return DataUpdateChecker(
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE),
            RetrofitClient.apiService::getDataVersion,
            keyPrefix,
        )
    }

    private fun isBatterySaverOn(context: Context): Boolean {
        val powerManager = context.getSystemService(PowerManager::class.java)
        return powerManager?.isPowerSaveMode == true
    }
}
