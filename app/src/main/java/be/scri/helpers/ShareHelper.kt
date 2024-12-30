/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/**
 * Class of methods to manage Scribe's behaviors.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
