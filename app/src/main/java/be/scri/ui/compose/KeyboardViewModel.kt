package be.scri.ui.compose

import be.scri.helpers.KeyboardBase
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

    // Suggestions
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

    // Conjugation data
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
    
    // Command Bar Suggestions
    private val _suggestion1 = MutableStateFlow<String?>(null)
    val suggestion1: StateFlow<String?> = _suggestion1.asStateFlow()
    
    private val _suggestion2 = MutableStateFlow<String?>(null)
    val suggestion2: StateFlow<String?> = _suggestion2.asStateFlow()
    
    private val _suggestion3 = MutableStateFlow<String?>(null)
    val suggestion3: StateFlow<String?> = _suggestion3.asStateFlow()
    
    // Gender Suggestions
    private val _genderSuggestionLeft = MutableStateFlow<String?>(null)
    val genderSuggestionLeft: StateFlow<String?> = _genderSuggestionLeft.asStateFlow()
    
    private val _genderSuggestionRight = MutableStateFlow<String?>(null)
    val genderSuggestionRight: StateFlow<String?> = _genderSuggestionRight.asStateFlow()
    
    // Currency Symbol
    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _bottomInset = MutableStateFlow(0)
    val bottomInset: StateFlow<Int> = _bottomInset.asStateFlow()

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
        verb: String?
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
    
    fun setSuggestions(s1: String?, s2: String?, s3: String?) {
        _suggestion1.value = s1
        _suggestion2.value = s2
        _suggestion3.value = s3
    }
    
    fun setGenderSuggestions(left: String?, right: String?) {
        _genderSuggestionLeft.value = left
        _genderSuggestionRight.value = right
    }
    
    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun setBottomInset(inset: Int) {
        _bottomInset.value = inset
    }

    // Clipboard
    private val _isClipboardPanelVisible = MutableStateFlow(false)
    val isClipboardPanelVisible: StateFlow<Boolean> = _isClipboardPanelVisible.asStateFlow()

    private val _clipboardItems = MutableStateFlow<List<be.scri.helpers.clipboard.ClipboardItem>>(emptyList())
    val clipboardItems: StateFlow<List<be.scri.helpers.clipboard.ClipboardItem>> = _clipboardItems.asStateFlow()

    fun setClipboardPanelVisible(visible: Boolean) {
        _isClipboardPanelVisible.value = visible
    }

    fun updateClipboardItems(items: List<be.scri.helpers.clipboard.ClipboardItem>) {
        _clipboardItems.value = items
    }
}
