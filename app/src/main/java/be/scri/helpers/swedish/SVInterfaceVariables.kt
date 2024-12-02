package be.scri.helpers.swedish

object SVInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "kr"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("kr", "$", "€", "£", "¥")

    // Keyboard Labels
    const val SPACE_BAR = "mellanslag"
    const val LANGUAGE = "Svenska"
    const val INVALID_COMMAND_MSG = "Inte i Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("jag", "det", "men")
    val NUMERIC_AUTOSUGGESTIONS = listOf("jag", "det", "och")

    // Translate Command Texts
    const val TRANSLATE_KEY_LBL = "Översätt"
    const val TRANSLATE_PLACEHOLDER = "Ange ett ord"
    const val TRANSLATE_PROMPT = "sv -› targetLanguage()" // Example, replace with actual language code

    // Replace with actual dynamic value when available
    const val TRANSLATE_PROMPT_AND_CURSOR = "$TRANSLATE_PROMPT commandCursor"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Konjugera"
    const val CONJUGATE_PLACEHOLDER = "Ange ett verb"
    const val CONJUGATE_PROMPT = "Konjugera: "
    const val CONJUGATE_PROMPT_AND_CURSOR = "$CONJUGATE_PROMPT commandCursor" // Replace with actual value
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Ange ett substantiv"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = "$PLURAL_PROMPT commandCursor" // Replace with actual value
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Redan plural"
}
