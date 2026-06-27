package be.scri.ui.compose

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

    // Toolbar actions
    fun onScribeKeyOptionsClicked()
    fun onScribeKeyToolbarClicked()
    fun onTranslateClicked()
    fun onConjugateClicked()
    fun onPluralClicked()
    fun onCloseClicked()
    fun onSuggestionClicked(suggestion: String)
}
