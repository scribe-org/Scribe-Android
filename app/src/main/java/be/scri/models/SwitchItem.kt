package be.scri.models

import kotlin.reflect.KFunction1

sealed class Item
data class SwitchItem(val title: String, var isChecked: Boolean, val action: (() -> Unit)? = null, val action2: (() -> Unit)? = null ):Item()
