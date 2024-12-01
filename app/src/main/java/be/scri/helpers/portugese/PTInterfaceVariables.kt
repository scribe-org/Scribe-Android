package be.scri.helpers.portugese

object PortugueseLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "$"
    val currencySymbolAlternates = listOf("$", "€", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val spaceBar = "espaço"
    const val language = "Português"
    const val invalidCommandMsg = "Não está no Wikidata"
    val baseAutosuggestions = listOf("o", "a", "eu")
    val numericAutosuggestions = listOf("de", "que", "a")

    // Translate Command Texts
    const val translateKeyLbl = "Traduzir"
    const val translatePlaceholder = "Digite uma palavra"
    const val translatePrompt = "pt -› targetLanguage()" // Example, replace with actual language code
    const val translatePromptAndCursor = "$translatePrompt commandCursor" // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Conjugar"
    const val conjugatePlaceholder = "Digite um verbo"
    const val conjugatePrompt = "Conjugar: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Plural"
    const val pluralPlaceholder = "Digite um substantivo"
    const val pluralPrompt = "Plural: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Já plural"
}
