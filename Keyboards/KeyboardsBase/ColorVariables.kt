/**
 * ColorVariables.kt
 *
 * Variables associated with coloration for Scribe.
 */


// The Scribe key icon that changes based on light and dark mode as well as device.
internal var scribeKeyIcon = UIImage(named = "ScribeKeyPhoneBlack.png")

// Initialize all colors.
internal var keyColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var keyCharColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var specialKeyColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var keyPressedColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var commandKeyColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var commandBarColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var commandBarBorderColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0).cgColor
internal var keyboardBackColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var keyShadowColor = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0).cgColor

// annotate colors.
internal var annotateRed = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var annotateBlue = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var annotatePurple = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var annotateGreen = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal var annotateOrange = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)

/// Determines if the device is in dark mode and sets the color scheme.
internal fun checkDarkModeSetColors() {
    if (UITraitCollection.current.userInterfaceStyle == .light) {
        if (DeviceType.isPhone) {
            scribeKeyIcon = UIImage(named = "ScribeKeyPhoneBlack.png")
        } else if (DeviceType.isPad) {
            scribeKeyIcon = UIImage(named = "ScribeKeyPadBlack.png")
        }
        keyColor = UIColor.keyColorLight
        keyCharColor = UIColor.keyCharColorLight
        specialKeyColor = UIColor.specialKeyColorLight
        keyPressedColor = UIColor.keyPressedColorLight
        commandKeyColor = UIColor.commandKeyColorLight
        commandBarColor = UIColor.commandBarColorLight
        commandBarBorderColor = UIColor.commandBarBorderColorLight
        keyboardBackColor = UIColor.keyboardBackColorLight
        keyShadowColor = UIColor.keyShadowColorLight
        annotateRed = UIColor.annotateRedLight
        annotateBlue = UIColor.annotateBlueLight
        annotatePurple = UIColor.annotatePurpleLight
        annotateGreen = UIColor.annotateGreenLight
        annotateOrange = UIColor.annotateOrangeLight
    } else if (UITraitCollection.current.userInterfaceStyle == .dark) {
        if (DeviceType.isPhone) {
            scribeKeyIcon = UIImage(named = "ScribeKeyPhoneWhite.png")
        } else if (DeviceType.isPad) {
            scribeKeyIcon = UIImage(named = "ScribeKeyPadWhite.png")
        }
        keyColor = UIColor.keyColorDark
        keyCharColor = UIColor.keyCharColorDark
        specialKeyColor = UIColor.specialKeyColorDark
        keyPressedColor = UIColor.keyPressedColorDark
        commandKeyColor = UIColor.commandKeyColorDark
        commandBarColor = UIColor.commandBarColorDark
        commandBarBorderColor = UIColor.commandBarBorderColorDark
        keyboardBackColor = UIColor.keyboardBackColorDark
        keyShadowColor = UIColor.keyShadowColorDark
        annotateRed = UIColor.annotateRedDark
        annotateBlue = UIColor.annotateBlueDark
        annotatePurple = UIColor.annotatePurpleDark
        annotateGreen = UIColor.annotateGreenDark
        annotateOrange = UIColor.annotateOrangeDark
    }
}

/// Extends UIColor with branding colors as well as those for annotating nouns.
internal val UIColor.scribeGrey = UIColor(red = 100.0 / 255.0, green = 100.0 / 255.0, blue = 100.0 / 255.0, alpha = 0.9)

