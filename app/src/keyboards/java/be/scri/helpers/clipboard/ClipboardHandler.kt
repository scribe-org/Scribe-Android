// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.clipboard

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import be.scri.helpers.KeyboardIMEContext
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages in-keyboard clipboard monitoring, suggestion chips, and history panel operations
 * for [GeneralKeyboardIME].
 *
 * @property ime The [KeyboardIMEContext] instance this handler is associated with.
 */
class ClipboardHandler(
    private val ime: KeyboardIMEContext,
) {
    var latestClipText: String? = null
        internal set
    var hasNewClip: Boolean = false
        internal set

    private lateinit var clipboardMonitor: ClipboardMonitor
    private var clipboardAdapter: ClipboardAdapter? = null
    private val clipboardRepository by lazy { ClipboardRepository(ime.imeContext) }

    fun initClipboardMonitor() {
        clipboardMonitor =
            ClipboardMonitor(ime.imeContext) { text ->
                latestClipText = text
                hasNewClip = true
                if (ime.currentState == ScribeState.IDLE && ime.isUiManagerInitialized) {
                    ime.uiManager.showClipboardSuggestionChip(text)
                }
            }
    }

    fun startMonitoring() {
        if (this::clipboardMonitor.isInitialized) {
            clipboardMonitor.startMonitoring()
        }
    }

    fun stopMonitoring() {
        if (this::clipboardMonitor.isInitialized) {
            clipboardMonitor.stopMonitoring()
        }
    }

    fun onClipboardSuggestionClicked() {
        latestClipText?.let { text ->
            ime.getInputConnection()?.commitText(text, 1)
        }
        hideClipboardSuggestionChip()
    }

    fun hideClipboardSuggestionChip() {
        hasNewClip = false
        latestClipText = null
        if (ime.isUiManagerInitialized) {
            ime.uiManager.hideClipboardSuggestionChip()
        }
    }

    fun openClipboardPanel() {
        if (!ime.isUiManagerInitialized) return
        ime.uiManager.showClipboardPanel()

        val recyclerView = ime.binding.clipboardItemsList
        val emptyText = ime.binding.clipboardEmptyText

        clipboardAdapter =
            ClipboardAdapter(
                items = emptyList(),
                onItemClick = { item ->
                    ime.getInputConnection()?.commitText(item.text, 1)
                    closeClipboardPanel()
                },
                onItemDelete = { item ->
                    CoroutineScope(Dispatchers.Main).launch {
                        clipboardRepository.deleteItem(item.id)
                        refreshClipboardPanel()
                    }
                },
                onItemPinToggle = { item ->
                    CoroutineScope(Dispatchers.Main).launch {
                        clipboardRepository.togglePin(item.id, item.isPinned)
                        refreshClipboardPanel()
                    }
                },
            )
        recyclerView.adapter = clipboardAdapter
        recyclerView.layoutManager = GridLayoutManager(ime.imeContext, 2)

        ime.binding.clipboardPanelClose.setOnClickListener { closeClipboardPanel() }
        ime.binding.clipboardClearAll.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                clipboardRepository.clearAll()
                refreshClipboardPanel()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            val items = clipboardRepository.getAllItems()
            clipboardAdapter?.updateItems(items)
            emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    fun closeClipboardPanel() {
        if (!ime.isUiManagerInitialized) return
        ime.uiManager.hideClipboardPanel()
    }

    private suspend fun refreshClipboardPanel() {
        val items = clipboardRepository.getAllItems()
        clipboardAdapter?.updateItems(items)
        ime.binding.clipboardEmptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        ime.binding.clipboardItemsList.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }
}
