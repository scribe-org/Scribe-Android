package be.scri.helpers.english

object ENInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("$", "€", "£", "¥", "₩", "¢")

    // Keyboard Labels
    const val SPACE_BAR = "space"
    const val LANGUAGE = "English"
    const val INVALID_COMMAND_MSG = "Not in Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("I", "I'm", "we")
    val NUMERIC_AUTOSUGGESTIONS = listOf("is", "to", "and")

    // Verbs After Pronouns (for suggestion)
    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("have", "be", "can")

    // Pronoun Tenses (for conjugation suggestion)
    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
            "I" to "presSimp",
            "you" to "presSimp",
            "he" to "presTPS",
            "she" to "presTPS",
            "it" to "presTPS",
            "we" to "presSimp",
            "they" to "presSimp",
        )

    // Translate Command Texts
    const val TRANSLATE_KEY_LBL = "Translate"
    const val TRANSLATE_PLACEHOLDER = "Enter a word"
    const val TRANSLATE_PROMPT = "Currently not utilized" // Example placeholder text
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT // Replace with actual dynamic value when available
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Conjugate"
    const val CONJUGATE_PLACEHOLDER = "Enter a verb"
    const val CONJUGATE_PROMPT = "Conjugate: "
    const val CONJUGATE_PROMPT_AND_CURSOR = "$CONJUGATE_PROMPT commandCursor" // Replace with actual value
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Enter a noun"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = "$PLURAL_PROMPT commandCursor" // Replace with actual value
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Already plural"
}
