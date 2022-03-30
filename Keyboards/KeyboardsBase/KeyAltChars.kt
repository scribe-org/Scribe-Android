//
//  KeyAltChars.kt
//
//  Functions and variables to create alternate key views.
//

/// Sets the alternates for certain keys given the chosen keyboard.
internal fun setKeyboardAlternateKeys() {
    if (DeviceType.isPhone) {
        keysWithAlternates += symbolKeysWithAlternatesLeft
        keysWithAlternates += symbolKeysWithAlternatesRight
        keysWithAlternates.append(currencySymbol)
        keysWithAlternatesLeft += symbolKeysWithAlternatesLeft
        keysWithAlternatesRight += symbolKeysWithAlternatesRight
        keysWithAlternatesRight.append(currencySymbol)
    }
    keyAlternatesDict = mapOf("a" to aAlternateKeys, "e" to eAlternateKeys, "е" to еAlternateKeys, // Russian е
    "i" to iAlternateKeys, "o" to oAlternateKeys, "u" to uAlternateKeys, "ä" to äAlternateKeys, "ö" to öAlternateKeys, "y" to yAlternateKeys, "s" to sAlternateKeys, "l" to lAlternateKeys, "z" to zAlternateKeys, "d" to dAlternateKeys, "c" to cAlternateKeys, "n" to nAlternateKeys, "ь" to ьAlternateKeys, "/" to backslashAlternateKeys, "?" to questionMarkAlternateKeys, "!" to exclamationAlternateKeys, "%" to percentAlternateKeys, "&" to ampersandAlternateKeys, "'" to apostropheAlternateKeys, "\"" to quotationAlternateKeys, "=" to equalSignAlternateKeys, currencySymbol to currencySymbolAlternates)
}
lateinit internal var alternatesKeyView: View
internal var alternatesShapeLayer = CAShapeLayer()
internal var keysWithAlternates = listOf<String>()
internal var alternateKeys = listOf<String>()

// Variables for alternate key view appearance.
internal var alternateBtnStartX = CGFloat(0)
internal var alternatesViewWidth = CGFloat(0)
internal var alternatesLongWidth = CGFloat(0)
internal var alternatesViewX = CGFloat(0)
internal var alternatesViewY = CGFloat(0)
internal var alternatesBtnHeight = CGFloat(0)
internal var alternatesCharHeight = CGFloat(0)

// The main currency symbol that will receive the alternates view for iPhones.
internal var currencySymbol: String = ""
internal var currencySymbolAlternates = listOf<String>()
internal val dollarAlternateKeys = listOf("¢", "₽", "₩", "¥", "£", "€")
internal val euroAlternateKeys = listOf("¢", "₽", "₩", "¥", "£", "$")
internal val roubleAlternateKeys = listOf("¢", "₩", "¥", "£", "$", "€")
internal val kronaAlternateKeys = listOf("¢", "₽", "¥", "£", "$", "€")

// Symbol keys that have consistent alternates for iPhones.
internal var symbolKeysWithAlternatesLeft = listOf("/", "?", "!", "%", "&")
internal val backslashAlternateKeys = listOf("\\")
internal val questionMarkAlternateKeys = listOf("¿")
internal val exclamationAlternateKeys = listOf("¡")
internal val percentAlternateKeys = listOf("‰")
internal val ampersandAlternateKeys = listOf("§")
internal var symbolKeysWithAlternatesRight = listOf("'", "\"", "=")
internal val apostropheAlternateKeys = listOf("`", "‘", "’")
internal val quotationAlternateKeys = listOf("«", "»", "„", "“", "”")
internal val equalSignAlternateKeys = listOf("≈", "±", "≠")
internal var keysWithAlternatesLeft = listOf<String>()
internal var keysWithAlternatesRight = listOf<String>()
internal var keyAlternatesDict = mapOf<String , listOf(String)>()
internal var aAlternateKeys = listOf<String>()
internal var eAlternateKeys = listOf<String>()
internal var еAlternateKeys = listOf<String>() // Russian е
internal var iAlternateKeys = listOf<String>()
internal var oAlternateKeys = listOf<String>()
internal var uAlternateKeys = listOf<String>()
internal var yAlternateKeys = listOf<String>()
internal var äAlternateKeys = listOf<String>()
internal var öAlternateKeys = listOf<String>()
internal var sAlternateKeys = listOf<String>()
internal var lAlternateKeys = listOf<String>()
internal var zAlternateKeys = listOf<String>()
internal var dAlternateKeys = listOf<String>()
internal var cAlternateKeys = listOf<String>()
internal var nAlternateKeys = listOf<String>()
internal var ьAlternateKeys = listOf<String>()

