//
//  CommandVariables.kt
//
//  Variables associated with Scribe commands.
//

// Basic keyboard functionality variables.
internal var capsLockPossible = false
internal var doubleSpacePeriodPossible = false
internal var backspaceTimer: Timer? = null

// All data needed for Scribe commands for the given language keyboard.
internal val nouns = loadJSONToDict(filename = "nouns")
internal val verbs = loadJSONToDict(filename = "verbs")
internal val translations = loadJSONToDict(filename = "translations")
internal val prepositions = loadJSONToDict(filename = "prepositions")

// A larger vertical bar than the normal | key for the cursor.
internal val commandCursor: String = "│"
internal var commandPromptSpacing: String = ""
internal var commandState: Boolean = false

// Command input and output variables.
internal var inputWordIsCapitalized: Boolean = false
internal var wordToReturn: String = ""
internal var invalidState: Boolean = false
internal var invalidCommandMsg: String = ""

// Annotation variables.
internal var annotationState: Boolean = false
internal var nounAnnotationsToDisplay: Int = 0
internal var prepAnnotationState: Boolean = false
internal var annotationHeight = CGFloat(0)

// Indicates that the keyboard has switched to another input language.
// For example another input method is needed to translate.
internal var switchInput: Boolean = false

// Prompts and saving groups of languages.
internal var allPrompts: List<String> = listOf("")
internal val languagesWithCapitalizedNouns = listOf("German")
internal val languagesWithCaseDependantOnPrepositions = listOf("German", "Russian")

// MARK: Translate Variables
internal var translateKeyLbl: String = ""
internal var translatePrompt: String = ""
internal var translatePromptAndCursor: String = ""
internal var getTranslation: Boolean = false
internal var wordToTranslate: String = ""

// MARK: Conjugate Variables
internal var conjugateKeyLbl: String = ""
internal var conjugatePrompt: String = ""
internal var conjugatePromptAndCursor: String = ""
internal var getConjugation: Boolean = false
internal var conjugateView: Boolean = false
internal var conjugateAlternateView: Boolean = false
internal var allTenses = listOf<String>()
internal var allConjugationBtns = listOf<Button>()
internal var tenseFPS: String = ""
internal var tenseSPS: String = ""
internal var tenseTPS: String = ""
internal var tenseFPP: String = ""
internal var tenseSPP: String = ""
internal var tenseTPP: String = ""
internal var labelFPS: String = ""
internal var labelSPS: String = ""
internal var labelTPS: String = ""
internal var labelFPP: String = ""
internal var labelSPP: String = ""
internal var labelTPP: String = ""
internal var tenseTopLeft: String = ""
internal var tenseTopRight: String = ""
internal var tenseBottomLeft: String = ""
internal var tenseBottomRight: String = ""
internal var labelTopLeft: String = ""
internal var labelTopRight: String = ""
internal var labelBottomLeft: String = ""
internal var labelBottomRight: String = ""
internal var verbToConjugate: String = ""
internal var verbToDisplay: String = ""
internal var conjugationToDisplay: String = ""
internal var verbConjugated: String = ""

// MARK: Plural Variables
internal var pluralKeyLbl: String = ""
internal var pluralPrompt: String = ""
internal var pluralPromptAndCursor: String = ""
internal var getPlural: Boolean = false
internal var isAlreadyPluralState: Boolean = false
internal var isAlreadyPluralMessage: String = ""
