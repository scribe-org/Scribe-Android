package be.scri.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import be.scri.R
import be.scri.extensions.isRTLLayout
import be.scri.extensions.onGlobalLayout

class LineColorPicker(
    context: Context,
    attrs: AttributeSet,
) : LinearLayout(context, attrs) {
    private var colorsCount = 0
    private var pickerWidth = 0
    private var stripeWidth = 0
    private var unselectedMargin = 0
    private var lastColorIndex = -1
    private var wasInit = false
    private var colors = ArrayList<Int>()

    init {
        unselectedMargin = context.resources.getDimension(R.dimen.line_color_picker_margin).toInt()
        onGlobalLayout {
            if (pickerWidth == 0) {
                pickerWidth = width

                if (colorsCount != 0) {
                    stripeWidth = width / colorsCount
                }
            }

            if (!wasInit) {
                wasInit = true
                updateItemMargin(lastColorIndex, false)
            }
        }
        orientation = HORIZONTAL

        setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    if (pickerWidth != 0 && stripeWidth != 0) {
                        touchAt(motionEvent.x.toInt())
                    }
                }
            }
            true
        }
    }

    private fun touchAt(touchX: Int) {
        var colorIndex = touchX / stripeWidth
        if (context.isRTLLayout) {
            colorIndex = colors.size - colorIndex - 1
        }
        val index = Math.max(0, Math.min(colorIndex, colorsCount - 1))
        if (lastColorIndex != index) {
            updateItemMargin(lastColorIndex, true)
            lastColorIndex = index
            updateItemMargin(index, false)
        }
    }

    private fun updateItemMargin(
        index: Int,
        addMargin: Boolean,
    ) {
        getChildAt(index)?.apply {
            (layoutParams as LayoutParams).apply {
                topMargin = if (addMargin) unselectedMargin else 0
                bottomMargin = if (addMargin) unselectedMargin else 0
            }
            requestLayout()
        }
    }
}
