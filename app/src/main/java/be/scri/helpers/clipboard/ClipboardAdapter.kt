package be.scri.helpers.clipboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.scri.R

class ClipboardAdapter(
    private var items: List<ClipboardItem>,
    private val onItemClick: (ClipboardItem) -> Unit,
    private val onItemDelete: (ClipboardItem) -> Unit,
    private val onItemPinToggle: (ClipboardItem) -> Unit,
) : RecyclerView.Adapter<ClipboardAdapter.ViewHolder>() {
    class ViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val clipText: TextView = view.findViewById(R.id.clip_text)
        val pinIcon: ImageView = view.findViewById(R.id.pin_icon)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.clipboard_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = items[position]
        holder.clipText.text = item.text
        holder.pinIcon.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.itemView.setOnLongClickListener { view ->
            val context = view.context
            val popup = PopupMenu(context, view)
            popup.menu.add(if (item.isPinned) "Unpin" else "Pin")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Pin", "Unpin" -> onItemPinToggle(item)
                    "Delete" -> onItemDelete(item)
                }
                true
            }
            popup.show()
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ClipboardItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
