//
//  FRInterfaceVariables.kt
//
//  Constants and functions to load the French Scribe keyboard.
//
enum class FrenchKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p"), listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m"), listOf("shift", "w", "x", "c", "v", "b", "n", "'", "delete"), listOf("123", "selectKeyboard", "space", "return"))
    // "undoArrow"

    internal val numberKeysPhone = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"), listOf("-", "/", ":", ";", "(", ")", "€", "&", "@", "\""), listOf("#+=", ".", ",", "?", "!", "'", "delete"), listOf("ABC", "selectKeyboard", "space", "return"))
    // "undoArrow"

    internal val symbolKeysPhone = listOf(listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="), listOf("_", "\\", "|", "~", "<", ">", "$", "£", "¥", "·"), listOf("123", ".", ",", "?", "!", "'", "delete"), listOf("ABC", "selectKeyboard", "space", "return"))
    // "undoArrow"

    internal val letterKeysPad = listOf(listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p", "delete"), listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m", "return"), listOf("shift", "w", "x", "c", "v", "b", "n", "'", ",", ".", "shift"), listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard"))
    // "undoArrow"

    internal val numberKeysPad = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"), listOf("@", "#", "&", "\"", "€", "(", "!", ")", "-", "*", "return"), listOf("#+=", "%", "_", "+", "=", "/", ";", ":", ",", ".", "#+="), listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard"))
    // "undoArrow"

    internal val symbolKeysPad = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"), listOf("~", "ᵒ", "[", "]", "{", "}", "^", "$", "£", "¥", "return"), listOf("123", "§", "<", ">", "|", "\\", "...", "·", "?", "'", "123"), listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard"))
    // "undoArrow"

    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "y", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "y", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "n")
    internal val aAlternateKeys = listOf("à", "â", "æ", "á", "ä", "ã", "å", "ā", "ᵃ")
    internal val eAlternateKeys = listOf("é", "è", "ê", "ë", "ę", "ė", "ē")
    internal val iAlternateKeys = listOf("ī", "į", "í", "ì", "ï", "î")
    internal val oAlternateKeys = listOf("ᵒ", "ō", "ø", "õ", "ó", "ò", "ö", "œ", "ô")
    internal val uAlternateKeys = listOf("ū", "ú", "ü", "ù", "û")
    internal val yAlternateKeys = listOf("ÿ")
    internal val cAlternateKeys = listOf("ç", "ć", "č")
    internal val nAlternateKeys = listOf("ń", "ñ")
}

/// Gets the keys for the French keyboard.
internal fun getFRKeys() {
    if (DeviceType.isPhone) {
        letterKeys = FrenchKeyboardConstants.letterKeysPhone
        numberKeys = FrenchKeyboardConstants.numberKeysPhone
        symbolKeys = FrenchKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("a", "q", "1", "-", "[", "_")
        rightKeyChars = listOf("p", "m", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = FrenchKeyboardConstants.letterKeysPad
        numberKeys = FrenchKeyboardConstants.numberKeysPad
        symbolKeys = FrenchKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "@", "~")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = FrenchKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = FrenchKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = FrenchKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = FrenchKeyboardConstants.aAlternateKeys
    eAlternateKeys = FrenchKeyboardConstants.eAlternateKeys
    iAlternateKeys = FrenchKeyboardConstants.iAlternateKeys
    oAlternateKeys = FrenchKeyboardConstants.oAlternateKeys
    uAlternateKeys = FrenchKeyboardConstants.uAlternateKeys
    yAlternateKeys = FrenchKeyboardConstants.yAlternateKeys
    cAlternateKeys = FrenchKeyboardConstants.cAlternateKeys
    nAlternateKeys = FrenchKeyboardConstants.nAlternateKeys
}

/// Provides a French keyboard layout.
internal fun setFRKeyboardLayout() {
    getFRKeys()
    currencySymbol = "€"
    currencySymbolAlternates = euroAlternateKeys
    spaceBar = "espace"
    invalidCommandMsg = "Pas dans Wikidata"
    translateKeyLbl = "Traduire"
    translatePrompt = commandPromptSpacing + "fr -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Conjuguer"
    conjugatePrompt = commandPromptSpacing + "Conjuguer: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Pluriel"
    pluralPrompt = commandPromptSpacing + "Pluriel: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Déjà pluriel"
}
