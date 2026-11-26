// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.util.SparseIntArray
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.util.size

// Colors are only split by night mode (values and values-night), so a color id and the
// dark flag fully decide the value. One SparseIntArray per mode keeps lookups free of boxing,
// as suggestions are colored on every keystroke. Every caller sets a view or paint color,
// so lookups happen on the main thread and the caches are not synchronized.
private val lightColorCache = SparseIntArray()
private val darkColorCache = SparseIntArray()

/**
 * Reads a shared `scribe_*` color for light or dark.
 * [dark] follows the Scribe dark mode setting, not the device night mode,
 * so the keyboard and the Compose theme pick the same value.
 */
@MainThread
@ColorInt
fun Context.scribeColorInt(
    @ColorRes id: Int,
    dark: Boolean,
): Int {
    val cache = if (dark) darkColorCache else lightColorCache
    // indexOfKey rather than get(id, default): any int, including 0 (transparent), is a valid color.
    val index = cache.indexOfKey(id)
    if (index >= 0) return cache.valueAt(index)
    val color = ContextCompat.getColor(scribeNightContext(dark), id)
    cache.put(id, color)
    return color
}

/**
 * Empties the color caches so tests can resolve again under a different device configuration.
 */
@VisibleForTesting
@MainThread
internal fun clearScribeColorCache() {
    lightColorCache.clear()
    darkColorCache.clear()
}

/**
 * Number of cached colors in both modes, for tests.
 */
@VisibleForTesting
internal fun scribeColorCacheSize(): Int = lightColorCache.size + darkColorCache.size

/**
 * A context whose resources resolve as day or night according to [dark].
 */
internal fun Context.scribeNightContext(dark: Boolean): Context {
    val nightMode = if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val config = Configuration(resources.configuration)
    config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
    return createConfigurationContext(config)
}
