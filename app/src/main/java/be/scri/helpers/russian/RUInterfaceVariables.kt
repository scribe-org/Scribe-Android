package be.scri.helpers.russian

object RussianLanguageConstants {

    // Currency Symbol and Alternates
    const val currencySymbol = "₽"
    val currencySymbolAlternates = listOf("₽", "$", "€", "£", "¥")

    // Keyboard Labels
    const val spaceBar = "Пробел"
    const val language = "Pусский"
    const val invalidCommandMsg = "Нет в Викиданных"
    val baseAutosuggestions = listOf("я", "а", "в")
    val numericAutosuggestions = listOf("в", "и", "я")

    // Translate Command Texts
    const val translateKeyLbl = "Перевести"
    const val translatePlaceholder = "Введите слово"
    const val translatePrompt = "ru -› targetLanguage()" // Example, replace with actual language code
    const val translatePromptAndCursor = "$translatePrompt commandCursor" // Replace with actual dynamic value when available
    const val translatePromptAndPlaceholder = "$translatePromptAndCursor $translatePlaceholder"

    // Conjugate Command Texts
    const val conjugateKeyLbl = "Спрягать"
    const val conjugatePlaceholder = "Введите глагол"
    const val conjugatePrompt = "Спрягать: "
    const val conjugatePromptAndCursor = "$conjugatePrompt commandCursor" // Replace with actual value
    const val conjugatePromptAndPlaceholder = "$conjugatePromptAndCursor $conjugatePlaceholder"

    // Plural Command Texts
    const val pluralKeyLbl = "Множ-ое"
    const val pluralPlaceholder = "Введите существительное"
    const val pluralPrompt = "Множ-ое: "
    const val pluralPromptAndCursor = "$pluralPrompt commandCursor" // Replace with actual value
    const val pluralPromptAndPlaceholder = "$pluralPromptAndCursor $pluralPlaceholder"

    // Already Plural Message
    const val alreadyPluralMsg = "Уже во множ-ом"
}
