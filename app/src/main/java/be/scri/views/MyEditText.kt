package be.scri.views

import android.content.Context
import android.util.AttributeSet
import be.scri.extensions.adjustAlpha
import be.scri.extensions.applyColorFilter
import be.scri.helpers.MEDIUM_ALPHA

class MyEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

}
