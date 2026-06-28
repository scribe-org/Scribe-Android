// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.clipboard

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClipboardMonitor(
    private val context: Context,
    private val onNewClip: (String) -> Unit,
) {
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val repository = ClipboardRepository(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    // Debounce: track last processed text + time to prevent double-fires on some devices
    private var lastProcessedText: String? = null
    private var lastProcessedTime: Long = 0L
    private val debounceMs = 1000L

    private val listener =
        ClipboardManager.OnPrimaryClipChangedListener {
            processCurrentClip()
        }

    fun startMonitoring() {
        try {
            // Always remove first to prevent duplicate registrations.
            // Android's ClipboardManager does not deduplicate listeners, so calling
            // addPrimaryClipChangedListener twice stacks up two callbacks per copy event.
            clipboardManager.removePrimaryClipChangedListener(listener)
            clipboardManager.addPrimaryClipChangedListener(listener)
        } catch (e: Exception) {
            Log.e("ClipboardMonitor", "Failed to add primary clip changed listener", e)
        }
    }

    fun stopMonitoring() {
        try {
            clipboardManager.removePrimaryClipChangedListener(listener)
        } catch (e: Exception) {
            Log.e("ClipboardMonitor", "Failed to remove primary clip changed listener", e)
        }
    }

    private fun processCurrentClip() {
        val clip = clipboardManager.primaryClip ?: return
        if (clip.itemCount == 0) return

        val description = clipboardManager.primaryClipDescription

        // Skip sensitive content (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (description?.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE) == true) {
                return
            }
        }

        // Support plain text only
        val text = clip.getItemAt(0).text?.toString() ?: return
        if (text.isBlank()) return

        // Debounce: skip if the same text was processed within debounceMs
        val now = System.currentTimeMillis()
        if (text == lastProcessedText && now - lastProcessedTime < debounceMs) return
        lastProcessedText = text
        lastProcessedTime = now

        scope.launch {
            repository.insertItem(text)
            onNewClip(text)
        }
    }
}
