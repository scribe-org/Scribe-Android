/**
 * Annotate.kt
 *
 * Functions and elements that control word annotations.
 */

// Dictionary for accessing keyboard conjugation state.
internal val formToColorDict: Map<String, UIColor> = mapOf(
    "F" to annotateRed,
    "M" to annotateBlue,
    "C" to annotatePurple,
    "N" to annotateGreen,
    "PL" to annotateOrange
)

// Dictionary to convert noun annotations into the keyboard language.
internal val nounAnnotationConversionDict = mapOf(
    "Swedish" to mapOf("C" to "U"),
    "Russian" to mapOf("F" to "Ж", "M" to "М", "N" to "Н", "PL" to "МН")
)

// Dictionary to convert case annotations into the keyboard language.
internal val caseAnnotationConversionDict = mapOf(
    "German" to mapOf("Acc" to "Akk"),
    "Russian" to mapOf("Acc" to "Вин", "Dat" to "Дат", "Gen" to "Род", "Loc" to "Мес", "Pre" to "Пре", "Ins" to "Инс")
)


/**
 * Hides the [annotationDisplay] so that it can be selectively shown to the user as needed.
 */
internal fun hideAnnotations(annotationDisplay: List<TextView>) {
    for (idx in 0 until annotationDisplay.size) {
        annotationDisplay[idx].backgroundColor = UIColor.clear
        annotationDisplay[idx].text = ""
    }
}

/**
 * Sets the annotation of an noun annotation element given its [label] and [annotation].
 */
internal fun setNounAnnotation(label: TextView, annotation: String) {
    var annotationToDisplay: String = annotation
    if (scribeKeyState != true) {
        /**
         * Cancel if typing while commands are displayed.
         * Convert annotation into the keyboard language if necessary.
         */
        if (nounAnnotationConversionDict[controllerLanguage] != null) {
            if (nounAnnotationConversionDict[controllerLanguage]?[annotation] != null) {
                annotationToDisplay = nounAnnotationConversionDict[controllerLanguage]?[annotation] ?: ""
            }
        }
        if (annotation == "PL") {
            // Make text smaller to fit the annotation.
            if (DeviceType.isPhone) {
                label.font = .systemFont(ofSize = annotationHeight * 0.6)
            } else if (DeviceType.isPad) {
                label.font = .systemFont(ofSize = annotationHeight * 0.8)
            }
        } else {
            if (DeviceType.isPhone) {
                label.font = .systemFont(ofSize = annotationHeight * 0.70)
            } else if (DeviceType.isPad) {
                label.font = .systemFont(ofSize = annotationHeight * 0.95)
            }
        }
        // Assign color and text to the label.
        label.backgroundColor = formToColorDict[annotation]
        label.text = annotationToDisplay
    }
}

/**
 * Checks if a word is a noun and annotates the command bar if so.
 *
 * @param commandBar The command bar into which an input was entered.
 * @param nounAnnotationDisplay The part of the annotation display that's for nouns.
 * @param annotationDisplay The full annotation display elements.
 * @param givenWord A word that is potentially a noun.
 */
