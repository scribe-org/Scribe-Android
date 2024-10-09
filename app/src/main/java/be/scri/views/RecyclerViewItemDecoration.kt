import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

class CustomDividerItemDecoration(
    context: Context,
    private val drawable: Drawable,
    private val width: Int,
    private val marginLeft: Int,
    private val marginRight: Int
) : RecyclerView.ItemDecoration() {

    override fun onDraw(@NonNull canvas: Canvas, @NonNull parent: RecyclerView, @NonNull state: RecyclerView.State) {
        val left = parent.paddingLeft + marginLeft
        val right = parent.width - parent.paddingRight - marginRight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) { // Exclude the last item
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + width

            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, width)
    }
}
