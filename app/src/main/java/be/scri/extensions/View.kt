// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Utility functions for managing visibility and haptic feedback in Android views.
 */

package be.scri.extensions

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Sets the view's visibility to VISIBLE if [beVisible] is true; otherwise, sets it to GONE.
 *
 * @receiver View to update visibility
 * @param beVisible Boolean flag indicating whether the view should be visible
 */
fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

/**
 * Sets the view's visibility to GONE if [beGone] is true; otherwise, sets it to VISIBLE.
 *
 * @receiver View to update visibility
 * @param beGone Boolean flag indicating whether the view should be gone
 */
fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

/**
 * Sets the view's visibility to VISIBLE.
 *
 * @receiver View to make visible
 */
fun View.beVisible() {
    visibility = View.VISIBLE
}

/**
 * Sets the view's visibility to GONE.
 *
 * @receiver View to make gone
 */
fun View.beGone() {
    visibility = View.GONE
}

/**
 * Performs haptic feedback using the VIRTUAL_KEY feedback type.
 *
 * @receiver View on which to perform haptic feedback
 */
fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
