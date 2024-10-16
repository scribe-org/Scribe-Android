package be.scri.extensions

import android.widget.RemoteViews

fun RemoteViews.setBackgroundColor(
    id: Int,
    color: Int,
) {
    setInt(id, "setBackgroundColor", color)
}
