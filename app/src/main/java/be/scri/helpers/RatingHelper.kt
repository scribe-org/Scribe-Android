package be.scri.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import be.scri.activities.MainActivity
import com.google.android.play.core.review.ReviewManagerFactory

class RatingHelper {

    companion object {
        private fun getInstallSource(context: Context): String? =
            try {
                val packageManager = context.packageManager
                packageManager.getInstallerPackageName(context.packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

        fun rateScribe(context: Context, activity: MainActivity) {
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
}
