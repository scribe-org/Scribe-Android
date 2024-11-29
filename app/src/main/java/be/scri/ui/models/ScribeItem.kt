package be.scri.ui.models

sealed class ScribeItem(
    open val title: String,
    open val desc: String?,
) {
    data class ClickableItem(
        override val title: String,
        override val desc: String? = null,
        val action: () -> Unit,
    ) : ScribeItem(title, desc)

    data class SwitchItem(
        override val title: String,
        override val desc: String,
        val state: Boolean,
        val onToggle: (Boolean) -> Unit,
    ) : ScribeItem(title, desc)

    data class ExternalLinkItem(
        override val title: String,
        override val desc: String,
        val url: String,
        val onClick: (String) -> Unit,
    ) : ScribeItem(title, desc)

    data class CustomItem(
        override val title: String,
        override val desc: String,
        val customAction: (Any?) -> Unit,
    ) : ScribeItem(title, desc)
}

enum class ItemType {
    CLICKABLE_ITEM,
    SWITCH_ITEM,
    EXTERNAL_LINK_ITEM,
    CUSTOM_ITEM,
}
