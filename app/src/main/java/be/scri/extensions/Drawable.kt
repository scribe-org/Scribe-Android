// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Extends the Drawable class to apply a color filter to a drawable using a specified color.
 */

package be.scri.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

fun Drawable.applyColorFilter(color: Int) = mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
