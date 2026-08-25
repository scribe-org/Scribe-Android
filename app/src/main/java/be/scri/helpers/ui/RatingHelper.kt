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
            INSTALLER_PLAY_STORE -> "Rate us on Google Play Store"
            INSTALLER_FDROID -> "Rate us on F-Droid"
            INSTALLER_AMAZON -> "Rate us on Amazon Appstore"
            INSTALLER_SAMSUNG -> "Rate us on Galaxy Store"
            else -> "Rate us on Google Play Store"
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
                        val url = "https://play.google.com/store/apps/details?id=${context.packageName}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No browser found to open Play Store page", Toast.LENGTH_SHORT).show()
                            Log.e("RatingHelper", "Unable to open Play Store link", e)
                        }
                    }
                }
            }

            INSTALLER_FDROID -> {
                val url = "https://f-droid.org/packages/${context.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No browser found to open F-Droid page", Toast.LENGTH_SHORT).show()
                    Log.e("RatingHelper", "Unable to open F-Droid link", e)
                }
            }

            INSTALLER_AMAZON -> {
                val url = "https://www.amazon.com/gp/mas/dl/android?p=${context.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No browser found to open Amazon page", Toast.LENGTH_SHORT).show()
                    Log.e("RatingHelper", "Unable to open Amazon link", e)
                }
            }

            INSTALLER_SAMSUNG -> {
                val url = "https://galaxystore.samsung.com/detail/${context.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No browser found to open Galaxy Store page", Toast.LENGTH_SHORT).show()
                    Log.e("RatingHelper", "Unable to open Galaxy Store link", e)
                }
            }

            else -> {
                val url = "https://play.google.com/store/apps/details?id=${context.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No browser found to open Play Store page", Toast.LENGTH_SHORT).show()
                    Log.e("RatingHelper", "Unable to open Play Store link", e)
                }
            }
        }
    }
}
