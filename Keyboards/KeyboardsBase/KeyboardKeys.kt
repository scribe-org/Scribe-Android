//
//  KeyboardKey.kt
//
//  Classes and variables that define keys for Scribe keyboards.

// The keys collection as well as one for the padding for placements.
internal var keyboardKeys: List<Button> = listOf()
internal var paddingViews: List<Button> = listOf()

/// Class of UIButton that allows the tap area to be increased so that edges between keys can still receive user input.
internal class KeyboardKey: Button {
    // Properties for the touch area - passing negative values will expand the touch area.
    internal var topShift = CGFloat(0)
    internal var leftShift = CGFloat(0)
    internal var bottomShift = CGFloat(0)
    internal var rightShift = CGFloat(0)

    /// Allows the bounds of the key to be expanded.
    internal override fun point(point: CGPoint, event: UIEvent?) : Boolean =
        bounds.inset(by = UIEdgeInsets(top = topShift, left = leftShift, bottom = bottomShift, right = rightShift)).contains(point)
    lateinit internal var row: Int
    lateinit internal var idx: Int
    lateinit internal var key: String

    /// Styles the key with a color, corner radius and shadow.
    internal fun style() {
        this.backgroundColor = keyColor
        this.layer.cornerRadius = keyCornerRadius
        this.layer.shadowColor = keyShadowColor
        this.layer.shadowOffset = CGSize(width = 0.0, height = 1.0)
        this.layer.shadowOpacity = 1.0
        this.layer.shadowRadius = 0.0
        this.layer.masksToBounds = false
    }

    /// Sets the character of the key and defines its capitalized state.
    internal fun setChar() {
        this.key = keyboard[this.row][this.idx]
        if (this.key == "space") {
            this.key = spaceBar
            this.layer.setValue(true, forKey = "isSpecial")
        }
        var capsKey = ""
        if (this.key != "ß" && this.key != spaceBar) {
            capsKey = keyboard[this.row][this.idx].capitalized
        } else {
            capsKey = this.key
        }
        val keyToDisplay = if (shiftButtonState == .normal) this.key else capsKey
        this.setTitleColor(keyCharColor, for = .normal)
        this.layer.setValue(this.key, forKey = "original")
        this.layer.setValue(keyToDisplay, forKey = "keyToDisplay")
        this.layer.setValue(false, forKey = "isSpecial")
        this.setTitle(keyToDisplay, for = .normal)
    }

