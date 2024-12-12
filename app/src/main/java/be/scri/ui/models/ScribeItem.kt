package be.scri.ui.models

import androidx.annotation.DrawableRes

sealed class ScribeItem(
    open val title: Int,
    open val desc: Int?,
) {
    data class ClickableItem(
        override val title: Int,
        override val desc: Int? = null,
        val action: () -> Unit,
    ) : ScribeItem(title, desc)

    data class SwitchItem(
        override val title: Int,
        override val desc: Int,
        val state: Boolean,
        val onToggle: (Boolean) -> Unit,
    ) : ScribeItem(title, desc)

    data class ExternalLinkItem(
        override val title: Int,
        override val desc: Int? = null,
        @DrawableRes val leadingIcon: Int,
        @DrawableRes val trailingIcon: Int,
        val url: String?,
        val onClick: () -> Unit,
    ) : ScribeItem(title, desc)

    data class CustomItem(
        override val title: Int,
        override val desc: Int,
        val customAction: (Any?) -> Unit,
    ) : ScribeItem(title, desc)
}
