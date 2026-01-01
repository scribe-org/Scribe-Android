// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.R
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScribeItemListTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun displaysClickableItems() {
        val items =
            listOf(
                ScribeItem.ClickableItem(
                    title = R.string.i18n_app_settings_menu_app_language,
                    desc = R.string.i18n_app_settings_menu_app_language_description,
                    action = { },
                ),
                ScribeItem.ClickableItem(
                    title = R.string.i18n_app_settings_keyboard_title,
                    desc = null,
                    action = { },
                ),
            )

        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(items))
        }

        composeRule.onNodeWithTag("scribe_item_list").assertIsDisplayed()
        composeRule.onAllNodesWithTag("scribe_item").assertCountEquals(2)
    }

    @Test
    fun displaysSwitchItems() {
        val items =
            listOf(
                ScribeItem.SwitchItem(
                    title = R.string.i18n_app_settings_menu_app_color_mode,
                    desc = R.string.i18n_app_settings_menu_app_color_mode_description,
                    state = false,
                    onToggle = { },
                ),
                ScribeItem.SwitchItem(
                    title = R.string.i18n_app_settings_keyboard_title,
                    desc = 0,
                    state = true,
                    onToggle = { },
                ),
            )

        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(items))
        }

        composeRule.onNodeWithTag("scribe_item_list").assertIsDisplayed()
        composeRule.onNodeWithTag("switch_item_0").assertIsDisplayed()
        composeRule.onNodeWithTag("switch_item_1").assertIsDisplayed()
    }

    @Test
    fun showsEmptyState() {
        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(emptyList()))
        }

        composeRule.onNodeWithTag("empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("No items available").assertIsDisplayed()
    }

    @Test
    fun handlesItemClicks() {
        var clicked = false
        val items =
            listOf(
                ScribeItem.ClickableItem(
                    title = R.string.i18n_app_settings_menu_app_language,
                    desc = R.string.i18n_app_settings_menu_app_language_description,
                    action = { clicked = true },
                ),
            )

        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(items))
        }

        composeRule.onNodeWithTag("clickable_item_0").performClick()
        assert(clicked)
    }

    @Test
    fun togglesSwitchState() {
        var state = false
        val items =
            listOf(
                ScribeItem.SwitchItem(
                    title = R.string.i18n_app_settings_menu_app_color_mode,
                    desc = R.string.i18n_app_settings_menu_app_color_mode_description,
                    state = state,
                    onToggle = { state = it },
                ),
            )

        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(items))
        }

        composeRule.onNodeWithTag("switch_item_0").performClick()
        assert(state)
    }

    @Test
    fun scrollsLargeListInLazyColumn() {
        val items =
            (0..49).map { index ->
                ScribeItem.ClickableItem(
                    title = R.string.i18n_app_settings_menu_app_language,
                    desc = R.string.i18n_app_settings_menu_app_language_description,
                    action = { },
                )
            }

        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(items))
        }

        composeRule.onNodeWithTag("scribe_item_list").assertIsDisplayed()

        composeRule.onNodeWithTag("clickable_item_0").assertIsDisplayed()

        composeRule.onNodeWithTag("clickable_item_49").assertIsNotDisplayed()

        composeRule.onNodeWithTag("scribe_item_list").performScrollToIndex(49)

        composeRule.onNodeWithTag("clickable_item_49").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyStateWhenNoItems() {
        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(emptyList()))
        }

        composeRule.onNodeWithTag("empty_state").assertIsDisplayed()
        composeRule.onNodeWithTag("scribe_item_list").assertIsNotDisplayed()
    }

    @Test
    fun displaysLazyColumnWhenItemsExist() {
        val items =
            listOf(
                ScribeItem.ClickableItem(
                    title = R.string.i18n_app_settings_menu_app_language,
                    desc = R.string.i18n_app_settings_menu_app_language_description,
                    action = { },
                ),
            )

        composeRule.setContent {
            ScribeItemList(itemList = ScribeItemList(items))
        }

        composeRule.onNodeWithTag("scribe_item_list").assertIsDisplayed()
        composeRule.onNodeWithTag("empty_state").assertIsNotDisplayed()
    }

    @Composable
    private fun ClickableItem(
        item: ScribeItem.ClickableItem,
        index: Int,
    ) {
        Column(
            modifier =
                Modifier
                    .testTag("clickable_item_$index")
                    .clickable { item.action() },
        ) {
            Text(
                text = context.getString(item.title),
            )
            item.desc?.let { desc ->
                Text(
                    text = context.getString(desc),
                    modifier = Modifier.testTag("description_$index"),
                )
            }
        }
    }

    @Composable
    private fun SwitchItem(
        item: ScribeItem.SwitchItem,
        index: Int,
    ) {
        Row(
            modifier =
                Modifier
                    .testTag("switch_item_$index")
                    .clickable(role = Role.Switch) { item.onToggle(!item.state) }
                    .semantics {
                        toggleableState = if (item.state) ToggleableState.On else ToggleableState.Off
                        stateDescription = if (item.state) "On" else "Off"
                    }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = context.getString(item.title),
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = item.state,
                onCheckedChange = item.onToggle,
            )
        }
    }

    @Composable
    private fun ScribeItemList(itemList: ScribeItemList) {
        if (itemList.items.isEmpty()) {
            Text(
                text = "No items available",
                modifier = Modifier.testTag("empty_state"),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag("scribe_item_list"),
            ) {
                items(itemList.items) { item ->
                    when (item) {
                        is ScribeItem.ClickableItem -> {
                            Text(
                                text = "",
                                modifier = Modifier.testTag("scribe_item"),
                            )
                            ClickableItem(
                                item = item,
                                index = itemList.items.indexOf(item),
                            )
                        }
                        is ScribeItem.SwitchItem -> {
                            Text(
                                text = "",
                                modifier = Modifier.testTag("scribe_item"),
                            )
                            SwitchItem(
                                item = item,
                                index = itemList.items.indexOf(item),
                            )
                        }
                        else -> {
                            Text(
                                text = "Unknown item type",
                                modifier = Modifier.testTag("scribe_item"),
                            )
                        }
                    }
                }
            }
        }
    }
}
