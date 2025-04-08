// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Adds a custom item decoration (divider) to a RecyclerView, using a specified drawable and custom margins.
 */

package be.scri.extensions

import CustomDividerItemDecoration
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import be.scri.R

fun RecyclerView.addCustomItemDecoration(context: android.content.Context) {
    val itemDecoration =
        CustomDividerItemDecoration(
            drawable = getDrawable(context, R.drawable.rv_divider)!!,
            width = 1,
            marginLeft = 50,
            marginRight = 50,
        )
    addItemDecoration(itemDecoration)
}
