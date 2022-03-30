//
//  InterfaceVariables.kt
//
//  Variables associated with the base keyboard interface.
//


// A proxy into which text is typed.
lateinit internal var proxy: UITextDocumentProxy

// MARK: Display Variables
// Variables for the keyboard and its appearance.
internal var keyboard: List<List<String>> = listOf<listOf(String)>()
internal var allKeys: List<String> = listOf<String>()
internal var allNonSpecialKeys: List<String> = listOf<String>()
lateinit internal var keyboardHeight: CGFloat
lateinit internal var keyCornerRadius: CGFloat
lateinit internal var commandKeyCornerRadius: CGFloat
internal var keyWidth = CGFloat(0)
internal var letterKeyWidth = CGFloat(0)
internal var numSymKeyWidth = CGFloat(0)

// Keyboard elements.
internal var spaceBar = String()

// Arrays for the possible keyboard views that are loaded with their characters.
internal var letterKeys: List<List<String>> = listOf<listOf(String)>()
internal var numberKeys: List<List<String>> = listOf<listOf(String)>()
internal var symbolKeys: List<List<String>> = listOf<listOf(String)>()

/// States of the keyboard corresponding to layouts found in KeyboardConstants.swift.
internal enum class KeyboardState {
    letters,
    numbers,
    symbols
}

/// What the keyboard state is in regards to the shift key.
/// - normal: not capitalized
/// - shift: capitalized
/// - caps: caps-lock
internal enum class ShiftButtonState {
    normal,
    shift,
    caps
}

// Baseline state variables.
internal var keyboardState: KeyboardState = .letters
internal var shiftButtonState: ShiftButtonState = .normal
internal var scribeKeyState: Boolean = false

// Variables and functions to determine display parameters.
internal data class DeviceType(
    internal val isPhone = UIDevice.current.userInterfaceIdiom == .phone,
    internal val isPad = UIDevice.current.userInterfaceIdiom == .pad) {}
internal var isLandscapeView: Boolean = false

/// Checks if the device is in landscape mode.
internal fun checkLandscapeMode() {
    if (UIScreen.main.bounds.height < UIScreen.main.bounds.width) {
        isLandscapeView = true
    } else {
        isLandscapeView = false
    }
}

// Keyboard language variables.
internal var controllerLanguage = String()
internal var controllerLanguageAbbr = String()

// Dictionary for accessing language abbreviations.
internal val languagesAbbrDict: Map<String, String> = mapOf("French" to "fr", "German" to "de", "Italian" to "it", "Portuguese" to "pt", "Russian" to "ru", "Spanish" to "es", "Swedish" to "sv")

/// Returns the abbreviation of the language for use in commands.
internal fun getControllerLanguageAbbr() : String {
    val abbreviation = languagesAbbrDict[controllerLanguage] ?: return ""
    return abbreviation
}

// Dictionary for accessing keyboard abbreviations and layouts.
internal val keyboardLayoutDict: Map<String, () -> Unit> = mapOf("French" to setFRKeyboardLayout, "German" to setDEKeyboardLayout, "Italian" to setITKeyboardLayout, "Portuguese" to setPTKeyboardLayout, "Russian" to setRUKeyboardLayout, "Spanish" to setESKeyboardLayout, "Swedish" to setSVKeyboardLayout)

/// Sets the keyboard layout and its alternate keys.
internal fun setKeyboard() {
    setKeyboardLayout()
    setKeyboardAlternateKeys()
}

