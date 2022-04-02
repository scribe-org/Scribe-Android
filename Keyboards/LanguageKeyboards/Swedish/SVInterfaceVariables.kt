/**
 * SVInterfaceVariables.kt
 *
 * Constants and functions to load the Swedish Scribe keyboard.
 */

class SwedishKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(
        listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "å"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
        listOf("shift", "y", "x", "c", "v", "b", "n", "m", "delete"),
        listOf("123", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val numberKeysPhone = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("-", "/", ":", ";", "(", ")", "kr", "&", "@", "\""),
        listOf("#+=", ".", ",", "?", "!", "'", "delete"),
        listOf("ABC", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val symbolKeysPhone = listOf(
        listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
        listOf("_", "\\", "|", "~", "<", ">", "€", "$", "£", "·"),
        listOf("123", ".", ",", "?", "!", "'", "delete"),
        listOf("ABC", "selectKeyboard", "space", "return") // "undoArrow"
    )

    internal val letterKeysPad = listOf(
        listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "å", "delete"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä", "return"),
        listOf("shift", "y", "x", "c", "v", "b", "n", "m", ",", ".", "-", "shift"),
        listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard") // "undoArrow"
    )

    internal val numberKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "`", "delete"),
        listOf("@", "#", "kr", "&", "*", "(", ")", "'", "\"", "+", "·", "return"),
        listOf("#+=", "%", "≈", "±", "=", "/", ";", ":", ",", ".", "-", "#+="),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    internal val symbolKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "*", "delete"),
        listOf("€", "$", "£", "^", "[", "]", "{", "}", "―", "ᵒ", "...", "return"),
        listOf("123", "§", "|", "~", "≠", "\\", "<", ">", "!", "?", "_", "123"),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "ä", "ö", "s", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "s", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "ä", "ö", "n")
    internal val aAlternateKeys = listOf("á", "à", "â", "ã", "ā")
    internal val eAlternateKeys = listOf("é", "ë", "è", "ê", "ẽ", "ē", "ę")
    internal val iAlternateKeys = listOf("ī", "î", "í", "ï", "ì", "ĩ")
    internal val oAlternateKeys = listOf("ō", "õ", "ô", "ò", "ó", "œ")
    internal val uAlternateKeys = listOf("û", "ú", "ü", "ù", "ũ", "ū")
    internal val äAlternateKeys = listOf("æ")
    internal val öAlternateKeys = listOf("ø")
    internal val sAlternateKeys = listOf("ß", "ś", "š")
    internal val cAlternateKeys = listOf("ç")
    internal val nAlternateKeys = listOf("ñ")
}

/**
 * Gets the keys for the Swedish keyboard.
 */
internal fun getSVKeys() {
    if (DeviceType.isPhone) {
        letterKeys = SwedishKeyboardConstants.letterKeysPhone
        numberKeys = SwedishKeyboardConstants.numberKeysPhone
        symbolKeys = SwedishKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "-", "[", "_")
        rightKeyChars = listOf("å", "ä", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = SwedishKeyboardConstants.letterKeysPad
        numberKeys = SwedishKeyboardConstants.numberKeysPad
        symbolKeys = SwedishKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "@", "€")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = SwedishKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = SwedishKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = SwedishKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = SwedishKeyboardConstants.aAlternateKeys
    eAlternateKeys = SwedishKeyboardConstants.eAlternateKeys
    iAlternateKeys = SwedishKeyboardConstants.iAlternateKeys
    oAlternateKeys = SwedishKeyboardConstants.oAlternateKeys
    uAlternateKeys = SwedishKeyboardConstants.uAlternateKeys
    äAlternateKeys = SwedishKeyboardConstants.äAlternateKeys
    öAlternateKeys = SwedishKeyboardConstants.öAlternateKeys
    sAlternateKeys = SwedishKeyboardConstants.sAlternateKeys
    cAlternateKeys = SwedishKeyboardConstants.cAlternateKeys
    nAlternateKeys = SwedishKeyboardConstants.nAlternateKeys
}

/**
 * Provides a Swedish keyboard layout.
 */
internal fun setSVKeyboardLayout() {
    getSVKeys()
    currencySymbol = "kr"
    currencySymbolAlternates = kronaAlternateKeys
    spaceBar = "mellanslag"
    invalidCommandMsg = "Inte i Wikidata"
    translateKeyLbl = "Översätt"
    translatePrompt = commandPromptSpacing + "sv -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Konjugera"
    conjugatePrompt = commandPromptSpacing + "Konjugera: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Plural"
    pluralPrompt = commandPromptSpacing + "Plural: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Redan plural"
}
