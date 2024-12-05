package be.scri.ui.models

import androidx.annotation.DrawableRes

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
        override val desc: String? = null,
        @DrawableRes val leadingIcon: Int,
        @DrawableRes val trailingIcon: Int,
        val url: String?,
        val onClick: () -> Unit,
    ) : ScribeItem(title, desc)

    data class CustomItem(
        override val title: String,
        override val desc: String,
        val customAction: (Any?) -> Unit,
    ) : ScribeItem(title, desc)
}
