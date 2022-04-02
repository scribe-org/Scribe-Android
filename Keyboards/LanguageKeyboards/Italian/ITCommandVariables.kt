/**
 * ITCommandVariables.kt
 *
 * Variables associated with Scribe commands for the Italian keyboard.
 */

/**
 * Provides labels for the Italian conjugation state.
 */
internal fun itSetConjugationLabels() {
    labelFPS = "io"
    labelSPS = "tu"
    labelTPS = "lei/lui"
    labelFPP = "noi"
    labelSPP = "voi"
    labelTPP = "loro"
}

/**
 * What the conjugation state is for the conjugate feature.
 */
internal enum class ITConjugationState {
    present,
    preterite,
    imperfect
}
internal var itConjugationState: ITConjugationState = .present

/**
 * Sets the title of the command bar when the keyboard is in conjugate mode.
 */
internal fun itGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (itConjugationState) {
        present -> return commandPromptSpacing + "Presente: " + verbToDisplay
        preterite -> return commandPromptSpacing + "Preterito: " + verbToDisplay
        imperfect -> return commandPromptSpacing + "Imperfetto: " + verbToDisplay
    }
}

/**
 * Returns the appropriate key in the verbs dictionary to access conjugations.
 */
internal fun itGetConjugationState() : String {
    when (itConjugationState) {
        present -> return "pres"
        preterite -> return "pret"
        imperfect -> return "imp"
    }
}

/**
 * Action associated with the left view switch button of the conjugation state.
 */
internal fun itConjugationStateLeft() {
    if (itConjugationState == .present) {
        return
    } else if (itConjugationState == .preterite) {
        itConjugationState = .present
        return
    } else if (itConjugationState == .imperfect) {
        itConjugationState = .preterite
        return
    }
}

/**
 * Action associated with the right view switch button of the conjugation state.
 */
internal fun itConjugationStateRight() {
    if (itConjugationState == .present) {
        itConjugationState = .preterite
    } else if (itConjugationState == .preterite) {
        itConjugationState = .imperfect
        return
    } else if (itConjugationState == .imperfect) {
        return
    }
}