/// Creates the shape that allows left most buttons to pop up after being pressed.
///
/// - Parameters
///   - startX: the x-axis starting point.
///   - startY: the y-axis starting point.
///   - keyHeight: the height of the key.
///   - numAlternates: the number of alternate characters to display.
///   - side: the side of the keyboard that the key is found.
internal fun setAlternatesPathState(startY: CGFloat, keyWidth: CGFloat, keyHeight: CGFloat, numAlternates: CGFloat, side: String) {
    if (DeviceType.isPad) {
        widthMultiplier = 0.2
        maxHeightMultiplier = 2.05
        if (isLandscapeView == true) {
            maxHeightMultiplier = 1.95
        }
    } else if (DeviceType.isPhone) {
        widthMultiplier = 0.4
        maxHeightMultiplier = 2.125
        if (isLandscapeView == true) {
            widthMultiplier = 0.2
        }
    }
    maxHeight = vertStart - (keyHeight * maxHeightMultiplier)
    maxHeightCurveControl = vertStart - (keyHeight * (maxHeightMultiplier - 0.125))
    minHeightCurveControl = vertStart - (keyHeight * 0.005)
    if (DeviceType.isPhone) {
        heightBeforeTopCurves = vertStart - (keyHeight * 1.8)
        maxWidthCurveControl = keyWidth * 0.5
    } else if (DeviceType.isPad || (DeviceType.isPhone && isLandscapeView == true)) {
        heightBeforeTopCurves = vertStart - (keyHeight * 1.6)
        maxWidthCurveControl = keyWidth * 0.25
    }
    if (side == "left") {
        alternatesLongWidth = horizStart + (keyWidth * numAlternates + (3.0 * numAlternates) + 8.0)
    } else if (side == "right") {
        alternatesLongWidth = horizStart + keyWidth - CGFloat(keyWidth * numAlternates + (3.0 * numAlternates) + 8.0)
    }
    alternatesShapeLayer.strokeColor = keyShadowColor
    alternatesShapeLayer.fillColor = keyColor.cgColor
    alternatesShapeLayer.lineWidth = 1.0
}

