package be.scri.helpers.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.play.core.review.ReviewManagerFactory

object RatingHelper {

    private const val INSTALLER_PLAY_STORE = "com.android.vending"
    private const val INSTALLER_FDROID = "org.fdroid.fdroid"

    private fun getInstallSource(context: Context): String? =
        try {
            context.packageManager.getInstallerPackageName(context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("RatingHelper", "Failed to get install source", e)
            null
        }

    fun rateScribe(context: Context, activity: ComponentActivity) {
        when (getInstallSource(context)) {
            INSTALLER_PLAY_STORE -> {
                val reviewManager = ReviewManagerFactory.create(context)
                val request = reviewManager.requestReviewFlow()

                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        reviewManager.launchReviewFlow(activity, reviewInfo)
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
                } catch (e: Exception) {
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
