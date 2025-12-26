// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log

/** Interface for sharing functionality. */
interface ShareHelperInterface {
    /**
     * Shares scribe.
     *
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

/**
 * A helper to facilitate sharing of the application and contacting the team.
 */
object ShareHelper {
    /**
     * Shares the Scribe GitHub repository link using an external app.
     *
     * Creates an ACTION_SEND intent with the repository URL and starts an activity chooser.
     * Handles [ActivityNotFoundException] and [IllegalArgumentException].
     *
     * @param context The Context to launch the sharing intent from.
     */
    fun shareScribe(context: Context) {
        try {
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "https://github.com/scribe-org/Scribe-Android")
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }

            val chooser = Intent.createChooser(intent, "Share via")
            if (context !is Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Log.e("ShareHelper", "No application found to share content", e)
        } catch (e: IllegalArgumentException) {
            Log.e("ShareHelper", "Invalid argument for sharing", e)
        }
    }

    /**
     * Sends an email to the Scribe team.
     *
     * Launches an email client with a pre-filled email to "team@scri.be"
     * and subject "Hey Scribe!".
     *
     * @param context The context to launch the email intent from.
     * @throws ActivityNotFoundException If no email client is installed.
     * @throws IllegalArgumentException If there's an issue with the email intent arguments.
     */

    fun sendEmail(context: Context) {
        try {
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("team@scri.be"))
                    putExtra(Intent.EXTRA_SUBJECT, "Hey Scribe!")
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }

            val chooser = Intent.createChooser(intent, "Choose an Email client:")
            if (context !is Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Log.e("ShareHelper", "No email client found", e)
        } catch (e: IllegalArgumentException) {
            Log.e("ShareHelper", "Invalid argument for sending email", e)
        }
    }
}
