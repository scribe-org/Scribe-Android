package be.scri.helpers.french

object FrenchLanguageConstants {
    // Currency Symbol and Alternates
    const val currencySymbol = "€"
    val currencySymbolAlternates = listOf("€", "$", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val spaceBar = "espace"
    const val language = "Français"
    const val invalidCommandMsg = "Pas dans Wikidata"
    val baseAutosuggestions = listOf("je", "il", "le")
    val numericAutosuggestions = listOf("je", "que", "c’est")

    // Verbs After Pronouns (for suggestion)
    val verbsAfterPronounsArray = listOf("être", "avoir", "ne")

    // Pronoun Tenses (for conjugation suggestion)
    val pronounAutosuggestionTenses =
        mapOf(
            "je" to "presFPS",
            "tu" to "presSPS",
            "il" to "presTPS",
            "elle" to "presTPS",
            "on" to "presTPS",
            "nous" to "presFPP",
            "vous" to "presSPP",
            "ils" to "presTPP",
            "elles" to "presTPP",
        )

    // Translate Command Texts
    const val translateKeyLbl = "Traduire"
    const val translatePlaceholder = "Entrez un mot"
    const val translatePrompt = "fr -› targetLanguage()" // Example, replace with actual language code
    const val translatePromptAndCursor = "$translatePrompt commandCursor" // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Conjuguer"
    const val conjugatePlaceholder = "Entrez un verbe"
    const val conjugatePrompt = "Conjuguer: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Pluriel"
    const val pluralPlaceholder = "Entrez un nom"
    const val pluralPrompt = "Pluriel: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Déjà pluriel"
}
