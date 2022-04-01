/**
 * AppTextIcons.kt
 *
 * Functions returning styled text elements for the app screen.
 */

internal var fontSize = CGFloat(0)

/**
 * Sets the font size for the text in the app screen and corresponding UIImage icons.
 */
internal fun setFontSize() {
    if (DeviceType.isPhone) {
        fontSize = UIScreen.main.bounds.height / 65
    } else if (DeviceType.isPad) {
        fontSize = UIScreen.main.bounds.height / 60
    }
}

/**
 * Concatenates attributed strings.
 *
 * @param left The left attributed string to concatenate.
 * @param right The right attributed string to concatenate.
 */
internal fun concatAttributedStrings(left: NSAttributedString, right: NSAttributedString) : NSMutableAttributedString {
    val result = NSMutableAttributedString()
    result.append(left)
    result.append(right)
    return result
}

/**
 * Returns an attributed text that hyperlinked.
 *
 * @param originalText The original text that hyperlinks will be added to.
 * @param hyperLinks A dictionary of strings and the link to which they should link.
 */
internal fun addHyperLinks(
    originalText: String, links: Map<String, String>, fontSize: CGFloat
    ) : NSMutableAttributedString {
    val style = NSMutableParagraphStyle()
    style.alignment = .left
    val attributedOriginalText = NSMutableAttributedString(
        string = originalText,
        attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>
    )
    for ((hyperLink, urlString) in links) {
        val linkRange = attributedOriginalText.mutableString.range(of = hyperLink)
        val fullRange = NSRange(location = 0, length = attributedOriginalText.length)
        attributedOriginalText.addAttribute(NSAttributedString.Key.link, value = urlString, range = linkRange)
        attributedOriginalText.addAttribute(NSAttributedString.Key.paragraphStyle, value = style, range = fullRange)
    }
    return attributedOriginalText
}

/**
 * Formats and returns an arrow icon for the app texts given a [fontSize] based on screen dimensions.
 */
internal fun getArrowIcon(fontSize: CGFloat) : NSAttributedString {
    // The down right arrow character as a text attachment.
    val arrowAttachment = NSTextAttachment()
    val selectArrowIconConfig = UIImage.SymbolConfiguration(pointSize = fontSize, weight = .medium, scale = .medium)
    arrowAttachment.image = UIImage(
        systemName = "arrow.turn.down.right",
        withConfiguration = selectArrowIconConfig
    )?.withTintColor(.scribeGrey)
    return NSAttributedString(attachment = arrowAttachment)
}

/**
 * Formats and returns an arrow icon for the app texts given a [fontSize] based on screen dimensions.
 */
internal fun getGlobeIcon(fontSize: CGFloat) : NSAttributedString {
    // The globe character as a text attachment.
    val globeAttachment = NSTextAttachment()
    val selectGlobeIconConfig = UIImage.SymbolConfiguration(pointSize = fontSize, weight = .medium, scale = .medium)
    globeAttachment.image = UIImage(
        systemName = "globe",
        withConfiguration = selectGlobeIconConfig
    )?.withTintColor(.scribeGrey)
    return NSAttributedString(attachment = globeAttachment)
}
