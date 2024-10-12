package be.scri.extensions

import android.content.res.Resources
import android.graphics.drawable.Drawable

fun Resources.getColoredDrawableWithColor(
    drawableId: Int,
    color: Int,
    alpha: Int = 255,
): Drawable {
    val drawable = getDrawable(drawableId)
    drawable.mutate().applyColorFilter(color)
    drawable.mutate().alpha = alpha
    return drawable
}
