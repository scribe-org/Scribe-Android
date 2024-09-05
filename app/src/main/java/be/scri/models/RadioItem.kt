package be.scri.models

data class RadioItem(
    val id: Int,
    val title: String,
    val value: Any = id,
)
