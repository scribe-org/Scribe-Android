package be.scri.helpers.french

object FRInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val SPACE_BAR = "espace"
    const val LANGUAGE = "Français"
    const val INVALID_COMMAND_MSG = "Pas dans Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("je", "il", "le")
    val NUMERIC_AUTOSUGGESTIONS = listOf("je", "que", "c’est")

    // Verbs After Pronouns (for suggestion)
    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("être", "avoir", "ne")

    // Pronoun Tenses (for conjugation suggestion)
    val PRONOUN_AUTOSUGGESTION_TENSES =
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
    const val TRANSLATE_KEY_LBL = "Traduire"
    const val TRANSLATE_PLACEHOLDER = "Entrez un mot"
    const val TRANSLATE_PROMPT = "fr -› targetLanguage()" // Example, replace with actual language code

    // Replace with actual dynamic value when available
    const val TRANSLATE_PROMPT_AND_CURSOR = "$TRANSLATE_PROMPT commandCursor"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Conjuguer"
    const val CONJUGATE_PLACEHOLDER = "Entrez un verbe"
    const val CONJUGATE_PROMPT = "Conjuguer: "
    const val CONJUGATE_PROMPT_AND_CURSOR = "$CONJUGATE_PROMPT commandCursor" // Replace with actual value
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Pluriel"
    const val PLURAL_PLACEHOLDER = "Entrez un nom"
    const val PLURAL_PROMPT = "Pluriel: "
    const val PLURAL_PROMPT_AND_CURSOR = "$PLURAL_PROMPT commandCursor" // Replace with actual value
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Déjà pluriel"
}
