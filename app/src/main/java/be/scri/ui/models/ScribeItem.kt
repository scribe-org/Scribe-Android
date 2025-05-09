// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.models

import androidx.annotation.DrawableRes

/** A class defining different types of items used in the application interface. */
sealed class ScribeItem(
    open val title: Int,
    open val desc: Int?,
) {
    /**
     * Represents a clickable item in the Scribe UI, typically used for actions like navigation or
     * triggering events.
     *
     * Inherits from [ScribeItem] and adds a lambda [action] that defines what happens when the item
     * is clicked.
     *
     * @property title The string resource ID for the item's title.
     * @property desc (Optional) The string resource ID for the item's description.
     * @property action The callback function to invoke when the item is clicked.
     */
    data class ClickableItem(
        override val title: Int,
        override val desc: Int? = null,
        val action: () -> Unit,
    ) : ScribeItem(title, desc)

    /**
     * Represents a toggleable switch item in the Scribe UI.
     *
     * Inherits from [ScribeItem] and adds a [state] to represent the current switch value, and a
     * [onToggle] lambda to handle state changes when toggled.
     *
     * @property title The string resource ID for the item's title.
     * @property desc The string resource ID for the item's description.
     * @property state The current state of the switch (true for on, false for off).
     * @property onToggle The callback function invoked with the new state when the switch is
     * toggled.
     */
    data class SwitchItem(
        override val title: Int,
        override val desc: Int,
        val state: Boolean,
        val onToggle: (Boolean) -> Unit,
    ) : ScribeItem(title, desc)

    /**
     * Represents an external link item in the Scribe UI.
     *
     * Inherits from [ScribeItem] and adds support for leading and trailing icons, an external URL,
     * and a click handler.
     *
     * @property title The string resource ID for the item's title.
     * @property desc (Optional) The string resource ID for the item's description.
     * @property leadingIcon The drawable resource ID for the icon displayed before the title.
     * @property trailingIcon The drawable resource ID for the icon displayed after the title.
     * @property url The external URL to open when the item is clicked.
     * @property onClick The callback function invoked when the item is clicked.
     */
    data class ExternalLinkItem(
        override val title: Int,
        override val desc: Int? = null,
        @DrawableRes val leadingIcon: Int,
        @DrawableRes val trailingIcon: Int,
        val url: String?,
        val onClick: () -> Unit,
    ) : ScribeItem(title, desc)

    /**
     * Represents a custom item in the Scribe UI.
     *
     * Inherits from [ScribeItem] and adds support for executing a custom action.
     *
     * @property title The string resource ID for the item's title.
     * @property desc (Optional) The string resource ID for the item's description.
     * @property customAction The callback function to execute when the item is triggered.
     */
    data class CustomItem(
        override val title: Int,
        override val desc: Int,
        val customAction: (Any?) -> Unit,
    ) : ScribeItem(title, desc)
}
