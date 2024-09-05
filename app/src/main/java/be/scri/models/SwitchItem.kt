package be.scri.models

sealed class Item

data class SwitchItem(
    val title: String,
    var isChecked: Boolean,
    val action: (() -> Unit)? = null,
    val action2: (() -> Unit)? = null,
) : Item()
