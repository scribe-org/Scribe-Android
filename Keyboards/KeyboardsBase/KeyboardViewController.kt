/**
 * KeyboardViewController.kt
 *
 * Classes for the parent keyboard view controller that language keyboards inherit and keyboard keys.
 */

/**
 * The parent KeyboardViewController class that is inherited by all Scribe keyboards.
 */
internal class KeyboardViewController: UIInputViewController {
    lateinit internal var keyboardView: View

    // Stack views that are populated with they keyboard rows.
    @BindView() lateinit internal var stackView0: LinearLayout
    @BindView() lateinit internal var stackView1: LinearLayout
    @BindView() lateinit internal var stackView2: LinearLayout
    @BindView() lateinit internal var stackView3: LinearLayout

    /**
     * Changes the keyboard state such that the letters view will be shown.
     */
    internal fun changeKeyboardToLetterKeys() {
        keyboardState = .letters
        loadKeys()
    }

    /**
     * Changes the keyboard state such that the numbers view will be shown.
     */
    internal fun changeKeyboardToNumberKeys() {
        keyboardState = .numbers
        shiftButtonState = .normal
        loadKeys()
    }

    /**
     * Changes the keyboard state such that the symbols view will be shown.
     */
    internal fun changeKeyboardToSymbolKeys() {
        keyboardState = .symbols
        loadKeys()
    }

    //region Display Activation Functions
    /**
     * Function to load the keyboard interface into which keyboardView is instantiated.
     */
    internal fun loadInterface() {
        val keyboardNib = UINib(nibName = "Keyboard", bundle = null)
        keyboardView = keyboardNib.instantiate(withOwner = this, options = null)[0] as? View
        keyboardView.translatesAutoresizingMaskIntoConstraints = true
        view.addSubview(keyboardView)
        // Override keyboards switching to others for translation and prior Scribe commands.
        switchInput = false
        scribeKeyState = false
        commandState = false
        conjugateView = false
        // Set height for Scribe command functionality.
        annotationHeight = nounAnnotation1.frame.size.height
        loadKeys()
    }