internal fun nounAnnotation(
    commandBar: TextView, nounAnnotationDisplay: List<TextView>, annotationDisplay: List<TextView>, givenWord: String
    ) {
    // Convert the given word to lower case unless nouns are capitalized in the language.
    var wordToCheck: String = ""
    if (!languagesWithCapitalizedNouns.contains(controllerLanguage)) {
        wordToCheck = givenWord.lowercased()
    } else {
        wordToCheck = givenWord
    }
    val isNoun = nouns?[wordToCheck] != null || nouns?[givenWord.lowercased()] != null
    if (isNoun) {
        // Clear the prior annotations to assure that preposition annotations don't persist.
        hideAnnotations(annotationDisplay = annotationDisplay)
        nounAnnotationsToDisplay = 0
        // Make command bar font larger for annotation.
        if (DeviceType.isPhone) {
            commandBar.font = .systemFont(ofSize = annotationHeight * 0.8)
        } else if (DeviceType.isPad) {
            commandBar.font = .systemFont(ofSize = annotationHeight)
        }
        val nounForm: String = nouns?[wordToCheck]?["form"] as String
        if (nounForm == "") {
            return
        } else {
            // Count how many annotations will be changed.
            var numberOfAnnotations: Int = 0
            var annotationsToAssign: List<String> = listOf<String>()
            if (nounForm.size >= 3) { // Would have a slash as the largest is PL
                annotationsToAssign = (nounForm.components(separatedBy = "/"))
                numberOfAnnotations = annotationsToAssign.size
            } else {
                numberOfAnnotations = 1
                annotationsToAssign.append(nounForm)
            }
            for (idx in 0 until numberOfAnnotations) {
                setNounAnnotation(label = nounAnnotationDisplay[idx], annotation = annotationsToAssign[idx])
            }
            if (formToColorDict[nounForm] != null) {
                commandBar.textColor = formToColorDict[nounForm]
            } else {
                commandBar.textColor = keyCharColor
            }
            val wordSpacing = String(repeating = " ", count = (numberOfAnnotations * 7) - (numberOfAnnotations - 1))
            if (invalidState != true) {
                commandBar.text = commandPromptSpacing + wordSpacing + givenWord
            }
            // Check if it's a preposition and pass information to prepositionAnnotation if so.
            val isPrep = prepositions?[wordToCheck.lowercased()] != null
            nounAnnotationsToDisplay = numberOfAnnotations
            if (isPrep) {
                prepAnnotationState = true
            }
        }
    }
}

/**
 * Annotates the command bar with the form of a valid selected noun.
 *
 * @param commandBar The command bar into which an input was entered.
 * @param nounAnnotationDisplay The part of the annotation display that's for nouns.
 * @param annotationDisplay The full annotation display elements.
 */
internal fun selectedNounAnnotation(commandBar: TextView, nounAnnotationDisplay: List<TextView>, annotationDisplay: List<TextView>) {
    val selectedWord = proxy.selectedText ?: ""
    nounAnnotation(commandBar = commandBar, nounAnnotationDisplay = nounAnnotationDisplay, annotationDisplay = annotationDisplay, givenWord = selectedWord)
}

/**
 * Annotates the command bar with the form of a valid typed noun.
 *
 * @param commandBar The command bar into which an input was entered.
 * @param nounAnnotationDisplay The part of the annotation display that's for nouns.
 * @param annotationDisplay The full annotation display elements.
 */
internal fun typedNounAnnotation(commandBar: TextView, nounAnnotationDisplay: List<TextView>, annotationDisplay: List<TextView>) {
    if (proxy.documentContextBeforeInput != null) {
        val wordsTyped = proxy.documentContextBeforeInput!!.components(separatedBy = " ")
        val lastWordTyped = wordsTyped.secondToLast()
        if (lastWordTyped != "") {
            nounAnnotation(commandBar = commandBar, nounAnnotationDisplay = nounAnnotationDisplay, annotationDisplay = annotationDisplay, givenWord = lastWordTyped ?: "")
        }
    }
}

/**
 * Sets the annotation of an preposition annotation element given parameters.
 *
 * @param label The label to change the appearance of to show annotations.
 * @param annotation The annotation to set to the element.
 */
internal fun setPrepAnnotation(label: TextView, annotation: String) {
    var annotationToDisplay: String = annotation
    if (scribeKeyState != true) {
        // Convert annotation into the keyboard language if necessary.
        if (caseAnnotationConversionDict[controllerLanguage] != null) {
            if (caseAnnotationConversionDict[controllerLanguage]?[annotation] != null) {
                annotationToDisplay = caseAnnotationConversionDict[controllerLanguage]?[annotation] ?: ""
            }
        }
        if (DeviceType.isPhone) {
            label.font = .systemFont(ofSize = annotationHeight * 0.65)
        } else if (DeviceType.isPad) {
            label.font = .systemFont(ofSize = annotationHeight * 0.85)
        }
        // Assign color and text to the label.
        label.backgroundColor = keyCharColor
        label.text = annotationToDisplay
    }
}

