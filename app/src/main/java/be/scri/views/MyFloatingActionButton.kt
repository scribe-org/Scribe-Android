package be.scri.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import be.scri.extensions.applyColorFilter
import be.scri.extensions.getContrastColor
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyFloatingActionButton : FloatingActionButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(
        textColor: Int,
        accentColor: Int,
        backgroundColor: Int,
    ) {
        backgroundTintList = ColorStateList.valueOf(accentColor)
        applyColorFilter(accentColor.getContrastColor())
    }
}
