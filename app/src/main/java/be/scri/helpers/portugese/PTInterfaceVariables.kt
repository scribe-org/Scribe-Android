package be.scri.helpers.portugese

object PTInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("$", "€", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val SPACE_BAR = "espaço"
    const val LANGUAGE = "Português"
    const val INVALID_COMMAND_MSG = "Não está no Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("o", "a", "eu")
    val NUMERIC_AUTOSUGGESTIONS = listOf("de", "que", "a")

    // Translate Command Texts
    const val TRANSLATE_KEY_LBL = "Traduzir"
    const val TRANSLATE_PLACEHOLDER = "Digite uma palavra"
    const val TRANSLATE_PROMPT = "pt -› targetLanguage()" // Example, replace with actual language code

    // Replace with actual dynamic value when available
    const val TRANSLATE_PROMPT_AND_CURSOR = "$TRANSLATE_PROMPT commandCursor"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Conjugar"
    const val CONJUGATE_PLACEHOLDER = "Digite um verbo"
    const val CONJUGATE_PROMPT = "Conjugar: "
    const val CONJUGATE_PROMPT_AND_CURSOR = "$CONJUGATE_PROMPT commandCursor" // Replace with actual value
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Digite um substantivo"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = "$PLURAL_PROMPT commandCursor" // Replace with actual value
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Já plural"
}
