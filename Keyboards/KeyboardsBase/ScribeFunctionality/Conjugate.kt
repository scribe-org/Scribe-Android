/**
 * Conjugation.kt
 *
 * Functions and elements that control the conjugation command.
 */

// Dictionary for accessing keyboard conjugation state.
internal val keyboardConjTitleDict: Map<String, Any> = mapOf(
    "French" to frGetConjugationTitle,
    "German" to deGetConjugationTitle,
    "Italian" to itGetConjugationTitle,
    "Portuguese" to ptGetConjugationTitle,
    "Russian" to ruGetConjugationTitle,
    "Spanish" to esGetConjugationTitle,
    "Swedish" to svGetConjugationTitle
)

// Dictionary for accessing keyboard conjugation state.
internal val keyboardConjStateDict: Map<String, Any> = mapOf(
    "French" to frGetConjugationState,
    "German" to deGetConjugationState,
    "Italian" to itGetConjugationState,
    "Portuguese" to ptGetConjugationState,
    "Russian" to ruGetConjugationState,
    "Spanish" to esGetConjugationState,
    "Swedish" to svGetConjugationState
)

// Dictionary for accessing keyboard conjugation state.
internal val keyboardConjLabelDict: Map<String, Any> = mapOf(
    "French" to frSetConjugationLabels,
    "German" to deSetConjugationLabels,
    "Italian" to itSetConjugationLabels,
    "Portuguese" to ptSetConjugationLabels,
    "Russian" to ruSetConjugationLabels,
    "Spanish" to esSetConjugationLabels,
    "Swedish" to svSetConjugationLabels
)

/**
 * Triggers the display of the conjugation view for a valid verb in the [commandBar].
 */
internal fun triggerConjugation(commandBar: TextView) : Boolean {
    // Cancel via a return press.
    if (commandBar.text!! == conjugatePromptAndCursor) {
        return false
    }
    verbToConjugate = (commandBar.text!!.substring(with = conjugatePrompt.size until (commandBar.text!!.size) - 1))
    verbToConjugate = String(verbToConjugate.trailingSpacesTrimmed)
    // Check to see if the input was uppercase to return an uppercase conjugation.
    inputWordIsCapitalized = false
    val firstLetter = verbToConjugate.substring(toIdx = 1)
    inputWordIsCapitalized = firstLetter.isUppercase
    verbToConjugate = verbToConjugate.lowercased()
    return verbs?[verbToConjugate] != null
}

/**
 * Returns a conjugation once a user presses a key in the conjugateView.
 *
 * @param keyPressed The button pressed as sender.
 * @param requestedTense The tense that is triggered by the given key.
 */
internal fun returnConjugation(keyPressed: Button, requestedTense: String) {
    // Don't change proxy if they select a conjugation that's missing.
    if (keyPressed.titleLabel?.text == invalidCommandMsg) {
        proxy.insertText("")
    } else if (conjugateAlternateView == false) {
        if (deConjugationState != .indicativePerfect) {
            wordToReturn = verbs?[verbToConjugate]!![requestedTense] as String
            if (inputWordIsCapitalized == true) {
                proxy.insertText(wordToReturn.capitalized + " ")
            } else {
                proxy.insertText(wordToReturn + " ")
            }
        } else {
            proxy.insertText(verbs?[verbToConjugate]!!["pastParticiple"] as String + " ")
        }
    } else if (conjugateAlternateView == true) {
        wordToReturn = verbs?[verbToConjugate]!![requestedTense] as String
        if (inputWordIsCapitalized == true) {
            proxy.insertText(wordToReturn.capitalized + " ")
        } else {
            proxy.insertText(wordToReturn + " ")
        }
    }
    commandState = false
    conjugateView = false
}
