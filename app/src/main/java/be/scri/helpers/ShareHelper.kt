package be.scri.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity

object ShareHelper {
    fun shareScribe(context: Context) {
        try {
            val sharingIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "https://github.com/scribe-org/Scribe-Android")
                }
            startActivity(context, Intent.createChooser(sharingIntent, "Share via"), null)
        } catch (e: ActivityNotFoundException) {
            Log.e("AboutFragment", "No application found to share content", e)
        } catch (e: IllegalArgumentException) {
            Log.e("AboutFragment", "Invalid argument for sharing", e)
        }
    }

    fun sendEmail(context: Context) {
        try {
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("team@scri.be"))
                    putExtra(Intent.EXTRA_SUBJECT, "Hey Scribe!")
                    type = "message/rfc822"
                }
            startActivity(context, Intent.createChooser(intent, "Choose an Email client:"), null)
        } catch (e: ActivityNotFoundException) {
            Log.e("AboutFragment", "No email client found", e)
        } catch (e: IllegalArgumentException) {
            Log.e("AboutFragment", "Invalid argument for sending email", e)
        }
    }
}
