/**
 * ITInterfaceVariables.kt
 *
 * Constants and functions to load the Italian Scribe keyboard.
 */

enum class ItalianKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("shift", "z", "x", "c", "v", "b", "n", "m", "delete"),
        listOf("123", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val numberKeysPhone = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("-", "/", ":", ";", "(", ")", "€", "&", "@", "\""),
        listOf("#+=", ".", ",", "?", "!", "'", "delete"),
        listOf("ABC", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val symbolKeysPhone = listOf(
        listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
        listOf("_", "\\", "|", "~", "<", ">", "$", "£", "¥", "·"),
        listOf("123", ".", ",", "?", "!", "'", "delete"),
        listOf("ABC", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val letterKeysPad = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "delete"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "return"),
        listOf("shift", "z", "x", "c", "v", "b", "n", "m", ",", ".", "shift"),
        listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard") // "undoArrow"
    )

    internal val numberKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"),
        listOf("@", "#", "€", "&", "*", "(", ")", "'", "\"", "return"),
        listOf("#+=", "%", "-", "+", "=", "/", ";", ":", ",", ".", "#+="),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    internal val symbolKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"),
        listOf("$", "£", "¥", "_", "^", "[", "]", "{", "}", "return"),
        listOf("123", "§", "|", "~", "...", "\\", "<", ">", "!", "?", "123"),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "s", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "s", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "n")
    internal val aAlternateKeys = listOf("à", "á", "ä", "â", "æ", "ã", "å", "ā", "ᵃ")
    internal val eAlternateKeys = listOf("é", "è", "ə", "ê", "ë", "ę", "ė", "ē")
    internal val iAlternateKeys = listOf("ī", "į", "ï", "î", "í", "ì")
    internal val oAlternateKeys = listOf("ᵒ", "ō", "ø", "œ", "õ", "ö", "ô", "ó", "ò")
    internal val uAlternateKeys = listOf("ū", "ü", "û", "ú", "ù")
    internal val sAlternateKeys = listOf("ß", "ś", "š")
    internal val cAlternateKeys = listOf("ç", "ć", "č")
    internal val nAlternateKeys = listOf("ñ")
}

/**
 * Gets the keys for the Italian keyboard.
 */
internal fun getITKeys() {
    if (DeviceType.isPhone) {
        letterKeys = ItalianKeyboardConstants.letterKeysPhone
        numberKeys = ItalianKeyboardConstants.numberKeysPhone
        symbolKeys = ItalianKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "1", "-", "[", "_")
        rightKeyChars = listOf("p", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = ItalianKeyboardConstants.letterKeysPad
        numberKeys = ItalianKeyboardConstants.numberKeysPad
        symbolKeys = ItalianKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "1")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = ItalianKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = ItalianKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = ItalianKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = ItalianKeyboardConstants.aAlternateKeys
    eAlternateKeys = ItalianKeyboardConstants.eAlternateKeys
    iAlternateKeys = ItalianKeyboardConstants.iAlternateKeys
    oAlternateKeys = ItalianKeyboardConstants.oAlternateKeys
    uAlternateKeys = ItalianKeyboardConstants.uAlternateKeys
    sAlternateKeys = ItalianKeyboardConstants.sAlternateKeys
    cAlternateKeys = ItalianKeyboardConstants.cAlternateKeys
    nAlternateKeys = ItalianKeyboardConstants.nAlternateKeys
}

/**
 * Provides a Italian keyboard layout.
 */
internal fun setITKeyboardLayout() {
    getITKeys()
    currencySymbol = "€"
    currencySymbolAlternates = euroAlternateKeys
    spaceBar = "spazio"
    invalidCommandMsg = "Non in Wikidata"
    translateKeyLbl = "Tradurre"
    translatePrompt = commandPromptSpacing + "it -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Coniugare"
    conjugatePrompt = commandPromptSpacing + "Coniugare: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Plurale"
    pluralPrompt = commandPromptSpacing + "Plurale: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Già plurale"
}
