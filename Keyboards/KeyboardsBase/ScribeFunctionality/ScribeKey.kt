//
//  ScribeKey.swift
//
//  Class defining the Scribe key that is used to access keyboard commands.
//

/// The main UI element that allows for accessing other commands and triggering annotation.
internal class ScribeKey: Button {

    internal override constructor(frame: CGRect) : super(frame = frame) {}

    internal required constructor?(coder: NSCoder) : super(coder = coder) {}

    /// Allows the class to be accessed from Keyboard.xib.
    internal class fun instanceFromNib() : View =
        UINib(nibName = "Keyboard", bundle = null).instantiate(withOwner = null, options = null)[0] as View

    /// Converts the Scribe key to an escape key to return to the base keyboard view.
    internal fun toEscape() {
        this.setTitle("", for = .normal)
        var selectKeyboardIconConfig = UIImage.SymbolConfiguration(pointSize = annotationHeight * 0.75, weight = .light, scale = .medium)
        if (DeviceType.isPad) {
            selectKeyboardIconConfig = UIImage.SymbolConfiguration(pointSize = annotationHeight * 1.1, weight = .light, scale = .medium)
        }
        this.setImage(UIImage(systemName = "xmark", withConfiguration = selectKeyboardIconConfig), for = .normal)
        this.tintColor = keyCharColor
        this.layer.cornerRadius = commandKeyCornerRadius
        this.layer.masksToBounds = true
    }

    /// Assigns the icon and sets up the Scribe key.
    internal fun set() {
        this.setImage(scribeKeyIcon, for = .normal)
        setBtn(btn = this, color = commandKeyColor, name = "Scribe", canCapitalize = false, isSpecial = false)
        this.layer.borderColor = commandBarBorderColor
        this.layer.borderWidth = 1.0
        this.contentMode = .center
        this.imageView?.contentMode = .scaleAspectFit
        this.shadow.isUserInteractionEnabled = false
    }

    /// Sets the corner radius for just the left side of the Scribe key.
    internal fun setLeftCornerRadius() {
        this.layer.maskedCorners = listOf(.layerMinXMinYCorner, .layerMinXMaxYCorner)
    }

    /// Sets the corner radius for all sides of the Scribe key.
    internal fun setFullCornerRadius() {
        this.layer.borderColor = UIColor.clear.cgColor
        // border is set by the shadow
        this.layer.maskedCorners = listOf(.layerMinXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMinYCorner, .layerMaxXMaxYCorner)
    }
    lateinit internal var shadow: Button

    /// Sets the shadow of the Scribe key.
    internal fun setShadow() {
        this.shadow.backgroundColor = specialKeyColor
        this.shadow.layer.cornerRadius = commandKeyCornerRadius
        this.shadow.layer.maskedCorners = listOf(.layerMinXMinYCorner, .layerMinXMaxYCorner)
        this.shadow.clipsToBounds = true
        this.shadow.layer.masksToBounds = false
        this.shadow.layer.shadowRadius = 0
        this.shadow.layer.shadowOpacity = 1.0
        this.shadow.layer.shadowOffset = CGSize(width = 0, height = 1)
        this.shadow.layer.shadowColor = keyShadowColor
    }

    /// Sets the shadow of the Scribe key when it's an escape key.
    internal fun setEscShadow() {
        this.shadow.backgroundColor = specialKeyColor
        this.shadow.layer.cornerRadius = commandKeyCornerRadius
        this.shadow.layer.maskedCorners = listOf(.layerMinXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMinYCorner, .layerMaxXMaxYCorner)
        this.shadow.clipsToBounds = true
        this.shadow.layer.masksToBounds = false
        this.shadow.layer.shadowRadius = 0
        this.shadow.layer.shadowOpacity = 1.0
        this.shadow.layer.shadowOffset = CGSize(width = 0, height = 1)
        this.shadow.layer.shadowColor = keyShadowColor
    }
}
