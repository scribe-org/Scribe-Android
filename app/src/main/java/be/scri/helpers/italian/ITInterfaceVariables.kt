package be.scri.helpers.italian

object ItalianLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "€"
    val currencySymbolAlternates = listOf("€", "$", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val spaceBar = "spazio"
    const val language = "Italiano"
    const val invalidCommandMsg = "Non in Wikidata"
    val baseAutosuggestions = listOf("ho", "non", "ma")
    val numericAutosuggestions = listOf("utenti", "anni", "e")

    // Translate Command Texts
    const val translateKeyLbl = "Tradurre"
    const val translatePlaceholder = "Inserisci una parola"
    const val translatePrompt = "it -› targetLanguage()" // Example, replace with actual language code
    const val translatePromptAndCursor = "$translatePrompt commandCursor" // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Coniugare"
    const val conjugatePlaceholder = "Inserisci un verbo"
    const val conjugatePrompt = "Coniugare: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Plurale"
    const val pluralPlaceholder = "Inserisci un nome"
    const val pluralPrompt = "Plurale: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Già plurale"
}
