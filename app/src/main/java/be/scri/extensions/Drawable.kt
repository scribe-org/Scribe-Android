// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

/**
 * Applies a color filter to the drawable using the specified color.
 *
 * @param color The color to apply using the SRC_IN mode.
 *
 * @return The mutated [Drawable] with the color filter applied.
 */
fun Drawable.applyColorFilter(color: Int) = mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
