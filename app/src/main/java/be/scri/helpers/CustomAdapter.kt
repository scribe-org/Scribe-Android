package be.scri.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import be.scri.R
import be.scri.models.ItemsViewModel
import be.scri.models.SwitchItem
import be.scri.models.TextItem

class CustomAdapter(private val mList: List<Any>, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_SWITCH = 1
        private const val VIEW_TYPE_TEXT = 2

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_view_with_image, parent, false)
                ImageViewHolder(view)
            }
            VIEW_TYPE_SWITCH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_view_with_switch, parent, false)
                SwitchViewHolder(view)
            }
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_view_text, parent, false)
                TextViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> bindImageViewHolder(holder, position)
            is SwitchViewHolder -> bindSwitchViewHolder(holder, position)
            is TextViewHolder -> bindTextViewHolder(holder, position)
        }

        val backgroundResource = when {
            mList.size == 1 -> R.drawable.rounded_all_corners
            position == 0 -> R.drawable.rounded_top
            position == mList.size - 1 -> R.drawable.rounded_bottom
            else -> R.drawable.rounded_middle
        }

        holder.itemView.setBackgroundResource(backgroundResource)
    }

    private fun bindImageViewHolder(holder: ImageViewHolder, position: Int) {
        val item = mList[position] as ItemsViewModel
        holder.imageView.setImageResource(item.image)
        holder.textView.text = getString(context, item.textResId)
        holder.imageView2.setImageResource(item.image2)

        holder.itemView.setOnClickListener {
            when {
                item.url != null -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                    context.startActivity(intent)
                }
                item.activity != null -> {
                    val intent = Intent(context, item.activity)
                    context.startActivity(intent)
                }
                item.action != null -> {
                    item.action.invoke()
                }
                else -> {
                    Toast.makeText(context, "No action defined", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindTextViewHolder(holder: TextViewHolder, position: Int) {
        val item = mList[position] as TextItem
        holder.textView.text = getString(context,item.text)
        holder.imageView.setImageResource(item.image)
        holder.itemView.setOnClickListener {
            when(item.action){
                null -> Toast.makeText(context, "No action defined", Toast.LENGTH_SHORT).show()
                else -> item.action.invoke()
            }
        }
    }

    private fun bindSwitchViewHolder(holder: SwitchViewHolder, position: Int) {
        val item = mList[position] as SwitchItem
        holder.switchView.isChecked = item.isChecked
        holder.switchView.setOnCheckedChangeListener(null)
        holder.textView.text = item.title
        holder.switchView.setOnCheckedChangeListener{_,isChecked ->
            if(isChecked){
                item.isChecked = isChecked
                item.action?.invoke()
            }
            else {
                item.isChecked = isChecked
                item.isChecked = false
                item.action2?.invoke()
            }

        }
    }

    override fun getItemCount(): Int = mList.size

    override fun getItemViewType(position: Int): Int {
        return when (mList[position]) {
            is ItemsViewModel -> VIEW_TYPE_IMAGE
            is SwitchItem -> VIEW_TYPE_SWITCH
            is TextItem -> VIEW_TYPE_TEXT
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgView1)
        val textView: TextView = itemView.findViewById(R.id.tvText)
        val imageView2: ImageView = itemView.findViewById(R.id.imgView2)
    }

    class SwitchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val switchView: Switch = itemView.findViewById(R.id.checkbox)
        val textView: TextView = itemView.findViewById(R.id.tvText)
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tvText)
        val imageView: ImageView = itemView.findViewById(R.id.imgView2)
    }
}