    /**
     * Activates a button [btn] by assigning key touch functions for their given actions.
     */
    internal fun activateBtn(btn: Button) {
        btn.addTarget(this, action = #selector(executeKeyActions), for = .touchUpInside)
        btn.addTarget(this, action = #selector(keyTouchDown), for = .touchDown)
        btn.addTarget(this, action = #selector(keyUntouched), for = .touchDragExit)
        btn.isUserInteractionEnabled = true
    }

    /**
     * Deactivates a button [btn] by removing key touch functions for their given actions and making it clear.
     */
    internal fun deactivateBtn(btn: Button) {
        btn.setTitle("", for = .normal)
        btn.backgroundColor = UIColor.clear
        btn.removeTarget(this, action = #selector(executeKeyActions), for = .touchUpInside)
        btn.removeTarget(this, action = #selector(keyTouchDown), for = .touchDown)
        btn.removeTarget(this, action = #selector(keyUntouched), for = .touchDragExit)
        btn.isUserInteractionEnabled = false
    }
    //endregion

    //region Override UIInputViewController Functions
    /**
     * Includes adding custom view sizing constraints.
     */
    internal override fun updateViewConstraints() {
        super.updateViewConstraints()
        checkLandscapeMode()
        if (DeviceType.isPhone) {
            if (isLandscapeView == true) {
                keyboardHeight = 200
            } else {
                keyboardHeight = 270
            }
        } else if (DeviceType.isPad) {
            if (isLandscapeView == true) {
                keyboardHeight = 420
            } else {
                keyboardHeight = 340
            }
        }
        val heightConstraint = NSLayoutConstraint(
            item = view!!,
            attribute = NSLayoutConstraint.Attribute.height,
            relatedBy = NSLayoutConstraint.Relation.equal,
            toItem = null,
            attribute = NSLayoutConstraint.Attribute.notAnAttribute,
            multiplier = 1.0,
            constant = keyboardHeight
        )
        view.addConstraint(heightConstraint)
        keyboardView.frame.size = view.frame.size
    }

    // Button to be assigned as the select keyboard button if necessary.
    @BindView() lateinit internal var selectKeyboardButton: Button

    /**
     * Overrides the loading of the view and includes the following:
     * Assignment of the proxy
     * Loading the Scribe interface
     * Making keys letters
     * Adding the keyboard selector target
     */
    internal override fun viewDidLoad() {
        super.viewDidLoad()
        checkDarkModeSetColors()
        // If alternateKeysView is already added than remove it so it's not colored wrong.
        if (this.view.viewWithTag(1001) != null) {
            val viewWithTag = this.view.viewWithTag(1001)
            viewWithTag?.removeFromSuperview()
            alternatesShapeLayer.removeFromSuperlayer()
        }
        proxy = textDocumentProxy as UITextDocumentProxy
        keyboardState = .letters
        loadInterface()
        this.selectKeyboardButton.addTarget(
            this, action = #selector(handleInputModeList(from:with:)), for = .allTouchEvents
        )
    }

    /**
     * Includes hiding the keyboard selector button if it is not needed for the current device.
     */
    internal override fun viewWillLayoutSubviews() {
        this.selectKeyboardButton.isHidden = !this.needsInputModeSwitchKey
        super.viewWillLayoutSubviews()
    }

    /**
     * Includes updateViewConstraints to change the keyboard height given device type and orientation.
     */
    internal override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        updateViewConstraints()
    }

    /**
     * Overrides the transitioning of the view and includes:
     * updateViewConstraints to change the keyboard height
     * A call to loadKeys to reload the display after an orientation change
     */
    internal override fun viewWillTransition(size: CGSize, coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to = size, with = coordinator)
        updateViewConstraints()
        loadKeys()
    }

    /**
     * Overrides the previous color variables if the user switches between light and dark mode.
     */
    internal override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        checkDarkModeSetColors()
        // If alternateKeysView is already added than remove it so it's not colored wrong.
        if (this.view.viewWithTag(1001) != null) {
            val viewWithTag = this.view.viewWithTag(1001)
            viewWithTag?.removeFromSuperview()
            alternatesShapeLayer.removeFromSuperlayer()
        }
        loadKeys()
    }
    //endregion

    //region Scribe Command Elements
    // The background for the Scribe command elements.
    @BindView() lateinit internal var commandBackground: TextView

    /**
     * Sets the background of the command area to match the keyboard.
     */
    internal fun setCommandBackground() {
        commandBackground.backgroundColor = keyboardBackColor
        commandBackground.isUserInteractionEnabled = false
    }

    // The bar that displays language logic or is typed into for Scribe commands.
    @BindView() lateinit internal var commandBar: CommandBar
    @BindView() lateinit internal var commandBarShadow: Button
    @BindView() lateinit internal var commandBarBlend: TextView

    /**
     * Clears the text found in the command bar.
     */
    internal fun clearCommandBar() {
        if (commandState == false) {
            commandBar.textColor = keyCharColor
            commandBar.text = " "
        }
        // Trigger the removal of the noun or preposition annotations.
        hideAnnotations(annotationDisplay = getAnnotationLabels())
    }

    /**
     * Deletes in the proxy or command bar given the current constraints.
     */
    internal fun handleDeleteButtonPressed() {
        if (commandState != true) {
            proxy.deleteBackward()
        } else if (!(commandState == true && allPrompts.contains(commandBar.text!!))) {
            val inputText = commandBar.text
            if (inputText == null || inputText.isEmpty()) {
                return
            }
            commandBar.text = commandBar.text!!.deletePriorToCursor()
        } else {
            backspaceTimer?.invalidate()
            backspaceTimer = null
        }
    }

    // The button used to display Scribe commands and its shadow.
    @BindView() lateinit internal var scribeKey: ScribeKey
    @BindView() lateinit internal var scribeKeyShadow: Button

    /**
     * Links various UI elements that interact concurrently.
     */
    internal fun linkElements() {
        scribeKey.shadow = scribeKeyShadow
        commandBar.shadow = commandBarShadow
        commandBar.blend = commandBarBlend
    }

    // Buttons used to trigger Scribe command functionality.
    @BindView() lateinit internal var translateKey: Button
    @BindView() lateinit internal var conjugateKey: Button
    @BindView() lateinit internal var pluralKey: Button

    /**
     * Sets up all buttons that are associated with Scribe commands.
     */
    internal fun setCommandBtns() {
        setBtn(btn = translateKey, color = commandKeyColor, name = "Translate", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKey, color = commandKeyColor, name = "Conjugate", canCap = false, isSpecial = false)
        setBtn(btn = pluralKey, color = commandKeyColor, name = "Plural", canCap = false, isSpecial = false)
        activateBtn(btn = translateKey)
        activateBtn(btn = conjugateKey)
        activateBtn(btn = pluralKey)
    }
    @BindView() lateinit internal var conjugateShiftLeft: Button
    @BindView() lateinit internal var conjugateShiftRight: Button

    // Buttons for the conjugation view.
    @BindView() lateinit internal var conjugateKeyFPS: Button
    @BindView() lateinit internal var conjugateKeySPS: Button
    @BindView() lateinit internal var conjugateKeyTPS: Button
    @BindView() lateinit internal var conjugateKeyFPP: Button
    @BindView() lateinit internal var conjugateKeySPP: Button
    @BindView() lateinit internal var conjugateKeyTPP: Button

    /**
     * Returns all buttons for the 3x2 conjugation display.
     */
    internal fun get3x2ConjButtons() : List<Button> {
        val conjugationButtons: List<Button> = listOf(
            conjugateKeyFPS, conjugateKeySPS, conjugateKeyTPS, conjugateKeyFPP, conjugateKeySPP, conjugateKeyTPP
        )
        return conjugationButtons
    }
    @BindView() lateinit internal var conjugateKeyTL: Button
    @BindView() lateinit internal var conjugateKeyTR: Button
    @BindView() lateinit internal var conjugateKeyBL: Button
    @BindView() lateinit internal var conjugateKeyBR: Button

    /**
     * Returns all buttons for the 2x2 conjugation display.
     */
    internal fun get2x2ConjButtons() : List<Button> {
        val conjugationButtons: List<Button> = listOf(conjugateKeyTL, conjugateKeyTR, conjugateKeyBL, conjugateKeyBR)
        return conjugationButtons
    }

    // Labels for the conjugation view buttons.
    // Note that we're using buttons as labels weren't allowing for certain constraints to be set.
    @BindView() lateinit internal var conjugateLblFPS: Button
    @BindView() lateinit internal var conjugateLblSPS: Button
    @BindView() lateinit internal var conjugateLblTPS: Button
    @BindView() lateinit internal var conjugateLblFPP: Button
    @BindView() lateinit internal var conjugateLblSPP: Button
    @BindView() lateinit internal var conjugateLblTPP: Button

    /**
     * Returns all labels for the 3x2 conjugation display.
     */
    internal fun get3x2ConjLabels() : List<Button> {
        val conjugationLabels: List<Button> = listOf(
            conjugateLblFPS, conjugateLblSPS, conjugateLblTPS, conjugateLblFPP, conjugateLblSPP, conjugateLblTPP
        )
        return conjugationLabels
    }
    @BindView() lateinit internal var conjugateLblTL: Button
    @BindView() lateinit internal var conjugateLblTR: Button
    @BindView() lateinit internal var conjugateLblBL: Button
    @BindView() lateinit internal var conjugateLblBR: Button

    /**
     * Returns all labels for the 2x2 conjugation display.
     */
    internal fun get2x2ConjLabels() : List<Button> {
        val conjugationLabels: List<Button> = listOf(conjugateLblTL, conjugateLblTR, conjugateLblBL, conjugateLblBR)
        return conjugationLabels
    }

    /**
     * Sets up all buttons and labels that are associated with the 3x2 conjugation display.
     */
    internal fun setConj3x2View() {
        setBtn(btn = conjugateKeyFPS, color = keyColor, name = "firstPersonSingular", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeySPS, color = keyColor, name = "secondPersonSingular", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeyTPS, color = keyColor, name = "thirdPersonSingular", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeyFPP, color = keyColor, name = "firstPersonPlural", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeySPP, color = keyColor, name = "secondPersonPlural", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeyTPP, color = keyColor, name = "thirdPersonPlural", canCap = false, isSpecial = false)
        for (btn in get3x2ConjButtons()) {
            activateBtn(btn = btn)
        }
        if (DeviceType.isPad) {
            var conjugationFontDivisor = 3.5
            if (isLandscapeView) {
                conjugationFontDivisor = 4
            }
            for (btn in get3x2ConjButtons()) {
                btn.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / conjugationFontDivisor)
            }
        }
    }

    /**
     * Sets up all buttons and labels that are associated with the 2x2 conjugation display.
     */
    internal fun setConj2x2View() {
        setBtn(btn = conjugateKeyTL, color = keyColor, name = "conjugateTopLeft", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeyTR, color = keyColor, name = "conjugateTopRight", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeyBL, color = keyColor, name = "conjugateBottomLeft", canCap = false, isSpecial = false)
        setBtn(btn = conjugateKeyBR, color = keyColor, name = "conjugateBottomRight", canCap = false, isSpecial = false)
        for (btn in get2x2ConjButtons()) {
            activateBtn(btn = btn)
        }
        if (DeviceType.isPad) {
            var conjugationFontDivisor = 3.5
            if (isLandscapeView) {
                conjugationFontDivisor = 4
            }
            for (btn in get2x2ConjButtons()) {
                btn.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / conjugationFontDivisor)
            }
        }
    }

    /**
     * Sets up all buttons and labels for the conjugation view given constraints to determine the dimensions.
     */
    internal fun setConjugationBtns() {
        // Set the conjugation view to 2x2 for Swedish and Russian past tense.
        if (controllerLanguage == "Swedish") {
            conjugateAlternateView = true
        } else if (controllerLanguage == "Russian" && ruConjugationState == .past) {
            conjugateAlternateView = true
        } else {
            conjugateAlternateView = false
        }

        // The base conjugation view is 3x2 for first, second, and third person in singular and plural.
        if (conjugateAlternateView == false) {
            setConj3x2View()
        } else {
            setConj2x2View()
        }

        // Setup the view shift buttons.
        setBtn(
            btn = conjugateShiftLeft, color = keyColor, name = "shiftConjugateLeft", canCap = false, isSpecial = false
        )
        setBtn(
            btn = conjugateShiftRight, color = keyColor, name = "shiftConjugateRight", canCap = false, isSpecial = false
        )
        activateBtn(btn = conjugateShiftLeft)
        activateBtn(btn = conjugateShiftRight)

        // Make all labels clear and set their font for if they will be used.
        val allConjLabels: List<Button> = get3x2ConjLabels() + get2x2ConjLabels()
        for (lbl in allConjLabels) {
            lbl.backgroundColor = UIColor.clear
            lbl.setTitleColor(specialKeyColor, for = .normal)
            lbl.isUserInteractionEnabled = false
            if (DeviceType.isPad) {
                lbl.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 4)
            }
        }
    }

