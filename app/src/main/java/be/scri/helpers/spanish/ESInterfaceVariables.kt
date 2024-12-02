package be.scri.helpers.spanish

object ESInterfaceVariables {
    // Currency Symbol and Alternates
    const val CURRENCY_SYMBOL = "$"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("₡", "S", "€", "£", "₲", "¢")

    // Keyboard Labels
    const val SPACE_BAR = "espacio"
    const val LANGUAGE = "Español"
    const val INVALID_COMMAND_MSG = "No en Wikidata"
    val BASE_AUTOSUGGESTIONS = listOf("el", "la", "no")
    val NUMERIC_AUTOSUGGESTIONS = listOf("que", "de", "en")

    // Verbs After Pronouns (for suggestion)
    val VERBS_AFTER_PRONOUNS_ARRAY = listOf("ser", "REFLEXIVE_PRONOUN", "no")

    // Pronoun Tenses (for conjugation suggestion)
    val PRONOUN_AUTOSUGGESTION_TENSES =
        mapOf(
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
            "ustedes" to "presTPP",
        )

    // Translate Command Texts
    const val TRANSLATE_KEY_LBL = "Traducir"
    const val TRANSLATE_PLACEHOLDER = "Ingrese una palabra"

    // Example, replace with actual language code
    const val TRANSLATE_PROMPT = " es -› ${"targetLanguage()"}"

    // commandCursor needs to be replaced with the actual value
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "commandCursor"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = "$TRANSLATE_PROMPT_AND_CURSOR $TRANSLATE_PLACEHOLDER"
// it would be better to add mutability for changing the color of the cursor when we are ready to use these variables

    // Conjugate Command Texts
    const val CONJUGATE_KEY_LBL = "Conjugar"
    const val CONJUGATE_PLACEHOLDER = "Ingrese un verbo"
    const val CONJUGATE_PROMPT = "Conjugar: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "commandCursor" // same here
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = "$CONJUGATE_PROMPT_AND_CURSOR $CONJUGATE_PLACEHOLDER"

    // Plural Command Texts
    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Ingrese un sustantivo"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "commandCursor" // same
    const val PLURAL_PROMPT_AND_PLACEHOLDER = "$PLURAL_PROMPT_AND_CURSOR $PLURAL_PLACEHOLDER"

    // Already Plural Message
    const val ALREADY_PLURAL_MSG = "Ya en plural"
}
