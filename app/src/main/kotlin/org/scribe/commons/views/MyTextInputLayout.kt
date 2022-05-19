package org.scribe.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import org.scribe.commons.extensions.adjustAlpha
import org.scribe.commons.extensions.onTextChangeListener
import org.scribe.commons.extensions.value
import org.scribe.commons.helpers.HIGHER_ALPHA

class MyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    // we need to use reflection to make some colors work well
    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        try {
            editText!!.setTextColor(textColor)
            editText!!.backgroundTintList = ColorStateList.valueOf(accentColor)

            val hintColor = if (editText!!.value.isEmpty()) textColor.adjustAlpha(HIGHER_ALPHA) else textColor
            val defaultTextColor = TextInputLayout::class.java.getDeclaredField("defaultHintTextColor")
            defaultTextColor.isAccessible = true
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))

            val focusedTextColor = TextInputLayout::class.java.getDeclaredField("focusedTextColor")
            focusedTextColor.isAccessible = true
            focusedTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(accentColor)))

            editText!!.onTextChangeListener { text ->
                val hintTextColor = if (text.isEmpty()) textColor.adjustAlpha(HIGHER_ALPHA) else textColor
                defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintTextColor)))
            }
        } catch (e: Exception) {
        }
    }
}
