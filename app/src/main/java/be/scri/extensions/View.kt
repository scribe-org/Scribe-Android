// SPDX-License-Identifier: AGPL-3.0-or-later

/**
 * Utility functions for managing visibility and haptic feedback in Android views.
 */

package be.scri.extensions

import android.view.HapticFeedbackConstants
import android.view.View

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
