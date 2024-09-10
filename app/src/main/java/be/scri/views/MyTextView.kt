package be.scri.views

import android.content.Context
import android.util.AttributeSet

class MyTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(
        textColor: Int,
        accentColor: Int,
        backgroundColor: Int,
    ) {
        setTextColor(textColor)
        setLinkTextColor(accentColor)
    }
}
