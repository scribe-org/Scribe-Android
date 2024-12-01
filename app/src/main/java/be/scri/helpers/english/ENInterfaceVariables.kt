package be.scri.helpers.english

object EnglishLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "$"
    val currencySymbolAlternates = listOf("$", "€", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val spaceBar = "space"
    const val language = "English"
    const val invalidCommandMsg = "Not in Wikidata"
    val baseAutosuggestions = listOf("I", "I'm", "we")
    val numericAutosuggestions = listOf("is", "to", "and")

    // Verbs After Pronouns (for suggestion)
    val verbsAfterPronounsArray = listOf("have", "be", "can")

    // Pronoun Tenses (for conjugation suggestion)
    val pronounAutosuggestionTenses = mapOf(
        "I" to "presSimp",
        "you" to "presSimp",
        "he" to "presTPS",
        "she" to "presTPS",
        "it" to "presTPS",
        "we" to "presSimp",
        "they" to "presSimp"
    )

    // Translate Command Texts
    const val translateKeyLbl = "Translate"
    const val translatePlaceholder = "Enter a word"
    const val translatePrompt = "Currently not utilized" // Example placeholder text
    const val translatePromptAndCursor = translatePrompt // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Conjugate"
    const val conjugatePlaceholder = "Enter a verb"
    const val conjugatePrompt = "Conjugate: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Plural"
    const val pluralPlaceholder = "Enter a noun"
    const val pluralPrompt = "Plural: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Already plural"
}
