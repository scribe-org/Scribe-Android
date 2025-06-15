// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity

/** Interface for sharing functionality. */
interface ShareHelperInterface {
    /**
     * Shares scribe.
     * @param context The Android context used to perform the share action.
     */
    fun shareScribe(context: Context)
}

/** Implementation of ShareHelperInterface to handle sharing a scribe. */
class ShareHelperImpl : ShareHelperInterface {
    /**
     * Shares a text message about Scribe using Android's share intent.
     *
     * @param context The context from which to launch the share intent.
     */
    override fun shareScribe(context: Context) {
        val sendIntent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out Scribe!")
                type = "text/plain"
            }
        val shareIntent = Intent.createChooser(sendIntent, "Share Scribe via…")
        context.startActivity(shareIntent)
    }
}

/** A helper to facilitate sharing of the application and contacting the team. */
object ShareHelper {
    /**
     * Shares the link to the Scribe Android project repository. This method opens an intent chooser
     * to allow the user to select an application to share the link.
     *
     * @param context The application context.
     */
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

    /**
     * Sends an email to the Scribe team. This method opens an intent chooser to allow the user to
     * select an email client to send the email.
     *
     * @param context The application context.
     */
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
