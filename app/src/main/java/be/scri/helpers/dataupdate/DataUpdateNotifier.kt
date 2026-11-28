// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.AppFlavor
import be.scri.helpers.FlavorProvider

/**
 * Posts one local notification naming the languages that have newer data.
 * The wording uses strings that already exist in Scribe-i18n.
 */
object DataUpdateNotifier {
    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val ROUTE_KEYBOARDS = "download_data"
    const val ROUTE_CONJUGATE = "conjugate_download_data"

    private const val CHANNEL_ID = "scribe_data_updates"
    private const val NOTIFICATION_ID = 1001
    private const val TAG = "DataUpdateNotifier"

    /**
     * Shows a notification for [languageCodes]. Does nothing when the list is empty.
     * A missing notification permission is ignored; the download screen still checks on open.
     */
    fun show(
        context: Context,
        languageCodes: List<String>,
    ) {
        if (languageCodes.isEmpty()) return
        val manager = NotificationManagerCompat.from(context)
        val channelName = context.getString(R.string.i18n_app_download_menu_ui_update_data)
        val channel =
            android.app.NotificationChannel(
                CHANNEL_ID,
                channelName,
                android.app.NotificationManager.IMPORTANCE_DEFAULT,
            )
        manager.createNotificationChannel(channel)

        val title = context.getString(R.string.i18n_app_download_menu_ui_update_data_check_new)
        val updateLabel = context.getString(R.string.i18n_app_download_menu_ui_download_data_update)
        val names =
            languageCodes
                .map { languageName(context, it) }
                .sorted()
                .joinToString(", ")
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_data_update)
                .setContentTitle(title)
                .setContentText("$updateLabel: $names")
                .setStyle(NotificationCompat.BigTextStyle().bigText("$updateLabel: $names"))
                .setAutoCancel(true)
                .setContentIntent(contentIntent(context))
                .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission is missing", e)
        }
    }

    private fun contentIntent(context: Context): PendingIntent {
        val route =
            if (FlavorProvider.get() == AppFlavor.CONJUGATE) {
                ROUTE_CONJUGATE
            } else {
                ROUTE_KEYBOARDS
            }
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_NAVIGATE_TO, route)
            }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun languageName(
        context: Context,
        isoCode: String,
    ): String {
        val resId =
            when (isoCode.lowercase()) {
                "en" -> R.string.i18n_app__global_english
                "fr" -> R.string.i18n_app__global_french
                "de" -> R.string.i18n_app__global_german
                "it" -> R.string.i18n_app__global_italian
                "pt" -> R.string.i18n_app__global_portuguese
                "ru" -> R.string.i18n_app__global_russian
                "es" -> R.string.i18n_app__global_spanish
                "sv" -> R.string.i18n_app__global_swedish
                else -> 0
            }
        return if (resId == 0) isoCode.uppercase() else context.getString(resId)
    }
}
