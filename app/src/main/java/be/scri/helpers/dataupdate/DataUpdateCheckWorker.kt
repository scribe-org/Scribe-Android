// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import be.scri.data.remote.RetrofitClient
import be.scri.helpers.AppFlavor
import be.scri.helpers.FlavorProvider
import be.scri.helpers.PreferencesHelper
import kotlinx.coroutines.CancellationException

/**
 * Runs one data-version check. A scheduled run waits out battery saver.
 * A run started from the switch does not, because the user just asked for it.
 */
class DataUpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!PreferencesHelper.getRegularlyCheckForDataUpdates(context)) {
            return Result.success()
        }

        val userInitiated = inputData.getBoolean(DataUpdateScheduler.KEY_USER_INITIATED, false)
        if (!userInitiated && isBatterySaverOn(context)) {
            DataUpdateScheduler.enqueueFollowUp(
                context,
                DataUpdateCalendar.batterySaverDelay(context),
                userInitiated = false,
            )
            return Result.success()
        }

        return try {
            val conjugate = FlavorProvider.get() == AppFlavor.CONJUGATE
            val prefsName = if (conjugate) CONJUGATE_DOWNLOAD_PREFS_NAME else DOWNLOAD_PREFS_NAME
            val keyPrefix = if (conjugate) CONJUGATE_LAST_UPDATE_PREFIX else LAST_UPDATE_PREFIX
            val downloadPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val updates =
                DataUpdateChecker(
                    downloadPrefs,
                    RetrofitClient.apiService::getDataVersion,
                    keyPrefix,
                ).languagesWithUpdates()
            if (updates.isNotEmpty() && PreferencesHelper.getRegularlyCheckForDataUpdates(context)) {
                DataUpdateNotifier.show(context, updates)
            }
            if (PreferencesHelper.getRegularlyCheckForDataUpdates(context)) {
                DataUpdateScheduler.enqueueFollowUp(
                    context,
                    DataUpdateCalendar.delayUntilNextSlot(context),
                    userInitiated = false,
                )
            }
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.w(TAG, "Data update check failed", e)
            if (userInitiated) {
                Result.retry()
            } else {
                DataUpdateScheduler.enqueueFollowUp(
                    context,
                    DataUpdateCalendar.delayUntilNextSlot(context),
                    userInitiated = false,
                )
                Result.success()
            }
        }
    }

    private fun isBatterySaverOn(context: Context): Boolean {
        val powerManager = context.getSystemService(PowerManager::class.java)
        return powerManager?.isPowerSaveMode == true
    }

    private companion object {
        const val TAG = "DataUpdateCheckWorker"
    }
}
