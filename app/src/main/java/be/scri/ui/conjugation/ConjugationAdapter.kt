// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")

package be.scri.ui.conjugation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import be.scri.R
import be.scri.databinding.ItemConjugationBinding
import be.scri.repository.VerbConjugationRow

class ConjugationAdapter(
    private val onCopied: () -> Unit,
) : ListAdapter<VerbConjugationRow, ConjugationAdapter.VH>(
        object : DiffUtil.ItemCallback<VerbConjugationRow>() {
            override fun areItemsTheSame(
                a: VerbConjugationRow,
                b: VerbConjugationRow,
            ): Boolean =
                a.tense == b.tense &&
                    a.personLabel == b.personLabel &&
                    a.form == b.form

            override fun areContentsTheSame(
                a: VerbConjugationRow,
                b: VerbConjugationRow,
            ): Boolean = a == b
        },
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VH {
        val binding =
            ItemConjugationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return VH(binding)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class VH(
        private val binding: ItemConjugationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: VerbConjugationRow) {
            val context = binding.root.context
            binding.tenseText.text = row.tense
            binding.personText.text = row.personLabel
            binding.formText.text = row.form
            val copyable = row.hasCopyableForm()
            binding.copyFormButton.isEnabled = copyable
            binding.copyFormButton.alpha = if (copyable) 1f else 0.38f
            binding.copyFormButton.setOnClickListener {
                if (!row.hasCopyableForm()) return@setOnClickListener
                val text = row.form
                val label = context.getString(R.string.app_conjugate_copy)
                val clip = ClipData.newPlainText(label, text)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(clip)
                onCopied()
            }
        }
    }
}
