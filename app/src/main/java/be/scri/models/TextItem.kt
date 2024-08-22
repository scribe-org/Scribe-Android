package be.scri.models


data class TextItem(val text: Int, val image:Int, val action: (() -> Unit)? = null, val language: String? = null ):Item()
