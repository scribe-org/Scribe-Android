// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import be.scri.R
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Helper object for managing app rating functionality.
 *
 * This object provides methods to determine the installation source of the app
 * and initiate the appropriate rating process, such as launching an in-app review
 * for Play Store installs or opening the F-Droid page for F-Droid installs.
 */
object RatingHelper {
    private const val INSTALLER_PLAY_STORE = "com.android.vending"
    private const val INSTALLER_FDROID = "org.fdroid.fdroid"
    private const val INSTALLER_AMAZON = "com.amazon.venezia"
    private const val INSTALLER_SAMSUNG = "com.sec.android.app.samsungapps"

    private const val URL_PLAY_STORE = "https://play.google.com/store/apps/details?id=%s"
    private const val URL_FDROID = "https://f-droid.org/packages/%s"
    private const val URL_AMAZON = "https://www.amazon.com/gp/mas/dl/android?p=%s"
    private const val URL_SAMSUNG = "https://galaxystore.samsung.com/detail/%s"

    /**
     * Gets the package name of the app that installed this app.
     * For example, "com.android.vending" for Google Play Store.
     *
     * @param context App context.
     *
     * @return Installer package name, or null if unknown or on error. Logs errors.
     */
    private fun getInstallSource(context: Context): String? =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("RatingHelper", "Failed to get install source", e)
            null
        }

    /**
     * Gets the store description text (e.g. "Rate us on Google Play Store").
     *
     * @param context App context.
     * @return String for the description.
     */
    fun getStoreDesc(context: Context): String? =
        when (getInstallSource(context)) {
            INSTALLER_PLAY_STORE -> context.getString(R.string.i18n_app_about_feedback_rate_description_google_play)
            INSTALLER_FDROID -> context.getString(R.string.i18n_app_about_feedback_rate_description_f_droid)
            INSTALLER_AMAZON -> context.getString(R.string.i18n_app_about_feedback_rate_description_amazon)
            INSTALLER_SAMSUNG -> context.getString(R.string.i18n_app_about_feedback_rate_description_galaxy)
            else -> context.getString(R.string.i18n_app_about_feedback_rate_description_google_play)
        }

    /**
     * Opens store page in browser
     *
     * @param context App context.
     * @param urlTemplate URL format string.
     * @param storeName Store name.
     */
    private fun openStore(
        context: Context,
        urlTemplate: String,
        storeName: String,
    ) {
        val url = urlTemplate.format(context.packageName)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No browser found to open $storeName page", Toast.LENGTH_SHORT).show()
            Log.e("RatingHelper", "Unable to open $storeName link", e)
        }
    }

    /**
     * Initiates the app rating process based on the installation source.
     *
     * If the app was installed from the Google Play Store, it attempts to launch the in-app review flow.
     * If the app was installed from F-Droid, it opens the app's F-Droid page in a browser.
     * For any other installation source, it displays a toast message indicating an unknown source.
     *
     * @param context The application context.
     * @param activity The current activity, required for launching the in-app review flow.
     */
    fun rateScribe(
        context: Context,
        activity: ComponentActivity,
    ) {
        val installer = getInstallSource(context)
        when (installer) {
            INSTALLER_PLAY_STORE -> {
                val reviewManager = ReviewManagerFactory.create(context)
                val request = reviewManager.requestReviewFlow()

                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        reviewManager
                            .launchReviewFlow(activity, reviewInfo)
                            .addOnCompleteListener { }
                    } else {
                        openStore(context, URL_PLAY_STORE, "Play Store")
                    }
                }
            }

            INSTALLER_FDROID -> openStore(context, URL_FDROID, "F-Droid")

            INSTALLER_AMAZON -> openStore(context, URL_AMAZON, "Amazon")

            INSTALLER_SAMSUNG -> openStore(context, URL_SAMSUNG, "Galaxy Store")

            else -> openStore(context, URL_PLAY_STORE, "Play Store")
        }
    }
}