/// Creates the shape that allows alternate keys to be displayed to the user for keys on the left side of the keyboard.
///
/// - Parameters
///   - startX: the x-axis starting point.
///   - startY: the y-axis starting point.
///   - keyWidth: the width of the key.
///   - keyHeight: the height of the key.
///   - numAlternates: the number of alternate characters to display.
internal fun alternateKeysPathLeft(startX: CGFloat, startY: CGFloat, keyWidth: CGFloat, keyHeight: CGFloat, numAlternates: CGFloat) : UIBezierPath {
    // Starting positions need to be updated.
    horizStart = startX
    vertStart = startY + keyHeight
    setAlternatesPathState(startY = startY, keyWidth = keyWidth, keyHeight = keyHeight, numAlternates = numAlternates, side = "left")
    // Path is clockwise from bottom left.
    val path = UIBezierPath()
    path.move(to = CGPoint(x = horizStart + (keyWidth * 0.075), y = vertStart))
    // Curve up past bottom left, path up, and curve out to the left.
    path.addCurve(to = CGPoint(x = horizStart, y = vertStart - (keyHeight * 0.075)), controlPoint1 = CGPoint(x = horizStart + (keyWidth * 0.075), y = minHeightCurveControl), controlPoint2 = CGPoint(x = horizStart, y = minHeightCurveControl))
    path.addLine(to = CGPoint(x = horizStart, y = vertStart - (keyHeight * 0.85)))
    path.addCurve(to = CGPoint(x = horizStart - (keyWidth * widthMultiplier), y = vertStart - (keyHeight * 1.2)), controlPoint1 = CGPoint(x = horizStart, y = vertStart - (keyHeight * 0.9)), controlPoint2 = CGPoint(x = horizStart - (keyWidth * widthMultiplier), y = vertStart - (keyHeight * 1.05)))
    // Path up and curve right past the top left.
    path.addLine(to = CGPoint(x = horizStart - (keyWidth * widthMultiplier), y = heightBeforeTopCurves))
    path.addCurve(to = CGPoint(x = horizStart + (keyWidth * 0.075), y = maxHeight), controlPoint1 = CGPoint(x = horizStart - (keyWidth * widthMultiplier), y = maxHeightCurveControl), controlPoint2 = CGPoint(x = horizStart - (keyWidth * 0.25), y = maxHeight))
    // Path right, curve down past the top right, and path down.
    path.addLine(to = CGPoint(x = alternatesLongWidth - maxWidthCurveControl, y = maxHeight))
    path.addCurve(to = CGPoint(x = alternatesLongWidth, y = heightBeforeTopCurves), controlPoint1 = CGPoint(x = alternatesLongWidth - (keyWidth * 0.2), y = maxHeight), controlPoint2 = CGPoint(x = alternatesLongWidth, y = maxHeightCurveControl))
    path.addLine(to = CGPoint(x = alternatesLongWidth, y = vertStart - (keyHeight * 1.15)))
    // Curve down past the left and path left.
    path.addCurve(to = CGPoint(x = alternatesLongWidth - maxWidthCurveControl, y = vertStart - (keyHeight * 0.95)), controlPoint1 = CGPoint(x = alternatesLongWidth, y = vertStart - (keyHeight * 1.05)), controlPoint2 = CGPoint(x = alternatesLongWidth - (keyWidth * 0.2), y = vertStart - (keyHeight * 0.95)))
    path.addLine(to = CGPoint(x = horizStart + (keyWidth * 1.15), y = vertStart - (keyHeight * 0.95)))
    // Curve in to the left, go down, and curve down past bottom left.
    path.addCurve(to = CGPoint(x = horizStart + keyWidth, y = vertStart - (keyHeight * 0.85)), controlPoint1 = CGPoint(x = horizStart + (keyWidth * 1.05), y = vertStart - (keyHeight * 0.95)), controlPoint2 = CGPoint(x = horizStart + keyWidth, y = vertStart - (keyHeight * 0.875)))
    path.addLine(to = CGPoint(x = horizStart + keyWidth, y = vertStart - (keyHeight * 0.075)))
    path.addCurve(to = CGPoint(x = horizStart + (keyWidth * 0.925), y = vertStart), controlPoint1 = CGPoint(x = horizStart + keyWidth, y = minHeightCurveControl), controlPoint2 = CGPoint(x = horizStart + (keyWidth * 0.925), y = minHeightCurveControl))
    path.close()
    return path
}

