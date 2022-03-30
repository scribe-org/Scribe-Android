//
//  DECommandVariables.kt
//
//  Variables associated with Scribe commands for the German keyboard.
//

internal fun deSetConjugationLabels() {
    labelFPS = "ich"
    labelSPS = "du"
    labelTPS = "er/sie/es"
    labelFPP = "wir"
    labelSPP = "ihr"
    labelTPP = "sie/Sie"
}

/// What the conjugation state is for the conjugate feature.
internal enum class DEConjugationState {
    indicativePresent,
    indicativePreterite,
    indicativePerfect
}
internal var deConjugationState: DEConjugationState = .indicativePresent

/// Sets the title of the command bar when the keyboard is in conjugate mode.
internal fun deGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (deConjugationState) {
        indicativePresent -> return commandPromptSpacing + "Präsens: " + verbToDisplay
        indicativePreterite -> return commandPromptSpacing + "Präteritum: " + verbToDisplay
        indicativePerfect -> return commandPromptSpacing + "Perfekt: " + verbToDisplay
    }
}

/// Returns the appropriate key in the verbs dictionary to access conjugations.
internal fun deGetConjugationState() : String {
    when (deConjugationState) {
        indicativePresent -> return "pres"
        indicativePreterite -> return "pret"
        indicativePerfect -> return "perf"
    }
}

/// Action associated with the left view switch button of the conjugation state.
internal fun deConjugationStateLeft() {
    if (deConjugationState == .indicativePresent) {
        return
    } else if (deConjugationState == .indicativePreterite) {
        deConjugationState = .indicativePresent
        return
    } else if (deConjugationState == .indicativePerfect) {
        deConjugationState = .indicativePreterite
        return
    }
}

/// Action associated with the right view switch button of the conjugation state.
internal fun deConjugationStateRight() {
    if (deConjugationState == .indicativePresent) {
        deConjugationState = .indicativePreterite
    } else if (deConjugationState == .indicativePreterite) {
        deConjugationState = .indicativePerfect
        return
    } else if (deConjugationState == .indicativePerfect) {
        return
    }
}
