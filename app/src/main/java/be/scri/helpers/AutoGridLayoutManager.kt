// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A GridLayoutManager that automatically calculates the number of columns
 * based on the available width and desired item width.
 *
 * @param context The application context.
 * @param itemWidth The desired width of each item in pixels.
 */
class AutoGridLayoutManager(
    context: Context,
    private val itemWidth: Int,
) : GridLayoutManager(context, 1) {
     // Recalculates the span count based on available width before laying out children.
    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?,
    ) {
        val width = width
        val height = height
        if (itemWidth > 0 && width > 0 && height > 0) {
            val totalSpace = width - paddingRight - paddingLeft
            val spanCount = maxOf(1, totalSpace / itemWidth)
            setSpanCount(spanCount)
        }
        super.onLayoutChildren(recycler, state)
    }
}
