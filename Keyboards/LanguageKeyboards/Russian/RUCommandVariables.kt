/**
 * RUCommandVariables.kt
 *
 * Variables associated with Scribe commands for the Russian keyboard.
 */

/**
 * Provides labels for the Russian conjugation state.
 */
internal fun ruSetConjugationLabels() {
    when (ruConjugationState) {
        present -> {
            labelFPS = "я"
            labelSPS = "ты"
            labelTPS = "он/она/оно"
            labelFPP = "мы"
            labelSPP = "вы"
            labelTPP = "они"
            labelTopLeft = ""
            labelTopRight = ""
            labelBottomLeft = ""
            labelBottomRight = ""
        }
        past -> {
            labelFPS = ""
            labelSPS = ""
            labelTPS = ""
            labelFPP = ""
            labelSPP = ""
            labelTPP = ""
            labelTopLeft = "я/ты/он"
            labelTopRight = "я/ты/она"
            labelBottomLeft = "оно"
            labelBottomRight = "мы/вы/они"
        }
    }
}

/**
 * What the conjugation state is for the conjugate feature.
 */
internal enum class RUConjugationState {
    present,
    past
}
internal var ruConjugationState: RUConjugationState = .present

/**
 * Sets the title of the command bar when the keyboard is in conjugate mode.
 */
internal fun ruGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (ruConjugationState) {
        present -> return commandPromptSpacing + "Настоящее: " + verbToDisplay
        past -> return commandPromptSpacing + "Прошедшее: " + verbToDisplay
    }
}

/**
 * Returns the appropriate key in the verbs dictionary to access conjugations.
 */
internal fun ruGetConjugationState() : String {
    when (ruConjugationState) {
        present -> return "pres"
        past -> return "past"
    }
}

/**
 * Action associated with the left view switch button of the conjugation state.
 */
internal fun ruConjugationStateLeft() {
    if (ruConjugationState == .present) {
        return
    } else if (ruConjugationState == .past) {
        ruConjugationState = .present
        return
    }
}

/**
 * Action associated with the right view switch button of the conjugation state.
 */
internal fun ruConjugationStateRight() {
    if (ruConjugationState == .present) {
        ruConjugationState = .past
        return
    } else if (ruConjugationState == .past) {
        return
    }
}