// Light theme.
internal val UIColor.scribeBlueLight = UIColor(red = 117.0 / 255.0, green = 206.0 / 255.0, blue = 250.0 / 255.0, alpha = 0.95)
internal val UIColor.keyColorLight = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal val UIColor.keyCharColorLight = UIColor(red = 0.0 / 255.0, green = 0.0 / 255.0, blue = 0.0 / 255.0, alpha = 0.9)
internal val UIColor.specialKeyColorLight = UIColor(red = 174.0 / 255.0, green = 179.0 / 255.0, blue = 190.0 / 255.0, alpha = 1.0)
internal val UIColor.keyPressedColorLight = UIColor(red = 233.0 / 255.0, green = 233.0 / 255.0, blue = 233.0 / 255.0, alpha = 1.0)
internal val UIColor.commandKeyColorLight = UIColor.scribeBlueLight
internal val UIColor.commandBarColorLight = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 1.0)
internal val UIColor.commandBarBorderColorLight = UIColor(red = 203.0 / 255.0, green = 203.0 / 255.0, blue = 206.0 / 255.0, alpha = 1.0).cgColor
internal val UIColor.keyboardBackColorLight = UIColor(red = 206.0 / 255.0, green = 210.0 / 255.0, blue = 217.0 / 255.0, alpha = 1.0)
internal val UIColor.keyShadowColorLight = UIColor(red = 0.0 / 255.0, green = 0.0 / 255.0, blue = 0.0 / 255.0, alpha = 0.35).cgColor
internal val UIColor.annotateRedLight = UIColor(red = 177.0 / 255.0, green = 27.0 / 255.0, blue = 39.0 / 255.0, alpha = 0.9)
internal val UIColor.annotateBlueLight = UIColor(red = 56.0 / 255.0, green = 101.0 / 255.0, blue = 168.0 / 255.0, alpha = 0.9)
internal val UIColor.annotatePurpleLight = UIColor(red = 122.0 / 255.0, green = 5.0 / 255.0, blue = 147.0 / 255.0, alpha = 0.9)
internal val UIColor.annotateGreenLight = UIColor(red = 65.0 / 255.0, green = 128.0 / 255.0, blue = 74.0 / 255.0, alpha = 0.9)
internal val UIColor.annotateOrangeLight = UIColor(red = 249.0 / 255.0, green = 106.0 / 255.0, blue = 78.0 / 255.0, alpha = 0.9)

// Dark theme.
internal val UIColor.scribeBlueDark = UIColor(red = 76.0 / 255.0, green = 173.0 / 255.0, blue = 230.0 / 255.0, alpha = 0.9)
internal val UIColor.keyColorDark = UIColor(red = 67.0 / 255.0, green = 67.0 / 255.0, blue = 67.0 / 255.0, alpha = 1.0)
internal val UIColor.keyCharColorDark = UIColor(red = 255.0 / 255.0, green = 255.0 / 255.0, blue = 255.0 / 255.0, alpha = 0.9)
internal val UIColor.specialKeyColorDark = UIColor(red = 32.0 / 255.0, green = 32.0 / 255.0, blue = 32.0 / 255.0, alpha = 1.0)
internal val UIColor.keyPressedColorDark = UIColor(red = 54.0 / 255.0, green = 54.0 / 255.0, blue = 54.0 / 255.0, alpha = 1.0)
internal val UIColor.commandKeyColorDark = UIColor.scribeBlueDark
internal val UIColor.commandBarColorDark = UIColor(red = 0.0 / 255.0, green = 0.0 / 255.0, blue = 0.0 / 255.0, alpha = 1.0)
internal val UIColor.commandBarBorderColorDark = UIColor(red = 70.0 / 255.0, green = 70.0 / 255.0, blue = 74.0 / 255.0, alpha = 1.0).cgColor
internal val UIColor.keyboardBackColorDark = UIColor(red = 30.0 / 255.0, green = 30.0 / 255.0, blue = 30.0 / 255.0, alpha = 1.0)
internal val UIColor.keyShadowColorDark = UIColor(red = 0.0 / 255.0, green = 0.0 / 255.0, blue = 0.0 / 255.0, alpha = 0.95).cgColor
internal val UIColor.annotateRedDark = UIColor(red = 248.0 / 255.0, green = 89.0 / 255.0, blue = 94.0 / 255.0, alpha = 0.9)
internal val UIColor.annotateBlueDark = UIColor(red = 32.0 / 255.0, green = 149.0 / 255.0, blue = 233.0 / 255.0, alpha = 0.9)
internal val UIColor.annotatePurpleDark = UIColor(red = 164.0 / 255.0, green = 92.0 / 255.0, blue = 235.0 / 255.0, alpha = 0.9)
internal val UIColor.annotateGreenDark = UIColor(red = 120.0 / 255.0, green = 188.0 / 255.0, blue = 97.0 / 255.0, alpha = 0.9)
internal val UIColor.annotateOrangeDark = UIColor(red = 254.0 / 255.0, green = 148.0 / 255.0, blue = 72.0 / 255.0, alpha = 0.9)
