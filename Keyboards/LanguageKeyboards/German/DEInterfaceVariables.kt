/**
 * DEInterfaceVariables.kt
 *
 * Constants and functions to load the German Scribe keyboard.
 */

enum class GermanKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(
        listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ü"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
        listOf("shift", "y", "x", "c", "v", "b", "n", "m", "delete"),
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
        listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ü", "delete"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä", "return"),
        listOf("shift", "y", "x", "c", "v", "b", "n", "m", ",", ".", "ß", "shift"),
        listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard") // "undoArrow"
    )

    internal val numberKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "+", "delete"),
        listOf("\"", "§", "€", "%", "&", "/", "(", ")", "=", "'", "#", "return"),
        listOf("#+=", "—", "`", "'", "...", "@", ";", ":", ",", ".", "-", "#+="),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    internal val symbolKeysPad = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "*", "delete"),
        listOf("$", "£", "¥", "¿", "―", "\\", "[", "]", "{", "}", "|", "return"),
        listOf("123", "¡", "<", ">", "≠", "·", "^", "~", "!", "?", "_", "123"),
        listOf("ABC", "selectKeyboard", "space", "ABC", "hideKeyboard") // "undoArrow"
    )

    // Alternate key vars.
    internal val keysWithAlternates = listOf("a", "e", "i", "o", "u", "y", "s", "l", "z", "c", "n")
    internal val keysWithAlternatesLeft = listOf("a", "e", "y", "s", "z", "c")
    internal val keysWithAlternatesRight = listOf("i", "o", "u", "l", "n")
    internal val aAlternateKeys = listOf("à", "á", "â", "æ", "ã", "å", "ā", "ą")
    internal val eAlternateKeys = listOf("é", "è", "ê", "ë", "ė", "ę")
    internal val iAlternateKeys = listOf("ì", "ī", "í", "î", "ï")
    internal val oAlternateKeys = listOf("ō", "ø", "œ", "õ", "ó", "ò", "ô")
    internal val uAlternateKeys = listOf("ū", "ú", "ù", "û")
    internal val yAlternateKeys = listOf("ÿ")
    internal val sAlternateKeys = listOf("ß", "ś", "š")
    internal val lAlternateKeys = listOf("ł")
    internal val zAlternateKeys = listOf("ź", "ż")
    internal val cAlternateKeys = listOf("ç", "ć", "č")
    internal val nAlternateKeys = listOf("ń", "ñ")
}

/**
 * Gets the keys for the German keyboard.
 */
internal fun getDEKeys() {
    if (DeviceType.isPhone) {
        letterKeys = GermanKeyboardConstants.letterKeysPhone
        numberKeys = GermanKeyboardConstants.numberKeysPhone
        symbolKeys = GermanKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "-", "[", "_")
        rightKeyChars = listOf("ü", "ä", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = GermanKeyboardConstants.letterKeysPad
        numberKeys = GermanKeyboardConstants.numberKeysPad
        symbolKeys = GermanKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("q", "a", "1", "\"", "$")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = GermanKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = GermanKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = GermanKeyboardConstants.keysWithAlternatesRight
    aAlternateKeys = GermanKeyboardConstants.aAlternateKeys
    eAlternateKeys = GermanKeyboardConstants.eAlternateKeys
    iAlternateKeys = GermanKeyboardConstants.iAlternateKeys
    oAlternateKeys = GermanKeyboardConstants.oAlternateKeys
    uAlternateKeys = GermanKeyboardConstants.uAlternateKeys
    yAlternateKeys = GermanKeyboardConstants.yAlternateKeys
    sAlternateKeys = GermanKeyboardConstants.sAlternateKeys
    lAlternateKeys = GermanKeyboardConstants.lAlternateKeys
    zAlternateKeys = GermanKeyboardConstants.zAlternateKeys
    cAlternateKeys = GermanKeyboardConstants.cAlternateKeys
    nAlternateKeys = GermanKeyboardConstants.nAlternateKeys
}

/**
 * Provides a German keyboard layout.
 */
internal fun setDEKeyboardLayout() {
    getDEKeys()
    currencySymbol = "€"
    currencySymbolAlternates = euroAlternateKeys
    spaceBar = "Leerzeichen"
    invalidCommandMsg = "Nicht in Wikidata"
    translateKeyLbl = "Übersetzen"
    translatePrompt = commandPromptSpacing + "de -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Konjugieren"
    conjugatePrompt = commandPromptSpacing + "Konjugieren: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Plural"
    pluralPrompt = commandPromptSpacing + "Plural: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Schon Plural"
}
