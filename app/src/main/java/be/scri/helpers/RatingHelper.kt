// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import be.scri.activities.MainActivity
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * A helper to facilitate rating the application on Google Play or F-Droid.
 */
object RatingHelper {
    /**
     * Retrieves the source from which the application was installed.
     *
     * @param context The application context.
     * @return The install source, or null if unable to determine the source.
     */
    private fun getInstallSource(context: Context): String? =
        try {
            val packageManager = context.packageManager
            packageManager.getInstallerPackageName(context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("RatingHelper", "Failed to get install source", e)
            null
        }

    /**
     * Launches the appropriate flow for rating the application based on its installation source.
     * If the application is installed from Google Play, the Google Play review flow is launched.
     * If the application is installed from F-Droid, the F-Droid page for the app is opened in the browser.
     *
     * @param context The application context.
     * @param activity The main activity of the application that is used to launch the review flow.
     */
    fun rateScribe(
        context: Context,
        activity: MainActivity,
    ) {
        val installSource = getInstallSource(context)

        if (installSource == "com.android.vending") {
            val reviewManager = ReviewManagerFactory.create(context)
            val request = reviewManager.requestReviewFlow()

            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewManager
                        .launchReviewFlow(activity, reviewInfo)
                        .addOnCompleteListener { _ ->
                        }
                } else {
                    Toast.makeText(context, "Failed to launch review flow", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (installSource == "org.fdroid.fdroid") {
            val url = "https://f-droid.org/packages/${context.packageName}"
            val intent =
                Intent(Intent.ACTION_VIEW)
                    .apply {
                        data = Uri.parse(url)
                    }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Unknown installation source", Toast.LENGTH_SHORT).show()
        }
    }
}