    // set button character
/// Sets the character size of a capital key if the device is an iPhone given the orientation.
    internal fun setPhoneCapCharSize() {
        if (isLandscapeView == true) {
            if (this.key == "#+=" || this.key == "ABC" || this.key == "АБВ" || this.key == "123") {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3.5)
            } else if (this.key == spaceBar) {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 4)
            } else {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 2.9)
            }
        } else {
            if (this.key == "#+=" || this.key == "ABC" || this.key == "АБВ" || this.key == "123") {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 1.75)
            } else if (this.key == spaceBar) {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 2)
            } else {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 1.5)
            }
        }
    }

    /// Checks if the character is a lower case letter and adjusts it if so.
    internal fun checkSetPhoneLowerCharSize() {
        val isSpecial = this.layer.value(forKey = "isSpecial") as? Boolean ?: return
        if (keyboardState == .letters && isSpecial == false && !listOf("123", spaceBar).contains(this.key) && shiftButtonState == .normal) {
            this.titleEdgeInsets = UIEdgeInsets(top = -4.0, left = 0.0, bottom = 0.0, right = 0.0)
            if (isLandscapeView == true) {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 2.4)
            } else {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 1.35)
            }
        } else {
            this.titleEdgeInsets = UIEdgeInsets(top = 0.0, left = 0.0, bottom = 0.0, right = 0.0)
        }
    }

    /// Sets the character size of a key if the device is an iPhone.
    internal fun setPhoneCharSize() {
        setPhoneCapCharSize()
        checkSetPhoneLowerCharSize()
    }

    /// Sets the character size of a key if the device is an iPad given the orientation.
    internal fun setPadCapCharSize() {
        if (isLandscapeView == true) {
            if (this.key == "#+=" || this.key == "ABC" || this.key == "АБВ" || this.key == "hideKeyboard") {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3.75)
            } else if (this.key == spaceBar) {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 4.25)
            } else if (this.key == ".?123") {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 4.5)
            } else {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3.75)
            }
        } else {
            if (this.key == "#+=" || this.key == "ABC" || this.key == "АБВ" || this.key == "hideKeyboard") {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3.25)
            } else if (this.key == spaceBar) {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3.5)
            } else if (this.key == ".?123") {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 4)
            } else {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3)
            }
        }
    }

    /// Sets the character size of a key if the device is an iPad given the orientation.
    internal fun checkSetPadLowerCharSize() {
        val isSpecial = this.layer.value(forKey = "isSpecial") as? Boolean ?: return
        if (keyboardState == .letters && isSpecial == false && !listOf(".?123", spaceBar, "ß", ",", ".", "'", "-").contains(this.key) && shiftButtonState == .normal) {
            this.titleEdgeInsets = UIEdgeInsets(top = -4.0, left = 0.0, bottom = 0.0, right = 0.0)
            if (isLandscapeView == true) {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 3.35)
            } else {
                this.titleLabel?.font = .systemFont(ofSize = letterKeyWidth / 2.75)
            }
        } else {
            this.titleEdgeInsets = UIEdgeInsets(top = 0.0, left = 0.0, bottom = 0.0, right = 0.0)
        }
    }

    /// Sets the character size of a key if the device is an iPad.
    internal fun setPadCharSize() {
        setPadCapCharSize()
        checkSetPadLowerCharSize()
    }

    /// Sets the key character sizes depending on device type and orientation.
    internal fun setCharSize() {
        if (DeviceType.isPhone) {
            setPhoneCharSize()
        } else if (DeviceType.isPad) {
            setPadCharSize()
        }
    }

    /// Adjusts the width of a key if it's one of the special characters on the iPhone keyboard.
    internal fun adjustPhoneKeyWidth() {
        if (this.key == "ABC" || this.key == "АБВ") {
            this.layer.setValue(true, forKey = "isSpecial")
            this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 2).isActive = true
        } else if (this.key == "delete" || this.key == "#+=" || this.key == "shift" || this.key == "selectKeyboard") {
            // Cancel Russian keyboard key resizing if translating as the keyboard is English.
            if (controllerLanguage == "Russian" && keyboardState == .letters && switchInput != true) {
                this.layer.setValue(true, forKey = "isSpecial")
                this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1).isActive = true
            } else {
                this.layer.setValue(true, forKey = "isSpecial")
                this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1.5).isActive = true
            }
        } else if (this.key == "123" || this.key == ".?123" || this.key == "return" || this.key == "hideKeyboard") {
            if (this.row == 2) {
                this.layer.setValue(true, forKey = "isSpecial")
                this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1.5).isActive = true
            } else if (this.row != 2) {
                this.layer.setValue(true, forKey = "isSpecial")
                this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 2).isActive = true
            }
        } else if ((keyboardState == .numbers || keyboardState == .symbols) && this.row == 2) {
            // Make second row number and symbol keys wider for iPhones.
            this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1.4).isActive = true
        } else if (this.key != spaceBar) {
            this.widthAnchor.constraint(equalToConstant = keyWidth).isActive = true
        }
    }

    /// Adjusts the width of a key if it's one of the special characters on the iPad keyboard.
    internal fun adjustPadKeyWidth() {
        if (this.key == "ABC" || this.key == "АБВ") {
            this.layer.setValue(true, forKey = "isSpecial")
            this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1).isActive = true
        } else if (this.key == "delete" || this.key == "#+=" || this.key == "shift" || this.key == "selectKeyboard") {
            this.layer.setValue(true, forKey = "isSpecial")
            this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1).isActive = true
        } else if (this.key == "123" || this.key == ".?123" || this.key == "return" || this.key == "hideKeyboard") {
            if (this.key == "return" && (controllerLanguage == "Portuguese" || controllerLanguage == "Italian" || switchInput == true) && this.row == 1 && DeviceType.isPad) {
                this.layer.setValue(true, forKey = "isSpecial")
                this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1.5).isActive = true
            } else {
                this.layer.setValue(true, forKey = "isSpecial")
                this.widthAnchor.constraint(equalToConstant = numSymKeyWidth * 1).isActive = true
            }
        } else if (this.key != spaceBar) {
            this.widthAnchor.constraint(equalToConstant = keyWidth).isActive = true
        }
    }

    /// Adjusts the width of a key if it's one of the special characters on the keyboard.
    internal fun adjustKeyWidth() {
        if (DeviceType.isPhone) {
            adjustPhoneKeyWidth()
        } else if (DeviceType.isPad) {
            adjustPadKeyWidth()
        }
        val isSpecial = this.layer.value(forKey = "isSpecial") as? Boolean ?: return
        if (this.key == "shift") {
            // Switch the shift key icon given its state.
            if (shiftButtonState == .shift) {
                this.backgroundColor = keyPressedColor
                styleIconBtn(btn = this, color = UIColor.label, iconName = "shift.fill")
            } else if (shiftButtonState == .caps) {
                this.backgroundColor = keyPressedColor
                styleIconBtn(btn = this, color = UIColor.label, iconName = "capslock.fill")
            } else {
                this.backgroundColor = specialKeyColor
            }
        } else if (this.key == "return" && commandState == true) {
            // Color the return key depending on if it's being used as enter for commands.
            this.backgroundColor = commandKeyColor
        } else if (isSpecial == true) {
            this.backgroundColor = specialKeyColor
        }
    }
}

/// Sets a button's values that are displayed and inserted into the proxy as well as assigning a color.
///
/// - Parameters
///   - btn: the button to be set up.
///   - color: the color to assign to the background.
///   - name: the name of the value for the key.
///   - canCapitalize: whether the key receives a special character for the shift state.
///   - isSpecial: whether the btn should be marked as special to be colored accordingly.
internal fun setBtn(btn: Button, color: UIColor, name: String, canCapitalize: Boolean, isSpecial: Boolean) {
    btn.backgroundColor = color
    btn.layer.setValue(name, forKey = "original")
    val charsWithoutShiftState = listOf("ß")
    var capsKey = ""
    if (canCapitalize == true) {
        if (!charsWithoutShiftState.contains(name)) {
            capsKey = name.capitalized
        } else {
            capsKey = name
        }
        val shiftChar = if (shiftButtonState == .normal) name else capsKey
        btn.layer.setValue(shiftChar, forKey = "keyToDisplay")
    } else {
        btn.layer.setValue(name, forKey = "keyToDisplay")
    }
    btn.layer.setValue(isSpecial, forKey = "isSpecial")
}