    /**
     * Activates all buttons that are associated with the conjugation display.
     */
    internal fun activateConjugationDisplay() {
        activateBtn(btn = conjugateShiftLeft)
        activateBtn(btn = conjugateShiftRight)
        if (conjugateAlternateView == false) {
            for (elem in get3x2ConjButtons()) {
                activateBtn(btn = elem)
            }
            for (elem in get2x2ConjButtons()) {
                deactivateBtn(btn = elem)
            }
        }
        if (conjugateAlternateView == true) {
            for (elem in get3x2ConjButtons()) {
                deactivateBtn(btn = elem)
            }
            for (elem in get2x2ConjButtons()) {
                activateBtn(btn = elem)
            }
        }
    }

    /**
     * Deactivates all buttons that are associated with the conjugation display.
     */
    internal fun deactivateConjugationDisplay() {
        deactivateBtn(btn = conjugateShiftLeft)
        conjugateShiftLeft.tintColor = UIColor.clear
        deactivateBtn(btn = conjugateShiftRight)
        conjugateShiftRight.tintColor = UIColor.clear
        val allConjLabels: List<Button> = get3x2ConjLabels() + get2x2ConjLabels()
        val allConjElements: List<Button> = get3x2ConjButtons() + get2x2ConjButtons() + allConjLabels
        for (elem in allConjElements) {
            deactivateBtn(btn = elem)
        }
        for (lbl in allConjLabels) {
            lbl.setTitle("", for = .normal)
        }
    }

    /**
     * Assign the conjugations that will be selectable in the conjugation display.
     */
    internal fun assignConjStates() {
        var conjugationStateFxn: () -> String = deGetConjugationState
        if (controllerLanguage != "Swedish") {
            conjugationStateFxn = keyboardConjStateDict[controllerLanguage] as () -> String
        }
        if (!listOf("Russian", "Swedish").contains(controllerLanguage)) {
            tenseFPS = conjugationStateFxn() + "FPS"
            tenseSPS = conjugationStateFxn() + "SPS"
            tenseTPS = conjugationStateFxn() + "TPS"
            tenseFPP = conjugationStateFxn() + "FPP"
            tenseSPP = conjugationStateFxn() + "SPP"
            tenseTPP = conjugationStateFxn() + "TPP"
        } else if (controllerLanguage == "Russian") {
            if (conjugateAlternateView == false) {
                tenseFPS = ruGetConjugationState() + "FPS"
                tenseSPS = ruGetConjugationState() + "SPS"
                tenseTPS = ruGetConjugationState() + "TPS"
                tenseFPP = ruGetConjugationState() + "FPP"
                tenseSPP = ruGetConjugationState() + "SPP"
                tenseTPP = ruGetConjugationState() + "TPP"
            } else {
                tenseTopLeft = "pastMasculine"
                tenseTopRight = "pastFeminine"
                tenseBottomLeft = "pastNeutral"
                tenseBottomRight = "pastPlural"
            }
        } else if (controllerLanguage == "Swedish") {
            val swedishTenses = svGetConjugationState()
            tenseTopLeft = swedishTenses[0]
            tenseTopRight = swedishTenses[1]
            tenseBottomLeft = swedishTenses[2]
            tenseBottomRight = swedishTenses[3]
        }
    }

    /**
     * Sets the label of the conjugation state and assigns the current tenses that are accessed to label the buttons.
     */
    internal fun setConjugationState() {
        // Assign the conjugations that will be selectable.
        assignConjStates()
        // Set the view title and its labels.
        var conjugationTitleFxn: () -> String = deGetConjugationTitle
        var conjugationLabelsFxn: () -> Unit = deSetConjugationLabels
        if (controllerLanguage != "Swedish") {
            conjugationTitleFxn = keyboardConjTitleDict[controllerLanguage] as () -> String
            conjugationLabelsFxn = keyboardConjLabelDict[controllerLanguage] as () -> Unit
        }
        if (!listOf("Russian", "Swedish").contains(controllerLanguage)) {
            commandBar.text = conjugationTitleFxn()
            conjugationLabelsFxn()
        } else if (controllerLanguage == "Russian") {
            commandBar.text = ruGetConjugationTitle()
            ruSetConjugationLabels()
        } else if (controllerLanguage == "Swedish") {
            commandBar.text = svGetConjugationTitle()
            svSetConjugationLabels()
        }

        // Assign labels that have been set by SetConjugationLabels functions.
        conjugateLblFPS.setTitle("  " + labelFPS, for = .normal)
        conjugateLblSPS.setTitle("  " + labelSPS, for = .normal)
        conjugateLblTPS.setTitle("  " + labelTPS, for = .normal)
        conjugateLblFPP.setTitle("  " + labelFPP, for = .normal)
        conjugateLblSPP.setTitle("  " + labelSPP, for = .normal)
        conjugateLblTPP.setTitle("  " + labelTPP, for = .normal)
        conjugateLblTL.setTitle("  " + labelTopLeft, for = .normal)
        conjugateLblTR.setTitle("  " + labelTopRight, for = .normal)
        conjugateLblBL.setTitle("  " + labelBottomLeft, for = .normal)
        conjugateLblBR.setTitle("  " + labelBottomRight, for = .normal)
        if (conjugateAlternateView == false) {
            allTenses = listOf(tenseFPS, tenseSPS, tenseTPS, tenseFPP, tenseSPP, tenseTPP)
            allConjugationBtns = get3x2ConjButtons()
        } else {
            allTenses = listOf(tenseTopLeft, tenseTopRight, tenseBottomLeft, tenseBottomRight)
            allConjugationBtns = get2x2ConjButtons()
        }

        // Populate conjugation view buttons.
        for (index in 0 until allTenses.size) {
            if (verbs?[verbToConjugate]!![allTenses[index]] as? String == "") {
                // Assign the invalid message if the conjugation isn't present in the directory.
                styleBtn(btn = allConjugationBtns[index], title = invalidCommandMsg, radius = keyCornerRadius)
            } else {
                conjugationToDisplay = verbs?[verbToConjugate]!![allTenses[index]] as String
                if (inputWordIsCapitalized && deConjugationState != .indicativePerfect) {
                    conjugationToDisplay = conjugationToDisplay.capitalized
                }
                styleBtn(btn = allConjugationBtns[index], title = conjugationToDisplay, radius = keyCornerRadius)
            }
        }
    }

    // Labels to annotate noun genders.
    @BindView() lateinit internal var nounAnnotation1: TextView
    @BindView() lateinit internal var nounAnnotation2: TextView
    @BindView() lateinit internal var nounAnnotation3: TextView
    @BindView() lateinit internal var nounAnnotation4: TextView
    @BindView() lateinit internal var nounAnnotation5: TextView

    // Labels to annotate preposition cases.
    // There are multiple versions to account for when a word is both a noun and a preposition.
    // In this case a shifted set is used - the noun annotations precede those of prepositions.
    @BindView() lateinit internal var prepAnnotation11: TextView
    @BindView() lateinit internal var prepAnnotation12: TextView
    @BindView() lateinit internal var prepAnnotation13: TextView
    @BindView() lateinit internal var prepAnnotation14: TextView
    @BindView() lateinit internal var prepAnnotation21: TextView
    @BindView() lateinit internal var prepAnnotation22: TextView
    @BindView() lateinit internal var prepAnnotation23: TextView
    @BindView() lateinit internal var prepAnnotation24: TextView
    @BindView() lateinit internal var prepAnnotation31: TextView
    @BindView() lateinit internal var prepAnnotation32: TextView
    @BindView() lateinit internal var prepAnnotation33: TextView
    @BindView() lateinit internal var prepAnnotation34: TextView

