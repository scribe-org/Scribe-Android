package be.scri.helpers.spanish

object SpanishLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "$"
    val currencySymbolAlternates = listOf("₡", "S", "€", "£", "₲", "¢")

    // Keyboard Labels
    const val spaceBar = "espacio"
    const val language = "Español"
    const val invalidCommandMsg = "No en Wikidata"
    val baseAutosuggestions = listOf("el", "la", "no")
    val numericAutosuggestions = listOf("que", "de", "en")

    // Verbs After Pronouns (for suggestion)
    val verbsAfterPronounsArray = listOf("ser", "REFLEXIVE_PRONOUN", "no")

    // Pronoun Tenses (for conjugation suggestion)
    val pronounAutosuggestionTenses = mapOf(
        "yo" to "presFPS",
        "tú" to "presSPS",
        "él" to "presTPS",
        "ella" to "presTPS",
        "nosotros" to "presFPP",
        "nosotras" to "presFPP",
        "vosotros" to "presSPP",
        "vosotras" to "presSPP",
        "ellos" to "presTPP",
        "ellas" to "presTPP",
        "ustedes" to "presTPP"
    )

    // Translate Command Texts
    const val translateKeyLbl = "Traducir"
    const val translatePlaceholder = "Ingrese una palabra"
    const val translatePrompt = " es -› ${"targetLanguage()"}" // Example, replace with actual language code
    const val translatePromptAndCursor = translatePrompt + "commandCursor"    // commandCursor needs to be replaced with the actual value
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"
    // it would be better to add mutability for changing the color of the cursor when we are ready to use this variables

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Conjugar"
    const val conjugatePlaceholder = "Ingrese un verbo"
    const val conjugatePrompt = "Conjugar: "
    val conjugatePromptAndCursor = conjugatePrompt + "commandCursor"  // same here
    val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Plural"
    const val pluralPlaceholder = "Ingrese un sustantivo"
    const val pluralPrompt = "Plural: "
    val pluralPromptAndCursor = pluralPrompt + "commandCursor" // same
    val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Ya en plural"
}

