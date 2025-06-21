// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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

    /**
     * Gets the package name of the app that installed this app.
     *
     * For example, "com.android.vending" for Google Play Store.
     *
     * @param context App context.
     * @return Installer package name, or null if unknown or on error. Logs errors.
     */
    private fun getInstallSource(context: Context): String? =
        try {
            context.packageManager.getInstallerPackageName(context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("RatingHelper", "Failed to get install source", e)
            null
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
        when (getInstallSource(context)) {
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
                        Toast.makeText(context, "Failed to launch review flow", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            INSTALLER_FDROID -> {
                val url = "https://f-droid.org/packages/${context.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: PackageManager.NameNotFoundException) {
                    Toast.makeText(context, "No browser found to open F-Droid page", Toast.LENGTH_SHORT).show()
                    Log.e("RatingHelper", "Unable to open F-Droid link", e)
                }
            }

            else -> {
                Toast.makeText(context, "Unknown installation source", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
