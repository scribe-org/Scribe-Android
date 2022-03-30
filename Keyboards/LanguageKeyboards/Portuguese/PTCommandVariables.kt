//
//  PTCommandVariables.kt
//
//  Variables associated with Scribe commands for the Portuguese keyboard.
//

internal fun ptSetConjugationLabels() {
    labelFPS = "eu"
    labelSPS = "tu"
    labelTPS = "ele/ela/você"
    labelFPP = "nós"
    labelSPP = "vós"
    labelTPP = "eles/elas/vocês"
}

/// What the conjugation state is for the conjugate feature.
internal enum class PTConjugationState {
    indicativePresent,
    pastPerfect,
    pastImperfect,
    futureSimple
}
internal var ptConjugationState: PTConjugationState = .indicativePresent

/// Sets the title of the command bar when the keyboard is in conjugate mode.
internal fun ptGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (ptConjugationState) {
        indicativePresent -> return commandPromptSpacing + "Presente: " + verbToDisplay
        pastPerfect -> return commandPromptSpacing + "Pretérito Perfeito: " + verbToDisplay
        pastImperfect -> return commandPromptSpacing + "Pretérito Imperfeito: " + verbToDisplay
        futureSimple -> return commandPromptSpacing + "Futuro Simples: " + verbToDisplay
    }
}

/// Returns the appropriate key in the verbs dictionary to access conjugations.
internal fun ptGetConjugationState() : String {
    when (ptConjugationState) {
        indicativePresent -> return "pres"
        pastPerfect -> return "perf"
        pastImperfect -> return "imp"
        futureSimple -> return "fSimp"
    }
}

/// Action associated with the left view switch button of the conjugation state.
internal fun ptConjugationStateLeft() {
    if (ptConjugationState == .indicativePresent) {
        return
    } else if (ptConjugationState == .pastPerfect) {
        ptConjugationState = .indicativePresent
        return
    } else if (ptConjugationState == .pastImperfect) {
        ptConjugationState = .pastPerfect
        return
    } else if (ptConjugationState == .futureSimple) {
        ptConjugationState = .pastImperfect
        return
    }
}

/// Action associated with the right view switch button of the conjugation state.
internal fun ptConjugationStateRight() {
    if (ptConjugationState == .indicativePresent) {
        ptConjugationState = .pastPerfect
    } else if (ptConjugationState == .pastPerfect) {
        ptConjugationState = .pastImperfect
        return
    } else if (ptConjugationState == .pastImperfect) {
        ptConjugationState = .futureSimple
        return
    } else if (ptConjugationState == .futureSimple) {
        return
    }
}