/// Sets the keyboard layouts given the chosen keyboard and device type.
internal fun setKeyboardLayout() {
    if (switchInput) {
        setENKeyboardLayout()
    } else {
        val setLayoutFxn: () -> Unit = keyboardLayoutDict[controllerLanguage]!!
        setLayoutFxn()
    }
    allPrompts = listOf(translatePromptAndCursor, conjugatePromptAndCursor, pluralPromptAndCursor)
}
// MARK: English Interface Variables
// Note: here only until there is an English keyboard.
enum class EnglishKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"), listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"), listOf("shift", "z", "x", "c", "v", "b", "n", "m", "delete"), listOf("123", "selectKeyboard", "space", "return"))

    // "undoArrow"
    internal val numberKeysPhone = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"), listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\""), listOf("#+=", ".", ",", "?", "!", "'", "delete"), listOf("ABC", "selectKeyboard", "space", "return"))

    // "undoArrow"
    internal val symbolKeysPhone = listOf(listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="), listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "·"), listOf("123", ".", ",", "?", "!", "'", "delete"), listOf("ABC", "selectKeyboard", "space", "return"))

    // "undoArrow"
    internal val letterKeysPad = listOf(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "delete"), listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "return"), listOf("shift", "w", "x", "c", "v", "b", "n", "m", ",", ".", "shift"), listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard"))

    // "undoArrow"
    internal val numberKeysPad = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"), listOf("@", "#", "$", "&", "*", "(", ")", "'", "\"", "return"), listOf("#+=", "%", "_", "+", "=", "/", ";", ":", ",", ".", "#+="), listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard"))

    // "undoArrow"
    internal val symbolKeysPad = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"), listOf("€", "£", "¥", "_", "^", "[", "]", "{", "}", "return"), listOf("123", "§", "|", "~", "...", "\\", "<", ">", "!", "?", "123"), listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard"))

    // "undoArrow"
    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "y", "s", "l", "z", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "y", "s", "z", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "l", "n")
    internal val aAlternateKeys = listOf("à", "á", "â", "ä", "æ", "ã", "å", "ā")
    internal val eAlternateKeys = listOf("è", "é", "ê", "ë", "ē", "ė", "ę")
    internal val iAlternateKeys = listOf("ì", "į", "ī", "í", "ï", "î")
    internal val oAlternateKeys = listOf("õ", "ō", "ø", "œ", "ó", "ò", "ö", "ô")
    internal val uAlternateKeys = listOf("ū", "ú", "ù", "ü", "û")
    internal val sAlternateKeys = listOf("ś", "š")
    internal val lAlternateKeys = listOf("ł")
    internal val zAlternateKeys = listOf("ž", "ź", "ż")
    internal val cAlternateKeys = listOf("ç", "ć", "č")
    internal val nAlternateKeys = listOf("ń", "ñ")
}

/// Gets the keys for the English keyboard.
internal fun getENKeys() {
    if (DeviceType.isPhone) {
        letterKeys = EnglishKeyboardConstants.letterKeysPhone
        numberKeys = EnglishKeyboardConstants.numberKeysPhone
        symbolKeys = EnglishKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "1", "-", "[", "_")
        rightKeyChars = listOf("p", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = EnglishKeyboardConstants.letterKeysPad
        numberKeys = EnglishKeyboardConstants.numberKeysPad
        symbolKeys = EnglishKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "1")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = EnglishKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = EnglishKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = EnglishKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = EnglishKeyboardConstants.aAlternateKeys
    eAlternateKeys = EnglishKeyboardConstants.eAlternateKeys
    iAlternateKeys = EnglishKeyboardConstants.iAlternateKeys
    oAlternateKeys = EnglishKeyboardConstants.oAlternateKeys
    uAlternateKeys = EnglishKeyboardConstants.uAlternateKeys
    sAlternateKeys = EnglishKeyboardConstants.sAlternateKeys
    lAlternateKeys = EnglishKeyboardConstants.lAlternateKeys
    zAlternateKeys = EnglishKeyboardConstants.zAlternateKeys
    cAlternateKeys = EnglishKeyboardConstants.cAlternateKeys
    nAlternateKeys = EnglishKeyboardConstants.nAlternateKeys
}

/// Provides an English keyboard layout.
internal fun setENKeyboardLayout() {
    getENKeys()
    currencySymbol = "$"
    currencySymbolAlternates = dollarAlternateKeys
    spaceBar = "space"
    invalidCommandMsg = "Not in directory"
    translateKeyLbl = "Translate"
    translatePrompt = commandPromptSpacing + "en -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Conjugate"
    conjugatePrompt = commandPromptSpacing + "Conjugate: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Plural"
    pluralPrompt = commandPromptSpacing + "Plural: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Already plural"
}
