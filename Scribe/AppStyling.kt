//
//  AppStyling.kt
//
//  Functions to style app elements.
//

/// Applies a shadow to a given UI element.
///
/// - Parameters
///  - elem: the element to have shadows added to.
internal fun applyShadowEffects(elem: Any) {
    elem.layer.shadowColor = UIColor.keyShadowColorLight
    elem.layer.shadowOffset = CGSize(width = 0.0, height = 3.0)
    elem.layer.shadowOpacity = 1.0
    elem.layer.shadowRadius = 3.0
}

/// Applies a corner radius to a given UI element.
///
/// - Parameters
///  - elem: the element to have shadows added to.
internal fun applyCornerRadius(elem: Any, radius: CGFloat) {
    elem.layer.masksToBounds = false
    elem.layer.cornerRadius = radius
}
