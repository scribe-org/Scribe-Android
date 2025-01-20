// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Extends the Drawable class to apply a color filter to a drawable using a specified color.
 */

package be.scri.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

fun Drawable.applyColorFilter(color: Int) = mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
