/**
 * ESCommandVariables.kt
 *
 * Variables associated with Scribe commands for the Spanish keyboard.
 */

/**
 * Provides labels for the Spanish conjugation state.
 */
internal fun esSetConjugationLabels() {
    labelFPS = "yo"
    labelSPS = "tú"
    labelTPS = "él/ella/Ud."
    labelFPP = "nosotros"
    labelSPP = "vosotros"
    labelTPP = "ellos/ellas/Uds."
}

/**
 * What the conjugation state is for the conjugate feature.
 */
internal enum class ESConjugationState {
    indicativePresent,
    preterite,
    imperfect
}
internal var esConjugationState: ESConjugationState = .indicativePresent

/**
 * Sets the title of the command bar when the keyboard is in conjugate mode.
 */
internal fun esGetConjugationTitle() : String {
    if (inputWordIsCapitalized == true) {
        verbToDisplay = verbToConjugate.capitalized
    } else {
        verbToDisplay = verbToConjugate
    }
    when (esConjugationState) {
        indicativePresent -> return commandPromptSpacing + "Presente: " + verbToDisplay
        preterite -> return commandPromptSpacing + "Pretérito: " + verbToDisplay
        imperfect -> return commandPromptSpacing + "Imperfecto: " + verbToDisplay
    }
}

/**
 * Returns the appropriate key in the verbs dictionary to access conjugations.
 */
internal fun esGetConjugationState() : String {
    when (esConjugationState) {
        indicativePresent -> return "pres"
        preterite -> return "pret"
        imperfect -> return "imp"
    }
}

/**
 * Action associated with the left view switch button of the conjugation state.
 */
internal fun esConjugationStateLeft() {
    if (esConjugationState == .indicativePresent) {
        return
    } else if (esConjugationState == .preterite) {
        esConjugationState = .indicativePresent
        return
    } else if (esConjugationState == .imperfect) {
        esConjugationState = .preterite
        return
    }
}

/**
 * Action associated with the right view switch button of the conjugation state.
 */
internal fun esConjugationStateRight() {
    if (esConjugationState == .indicativePresent) {
        esConjugationState = .preterite
    } else if (esConjugationState == .preterite) {
        esConjugationState = .imperfect
        return
    } else if (esConjugationState == .imperfect) {
        return
    }
}
