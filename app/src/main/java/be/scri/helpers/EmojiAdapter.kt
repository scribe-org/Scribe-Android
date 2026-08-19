// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.scri.R

/**
 * Displaying emojis, kaomojis, and category headers in the emoji palette.
 *
 * @param context The application context.
 * @param items The list of items to display, either categories, emojis, or kaomojis.
 * @param categoryHeaders Localized headers map for categories.
 * @param itemClick Callback invoked when the user taps an emoji.
 * @param kaomojiClick Callback invoked when the user taps a kaomoji.
 */
class EmojiAdapter(
    val context: Context,
    var items: List<Item>,
    val categoryHeaders: Map<String, String> = emptyMap(),
    val itemClick: (emoji: EmojiData) -> Unit,
    val kaomojiClick: ((kaomoji: KaomojiData) -> Unit)? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            ITEM_TYPE_EMOJI ->
                EmojiViewHolder(
                    layoutInflater.inflate(R.layout.item_emoji, parent, false),
                )
            ITEM_TYPE_KAOMOJI ->
                KaomojiViewHolder(
                    layoutInflater.inflate(R.layout.item_kaomoji, parent, false),
                )
            ITEM_TYPE_CATEGORY ->
                EmojiCategoryViewHolder(
                    layoutInflater.inflate(R.layout.item_emoji_category_title, parent, false),
                )
            else -> throw IllegalArgumentException("Unsupported view type: $viewType")
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is EmojiViewHolder -> holder.bindView(items[position] as Item.Emoji)
            is KaomojiViewHolder -> holder.bindView(items[position] as Item.Kaomoji)
            is EmojiCategoryViewHolder -> holder.bindView(items[position] as Item.Category)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is Item.Emoji -> ITEM_TYPE_EMOJI
            is Item.Kaomoji -> ITEM_TYPE_KAOMOJI
            is Item.Category -> ITEM_TYPE_CATEGORY
        }

    override fun getItemCount() = items.size

    /**
     * Update the adapter's item list and refreshes the RecyclerView.
     *
     * @param emojiItems The new list of items to display.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(emojiItems: List<Item>) {
        items = emojiItems
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for a single emoji item.
     *
     * @param view The item_emoji view.
     */
    inner class EmojiViewHolder(
        view: android.view.View,
    ) : RecyclerView.ViewHolder(view) {
        private val emojiValue: TextView = view.findViewById(R.id.emoji_value)

        fun bindView(emoji: Item.Emoji) {
            emojiValue.text = emoji.emojiData.emoji
            itemView.setOnClickListener {
                itemClick.invoke(emoji.emojiData)
            }
        }
    }

    /**
     * ViewHolder for a single kaomoji item.
     *
     * @param view The item_kaomoji view.
     */
    inner class KaomojiViewHolder(
        view: android.view.View,
    ) : RecyclerView.ViewHolder(view) {
        private val kaomojiValue: TextView = view.findViewById(R.id.kaomoji_value)

        fun bindView(kaomoji: Item.Kaomoji) {
            kaomojiValue.text = kaomoji.kaomojiData.kaomoji
            itemView.setOnClickListener {
                kaomojiClick?.invoke(kaomoji.kaomojiData)
            }
        }
    }

    /**
     * ViewHolder for a category header.
     *
     * @param view The item_emoji_category_title view.
     */
    inner class EmojiCategoryViewHolder(
        view: android.view.View,
    ) : RecyclerView.ViewHolder(view) {
        private val emojiCategoryTitle: TextView = view.findViewById(R.id.emoji_category_title)

        fun bindView(category: Item.Category) {
            emojiCategoryTitle.text = categoryHeaders[category.value] ?: category.value
        }
    }

    /**
     * Sealed interface representing items in the emoji list.
     */
    sealed interface Item {
        // A single tappable emoji.
        data class Emoji(
            val emojiData: EmojiData,
        ) : Item

        // A single tappable kaomoji.
        data class Kaomoji(
            val kaomojiData: KaomojiData,
        ) : Item

        // A category header row.
        data class Category(
            val value: String,
        ) : Item
    }

    companion object {
        private const val ITEM_TYPE_EMOJI = 0
        private const val ITEM_TYPE_KAOMOJI = 1
        private const val ITEM_TYPE_CATEGORY = 2
    }
}
