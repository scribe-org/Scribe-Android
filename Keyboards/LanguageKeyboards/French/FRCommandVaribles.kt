/**
 * FRCommandVaribles.kt
 *
 * Variables associated with Scribe commands for the French keyboard.
 */

/**
 * Provides labels for the French conjugation state.
 */
internal fun frSetConjugationLabels() {
    labelFPS = "je"
    labelSPS = "tu"
    labelTPS = "il/elle"
    labelFPP = "nous"
    labelSPP = "vous"
    labelTPP = "ils/elles"
}

/**
 * What the conjugation state is for the conjugate feature.
 */
internal enum class FRConjugationState {
    indicativePresent,
    preterite,
    imperfect
}
internal var frConjugationState: FRConjugationState = .indicativePresent

/**
 * Sets the title of the command bar when the keyboard is in conjugate mode.
 */
internal fun frGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (frConjugationState) {
        indicativePresent -> return commandPromptSpacing + "Présent: " + verbToDisplay
        preterite -> return commandPromptSpacing + "Passé simple: " + verbToDisplay
        imperfect -> return commandPromptSpacing + "Imparfait: " + verbToDisplay
    }
}

/**
 * Returns the appropriate key in the verbs dictionary to access conjugations.
 */
internal fun frGetConjugationState() : String {
    when (frConjugationState) {
        indicativePresent -> return "pres"
        preterite -> return "pret"
        imperfect -> return "imp"
    }
}

/**
 * Action associated with the left view switch button of the conjugation state.
 */
internal fun frConjugationStateLeft() {
    if (frConjugationState == .indicativePresent) {
        return
    } else if (frConjugationState == .preterite) {
        frConjugationState = .indicativePresent
        return
    } else if (frConjugationState == .imperfect) {
        frConjugationState = .preterite
        return
    }
}

/**
 * Action associated with the right view switch button of the conjugation state.
 */
internal fun frConjugationStateRight() {
    if (frConjugationState == .indicativePresent) {
        frConjugationState = .preterite
    } else if (frConjugationState == .preterite) {
        frConjugationState = .imperfect
        return
    } else if (frConjugationState == .imperfect) {
        return
    }
}
