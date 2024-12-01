package be.scri.helpers.swedish

object SwedishLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "kr"
    val currencySymbolAlternates = listOf("kr", "$", "€", "£", "¥")

    // Keyboard Labels
    const val spaceBar = "mellanslag"
    const val language = "Svenska"
    const val invalidCommandMsg = "Inte i Wikidata"
    val baseAutosuggestions = listOf("jag", "det", "men")
    val numericAutosuggestions = listOf("jag", "det", "och")

    // Translate Command Texts
    const val translateKeyLbl = "Översätt"
    const val translatePlaceholder = "Ange ett ord"
    const val translatePrompt = "sv -› targetLanguage()" // Example, replace with actual language code
    const val translatePromptAndCursor = "$translatePrompt commandCursor" // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Konjugera"
    const val conjugatePlaceholder = "Ange ett verb"
    const val conjugatePrompt = "Konjugera: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Plural"
    const val pluralPlaceholder = "Ange ett substantiv"
    const val pluralPrompt = "Plural: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Redan plural"
}
