// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

/**
 * Extends the Drawable class to apply a color filter to a drawable using a specified color.
 */
fun Drawable.applyColorFilter(color: Int) = mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
