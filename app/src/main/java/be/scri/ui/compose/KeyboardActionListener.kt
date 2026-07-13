package be.scri.ui.compose

import be.scri.helpers.clipboard.ClipboardItem

interface KeyboardActionListener {
    fun onPress(primaryCode: Int)
    fun onKey(code: Int)
    fun onActionUp()
    fun moveCursorLeft()
    fun moveCursorRight()
    fun onText(text: String)
    fun hasTextBeforeCursor(): Boolean
    fun commitPeriodAfterSpace()
    fun setDeleteRepeating(isRepeating: Boolean) {}

    fun onScribeKeyOptionsClicked()
    fun onScribeKeyToolbarClicked()
    fun onTranslateClicked()
    fun onConjugateClicked()
    fun onPluralClicked()
    fun onCloseClicked()
    fun onSuggestionClicked(suggestion: String)

    fun onClipboardItemClicked(item: ClipboardItem) {}
    fun onClipboardItemDelete(item: ClipboardItem) {}
    fun onClipboardItemPinToggle(item: ClipboardItem) {}
    fun onClipboardClearAll() {}
    fun onClipboardPanelClose() {}
}