/// Creates the shape that allows alternate keys to be displayed to the user for keys on the left side of the keyboard.
///
/// - Parameters
///   - startX: the x-axis starting point.
///   - startY: the y-axis starting point.
///   - keyWidth: the width of the key.
///   - keyHeight: the height of the key.
///   - numAlternates: the number of alternate characters to display.
internal fun alternateKeysPathRight(startX: CGFloat, startY: CGFloat, keyWidth: CGFloat, keyHeight: CGFloat, numAlternates: CGFloat) : UIBezierPath {
    // Starting positions need to be updated.
    horizStart = startX
    vertStart = startY + keyHeight
    setAlternatesPathState(startY = startY, keyWidth = keyWidth, keyHeight = keyHeight, numAlternates = numAlternates, side = "right")
    // Path is clockwise from bottom left.
    val path = UIBezierPath()
    path.move(to = CGPoint(x = horizStart + (keyWidth * 0.075), y = vertStart))
    // Curve up past bottom left, path up, and curve out to the left.
    path.addCurve(to = CGPoint(x = horizStart, y = vertStart - (keyHeight * 0.075)), controlPoint1 = CGPoint(x = horizStart + (keyWidth * 0.075), y = minHeightCurveControl), controlPoint2 = CGPoint(x = horizStart, y = minHeightCurveControl))
    path.addLine(to = CGPoint(x = horizStart, y = vertStart - (keyHeight * 0.85)))
    path.addCurve(to = CGPoint(x = horizStart - (keyWidth * 0.15), y = vertStart - (keyHeight * 0.95)), controlPoint1 = CGPoint(x = horizStart, y = vertStart - (keyHeight * 0.875)), controlPoint2 = CGPoint(x = horizStart - (keyWidth * 0.05), y = vertStart - (keyHeight * 0.95)))
    // Path left and path up past the left.
    path.addLine(to = CGPoint(x = alternatesLongWidth + maxWidthCurveControl, y = vertStart - (keyHeight * 0.95)))
    path.addCurve(to = CGPoint(x = alternatesLongWidth, y = vertStart - (keyHeight * 1.15)), controlPoint1 = CGPoint(x = alternatesLongWidth + (keyWidth * 0.2), y = vertStart - (keyHeight * 0.95)), controlPoint2 = CGPoint(x = alternatesLongWidth, y = vertStart - (keyHeight * 1.05)))
    // Path up and curve up past the top left.
    path.addLine(to = CGPoint(x = alternatesLongWidth, y = heightBeforeTopCurves))
    path.addCurve(to = CGPoint(x = alternatesLongWidth + maxWidthCurveControl, y = maxHeight), controlPoint1 = CGPoint(x = alternatesLongWidth, y = maxHeightCurveControl), controlPoint2 = CGPoint(x = alternatesLongWidth + (keyWidth * 0.2), y = maxHeight))
    // Path right, curve down past the top right, and path down.
    path.addLine(to = CGPoint(x = horizStart + (keyWidth * 0.925), y = maxHeight))
    path.addCurve(to = CGPoint(x = horizStart + (keyWidth * (1 + widthMultiplier)), y = heightBeforeTopCurves), controlPoint1 = CGPoint(x = horizStart + (keyWidth * 1.25), y = maxHeight), controlPoint2 = CGPoint(x = horizStart + (keyWidth * (1 + widthMultiplier)), y = maxHeightCurveControl))
    path.addLine(to = CGPoint(x = horizStart + (keyWidth * (1 + widthMultiplier)), y = vertStart - (keyHeight * 1.2)))
    // Curve in to the left, go down, and curve down past bottom left.
    path.addCurve(to = CGPoint(x = horizStart + keyWidth, y = vertStart - (keyHeight * 0.85)), controlPoint1 = CGPoint(x = horizStart + (keyWidth * (1 + widthMultiplier)), y = vertStart - (keyHeight * 1.05)), controlPoint2 = CGPoint(x = horizStart + keyWidth, y = vertStart - (keyHeight * 0.9)))
    path.addLine(to = CGPoint(x = horizStart + keyWidth, y = vertStart - (keyHeight * 0.075)))
    path.addCurve(to = CGPoint(x = horizStart + (keyWidth * 0.925), y = vertStart), controlPoint1 = CGPoint(x = horizStart + keyWidth, y = minHeightCurveControl), controlPoint2 = CGPoint(x = horizStart + (keyWidth * 0.925), y = minHeightCurveControl))
    path.close()
    return path
}

/// Generates an alternates view to select other characters related to a long held key.
///
/// - Parameters
///   - sender: the long press of the given key.
internal fun genAlternatesView(key: Button) {
    // Get the frame in respect to the superview.
    val frame: CGRect = (key.superview?.convert(key.frame, to = null))!!
    val width = key.frame.width
    // Derive which button was pressed and get its alternates.
    val char: String = key.layer.value(forKey = "original") as? String ?: ""
    alternateKeys = keyAlternatesDict[char] ?: listOf("")
    // Add the original key given its location on the keyboard.
    if (keysWithAlternatesLeft.contains(char)) {
        alternateKeys.insert(char, at = 0)
    } else if (keysWithAlternatesRight.contains(char)) {
        alternateKeys.append(char)
    }
    val numAlternates: CGFloat = CGFloat(alternateKeys.size)
    if (keysWithAlternatesLeft.contains(char)) {
        alternatesViewX = frame.origin.x - 4.0
        alternatesShapeLayer.path = alternateKeysPathLeft(startX = frame.origin.x, startY = frame.origin.y, keyWidth = width, keyHeight = key.frame.height, numAlternates = numAlternates).cgPath
    } else if (keysWithAlternatesRight.contains(char)) {
        alternatesViewX = frame.origin.x + width - CGFloat(width * numAlternates + (3.0 * numAlternates) + 2.0)
        alternatesShapeLayer.path = alternateKeysPathRight(startX = frame.origin.x, startY = frame.origin.y, keyWidth = width, keyHeight = key.frame.height, numAlternates = numAlternates).cgPath
    }
    if (numAlternates > 0) {
        alternatesViewWidth = CGFloat(width * numAlternates + (3.0 * numAlternates) + 8.0)
    }
    alternatesViewY = frame.origin.y - key.frame.height * 1.135
    alternatesBtnHeight = key.frame.height * 0.9
    alternatesKeyView = View(frame = CGRect(x = alternatesViewX, y = alternatesViewY, width = alternatesViewWidth, height = key.frame.height * 1.2))
    alternatesKeyView.tag = 1001
    key.backgroundColor = keyColor
}
