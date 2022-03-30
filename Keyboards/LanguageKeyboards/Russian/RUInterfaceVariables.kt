//
//  RUInterfaceVariables.kt
//
//  Constants and functions to load the Russian Scribe keyboard.
//
enum class RussianKeyboardConstants {

    // Keyboard key layouts.
    internal val letterKeysPhone = listOf(listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х"), listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"), listOf("shift", "я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "delete"), listOf("123", "selectKeyboard", "space", "return"))
    // "undoArrow"

    internal val numberKeysPhone = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"), listOf("-", "/", ":", ";", "(", ")", "₽", "&", "@", "\""), listOf("#+=", ".", ",", "?", "!", "'", "delete"), listOf("АБВ", "selectKeyboard", "space", "return"))
    // "undoArrow"

    internal val symbolKeysPhone = listOf(listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="), listOf("_", "\\", "|", "~", "<", ">", "$", "€", "£", "·"), listOf("123", ".", ",", "?", "!", "'", "delete"), listOf("АБВ", "selectKeyboard", "space", "return"))
    // "undoArrow"

    internal val letterKeysPad = listOf(listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "delete"), listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э", "return"), listOf("shift", "я", "ч", "с", "м", "и", "т", "ь", "б", "ю", ".", "shift"), listOf(".?123", "selectKeyboard", "space", ".?123", "hideKeyboard"))
    // "undoArrow"

    internal val numberKeysPad = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "—", "delete"), listOf("@", "#", "№", "₽", "ʼ", "&", "*", "(", ")", "'", "\"", "return"), listOf("#+=", "%", "_", "-", "+", "=", "≠", ";", ":", ",", ".", "#+="), listOf("АБВ", "selectKeyboard", "space", "АБВ", "hideKeyboard"))
    // "undoArrow"

    internal val symbolKeysPad = listOf(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "—", "delete"), listOf("$", "€", "£", "¥", "±", "·", "`", "[", "]", "{", "}", "return"), listOf("123", "§", "|", "~", "...", "^", "\\", "<", ">", "!", "?", "123"), listOf("АБВ", "selectKeyboard", "space", "АБВ", "hideKeyboard"))
    // "undoArrow"

    // Alternate key vars.
    internal val keysWithAlternates = listOf("е", "ь")
    internal val keysWithAlternatesLeft = listOf("е")
    internal val keysWithAlternatesRight = listOf("ь")
    internal val еAlternateKeys = listOf("ë")
    internal val ьAlternateKeys = listOf("Ъ")
}

/// Gets the keys for the Russian keyboard.
internal fun getRUKeys() {
    if (DeviceType.isPhone) {
        letterKeys = RussianKeyboardConstants.letterKeysPhone
        numberKeys = RussianKeyboardConstants.numberKeysPhone
        symbolKeys = RussianKeyboardConstants.symbolKeysPhone
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("й", "ф", "1", "-", "[", "_")
        rightKeyChars = listOf("х", "э", "0", "\"", "=", "·")
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    } else {
        letterKeys = RussianKeyboardConstants.letterKeysPad
        numberKeys = RussianKeyboardConstants.numberKeysPad
        symbolKeys = RussianKeyboardConstants.symbolKeysPad
        allKeys = Array(letterKeys.joined()) + Array(numberKeys.joined()) + Array(symbolKeys.joined())
        leftKeyChars = listOf("й", "ф", "1", "@", "$")
        rightKeyChars = listOf()
        centralKeyChars = allKeys.filter { !leftKeyChars.contains(it) && !rightKeyChars.contains(it) }
    }
    keysWithAlternates = RussianKeyboardConstants.keysWithAlternates
    keysWithAlternatesLeft = RussianKeyboardConstants.keysWithAlternatesLeft
    keysWithAlternatesRight = RussianKeyboardConstants.keysWithAlternatesRight
    еAlternateKeys = RussianKeyboardConstants.еAlternateKeys
    ьAlternateKeys = RussianKeyboardConstants.ьAlternateKeys
}

/// Provides a Russian keyboard layout.
internal fun setRUKeyboardLayout() {
    getRUKeys()
    currencySymbol = "₽"
    currencySymbolAlternates = roubleAlternateKeys
    spaceBar = "Пробел"
    invalidCommandMsg = "Нет в Викиданных"
    translateKeyLbl = "Перевести"
    translatePrompt = commandPromptSpacing + "ru -› ${getControllerLanguageAbbr()}: "
    translatePromptAndCursor = translatePrompt + commandCursor
    conjugateKeyLbl = "Спрягать"
    conjugatePrompt = commandPromptSpacing + "Спрягать: "
    conjugatePromptAndCursor = conjugatePrompt + commandCursor
    pluralKeyLbl = "Множ"
    pluralPrompt = commandPromptSpacing + "Множ: "
    pluralPromptAndCursor = pluralPrompt + commandCursor
    isAlreadyPluralMessage = "Уже во множ"
}
