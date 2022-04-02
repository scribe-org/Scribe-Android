/**
 * ESInterfaceVariables.kt
 *
 * Constants and functions to load the Spanish Scribe keyboard.
 */

class SpanishKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ"),
        listOf("shift", "z", "x", "c", "v", "b", "n", "m", "delete"),
        listOf("123", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val numberKeysPhone = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\""),
        listOf("#+=", ".", ",", "?", "!", "'", "delete"),
        listOf("ABC", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val symbolKeysPhone = listOf(
        listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
        listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "·"),
        listOf("123", ".", ",", "?", "!", "'", "delete"),
        listOf("ABC", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val letterKeysPad = listOf(
        listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "delete"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ", "return"),
        listOf("shift", "y", "x", "c", "v", "b", "n", "m", ",", ".", "shift"),
        listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard") // "undoArrow"
    )

    internal val numberKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"),
        listOf("@", "#", "$", "&", "*", "(", ")", "'", "\"", "+", "return"),
        listOf("#+=", "%", "_", "-", "=", "/", ";", ":", ",", ".", "#+="),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    internal val symbolKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"),
        listOf("€", "£", "¥", "^", "[", "]", "{", "}", "ᵒ", "ᵃ", "return"),
        listOf("123", "§", "|", "~", "¶", "\\", "<", ">", "¡", "¿", "123"),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "s", "d", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "s", "d", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "n")
    internal val aAlternateKeys = listOf("á", "à", "ä", "â", "ã", "å", "ą", "æ", "ā", "ᵃ")
    internal val eAlternateKeys = listOf("é", "è", "ë", "ê", "ę", "ė", "ē")
    internal val iAlternateKeys = listOf("ī", "į", "î", "ì", "ï", "í")
    internal val oAlternateKeys = listOf("ᵒ", "ō", "œ", "ø", "õ", "ô", "ö", "ó", "ò")
    internal val uAlternateKeys = listOf("ū", "û", "ù", "ü", "ú")
    internal val sAlternateKeys = listOf("š")
    internal val dAlternateKeys = listOf("đ")
    internal val cAlternateKeys = listOf("ç", "ć", "č")
    internal val nAlternateKeys = listOf("ń")
}

/**
 * Gets the keys for the Spanish keyboard.
 */
internal fun getESKeys() {
    if (DeviceType.isPhone) {
        letterKeys = SpanishKeyboardConstants.letterKeysPhone
        numberKeys = SpanishKeyboardConstants.numberKeysPhone
        symbolKeys = SpanishKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "-", "[", "_")
        rightKeyChars = listOf("p", "ñ", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = SpanishKeyboardConstants.letterKeysPad
        numberKeys = SpanishKeyboardConstants.numberKeysPad
        symbolKeys = SpanishKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "@", "€")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = SpanishKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = SpanishKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = SpanishKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = SpanishKeyboardConstants.aAlternateKeys
    eAlternateKeys = SpanishKeyboardConstants.eAlternateKeys
    iAlternateKeys = SpanishKeyboardConstants.iAlternateKeys
    oAlternateKeys = SpanishKeyboardConstants.oAlternateKeys
    uAlternateKeys = SpanishKeyboardConstants.uAlternateKeys
    sAlternateKeys = SpanishKeyboardConstants.sAlternateKeys
    dAlternateKeys = SpanishKeyboardConstants.dAlternateKeys
    cAlternateKeys = SpanishKeyboardConstants.cAlternateKeys
    nAlternateKeys = SpanishKeyboardConstants.nAlternateKeys
}

/**
 * Provides a Spanish keyboard layout.
 */
internal fun setESKeyboardLayout() {
    getESKeys()
    currencySymbol = "$"
    currencySymbolAlternates = dollarAlternateKeys
    spaceBar = "espacio"
    invalidCommandMsg = "No en Wikidata"
    translateKeyLbl = "Traducir"
    translatePrompt = commandPromptSpacing + "es -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Conjugar"
    conjugatePrompt = commandPromptSpacing + "Conjugar: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Plural"
    pluralPrompt = commandPromptSpacing + "Plural: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Ya en plural"
}