/**
 * Checks if a word is a preposition and annotates the command bar if so.
 *
 * @param commandBar The command bar into which an input was entered.
 * @param prepAnnotationDisplay The part of the annotation display that's for prepositions.
 * @param givenWord A word that is potentially a preposition.
 */
internal fun prepositionAnnotation(commandBar: TextView, prepAnnotationDisplay: List<TextView>, givenWord: String) {
    // Check to see if the input was uppercase to return an uppercase annotation.
    val wordToCheck = givenWord.lowercased()
    // Check if prepAnnotationState has been passed and reset nounAnnotationsToDisplay if not.
    if (prepAnnotationState == false) {
        nounAnnotationsToDisplay = 0
    }
    val isPreposition = prepositions?[wordToCheck] != null
    if (isPreposition) {
        prepAnnotationState = true
        // Make command bar font larger for annotation.
        if (DeviceType.isPhone) {
            commandBar.font = .systemFont(ofSize = annotationHeight * 0.8)
        } else if (DeviceType.isPad) {
            commandBar.font = .systemFont(ofSize = annotationHeight)
        }
        commandBar.textColor = keyCharColor
        val prepositionCase: String = prepositions?[wordToCheck] as? String ?: return
        var numberOfAnnotations: Int = 0
        var annotationsToAssign: List<String> = listOf<String>()
        if (prepositionCase.size >= 4) { // Would have a slash as they all are three characters long
            annotationsToAssign = (prepositionCase.components(separatedBy = "/"))
            numberOfAnnotations = annotationsToAssign.size
        } else {
            numberOfAnnotations = 1
            annotationsToAssign.append(prepositionCase)
        }
        for (idx in 0 until numberOfAnnotations) {
            setPrepAnnotation(label = prepAnnotationDisplay[idx], annotation = annotationsToAssign[idx])
        }
        // Cancel the state to allow for symbol coloration in selection annotation without calling loadKeys.
        prepAnnotationState = false
        val wordSpacing = String(repeating = " ", count = (nounAnnotationsToDisplay * 7) - (nounAnnotationsToDisplay - 1) + (numberOfAnnotations * 9) - (numberOfAnnotations - 1))
        commandBar.text = commandPromptSpacing + wordSpacing + givenWord
    }
}

/**
 * Annotates the command bar with the form of a valid selected preposition.
 *
 * @param commandBar The command bar into which an input was entered.
 * @param prepAnnotationDisplay The part of the annotation display that's for prepositions.
 */
internal fun selectedPrepAnnotation(commandBar: TextView, prepAnnotationDisplay: List<TextView>) {
    if (languagesWithCaseDependantOnPrepositions.contains(controllerLanguage)) {
        val selectedWord = proxy.selectedText ?: ""
        prepositionAnnotation(commandBar = commandBar, prepAnnotationDisplay = prepAnnotationDisplay, givenWord = selectedWord)
    }
}

/**
 * Annotates the command bar with the form of a valid typed preposition.
 *
 * @param commandBar The command bar into which an input was entered.
 * @param prepAnnotationDisplay The part of the annotation display that's for prepositions.
 */
internal fun typedPrepAnnotation(commandBar: TextView, prepAnnotationDisplay: List<TextView>) {
    if (languagesWithCaseDependantOnPrepositions.contains(controllerLanguage)) {
        if (proxy.documentContextBeforeInput != null) {
            val wordsTyped = proxy.documentContextBeforeInput!!.components(separatedBy = " ")
            val lastWordTyped = wordsTyped.secondToLast()
            if (lastWordTyped != "") {
                prepositionAnnotation(
                    commandBar = commandBar,
                    prepAnnotationDisplay = prepAnnotationDisplay,
                    givenWord = lastWordTyped ?: ""
                )
            }
        }
    }
}
