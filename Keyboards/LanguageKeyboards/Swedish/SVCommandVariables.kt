/**
 * SVCommandVariables.kt
 *
 * Variables associated with Scribe commands for the Swedish keyboard.
 */

/**
 * Provides labels for the Swedish conjugation state.
 */
internal fun svSetConjugationLabels() {
    when (svConjugationState) {
        active -> {
            labelTopLeft = "imperativ"
            labelTopRight = "liggande"
            labelBottomLeft = "presens"
            labelBottomRight = "dåtid"
        }
        passive -> {
            labelTopLeft = "infinitiv"
            labelTopRight = "liggande"
            labelBottomLeft = "presens"
            labelBottomRight = "dåtid"
        }
    }
}

/**
 * What the conjugation state is for the conjugate feature.
 */
internal enum class SVConjugationState {
    active,
    passive
}
internal var svConjugationState: SVConjugationState = .active

/**
 * Sets the title of the command bar when the keyboard is in conjugate mode.
 */
internal fun svGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (svConjugationState) {
        active -> return commandPromptSpacing + "Aktiv: " + verbToDisplay
        passive -> return commandPromptSpacing + "Passiv: " + verbToDisplay
    }
}

/**
 * Returns the appropriate key in the verbs dictionary to access conjugations.
 */
internal fun svGetConjugationState() : List<String> {
    when (svConjugationState) {
        active -> return listOf("imperative", "activeSupine", "activePresent", "activePreterite")
        passive -> return listOf("passiveInfinitive", "passiveSupine", "passivePresent", "passivePreterite")
    }
}

/**
 * Action associated with the left view switch button of the conjugation state.
 */
internal fun svConjugationStateLeft() {
    if (svConjugationState == .active) {
        return
    } else if (svConjugationState == .passive) {
        svConjugationState = .active
        return
    }
}

/**
 * Action associated with the right view switch button of the conjugation state.
 */
internal fun svConjugationStateRight() {
    if (svConjugationState == .active) {
        svConjugationState = .passive
    } else if (svConjugationState == .passive) {
        return
    }
}
