/**
 * ViewController.kt
 *
 * The ViewController for the Scribe app.
 */

/**
 * A UIViewController that provides instructions on how to install Keyboards as well as information about Scribe.
 */
internal class ViewController: UIViewController {
    // Variables linked to elements in AppScreen.storyboard.
    @BindView() lateinit internal var appTextView: UITextView
    @BindView() lateinit internal var appTextBackground: View
    @BindView() lateinit internal var settingsBtn: Button
    @BindView() lateinit internal var topIconPhone: ImageView
    @BindView() lateinit internal var topIconPad: ImageView
    @BindView() lateinit internal var settingsCorner: ImageView
    @BindView() lateinit internal var GHTextView: UITextView
    @BindView() lateinit internal var GHTextBackground: View
    @BindView() lateinit internal var GHBtn: Button
    @BindView() lateinit internal var GHCorner: ImageView
    @BindView() lateinit internal var privacyTextBackground: View
    @BindView() lateinit internal var privacyTextView: UITextView
    @BindView() lateinit internal var privacyScroll: ImageView
    @BindView() lateinit internal var switchView: Button
    @BindView() lateinit internal var switchViewBackground: View
    internal var displayPrivacyPolicy = false
    @// Spacing views to size app screen proportionally.
    BindView() lateinit internal var topSpace: View
    @BindView() lateinit internal var logoSpace: View
    @BindView() lateinit internal var svSpace: View
    @BindView() lateinit internal var bottomSpace: View

    /**
    * Includes a call to checkDarkModeSetColors to set brand colors and a call to set the UI for the app screen.
    */
    internal override fun viewDidLoad() {
        super.viewDidLoad()
        checkDarkModeSetColors()
        setCurrentUI()
    }

