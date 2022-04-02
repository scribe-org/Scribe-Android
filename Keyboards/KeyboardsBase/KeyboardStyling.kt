/**
 * KeyboardStyling.kt
 *
 * Functions to style keyboard elements.
 */

/**
 * Styles a button including it's shape and text.
 *
 * @param btn The button to be styled.
 * @param title The title to be assigned.
 * @param radius The corner radius of the button.
 */
internal fun styleBtn(btn: Button, title: String, radius: CGFloat) {
    btn.clipsToBounds = true
    btn.layer.masksToBounds = false
    btn.layer.cornerRadius = radius
    btn.setTitle(title, for = .normal)
    btn.contentHorizontalAlignment = UIControl.ContentHorizontalAlignment.center
    btn.setTitleColor(keyCharColor, for = .normal)
    if (title != "Scribe") {
        btn.layer.shadowColor = keyShadowColor
        btn.layer.shadowOffset = CGSize(width = 0.0, height = 1.0)
        btn.layer.shadowOpacity = 1.0
        btn.layer.shadowRadius = 0.0
    }
}

// The names of symbols whose keys should be slightly larger than the default size.
internal var keysThatAreSlightlyLarger: List<String> = listOf(
    "delete.left", "chevron.left", "chevron.right", "shift", "shift.fill", "capslock.fill"
)

/**
 * Get the icon configurations for keys if the device is an iPhone.
 *
 * @param iconName The name of the UIImage systemName icon to be used.
 */
internal fun getPhoneIconConfig(iconName: String) : UIImage.SymbolConfiguration {
    var iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 1.75, weight = .light, scale = .medium)
    if (keysThatAreSlightlyLarger.contains(iconName)) {
        iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 1.55, weight = .light, scale = .medium)
    }
    if (isLandscapeView == true) {
        iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 3.5, weight = .light, scale = .medium)
        if (keysThatAreSlightlyLarger.contains(iconName)) {
            iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 3.2, weight = .light, scale = .medium)
        }
    }
    return iconConfig
}

/**
 * Get the icon configurations for keys if the device is an iPad.
 *
 * @param iconName The name of the UIImage systemName icon to be used.
 */
internal fun getPadIconConfig(iconName: String) : UIImage.SymbolConfiguration {
    keysThatAreSlightlyLarger.append("globe")
    var iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 3, weight = .light, scale = .medium)
    if (keysThatAreSlightlyLarger.contains(iconName)) {
        iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 2.75, weight = .light, scale = .medium)
    }
    if (isLandscapeView == true) {
        iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 3.75, weight = .light, scale = .medium)
        if (keysThatAreSlightlyLarger.contains(iconName)) {
            iconConfig = UIImage.SymbolConfiguration(pointSize = letterKeyWidth / 3.4, weight = .light, scale = .medium)
        }
    }
    keysThatAreSlightlyLarger.removeAll { it == "globe" }
    return iconConfig
}

/**
 * Styles buttons that have icon keys.
 *
 * @param btn The button to be styled.
 * @param color The tint color for the icon on the key.
 * @param iconName The name of the UIImage systemName icon to be used.
 */
internal fun styleIconBtn(btn: Button, color: UIColor, iconName: String) {
    btn.setTitle("", for = .normal)
    var iconConfig = getPhoneIconConfig(iconName = iconName)
    if (DeviceType.isPad) {
        iconConfig = getPadIconConfig(iconName = iconName)
    }
    btn.setImage(UIImage(systemName = iconName, withConfiguration = iconConfig), for = .normal)
    btn.tintColor = color
}

/**
 * Adds padding to keys to position them.
 *
 * @param to The stackView in which the button is found.
 * @param width The width of the padding.
 * @param key The key associated with the button.
 */
internal fun addPadding(stackView: LinearLayout, width: CGFloat, key: String) {
    val padding = Button(frame = CGRect(x = 0, y = 0, width = 0, height = 0))
    padding.setTitleColor(.clear, for = .normal)
    padding.alpha = 0.0
    padding.widthAnchor.constraint(equalToConstant = width).isActive = true
    padding.isUserInteractionEnabled = false
    paddingViews.append(padding)
    stackView.addArrangedSubview(padding)
}