    /**
     * Returns all available annotation labels.
     */
    internal fun getAnnotationLabels() : List<TextView> {
        val nounAnnotationDisplay: List<TextView> = listOf(
            nounAnnotation1, nounAnnotation2, nounAnnotation3, nounAnnotation4, nounAnnotation5
        )
        val prepAnnotationDisplay: List<TextView> = listOf(
            prepAnnotation11, prepAnnotation12, prepAnnotation13, prepAnnotation14,
            prepAnnotation21, prepAnnotation22, prepAnnotation23, prepAnnotation24,
            prepAnnotation31, prepAnnotation32, prepAnnotation33, prepAnnotation34
        )
        return nounAnnotationDisplay + prepAnnotationDisplay
    }

    /**
     * Returns the noun annotation labels.
     */
    internal fun getNounAnnotationLabels() : List<TextView> {
        val nounAnnotationDisplay: List<TextView> = listOf(
            nounAnnotation1, nounAnnotation2, nounAnnotation3, nounAnnotation4, nounAnnotation5
        )
        return nounAnnotationDisplay
    }

    /**
     * Returns the preposition annotation labels given the current number of noun annotations displayed.
     */
    internal fun getPrepAnnotationLabels() : List<TextView> {
        // Initialize an array of display elements and count how many will be changed.
        // This is initialized based on how many noun annotations have already been assigned (max 2).
        var prepAnnotationDisplay: List<TextView> = listOf<TextView>()
        if (nounAnnotationsToDisplay == 0) {
            prepAnnotationDisplay = listOf(prepAnnotation11, prepAnnotation12, prepAnnotation13, prepAnnotation14)
        } else if (nounAnnotationsToDisplay == 1) {
            prepAnnotationDisplay = listOf(prepAnnotation21, prepAnnotation22, prepAnnotation23, prepAnnotation24)
        } else if (nounAnnotationsToDisplay == 2) {
            prepAnnotationDisplay = listOf(prepAnnotation31, prepAnnotation32, prepAnnotation33, prepAnnotation34)
        }
        return prepAnnotationDisplay
    }

    /**
     * Styles the labels within the annotation display and removes user interactions.
     */
    internal fun styleAnnotations() {
        val nounAnnotationDisplay = getNounAnnotationLabels()
        val prepAnnotationDisplay: List<TextView> = listOf(
            prepAnnotation11, prepAnnotation12, prepAnnotation13, prepAnnotation14,
            prepAnnotation21, prepAnnotation22, prepAnnotation23, prepAnnotation24,
            prepAnnotation31, prepAnnotation32, prepAnnotation33, prepAnnotation34
        )
        for (annotationDisplay in nounAnnotationDisplay) {
            annotationDisplay.clipsToBounds = true
            annotationDisplay.layer.cornerRadius = keyCornerRadius / 2
            annotationDisplay.textAlignment = NSTextAlignment.center
            annotationDisplay.isUserInteractionEnabled = false
            annotationDisplay.font = .systemFont(ofSize = annotationHeight * 0.70)
            annotationDisplay.textColor = commandBarColor
        }
        for (annotationDisplay in prepAnnotationDisplay) {
            annotationDisplay.clipsToBounds = true
            annotationDisplay.layer.cornerRadius = keyCornerRadius / 2
            annotationDisplay.textAlignment = NSTextAlignment.center
            annotationDisplay.isUserInteractionEnabled = false
            annotationDisplay.font = .systemFont(ofSize = annotationHeight * 0.65)
            annotationDisplay.textColor = commandBarColor
        }
    }
    //endregion

