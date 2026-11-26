// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import java.util.concurrent.ConcurrentHashMap

// Colors are only split by night mode (values and values-night), so a color id and the
// dark flag fully decide the value. Caching avoids a new configuration context per lookup,
// as suggestions are colored on every keystroke.
private val scribeColorCache = ConcurrentHashMap<Long, Int>()

/**
 * Reads a shared `scribe_*` color for light or dark.
 * [dark] follows the Scribe dark mode setting, not the device night mode,
 * so the keyboard and the Compose theme pick the same value.
 */
@ColorInt
fun Context.scribeColorInt(
    @ColorRes id: Int,
    dark: Boolean,
): Int {
    val key = (id.toLong() shl 1) or (if (dark) 1L else 0L)
    return scribeColorCache.getOrPut(key) { ContextCompat.getColor(scribeNightContext(dark), id) }
}

/**
 * Empties the color cache so tests can resolve again under a different device configuration.
 */
@VisibleForTesting
internal fun clearScribeColorCache() = scribeColorCache.clear()

/**
 * A context whose resources resolve as day or night according to [dark].
 */
internal fun Context.scribeNightContext(dark: Boolean): Context {
    val nightMode = if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val config = Configuration(resources.configuration)
    config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
    return createConfigurationContext(config)
}
