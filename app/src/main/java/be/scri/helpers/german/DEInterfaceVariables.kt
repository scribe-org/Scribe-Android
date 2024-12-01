package be.scri.helpers.german

object GermanLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "€"
    val currencySymbolAlternates = listOf("€", "$", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val spaceBar = "Leerzeichen"
    const val language = "Deutsch"
    const val invalidCommandMsg = "Nicht in Wikidata"
    val baseAutosuggestions = listOf("ich", "die", "das")
    val numericAutosuggestions = listOf("Prozent", "Milionen", "Meter")

    // Verbs After Pronouns (for suggestion)
    val verbsAfterPronounsArray = listOf("haben", "sein", "können")

    // Pronoun Tenses (for conjugation suggestion)
    val pronounAutosuggestionTenses = mapOf(
        "ich" to "presFPS",
        "du" to "presSPS",
        "er" to "presTPS",
        "sie" to "presTPS",
        "es" to "presTPS",
        "wir" to "presFPP",
        "ihr" to "presSPP",
        "Sie" to "presTPP"
    )

    // Translate Command Texts
    const val translateKeyLbl = "Übersetzen"
    const val translatePlaceholder = "Wort eingeben"
    const val translatePrompt = "de -› targetLanguage()" // Example, replace with actual language code
    const val translatePromptAndCursor = "$translatePrompt commandCursor" // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Konjugieren"
    const val conjugatePlaceholder = "Verb eingeben"
    const val conjugatePrompt = "Konjugieren: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Plural"
    const val pluralPlaceholder = "Nomen eingeben"
    const val pluralPrompt = "Plural: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Schon Plural"
}
