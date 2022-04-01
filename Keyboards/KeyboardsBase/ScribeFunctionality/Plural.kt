/**
 * Plural.kt
 *
 * Functions that control the plural command.
 */

/**
 * Inserts the plural of a valid noun in the [commandBar] into the proxy.
 *
 * @param keyPressed The button pressed as sender.
 * @param requestedTense The tense that is triggered by the given key.
 */
internal fun queryPlural(commandBar: TextView) {
    // Cancel via a return press.
    if (commandBar.text!! == pluralPromptAndCursor) {
        return
    }
    var noun: String = (commandBar.text!!.substring(with = pluralPrompt.size until ((commandBar.text!!.size) - 1)))
    noun = String(noun.trailingSpacesTrimmed)
    // Check to see if the input was uppercase to return an uppercase plural.
    inputWordIsCapitalized = false
    if (!languagesWithCapitalizedNouns.contains(controllerLanguage)) {
        val firstLetter = noun.substring(toIdx = 1)
        inputWordIsCapitalized = firstLetter.isUppercase
        noun = noun.lowercased()
    }
    val nounInDirectory = nouns?[noun] != null
    if (nounInDirectory) {
        if (nouns?[noun]?["plural"] as? String != "isPlural") {
            val plural = nouns?[noun]?["plural"] as? String ?: return
            if (inputWordIsCapitalized == false) {
                proxy.insertText(plural + " ")
            } else {
                proxy.insertText(plural.capitalized + " ")
            }
        } else {
            proxy.insertText(noun + " ")
            commandBar.text = commandPromptSpacing + isAlreadyPluralMessage
            invalidState = true
            isAlreadyPluralState = true
        }
    } else {
        invalidState = true
    }
}
