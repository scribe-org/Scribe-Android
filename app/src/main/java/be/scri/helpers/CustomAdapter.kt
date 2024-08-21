package be.scri.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import be.scri.R
import be.scri.models.ItemsViewModel


class CustomAdapter(private val mList: List<ItemsViewModel> , private val context:Context) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewModel = mList[position]
        holder.imageView.setImageResource(itemViewModel.image)

        holder.textView.text = getString(context,itemViewModel.textResId)
        holder.imageView2.setImageResource(itemViewModel.image2)
        if (position == 0) {
            holder.itemView.setBackgroundResource(R.drawable.rounded_top);
        } else if (position == mList.size - 1) {
            holder.itemView.setBackgroundResource(R.drawable.rounded_bottom);

        } else {
            holder.itemView.setBackgroundResource(R.drawable.rounded_middle);
        }
        holder.itemView.setOnClickListener {
            when {
                itemViewModel.url != null -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(itemViewModel.url))
                    context.startActivity(intent)
                }
                itemViewModel.activity != null -> {
                    val intent = Intent(context, itemViewModel.activity)
                    context.startActivity(intent)
                }
                itemViewModel.action != null -> {
                    itemViewModel.action.invoke()
                }
                else -> {
                    Toast.makeText(context, "No action defined", Toast.LENGTH_SHORT).show()
        }}}}




    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgView1)
        val textView: TextView = itemView.findViewById(R.id.tvText)
        val imageView2: ImageView = itemView.findViewById(R.id.imgView2)
        val activity: Activity = itemView.context as Activity

    }
}