    //region Load keys
    /**
     * Loads the keys given the current constraints.
     */
    internal fun loadKeys() {
        // The name of the language keyboard that's referencing KeyboardViewController.
        controllerLanguage = classForCoder.description().components(separatedBy = ".KeyboardViewController")[0]
        setCommandBackground()
        setKeyboard()
        linkElements()
        setCommandBtns()
        setConjugationBtns()
        invalidState = false
        val specialKeys = listOf(
            "shift", "delete", "ABC", "123", "#+=", "selectKeyboard", "space", "return", ".?123", "hideKeyboard"
        )
        allNonSpecialKeys = allKeys.filter { !specialKeys.contains(it) }
        // Clear interface from the last state.
        keyboardKeys.forEach { it.removeFromSuperview() }
        paddingViews.forEach { it.removeFromSuperview() }
        keyboardView.backgroundColor? = keyboardBackColor
        // keyWidth determined per keyboard by the top row.
        if (isLandscapeView == true) {
            if (DeviceType.isPhone) {
                letterKeyWidth = (UIScreen.main.bounds.height - 5) / CGFloat(letterKeys[0].size) * 1.5
                numSymKeyWidth = (UIScreen.main.bounds.height - 5) / CGFloat(numberKeys[0].size) * 1.5
            } else if (DeviceType.isPad) {
                letterKeyWidth = (UIScreen.main.bounds.height - 5) / CGFloat(letterKeys[0].size) * 1.2
                numSymKeyWidth = (UIScreen.main.bounds.height - 5) / CGFloat(numberKeys[0].size) * 1.2
            }
        } else {
            letterKeyWidth = (UIScreen.main.bounds.width - 6) / CGFloat(letterKeys[0].size) * 0.9
            numSymKeyWidth = (UIScreen.main.bounds.width - 6) / CGFloat(numberKeys[0].size) * 0.9
        }

        // Derive keyboard given current states and set widths.
        when (keyboardState) {
            letters -> {
                keyboard = letterKeys
                keyWidth = letterKeyWidth
                // Auto-capitalization if the cursor is at the start of the proxy.
                if (proxy.documentContextBeforeInput?.size == 0) {
                    shiftButtonState = .shift
                }
            }
            numbers -> {
                keyboard = numberKeys
                keyWidth = numSymKeyWidth
            }
            symbols -> {
                keyboard = symbolKeys
                keyWidth = numSymKeyWidth
            }
        }

        // Derive corner radii.
        if (DeviceType.isPhone) {
            if (isLandscapeView == true) {
                keyCornerRadius = keyWidth / 9
                commandKeyCornerRadius = keyWidth / 5
            } else {
                keyCornerRadius = keyWidth / 6
                commandKeyCornerRadius = keyWidth / 3
            }
        } else if (DeviceType.isPad) {
            if (isLandscapeView == true) {
                keyCornerRadius = keyWidth / 12
                commandKeyCornerRadius = keyWidth / 7.5
            } else {
                keyCornerRadius = keyWidth / 9
                commandKeyCornerRadius = keyWidth / 5
            }
        }
        styleAnnotations()
        if (annotationState == false) {
            hideAnnotations(annotationDisplay = getAnnotationLabels())
        }
        if (!conjugateView) {
            // normal keyboard view
            for (view in listOf(stackView0, stackView1, stackView2, stackView3)) {
                view?.isUserInteractionEnabled = true
                view?.isLayoutMarginsRelativeArrangement = true
                // Set edge insets for stack views to provide vertical key spacing.
                if (view == stackView0) {
                    view?.layoutMargins = UIEdgeInsets(top = 3, left = 0, bottom = 8, right = 0)
                } else if (view == stackView1) {
                    view?.layoutMargins = UIEdgeInsets(top = 5, left = 0, bottom = 6, right = 0)
                } else if (view == stackView2) {
                    view?.layoutMargins = UIEdgeInsets(top = 5, left = 0, bottom = 6, right = 0)
                } else if (view == stackView3) {
                    view?.layoutMargins = UIEdgeInsets(top = 6, left = 0, bottom = 5, right = 0)
                }
            }

            // Set up and activate Scribe key and other command elements.
            scribeKey.set()
            activateBtn(btn = scribeKey)
            styleBtn(btn = scribeKey, title = "Scribe", radius = commandKeyCornerRadius)
            scribeKey.setTitle("", for = .normal)
            scribeKey.setLeftCornerRadius()
            scribeKey.setShadow()
            commandBar.set()
            deactivateConjugationDisplay()
            if (scribeKeyState) {
                scribeKey.toEscape()
                scribeKey.setFullCornerRadius()
                scribeKey.setEscShadow()
                commandBar.hide()
                styleBtn(btn = translateKey, title = translateKeyLbl, radius = commandKeyCornerRadius)
                styleBtn(btn = conjugateKey, title = conjugateKeyLbl, radius = commandKeyCornerRadius)
                styleBtn(btn = pluralKey, title = pluralKeyLbl, radius = commandKeyCornerRadius)
                if (DeviceType.isPhone) {
                    translateKey.titleLabel?.font = .systemFont(ofSize = annotationHeight * 0.65)
                    conjugateKey.titleLabel?.font = .systemFont(ofSize = annotationHeight * 0.65)
                    pluralKey.titleLabel?.font = .systemFont(ofSize = annotationHeight * 0.65)
                } else if (DeviceType.isPad) {
                    translateKey.titleLabel?.font = .systemFont(ofSize = annotationHeight * 0.9)
                    conjugateKey.titleLabel?.font = .systemFont(ofSize = annotationHeight * 0.9)
                    pluralKey.titleLabel?.font = .systemFont(ofSize = annotationHeight * 0.9)
                }
            } else {
                if (commandState == true) {
                    scribeKey.toEscape()
                }
                deactivateBtn(btn = conjugateKey)
                deactivateBtn(btn = translateKey)
                deactivateBtn(btn = pluralKey)
                commandBar.setCornerRadiusAndShadow()
                if (commandState == false) {
                    commandBar.text = ""
                }
                commandBar.sizeToFit()
            }
            val numRows = keyboard.size
            for (row in 0 .. numRows - 1) {
                for (idx in 0 .. keyboard[row].size - 1) {
                    // Set up button as a key with its values and properties.
                    val btn = KeyboardKey(type = .custom)
                    btn.row = row
                    btn.idx = idx
                    btn.style()
                    btn.setChar()
                    btn.setCharSize()
                    val key: String = btn.key
                    // Pad before key is added.
                    var leftPadding = CGFloat(0)
                    if (DeviceType.isPhone
                        && key == "y"
                        && listOf("German", "Swedish").contains(controllerLanguage)
                        && switchInput != true
                    ) {
                        leftPadding = keyWidth / 3
                        addPadding(to = stackView2, width = leftPadding, key = "y")
                    }
                    if (DeviceType.isPhone
                        && key == "a"
                        && (controllerLanguage == "Portuguese" || controllerLanguage == "Italian" || switchInput == true)
                    ) {
                        leftPadding = keyWidth / 4
                        addPadding(to = stackView1, width = leftPadding, key = "a")
                    }
                    if (DeviceType.isPad
                        && key == "a"
                        && (controllerLanguage == "Portuguese" || controllerLanguage == "Italian" || switchInput == true)
                    ) {
                        leftPadding = keyWidth / 3
                        addPadding(to = stackView1, width = leftPadding, key = "a")
                    }
                    if (DeviceType.isPad
                        && key == "@"
                        && (controllerLanguage == "Portuguese" || controllerLanguage == "Italian" || switchInput == true)
                    ) {
                        leftPadding = keyWidth / 3
                        addPadding(to = stackView1, width = leftPadding, key = "@")
                    }
                    if (DeviceType.isPad && key == "$" && controllerLanguage == "Italian") {
                        leftPadding = keyWidth / 3
                        addPadding(to = stackView1, width = leftPadding, key = "$")
                    }
                    if (DeviceType.isPad && key == "€" && (controllerLanguage == "Portuguese" || switchInput == true)) {
                        leftPadding = keyWidth / 3
                        addPadding(to = stackView1, width = leftPadding, key = "€")
                    }
                    keyboardKeys.append(btn)
                    when (row) {
                        0 -> stackView0.addArrangedSubview(btn)
                        1 -> stackView1.addArrangedSubview(btn)
                        2 -> stackView2.addArrangedSubview(btn)
                        3 -> stackView3.addArrangedSubview(btn)
                        else -> break
                    }

                    // Special key styling.
                    if (key == "delete") {
                        val longPressRecognizer = UILongPressGestureRecognizer(
                            target = this, action = #selector(keyLongPressed(_:))
                        )
                        btn.addGestureRecognizer(longPressRecognizer)
                    }
                    if (key == "selectKeyboard") {
                        selectKeyboardButton = btn
                        this.selectKeyboardButton.addTarget(
                            this, action = #selector(handleInputModeList(from:with:)), for = .allTouchEvents
                        )
                        styleIconBtn(btn = btn, color = keyCharColor, iconName = "globe")
                    }
                    if (key == "hideKeyboard") {
                        styleIconBtn(btn = btn, color = keyCharColor, iconName = "keyboard.chevron.compact.down")
                    }
                    if (key == "shift") {
                        styleIconBtn(btn = btn, color = keyCharColor, iconName = "shift")
                    }
                    if (key == "return") {
                        styleIconBtn(btn = btn, color = keyCharColor, iconName = "arrow.turn.down.left")
                    }
                    if (key == "delete") {
                        styleIconBtn(btn = btn, color = keyCharColor, iconName = "delete.left")
                    }

                    // Setting key pop functionality.
                    val keyHoldPop = UILongPressGestureRecognizer(
                        target = this, action = #selector(genHoldPopUpView(sender:))
                    )
                    keyHoldPop.minimumPressDuration = 0.125
                    if (allNonSpecialKeys.contains(key)) {
                        btn.addTarget(this, action = #selector(genPopUpView), for = .touchDown)
                        btn.addGestureRecognizer(keyHoldPop)
                    }

                    // Pad after key is added.
                    var rightPadding = CGFloat(0)
                    if (DeviceType.isPhone
                        && key == "m"
                        && listOf("German", "Swedish").contains(controllerLanguage)
                        && switchInput != true
                    ) {
                        rightPadding = keyWidth / 3
                        addPadding(to = stackView2, width = rightPadding, key = "m")
                    }
                    if (DeviceType.isPhone
                        && key == "l"
                        && (controllerLanguage == "Portuguese" || controllerLanguage == "Italian" || switchInput == true)
                    ) {
                        rightPadding = keyWidth / 4
                        addPadding(to = stackView1, width = rightPadding, key = "l")
                    }

                    // Set the width of the key given device and the given key.
                    btn.adjustKeyWidth()

                    // Extend button touch areas.
                    var widthOfSpacing = CGFloat(0)
                    if (keyboardState == .letters) {
                        widthOfSpacing = (
                            (UIScreen.main.bounds.width - 6.0)
                            - (CGFloat(letterKeys[0].size) * keyWidth))
                            / (CGFloat(letterKeys[0].size) - 1.0
                        )
                    } else {
                        widthOfSpacing = (
                            (UIScreen.main.bounds.width - 6.0)
                            - (CGFloat(numberKeys[0].size) * numSymKeyWidth))
                            / (CGFloat(letterKeys[0].size) - 1.0
                        )
                    }
                    when (row) {
                        0 -> {
                            btn.topShift = -5
                            btn.bottomShift = -6
                        }
                        1 -> {
                            btn.topShift = -6
                            btn.bottomShift = -6
                        }
                        2 -> {
                            btn.topShift = -6
                            btn.bottomShift = -6
                        }
                        3 -> {
                            btn.topShift = -6
                            btn.bottomShift = -5
                        }
                        else -> break
                    }

                    // Pad left and right based on if the button has been shifted.
                    if (leftPadding == CGFloat(0)) {
                        btn.leftShift = -(widthOfSpacing / 2)
                    } else {
                        btn.leftShift = -(leftPadding)
                    }
                    if (rightPadding == CGFloat(0)) {
                        btn.rightShift = -(widthOfSpacing / 2)
                    } else {
                        btn.rightShift = -(rightPadding)
                    }

                    // Activate keyboard interface buttons.
                    activateBtn(btn = btn)
                    if (key == "shift" || key == spaceBar) {
                        btn.addTarget(this, action = #selector(keyMultiPress(_:event:)), for = .touchDownRepeat)
                    }
                }
            }

            // End padding.
            when (keyboardState) {
                letters -> break
                numbers -> break
                symbols -> break
            }
        } else {
            // Load conjugation view.
            for (view in listOf(stackView0, stackView1, stackView2, stackView3)) {
                view?.isUserInteractionEnabled = false
            }
            scribeKey.toEscape()
            scribeKey.setLeftCornerRadius()
            commandBar.backgroundColor = commandBarColor
            commandBarBlend.backgroundColor = commandBarColor
            commandBar.textColor = keyCharColor
            deactivateBtn(btn = conjugateKey)
            deactivateBtn(btn = translateKey)
            deactivateBtn(btn = pluralKey)
            activateConjugationDisplay()
            setConjugationState()
            styleBtn(btn = conjugateShiftLeft, title = "", radius = keyCornerRadius)
            styleIconBtn(btn = conjugateShiftLeft, color = keyCharColor, iconName = "chevron.left")
            styleBtn(btn = conjugateShiftRight, title = "", radius = keyCornerRadius)
            styleIconBtn(btn = conjugateShiftRight, color = keyCharColor, iconName = "chevron.right")
        }
    }
    //endregion

    //region Button Actions
    /**
     * Triggers actions based on the press of a key [sender].
     */
    @OnClick() internal fun executeKeyActions(sender: Button) {
        val originalKey = sender.layer.value(forKey = "original") as? String
        val keyToDisplay = sender.layer.value(forKey = "keyToDisplay") as? String
        if (originalKey == null || keyToDisplay == null) {
            return
        }
        val isSpecial = sender.layer.value(forKey = "isSpecial") as? Boolean ?: return
        sender.backgroundColor = if (isSpecial) specialKeyColor else keyColor

        // Disable the possibility of a double shift call.
        if (originalKey != "shift") {
            capsLockPossible = false
        }

        // Disable the possibility of a double space period call.
        if (originalKey != spaceBar) {
            doubleSpacePeriodPossible = false
        }

        // Reset the Russian verbs view after a selection.
        ruConjugationState = .present
        when (originalKey) {
            "Scribe" -> {
                if (proxy.selectedText != null && commandState != true) {
                    // annotate word
                    if (scribeKeyState) {
                        scribeKeyState = false
                    }
                    loadKeys()
                    selectedNounAnnotation(
                        commandBar = commandBar,
                        nounAnnotationDisplay = getNounAnnotationLabels(),
                        annotationDisplay = getAnnotationLabels()
                    )
                    selectedPrepAnnotation(commandBar = commandBar, prepAnnotationDisplay = getPrepAnnotationLabels())
                } else {
                    if (commandState == true) {
                        // escape
                        scribeKeyState = false
                        commandState = false
                        getTranslation = false
                        getConjugation = false
                        getPlural = false
                        switchInput = false
                    } else if (scribeKeyState == false && conjugateView != true) {
                        // ScribeKey
                        scribeKeyState = true
                        activateBtn(btn = translateKey)
                        activateBtn(btn = conjugateKey)
                        activateBtn(btn = pluralKey)
                    } else {
                        // escape
                        conjugateView = false
                        scribeKeyState = false
                        commandState = false
                        getTranslation = false
                        getConjugation = false
                        getPlural = false
                        switchInput = false
                    }
                    loadKeys()
                }
            }

            // Switch to translate state.
            "Translate" -> {
                scribeKeyState = false
                commandState = true
                getTranslation = true
                switchInput = true

                // Always start in letters with a new keyboard.
                keyboardState = .letters
                loadKeys()
                commandBar.text = translatePromptAndCursor
            }

            // Switch to conjugate state.
            "Conjugate" -> {
                scribeKeyState = false
                commandState = true
                getConjugation = true
                loadKeys()
                commandBar.text = conjugatePromptAndCursor
            }

            // Switch to plural state.
            "Plural" -> {
                scribeKeyState = false
                if (controllerLanguage == "German") { // capitalize for nouns
                    if (shiftButtonState == .normal) {
                        shiftButtonState = .shift
                    }
                }
                commandState = true
                getPlural = true
                loadKeys()
                commandBar.text = pluralPromptAndCursor
            }

            // Move displayed conjugations to the left in order if able.
            "shiftConjugateLeft" -> {
                if (controllerLanguage == "French") {
                    frConjugationStateLeft()
                } else if (controllerLanguage == "German") {
                    deConjugationStateLeft()
                } else if (controllerLanguage == "Italian") {
                    itConjugationStateLeft()
                } else if (controllerLanguage == "Portuguese") {
                    ptConjugationStateLeft()
                } else if (controllerLanguage == "Russian") {
                    ruConjugationStateLeft()
                } else if (controllerLanguage == "Spanish") {
                    esConjugationStateLeft()
                } else if (controllerLanguage == "Swedish") {
                    svConjugationStateLeft()
                }
                loadKeys()
            }

            // Move displayed conjugations to the right in order if able.
            "shiftConjugateRight" -> {
                if (controllerLanguage == "French") {
                    frConjugationStateRight()
                } else if (controllerLanguage == "German") {
                    deConjugationStateRight()
                } else if (controllerLanguage == "Italian") {
                    itConjugationStateRight()
                } else if (controllerLanguage == "Portuguese") {
                    ptConjugationStateRight()
                } else if (controllerLanguage == "Russian") {
                    ruConjugationStateRight()
                } else if (controllerLanguage == "Spanish") {
                    esConjugationStateRight()
                } else if (controllerLanguage == "Swedish") {
                    svConjugationStateRight()
                }
                loadKeys()
            }
            "firstPersonSingular" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseFPS)
                loadKeys()
            }
            "secondPersonSingular" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseSPS)
                loadKeys()
            }
            "thirdPersonSingular" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseTPS)
                loadKeys()
            }
            "firstPersonPlural" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseFPP)
                loadKeys()
            }
            "secondPersonPlural" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseSPP)
                loadKeys()
            }
            "thirdPersonPlural" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseTPP)
                loadKeys()
            }
            "conjugateTopLeft" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseTopLeft)
                loadKeys()
            }
            "conjugateTopRight" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseTopRight)
                loadKeys()
            }
            "conjugateBottomLeft" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseBottomLeft)
                loadKeys()
            }
            "conjugateBottomRight" -> {
                returnConjugation(keyPressed = sender, requestedTense = tenseBottomRight)
                loadKeys()
            }
            "delete" -> {
                if (shiftButtonState == .shift) {
                    shiftButtonState = .normal
                    loadKeys()
                }

                // Prevent the command state prompt from being deleted.
                if (commandState == true && allPrompts.contains((commandBar?.text!!)!!)) {
                    shiftButtonState = .shift
                    // Auto-capitalization
                    loadKeys()
                    return
                }
                handleDeleteButtonPressed()

                // Auto-capitalization if delete goes to the start of the proxy.
                if (proxy.documentContextBeforeInput == null && commandState != true) {
                    if (keyboardState == .letters && shiftButtonState == .normal) {
                        shiftButtonState = .shift
                        loadKeys()
                    }
                }
                clearCommandBar()
            }
            spaceBar -> {
                if (commandState != true) {
                    proxy.insertText(" ")
                    if (listOf(". ", "? ", "! ").contains(proxy.documentContextBeforeInput?.suffix(2))) {
                        shiftButtonState = .shift
                    }
                    if (keyboardState != .letters) {
                        changeKeyboardToLetterKeys()
                    }
                } else {
                    commandBar.text!! = (commandBar?.text!!?.insertPriorToCursor(char = " "))!!
                    if (listOf(
                        ". " + commandCursor, "? " + commandCursor, "! " + commandCursor
                        ).contains(String(commandBar.text!!.suffix(3)))) {
                        shiftButtonState = .shift
                    }
                    if (keyboardState != .letters) {
                        changeKeyboardToLetterKeys()
                    }
                }

                // Prevent annotations from being triggered during commands.
                if (getConjugation == false && getTranslation == false) {
                    typedNounAnnotation(
                        commandBar = commandBar,
                        nounAnnotationDisplay = getNounAnnotationLabels(),
                        annotationDisplay = getAnnotationLabels()
                    )
                    typedPrepAnnotation(commandBar = commandBar, prepAnnotationDisplay = getPrepAnnotationLabels())
                    annotationState = false
                    prepAnnotationState = false
                    nounAnnotationsToDisplay = 0
                }
                if (proxy.documentContextBeforeInput?.suffix("  ".size) == "  ") {
                    clearCommandBar()
                }
                doubleSpacePeriodPossible = true
            }
            "selectKeyboard" -> this.advanceToNextInputMode()
            "hideKeyboard" -> this.dismissKeyboard()
            "return" -> {
                if (getTranslation && commandState == true) { // translate state
                    queryTranslation(commandBar = commandBar)
                    getTranslation = false
                    switchInput = false
                }

                if (getConjugation && commandState == true) { // conjugate state
                    // Reset to the most basic conjugations.
                    deConjugationState = .indicativePresent
                    val triggerConjugationState = triggerConjugation(commandBar = commandBar)
                    if (triggerConjugationState == true) {
                        conjugateView = true
                        loadKeys()
                    } else {
                        invalidState = true
                    }
                    getConjugation = false
                }

                if (getPlural && commandState == true) { // plural state
                    queryPlural(commandBar = commandBar)
                    getPlural = false
                }

                if (commandState == false) { // normal return button
                    proxy.insertText("\n")
                    clearCommandBar()
                } else if (invalidState == true) { // invalid state
                    commandState = false

                    // Return to the original input method if it has been switched away from.
                    if (switchInput == true) {
                        switchInput = false
                        loadKeys()
                    }
                    autoCapAtStartOfProxy()

                    if (isAlreadyPluralState != true) {
                        commandBar.text = commandPromptSpacing + invalidCommandMsg
                    }
                    commandBar.textColor = keyCharColor
                    invalidState = false
                    isAlreadyPluralState = false
                } else {
                    commandState = false
                    clearCommandBar()
                    autoCapAtStartOfProxy()
                    loadKeys()
                    // Avoid showing noun annotation instead of conjugation state header.
                    if (conjugateView == false) {
                        typedNounAnnotation(
                            commandBar = commandBar,
                            nounAnnotationDisplay = getNounAnnotationLabels(),
                            annotationDisplay = getAnnotationLabels()
                        )
                        typedPrepAnnotation(commandBar = commandBar, prepAnnotationDisplay = getPrepAnnotationLabels())
                        annotationState = false
                        prepAnnotationState = false
                        nounAnnotationsToDisplay = 0
                    }
                }
            }

            "123" -> {
                changeKeyboardToNumberKeys()
                clearCommandBar()
            }

            ".?123" -> {
                changeKeyboardToNumberKeys()
                clearCommandBar()
            }

            "#+=" -> {
                changeKeyboardToSymbolKeys()
                clearCommandBar()
            }

            "ABC" -> {
                changeKeyboardToLetterKeys()
                clearCommandBar()
                autoCapAtStartOfProxy()
            }

            "АБВ" -> {
                changeKeyboardToLetterKeys()
                clearCommandBar()
                autoCapAtStartOfProxy()
            }

            "'" -> {
                // Change back to letter keys.
                if (commandState != true) {
                    proxy.insertText("'")
                } else {
                    commandBar.text!! = (commandBar.text!!.insertPriorToCursor(char = "'"))
                }
                changeKeyboardToLetterKeys()
                clearCommandBar()
            }

            "shift" -> {
                shiftButtonState = if (shiftButtonState == .normal) .shift else .normal
                loadKeys()
                clearCommandBar()
                capsLockPossible = true
            }

            else -> if (shiftButtonState == .shift) {
                shiftButtonState = .normal
                loadKeys()
            }

            if (commandState == false) {
                proxy.insertText(keyToDisplay)
                clearCommandBar()
            } else {
                commandBar.text = commandBar.text!!.insertPriorToCursor(char = keyToDisplay)
            }
        }

        // Remove alternates view if it's present.
        if (this.view.viewWithTag(1001) != null) {
            val viewWithTag = this.view.viewWithTag(1001)
            viewWithTag?.removeFromSuperview()
            alternatesShapeLayer.removeFromSuperlayer()
        }
    }
    //endregion

    //region Key press functions
    /**
     * Auto-capitalization if the cursor is at the start of the proxy.
     */
    internal fun autoCapAtStartOfProxy() {
        proxy.insertText(" ")
        if (proxy.documentContextBeforeInput == " ") {
            if (shiftButtonState == .normal) {
                shiftButtonState = .shift
                loadKeys()
            }
        }
        proxy.deleteBackward()
    }

    /**
     * Colors a key [sender] to show they have been pressed.
     */
    @objc internal fun keyTouchDown(sender: Button) {
        sender.backgroundColor = keyPressedColor
    }

    /**
     * Defines events that occur given multiple presses of a single key.
     *
     * @param sender The key that was pressed multiple times.
     * @param event Event to derive tap counts.
     */
    @objc internal fun keyMultiPress(sender: Button, event: UIEvent) {
        var originalKey = sender.layer.value(forKey = "original") as? String ?: return
        val touch: UITouch = event.allTouches!!.firstOrNull()!!

        // Caps lock given two taps of shift.
        if (touch.tapCount == 2 && originalKey == "shift" && capsLockPossible == true) {
            shiftButtonState = .caps
            loadKeys()
            clearCommandBar()
        }

        // To make sure that the user can still use the double space period shortcut after numbers and symbols.
        val punctuationThatCancelsShortcut = listOf("?", "!", ",", ".", ":", ";", "-")
        if (originalKey != "shift" && proxy.documentContextBeforeInput?.size != 1 && commandState == false) {
            val charBeforeSpace = String(Array(proxy.documentContextBeforeInput!!).secondToLast()!!)
            if (punctuationThatCancelsShortcut.contains(charBeforeSpace)) {
                originalKey = "Cancel shortcut"
            }
        } else if (commandState == true) {
            val charBeforeSpace = String(Array((commandBar?.text!!)!!).secondToLast()!!)
            if (punctuationThatCancelsShortcut.contains(charBeforeSpace)) {
                originalKey = "Cancel shortcut"
            }
        }

        // Double space period shortcut.
        if (touch.tapCount == 2
            && originalKey == spaceBar
            && proxy.documentContextBeforeInput?.size != 1
            && doubleSpacePeriodPossible == true
        ) {
            // The fist condition prevents a period if the prior characters are spaces as the user wants a series of spaces.
            if (proxy.documentContextBeforeInput?.suffix(2) != "  " && commandState == false) {
                proxy.deleteBackward()
                proxy.insertText(". ")
                keyboardState = .letters
                shiftButtonState = .shift
                loadKeys()
            // The fist condition prevents a period if the prior characters are spaces as the user wants a series of spaces.
            } else if (commandBar.text!!.suffix(2) != "  " && commandState == true) {
                commandBar.text!! = (commandBar?.text!!?.deletePriorToCursor())!!
                commandBar.text!! = (commandBar?.text!!?.insertPriorToCursor(char = ". "))!!
                keyboardState = .letters
                shiftButtonState = .shift
                loadKeys()
            }
            clearCommandBar()
        }
    }

    /**
     * Defines the criteria under which a key is long pressed depending on the [gesture].
     */
    @objc internal fun keyLongPressed(gesture: UIGestureRecognizer) {
        // Prevent the command state prompt from being deleted.
        if (commandState == true && allPrompts.contains((commandBar?.text!!)!!)) {
            gesture.state = .cancelled
        }

        if (gesture.state == .began) {
            backspaceTimer = Timer.scheduledTimer(withTimeInterval = 0.1, repeats = true) { _  ->
                this.handleDeleteButtonPressed()
            }
        } else if (gesture.state == .ended || gesture.state == .cancelled) {
            backspaceTimer?.invalidate()
            backspaceTimer = null(gesture.view as Button).backgroundColor = specialKeyColor
        }
    }

    /**
     * Resets key [sender] coloration after they have been annotated to by keyPressedColor.
     */
    @objc internal fun keyUntouched(sender: Button) {
        val isSpecial = sender.layer.value(forKey = "isSpecial") as? Boolean ?: return
        sender.backgroundColor = if (isSpecial) specialKeyColor else keyColor
    }

    /**
     * Generates a pop up of the key [key] pressed.
     */
    @objc internal fun genPopUpView(key: Button) {
        val charPressed: String = key.layer.value(forKey = "original") as? String ?: ""
        val displayChar: String = key.layer.value(forKey = "keyToDisplay") as? String ?: ""
        genKeyPop(key = key, layer = keyPopLayer, char = charPressed, displayChar = displayChar)
        this.view.layer.addSublayer(keyPopLayer)
        this.view.addSubview(keyPopChar)
        DispatchQueue.main.asyncAfter(deadline = .now() + 0.125) {
            keyPopLayer.removeFromSuperlayer()
            keyPopChar.removeFromSuperview()
        }
    }

    /**
     * Generates a pop up of the key [sender] long pressed.
     */
    @objc internal fun genHoldPopUpView(sender: UILongPressGestureRecognizer) {
        // Derive which button was pressed and get its alternates.
        val key: Button = sender.view as? Button ?: return
        val charPressed: String = key.layer.value(forKey = "original") as? String ?: ""
        val displayChar: String = key.layer.value(forKey = "keyToDisplay") as? String ?: ""

        // Timer is short as the alternates view gets canceled by sender.state.changed.
        Timer.scheduledTimer(withTimeInterval = 0.00001, repeats = false) { _  ->
            if (keysWithAlternates.contains(charPressed)) {
                this.setAlternatesView(sender = sender)
                keyHoldPopLayer.removeFromSuperlayer()
                keyHoldPopChar.removeFromSuperview()
            }
        }
        when (sender.state) {
            began -> {
                genKeyPop(key = key, layer = keyHoldPopLayer, char = charPressed, displayChar = displayChar)
                this.view.layer.addSublayer(keyHoldPopLayer)
                this.view.addSubview(keyHoldPopChar)
            }
            ended -> {
                // Remove the key hold pop up and execute key only if the alternates view isn't present.
                keyHoldPopLayer.removeFromSuperlayer()
                keyHoldPopChar.removeFromSuperview()
                if (!keysWithAlternates.contains(charPressed)) {
                    executeKeyActions(key)
                } else if (this.view.viewWithTag(1001) == null) {
                    executeKeyActions(key)
                }
                keyUntouched(key)
            }
            else -> break
        }
    }

    /**
     * Sets the characters that can be selected on an alternates view that is generated.
     *
     * @param sender The long press of the given key.
     */
    @objc internal fun setAlternatesView(sender: UILongPressGestureRecognizer) {
        // Only run this code when the state begins.
        if (sender.state != UIGestureRecognizer.State.began) {
            return
        }

        // Derive which button was pressed and get its alternates.
        val key: Button = sender.view as? Button ?: return
        genAlternatesView(key = key)
        alternateBtnStartX = 5.0
        var alternatesBtnY = key.frame.height * 0.15
        if (DeviceType.isPad) {
            alternatesBtnY = key.frame.height * 0.2
        }

        for (char in alternateKeys) {
            val alternateKey: KeyboardKey = KeyboardKey(
                frame = CGRect(
                    x = alternateBtnStartX, y = alternatesBtnY, width = key.frame.width, height = alternatesBtnHeight
                )
            )
            if (shiftButtonState == .normal || char == "ß") {
                alternateKey.setTitle(char, for = .normal)
            } else {
                alternateKey.setTitle(char.capitalized, for = .normal)
            }
            alternateKey.setCharSize()
            alternateKey.setTitleColor(keyCharColor, for = .normal)
            alternateKey.layer.cornerRadius = keyCornerRadius
            alternatesKeyView.addSubview(alternateKey)
            if (char == alternateKeys.firstOrNull() && keysWithAlternatesLeft.contains(char)) {
                setBtn(btn = alternateKey, color = commandKeyColor, name = char, canCap = true, isSpecial = false)
            } else if (char == alternateKeys.lastOrNull() && keysWithAlternatesRight.contains(char)) {
                setBtn(btn = alternateKey, color = commandKeyColor, name = char, canCap = true, isSpecial = false)
            } else {
                setBtn(btn = alternateKey, color = keyColor, name = char, canCap = true, isSpecial = false)
            }
            activateBtn(btn = alternateKey)
            alternateBtnStartX += (key.frame.width + 3.0)
        }

        // If alternateKeysView is already added than remove and then add again.
        if (this.view.viewWithTag(1001) != null) {
            val viewWithTag = this.view.viewWithTag(1001)
            viewWithTag?.removeFromSuperview()
            alternatesShapeLayer.removeFromSuperlayer()
        }
        this.view.layer.addSublayer(alternatesShapeLayer)
        this.view.addSubview(alternatesKeyView)
    }
    //endregion
}
