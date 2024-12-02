package be.scri.helpers.german

object DEInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "€"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("€", "$", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val SPACE_BAR = "Leerzeichen"
    const val LANGUAGE = "Deutsch"
    const val INVALID_COMMAND_MSG = "Nicht in Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("ich", "die", "das")
    val NUMERIC_AUTOSUGGESTIONS = listOf("Prozent", "Milionen", "Meter")

    // Verbs After Pronouns (for suggestion)
    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("haben", "sein", "können")

    // Pronoun Tenses (for conjugation suggestion)
    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
            "ich" to "presFPS",
            "du" to "presSPS",
            "er" to "presTPS",
            "sie" to "presTPS",
            "es" to "presTPS",
            "wir" to "presFPP",
            "ihr" to "presSPP",
            "Sie" to "presTPP",
        )

    // Translate Command Texts
    const val TRANSLATE_KEY_LBL = "Übersetzen"
    const val TRANSLATE_PLACEHOLDER = "Wort eingeben"
    const val TRANSLATE_PROMPT = "de -› targetLanguage()" // Example, replace with actual language code
    const val TRANSLATE_PROMPT_AND_CURSOR = "$TRANSLATE_PROMPT commandCursor" // Replace with actual dynamic value when available
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Konjugieren"
    const val CONJUGATE_PLACEHOLDER = "Verb eingeben"
    const val CONJUGATE_PROMPT = "Konjugieren: "
    const val CONJUGATE_PROMPT_AND_CURSOR = "$CONJUGATE_PROMPT commandCursor" // Replace with actual value
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Nomen eingeben"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = "$PLURAL_PROMPT commandCursor" // Replace with actual value
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Schon Plural"
}
