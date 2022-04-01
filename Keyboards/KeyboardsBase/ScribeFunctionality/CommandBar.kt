/**
 * CommandBar.kt
 *
 * Class defining the bar into which commands are typed.
 */

/**
 * A custom UILabel used to house all the functionality of the command bar.
 */
internal class CommandBar: TextView {

    internal override constructor(frame: CGRect) : super(frame = frame) {}

    internal required constructor?(coder: NSCoder) : super(coder = coder) {}

    /// Allows the class to be accessed from Keyboard.xib.
    internal class fun instanceFromNib() : View =
        UINib(nibName = "Keyboard", bundle = null).instantiate(withOwner = null, options = null)[0] as View
    lateinit internal var shadow: Button
    lateinit internal var blend: TextView

    /**
     * Sets up the command bar's color and text alignment.
     */
    internal fun set() {
        this.backgroundColor = commandBarColor
        this.blend.backgroundColor = commandBarColor
        this.layer.borderColor = commandBarBorderColor
        this.layer.borderWidth = 1.0
        this.textAlignment = NSTextAlignment.left
        if (DeviceType.isPhone) {
            this.font = .systemFont(ofSize = annotationHeight * 0.7)
        } else if (DeviceType.isPad) {
            this.font = .systemFont(ofSize = annotationHeight * 0.85)
        }
        this.shadow.isUserInteractionEnabled = false
        if (DeviceType.isPhone) {
            commandPromptSpacing = String(repeating = " ", count = 2)
        } else if (DeviceType.isPad) {
            commandPromptSpacing = String(repeating = " ", count = 5)
        }
    }

    /**
     * Sets up the command bar's radius and shadow.
     */
    internal fun setCornerRadiusAndShadow() {
        this.clipsToBounds = true
        this.layer.cornerRadius = commandKeyCornerRadius
        this.layer.maskedCorners = listOf(.layerMaxXMinYCorner, .layerMaxXMaxYCorner)
        this.textColor = keyCharColor
        this.lineBreakMode = NSLineBreakMode.byWordWrapping
        this.shadow.backgroundColor = specialKeyColor
        this.shadow.layer.cornerRadius = commandKeyCornerRadius
        this.shadow.layer.maskedCorners = listOf(.layerMaxXMinYCorner, .layerMaxXMaxYCorner)
        this.shadow.clipsToBounds = true
        this.shadow.layer.masksToBounds = false
        this.shadow.layer.shadowRadius = 0
        this.shadow.layer.shadowOpacity = 1.0
        this.shadow.layer.shadowOffset = CGSize(width = 0, height = 1)
        this.shadow.layer.shadowColor = keyShadowColor
    }

    /**
     * Hides the command bar when command buttons will be showed.
     */
    internal fun hide() {
        this.backgroundColor = UIColor.clear
        this.layer.borderColor = UIColor.clear.cgColor
        this.text = ""
        this.shadow.backgroundColor = UIColor.clear
        this.blend.backgroundColor = UIColor.clear
    }
}