    /**
    * Includes a call to checkDarkModeSetColors to set brand colors and a call to set the UI for the app screen.
    */
    internal override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        checkDarkModeSetColors()
        setCurrentUI()
    }

    /**
    * Includes a call to set the UI for the app screen.
    */
    internal override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        setCurrentUI()
    }

    /**
    * Includes a call to set the UI for the app screen.
    */
    internal override fun viewWillTransition(size: CGSize, coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to = size, with = coordinator)
        setCurrentUI()
    }

    /**
    * Includes a call to set the UI for the app screen.
    */
    internal override fun viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        setCurrentUI()
    }

    // Lock the device into portrait mode to avoid resizing issues.
    internal var orientations = UIInterfaceOrientationMask.portrait
    internal override var supportedInterfaceOrientations: UIInterfaceOrientationMask
        get() = this.orientations
        set(newValue) {
            this.orientations = newValue
        }

    // The app screen is white content on blue, so match the status bar.
    internal override val preferredStatusBarStyle: UIStatusBarStyle
        get() = .lightContent

    /**
    * Sets the top icon for the app screen given the device to assure that it's oriented correctly to its background.
    */
    internal fun setTopIcon() {
        if (DeviceType.isPhone) {
            topIconPhone.isHidden = false
            topIconPad.isHidden = true
        } else if (DeviceType.isPad) {
            topIconPhone.isHidden = true
            topIconPad.isHidden = false
        }
    }
    internal val switchViewColor = UIColor(red = 241.0 / 255.0, green = 204.0 / 255.0, blue = 131.0 / 255.0, alpha = 1.0)

    /**
    * Sets the functionality of the button that switches between installation instructions and the privacy policy.
    */
    internal fun setSwitchViewBtn() {
        if (displayPrivacyPolicy == false) {
            switchView.setTitle("View Privacy Policy", for = .normal)
        } else if (displayPrivacyPolicy == true) {
            switchView.setTitle("View Installation", for = .normal)
        }
        switchView.contentHorizontalAlignment = UIControl.ContentHorizontalAlignment.center
        switchView.setTitleColor(UIColor.keyCharColorLight, for = .normal)
        switchView.titleLabel?.font = .systemFont(ofSize = fontSize * 1.5)
        switchView.clipsToBounds = true
        switchView.backgroundColor = switchViewColor
        applyCornerRadius(elem = switchView, radius = switchView.frame.height * 0.35)
        applyShadowEffects(elem = switchView)
        switchView.addTarget(this, action = #selector(switchAppView), for = .touchUpInside)
        switchView.addTarget(this, action = #selector(keyTouchDown), for = .touchDown)
        // Add a white background so that the key press doesn't show the blue app screen background.
        switchViewBackground.backgroundColor = .white
        switchViewBackground.clipsToBounds = true
        applyCornerRadius(elem = switchViewBackground, radius = switchView.frame.height * 0.35)
    }

    /**
    * Sets the functionality of the button over the keyboard installation guide that opens Settings.
    */
    internal fun setSettingsBtn() {
        settingsBtn.addTarget(this, action = #selector(openSettingsApp), for = .touchUpInside)
        settingsBtn.addTarget(this, action = #selector(keyTouchDown), for = .touchDown)
    }

    /**
    * Sets the functionality of the button over the keyboard installation guide that links to Scribe's GitHub.
    */
    internal fun setGHBtn() {
        GHBtn.addTarget(this, action = #selector(openScribeGH), for = .touchUpInside)
        GHBtn.addTarget(this, action = #selector(keyTouchDown), for = .touchDown)
    }

    /**
    * Sets constant properties for the app screen.
    */
    internal fun setUIConstantProperties() {
        // Set the scroll bar so that it appears on a white background regardless of light or dark mode.
        val scrollbarAppearance = UINavigationBarAppearance()
        scrollbarAppearance.configureWithOpaqueBackground()
        privacyScroll.isUserInteractionEnabled = false
        // Disable spacing views.
        val allSpacingViews: List<View> = listOf(topSpace, logoSpace, svSpace, bottomSpace)
        for (view in allSpacingViews) {
            view.isUserInteractionEnabled = false
            view.backgroundColor = .clear
        }
        topIconPhone.tintColor = .white
        topIconPad.tintColor = .white
    }

    /**
    * Sets properties for the app screen given the current device.
    */
    internal fun setUIDeviceProperties() {
        // Height ratios to set corner radii exactly.
        val installTextToSwitchBtnHeightRatio = appTextBackground.frame.height / switchViewBackground.frame.height
        val GHTextToSwitchBtnHeightRatio = GHTextBackground.frame.height / switchViewBackground.frame.height
        val privacyTextToSwitchBtnHeightRatio = privacyTextBackground.frame.height / switchViewBackground.frame.height
        settingsCorner.layer.maskedCorners = .layerMaxXMinYCorner
        settingsCorner.layer.cornerRadius = appTextBackground.frame.height * 0.4 / installTextToSwitchBtnHeightRatio
        settingsCorner.alpha = 0.9
        GHCorner.layer.maskedCorners = .layerMaxXMinYCorner
        GHCorner.layer.cornerRadius = GHTextBackground.frame.height * 0.4 / GHTextToSwitchBtnHeightRatio
        GHCorner.alpha = 0.9
        settingsBtn.clipsToBounds = true
        settingsBtn.layer.masksToBounds = false
        settingsBtn.layer.cornerRadius = appTextBackground.frame.height * 0.4 / installTextToSwitchBtnHeightRatio
        GHBtn.clipsToBounds = true
        GHBtn.layer.masksToBounds = false
        GHBtn.layer.cornerRadius = GHTextBackground.frame.height * 0.4 / GHTextToSwitchBtnHeightRatio
        val allTextViews: List<UITextView> = listOf(appTextView, GHTextView, privacyTextView)
        // Disable text views.
        for (textView in allTextViews) {
            textView.isUserInteractionEnabled = false
            textView.backgroundColor = .clear
            textView.isEditable = false
        }
        // Set backgrounds and corner radii.
        appTextBackground.isUserInteractionEnabled = false
        appTextBackground.clipsToBounds = true
        applyCornerRadius(
            elem = appTextBackground, radius = appTextBackground.frame.height * 0.4 / installTextToSwitchBtnHeightRatio
        )
        GHTextBackground.isUserInteractionEnabled = false
        GHTextBackground.clipsToBounds = true
        applyCornerRadius(
            elem = GHTextBackground, radius = GHTextBackground.frame.height * 0.4 / GHTextToSwitchBtnHeightRatio
        )
        privacyTextView.backgroundColor = .clear
        applyCornerRadius(
            elem = privacyTextBackground,
            radius = privacyTextBackground.frame.height * 0.4 / privacyTextToSwitchBtnHeightRatio
        )
        // Set link attributes for all textViews.
        for (textView in allTextViews) {
            textView.linkTextAttributes = mapOf(
                NSAttributedString.Key.foregroundColor to UIColor.annotateBlueLight,
                NSAttributedString.Key.underlineStyle to NSUnderlineStyle.single.rawValue
            )
        }
    }

    /**
    * Sets the necessary properties for the installation UI including calling text generation functions.
    */
    internal fun setInstallationUI() {
        val settingsSymbol: UIImage = getSettingsSymbol(fontSize = fontSize)
        topIconPhone.image = settingsSymbol
        topIconPad.image = settingsSymbol
        // Enable installation directions and GitHub notice elements.
        settingsBtn.isUserInteractionEnabled = true
        appTextBackground.backgroundColor = .white
        applyShadowEffects(elem = appTextBackground)
        GHBtn.isUserInteractionEnabled = true
        GHCorner.isHidden = false
        GHTextBackground.backgroundColor = .white
        applyShadowEffects(elem = GHTextBackground)
        // Disable the privacy policy elements.
        privacyTextView.isUserInteractionEnabled = false
        privacyTextView.backgroundColor = .clear
        privacyTextView.text = ""
        privacyTextBackground.backgroundColor = .clear
        privacyScroll.isHidden = true
        // Set the texts for the fields.
        appTextView.attributedText = setENInstallation(fontSize = fontSize)
        appTextView.textColor = UIColor.keyCharColorLight
        GHTextView.attributedText = setENGitHubText(fontSize = fontSize)
        GHTextView.textColor = UIColor.keyCharColorLight
    }

    /**
    * Sets the necessary properties for the privacy policy UI including calling the text generation function.
    */
    internal fun setPrivacyUI() {
        val privacySymbol: UIImage = getPrivacySymbol(fontSize = fontSize)
        topIconPhone.image = privacySymbol
        topIconPad.image = privacySymbol
        // Disable installation directions and GitHub notice elements.
        settingsBtn.isUserInteractionEnabled = false
        appTextView.text = ""
        appTextBackground.backgroundColor = .clear
        GHBtn.isUserInteractionEnabled = false
        GHCorner.isHidden = true
        GHTextView.text = ""
        GHTextBackground.backgroundColor = .clear
        // Enable the privacy policy elements.
        privacyTextView.isUserInteractionEnabled = true
        privacyTextBackground.backgroundColor = .white
        applyShadowEffects(elem = privacyTextBackground)
        privacyScroll.isHidden = false
        privacyTextView.attributedText = setENPrivacyPolicy(fontSize = fontSize)
        privacyTextView.textColor = UIColor.keyCharColorLight
    }

    /**
    * Creates the current app UI by applying constraints and calling child UI functions.
    */
    internal fun setCurrentUI() {
        // Set the font size and all button elements.
        setFontSize()
        setTopIcon()
        setSwitchViewBtn()
        setSettingsBtn()
        setGHBtn()
        setUIConstantProperties()
        setUIDeviceProperties()
        if (displayPrivacyPolicy == false) {
            setInstallationUI()
        } else {
            setPrivacyUI()
        }
    }

    /**
    * Switches the view of the app based on the current view.
    */
    @objc internal fun switchAppView() {
        if (displayPrivacyPolicy == false) {
            displayPrivacyPolicy = true
        } else {
            displayPrivacyPolicy = false
        }
        setCurrentUI()
    }

    /**
    * Function to open the settings app that is targeted by settingsBtn.
    */
    @objc internal fun openSettingsApp() {
        UIApplication.shared.open(URL(string = UIApplication.openSettingsURLString)!!)
    }

    /**
    * Function to open Scribe's GitHub page that is targeted by GHBtn.
    */
    @objc internal fun openScribeGH() {
        val url = URL(string = "https://github.com/scribe-org") ?: return
        if (#available(iOS 10.0, *)) {
            UIApplication.shared.open(url, options = mapOf<>, completionHandler = null)
        } else {
            UIApplication.shared.openURL(url)
        }
    }

    /**
    * Function to change the key coloration given a touch down of a button [sender].
    */
    @objc internal fun keyTouchDown(sender: Button) {
        if (sender == switchView) {
            sender.backgroundColor = .clear
            sender.setTitleColor(switchViewColor, for = .normal)
            // Bring sender's background and text colors back to their original values.
            DispatchQueue.main.asyncAfter(deadline = .now() + 0.15) {
                sender.backgroundColor = switchViewColor
                sender.setTitleColor(UIColor.keyCharColorLight, for = .normal)
            }
        } else {
            sender.backgroundColor = .black
            sender.alpha = 0.2
            // Bring sender's opacity back up to fully opaque and replace the background color.
            DispatchQueue.main.asyncAfter(deadline = .now() + 0.15) {
                sender.backgroundColor = .clear
                sender.alpha = 1.0
            }
        }
    }
}
