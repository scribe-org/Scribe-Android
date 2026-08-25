// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

import be.scri.helpers.KeyboardBase
import be.scri.helpers.clipboard.ClipboardItem
import be.scri.models.ScribeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KeyboardViewModel {
    private val _currentState = MutableStateFlow(ScribeState.IDLE)
    val currentState: StateFlow<ScribeState> = _currentState.asStateFlow()

    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _keyboard = MutableStateFlow<KeyboardBase?>(null)
    val keyboard: StateFlow<KeyboardBase?> = _keyboard.asStateFlow()

    private val _isNumericKeyboardActive = MutableStateFlow(false)
    val isNumericKeyboardActive: StateFlow<Boolean> = _isNumericKeyboardActive.asStateFlow()

    private val _hasLanguageData = MutableStateFlow(true)
    val hasLanguageData: StateFlow<Boolean> = _hasLanguageData.asStateFlow()

    private val _shiftState = MutableStateFlow(0)
    val shiftState: StateFlow<Int> = _shiftState.asStateFlow()

    private val _emojiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val emojiSuggestions: StateFlow<List<String>> = _emojiSuggestions.asStateFlow()

    private val _commandBarText = MutableStateFlow("")
    val commandBarText: StateFlow<String> = _commandBarText.asStateFlow()

    private val _commandBarHint = MutableStateFlow("")
    val commandBarHint: StateFlow<String> = _commandBarHint.asStateFlow()

    private val _commandBarHintColor = MutableStateFlow<Int?>(null)
    val commandBarHintColor: StateFlow<Int?> = _commandBarHintColor.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _hasData = MutableStateFlow(true)
    val hasData: StateFlow<Boolean> = _hasData.asStateFlow()

    private val _conjugateOutput = MutableStateFlow<Map<String, Map<String, Collection<String>>>?>(null)
    val conjugateOutput: StateFlow<Map<String, Map<String, Collection<String>>>?> = _conjugateOutput.asStateFlow()

    private val _selectedConjugationSubCategory = MutableStateFlow<String?>(null)
    val selectedConjugationSubCategory: StateFlow<String?> = _selectedConjugationSubCategory.asStateFlow()

    private val _currentVerbForConjugation = MutableStateFlow<String?>(null)
    val currentVerbForConjugation: StateFlow<String?> = _currentVerbForConjugation.asStateFlow()

    private val _invalidCommandSource = MutableStateFlow(ScribeState.IDLE)
    val invalidCommandSource: StateFlow<ScribeState> = _invalidCommandSource.asStateFlow()

    private val _isInvalidInfoVisible = MutableStateFlow(false)
    val isInvalidInfoVisible: StateFlow<Boolean> = _isInvalidInfoVisible.asStateFlow()

    private val _invalidInfoTexts = MutableStateFlow<List<String>>(emptyList())
    val invalidInfoTexts: StateFlow<List<String>> = _invalidInfoTexts.asStateFlow()

    private val _invalidMsg = MutableStateFlow("")
    val invalidMsg: StateFlow<String> = _invalidMsg.asStateFlow()

    private val _suggestion1 = MutableStateFlow<String?>(null)
    val suggestion1: StateFlow<String?> = _suggestion1.asStateFlow()

    private val _suggestion2 = MutableStateFlow<String?>(null)
    val suggestion2: StateFlow<String?> = _suggestion2.asStateFlow()

    private val _suggestion3 = MutableStateFlow<String?>(null)
    val suggestion3: StateFlow<String?> = _suggestion3.asStateFlow()

    private val _highlightedSuggestion = MutableStateFlow<String?>(null)
    val highlightedSuggestion: StateFlow<String?> = _highlightedSuggestion.asStateFlow()

    private val _isEmojiColonMode = MutableStateFlow(false)
    val isEmojiColonMode: StateFlow<Boolean> = _isEmojiColonMode.asStateFlow()

    private val _isAutocompleteActive = MutableStateFlow(false)
    val isAutocompleteActive: StateFlow<Boolean> = _isAutocompleteActive.asStateFlow()

    private val _genderSuggestionLeft = MutableStateFlow<String?>(null)
    val genderSuggestionLeft: StateFlow<String?> = _genderSuggestionLeft.asStateFlow()

    private val _genderSuggestionRight = MutableStateFlow<String?>(null)
    val genderSuggestionRight: StateFlow<String?> = _genderSuggestionRight.asStateFlow()

    private val _genderColorLeft = MutableStateFlow<Int?>(null)
    val genderColorLeft: StateFlow<Int?> = _genderColorLeft.asStateFlow()

    private val _genderColorRight = MutableStateFlow<Int?>(null)
    val genderColorRight: StateFlow<Int?> = _genderColorRight.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _bottomInsetPx = MutableStateFlow(0)
    val bottomInsetPx: StateFlow<Int> = _bottomInsetPx.asStateFlow()

    private val _isClipboardPanelVisible = MutableStateFlow(false)
    val isClipboardPanelVisible: StateFlow<Boolean> = _isClipboardPanelVisible.asStateFlow()

    private val _isEmojiKeyboardVisible = MutableStateFlow(false)
    val isEmojiKeyboardVisible: StateFlow<Boolean> = _isEmojiKeyboardVisible.asStateFlow()

    fun setEmojiKeyboardVisible(visible: Boolean) {
        _isEmojiKeyboardVisible.value = visible
    }

    private val _isFloatingMode = MutableStateFlow(false)
    val isFloatingMode: StateFlow<Boolean> = _isFloatingMode.asStateFlow()

    private val _floatingOffsetX = MutableStateFlow(0f)
    val floatingOffsetX: StateFlow<Float> = _floatingOffsetX.asStateFlow()

    private val _floatingOffsetY = MutableStateFlow(0f)
    val floatingOffsetY: StateFlow<Float> = _floatingOffsetY.asStateFlow()

    private val _floatingScaleX = MutableStateFlow(1f)
    val floatingScaleX: StateFlow<Float> = _floatingScaleX.asStateFlow()

    private val _floatingScaleY = MutableStateFlow(1f)
    val floatingScaleY: StateFlow<Float> = _floatingScaleY.asStateFlow()

    fun setFloatingMode(active: Boolean) {
        _isFloatingMode.value = active
    }

    fun setFloatingTransform(
        offsetX: Float,
        offsetY: Float,
        scaleX: Float,
        scaleY: Float,
    ) {
        _floatingOffsetX.value = offsetX
        _floatingOffsetY.value = offsetY
        _floatingScaleX.value = scaleX
        _floatingScaleY.value = scaleY
    }

    private val _floatingCardBounds = MutableStateFlow(FloatingCardBounds())
    val floatingCardBounds: StateFlow<FloatingCardBounds> = _floatingCardBounds.asStateFlow()

    fun setFloatingCardBounds(bounds: FloatingCardBounds) {
        if (_floatingCardBounds.value != bounds) {
            _floatingCardBounds.value = bounds
        }
    }

    private val _clipboardItems = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val clipboardItems: StateFlow<List<ClipboardItem>> = _clipboardItems.asStateFlow()

    private val _translateLabel = MutableStateFlow("Translate")
    val translateLabel: StateFlow<String> = _translateLabel.asStateFlow()

    private val _conjugateLabel = MutableStateFlow("Conjugate")
    val conjugateLabel: StateFlow<String> = _conjugateLabel.asStateFlow()

    private val _pluralLabel = MutableStateFlow("Plural")
    val pluralLabel: StateFlow<String> = _pluralLabel.asStateFlow()

    private val _clipboardSuggestion = MutableStateFlow<String?>(null)
    val clipboardSuggestion: StateFlow<String?> = _clipboardSuggestion.asStateFlow()

    fun showClipboardSuggestion(text: String?) {
        _clipboardSuggestion.value = text
    }

    fun updateState(state: ScribeState) {
        _currentState.value = state
    }

    fun updateLanguage(lang: String) {
        _language.value = lang
    }

    fun updateKeyboard(kbd: KeyboardBase?) {
        _keyboard.value = kbd
    }

    fun setNumericKeyboardActive(active: Boolean) {
        _isNumericKeyboardActive.value = active
    }

    fun setShiftState(state: Int) {
        _shiftState.value = state
    }

    fun setHasLanguageData(hasData: Boolean) {
        _hasLanguageData.value = hasData
    }

    fun updateEmojiSuggestions(emojis: List<String>) {
        _emojiSuggestions.value = emojis
    }

    fun setCommandBarText(text: String) {
        _commandBarText.value = text
    }

    fun setCommandBarHint(hint: String) {
        _commandBarHint.value = hint
    }

    fun setCommandBarHintColor(color: Int) {
        _commandBarHintColor.value = color
    }

    fun setPromptText(prompt: String) {
        _promptText.value = prompt
    }

    fun setHasData(hasData: Boolean) {
        _hasData.value = hasData
    }

    fun updateConjugateData(
        output: Map<String, Map<String, Collection<String>>>?,
        subCategory: String?,
        verb: String?,
    ) {
        _conjugateOutput.value = output
        _selectedConjugationSubCategory.value = subCategory
        _currentVerbForConjugation.value = verb
    }

    fun setInvalidCommandSource(source: ScribeState) {
        _invalidCommandSource.value = source
    }

    fun setInvalidInfoVisible(visible: Boolean) {
        _isInvalidInfoVisible.value = visible
    }

    fun setInvalidInfoTexts(texts: List<String>) {
        _invalidInfoTexts.value = texts
    }

    fun setInvalidMsg(msg: String) {
        _invalidMsg.value = msg
    }

    fun setSuggestions(
        s1: String?,
        s2: String?,
        s3: String?,
    ) {
        _suggestion1.value = s1
        _suggestion2.value = s2
        _suggestion3.value = s3
    }

    fun setAutocompleteActive(active: Boolean) {
        _isAutocompleteActive.value = active
    }

    fun setEmojiColonMode(enabled: Boolean) {
        _isEmojiColonMode.value = enabled
    }

    fun setHighlightedSuggestion(suggestion: String?) {
        _highlightedSuggestion.value = suggestion
    }

    fun setGenderSuggestions(
        left: String?,
        right: String?,
        leftColor: Int? = null,
        rightColor: Int? = null,
    ) {
        _genderSuggestionLeft.value = left
        _genderSuggestionRight.value = right
        _genderColorLeft.value = leftColor
        _genderColorRight.value = rightColor
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun setBottomInset(px: Int) {
        _bottomInsetPx.value = px
    }

    fun setClipboardPanelVisible(visible: Boolean) {
        _isClipboardPanelVisible.value = visible
    }

    fun updateClipboardItems(items: List<ClipboardItem>) {
        _clipboardItems.value = items
    }

    fun updateCommandLabels(
        translate: String,
        conjugate: String,
        plural: String,
    ) {
        _translateLabel.value = translate
        _conjugateLabel.value = conjugate
        _pluralLabel.value = plural
    }
}

data class FloatingCardBounds(
    val left: Float = 0f,
    val top: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
)
