/**
 * A helper to facilitate rating the application on Google Play or F-Droid.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package be.scri.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import be.scri.activities.MainActivity
import com.google.android.play.core.review.ReviewManagerFactory

object RatingHelper {
    private fun getInstallSource(context: Context): String? =
        try {
            val packageManager = context.packageManager
            packageManager.getInstallerPackageName(context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("RatingHelper", "Failed to get install source", e)
            null
        }

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
