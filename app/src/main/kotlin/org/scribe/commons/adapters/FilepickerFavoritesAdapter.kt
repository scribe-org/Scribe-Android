package org.scribe.commons.adapters

import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.filepicker_favorite.view.*
import org.scribe.R
import org.scribe.commons.activities.BaseSimpleActivity
import org.scribe.commons.extensions.getTextSize
import org.scribe.commons.views.MyRecyclerView

class FilepickerFavoritesAdapter(
    activity: BaseSimpleActivity, val paths: List<String>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private var fontSize = 0f

    init {
        fontSize = activity.getTextSize()
    }

    override fun getActionMenuId() = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.filepicker_favorite, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = paths[position]
        holder.bindView(path, true, false) { itemView, adapterPosition ->
            setupView(itemView, path)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = paths.size

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = paths.size

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemKeyPosition(key: Int) = paths.indexOfFirst { it.hashCode() == key }

    override fun getItemSelectionKey(position: Int) = paths[position].hashCode()

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    private fun setupView(view: View, path: String) {
        view.apply {
            filepicker_favorite_label.text = path
            filepicker_favorite_label.setTextColor(textColor)
            filepicker_favorite_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        }
    }
}
