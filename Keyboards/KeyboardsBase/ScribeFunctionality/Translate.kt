//
//  Translate.swift
//
//  Functions that control the translate command.
//

/// Inserts the translation of a valid word in the command bar into the proxy.
///
/// - Parameters
///   - commandBar: the command bar into which an input was entered.
internal fun queryTranslation(commandBar: TextView) {
    // Cancel via a return press.
    if (commandBar.text!! == translatePromptAndCursor) {
        return
    }
    wordToTranslate = (commandBar.text!!.substring(with = translatePrompt.size until ((commandBar.text!!.size) - 1)))
    wordToTranslate = String(wordToTranslate.trailingSpacesTrimmed)
    // Check to see if the input was uppercase to return an uppercase conjugation.
    inputWordIsCapitalized = false
    val firstLetter = wordToTranslate.substring(toIdx = 1)
    inputWordIsCapitalized = firstLetter.isUppercase
    wordToTranslate = wordToTranslate.lowercased()
    val wordInDirectory = translations?[wordToTranslate] != null
    if (wordInDirectory) {
        wordToReturn = translations?[wordToTranslate] as String
        if (inputWordIsCapitalized) {
            proxy.insertText(wordToReturn.capitalized + " ")
        } else {
            proxy.insertText(wordToReturn + " ")
        }
    } else {
        invalidState = true
    }
}
