/**
 * PTInterfaceVariables.kt
 *
 * Constants and functions to load the Portuguese Scribe keyboard.
 */

enum class PortugueseKeyboardConstants {

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
        listOf("shift", "z", "x", "c", "v", "b", "n", "m", "!", "?", "shift"),
        listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard") // "undoArrow"
    )

    internal val numberKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"),
        listOf("@", "#", "$", "&", "*", "(", ")", "'", "\"", "return"),
        listOf("#+=", "%", "-", "+", "=", "/", ";", ":", ",", ".", "#+="),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    internal val symbolKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "delete"),
        listOf("€", "£", "¥", "_", "^", "[", "]", "{", "}", "return"),
        listOf("123", "§", "|", "~", "...", "\\", "<", ">", "!", "?", "123"),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "n")
    internal val aAlternateKeys = listOf("á", "ã", "à", "â", "ä", "å", "æ", "ᵃ")
    internal val eAlternateKeys = listOf("é", "ê", "è", "ę", "ė", "ē", "ë")
    internal val iAlternateKeys = listOf("ī", "į", "ï", "ì", "î", "í")
    internal val oAlternateKeys = listOf("ᵒ", "ō", "ø", "œ", "ö", "ò", "ô", "õ", "ó")
    internal val uAlternateKeys = listOf("ū", "û", "ù", "ü", "ú")
    internal val cAlternateKeys = listOf("ç")
    internal val nAlternateKeys = listOf("ñ")
}

/**
 * Gets the keys for the Portuguese keyboard.
 */
internal fun getPTKeys() {
    if (DeviceType.isPhone) {
        letterKeys = PortugueseKeyboardConstants.letterKeysPhone
        numberKeys = PortugueseKeyboardConstants.numberKeysPhone
        symbolKeys = PortugueseKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "1", "-", "[", "_")
        rightKeyChars = listOf("p", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = PortugueseKeyboardConstants.letterKeysPad
        numberKeys = PortugueseKeyboardConstants.numberKeysPad
        symbolKeys = PortugueseKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "1")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = PortugueseKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = PortugueseKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = PortugueseKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = PortugueseKeyboardConstants.aAlternateKeys
    eAlternateKeys = PortugueseKeyboardConstants.eAlternateKeys
    iAlternateKeys = PortugueseKeyboardConstants.iAlternateKeys
    oAlternateKeys = PortugueseKeyboardConstants.oAlternateKeys
    uAlternateKeys = PortugueseKeyboardConstants.uAlternateKeys
    cAlternateKeys = PortugueseKeyboardConstants.cAlternateKeys
    nAlternateKeys = PortugueseKeyboardConstants.nAlternateKeys
}

/**
 * Provides a Portuguese keyboard layout.
 */
internal fun setPTKeyboardLayout() {
    getPTKeys()
    currencySymbol = "$"
    currencySymbolAlternates = dollarAlternateKeys
    spaceBar = "espaço"
    invalidCommandMsg = "Não está no Wikidata"
    translateKeyLbl = "Traduzir"
    translatePrompt = commandPromptSpacing + "pt -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Conjugar"
    conjugatePrompt = commandPromptSpacing + "Conjugar: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Plural"
    pluralPrompt = commandPromptSpacing + "Plural: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Já plural"
}
