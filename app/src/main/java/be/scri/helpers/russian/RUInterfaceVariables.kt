package be.scri.helpers.russian

object RUInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "₽"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("₽", "$", "€", "£", "¥")

    // Keyboard Labels
    const val SPACE_BAR = "Пробел"
    const val LANGUAGE = "Pусский"
    const val INVALID_COMMAND_MSG = "Нет в Викиданных"
    val BASE_AUTOSUGGESTIONS = listOf("я", "а", "в")
    val NUMERIC_AUTOSUGGESTIONS = listOf("в", "и", "я")

    // Translate Command Texts
    const val TRANSLATE_KEY_LBL = "Перевести"
    const val TRANSLATE_PLACEHOLDER = "Введите слово"
    const val TRANSLATE_PROMPT = "ru -› targetLanguage()" // Example, replace with actual language code

    // Replace with actual dynamic value when available
    const val TRANSLATE_PROMPT_AND_CURSOR = "$TRANSLATE_PROMPT commandCursor"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Спрягать"
    const val CONJUGATE_PLACEHOLDER = "Введите глагол"
    const val CONJUGATE_PROMPT = "Спрягать: "
    const val CONJUGATE_PROMPT_AND_CURSOR = "$CONJUGATE_PROMPT commandCursor" // Replace with actual value
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Множ-ое"
    const val PLURAL_PLACEHOLDER = "Введите существительное"
    const val PLURAL_PROMPT = "Множ-ое: "
    const val PLURAL_PROMPT_AND_CURSOR = "$PLURAL_PROMPT commandCursor" // Replace with actual value
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Уже во множ-ом"
}
