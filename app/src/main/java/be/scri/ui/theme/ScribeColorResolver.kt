// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * Reads a shared `scribe_*` color for light or dark.
 * [dark] follows the Scribe dark mode setting, not the device night mode,
 * so the keyboard and the Compose theme pick the same value.
 */
@ColorInt
fun Context.scribeColorInt(
    @ColorRes id: Int,
    dark: Boolean,
): Int = ContextCompat.getColor(scribeNightContext(dark), id)

/**
 * A context whose resources resolve as day or night according to [dark].
 */
internal fun Context.scribeNightContext(dark: Boolean): Context {
    val nightMode = if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val config = Configuration(resources.configuration)
    config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
    return createConfigurationContext(config)
}
