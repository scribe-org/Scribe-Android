// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.EMOJI_SPEC_FILE_PATH
import be.scri.helpers.EmojiData
import be.scri.helpers.KeyboardBase
import be.scri.helpers.KeyboardLanguageMappingConstants
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.getCategoryIconRes
import be.scri.helpers.parseRawEmojiSpecsFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface EmojiListItem {
    data class Category(
        val name: String,
    ) : EmojiListItem

    data class Emoji(
        val data: EmojiData,
    ) : EmojiListItem
}

@Composable
fun EmojiKeyboardPanel(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val language by viewModel.language.collectAsState()
    val keyboard by viewModel.keyboard.collectAsState()

    val density = androidx.compose.ui.platform.LocalDensity.current
    val contentHeightDp = keyboard?.let { with(density) { it.mHeight.toDp() } } ?: 250.dp

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val bgColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)
    val iconTint = if (isDarkMode) Color.White else Color.Black
    val activeColor =
        if (isDarkMode) {
            androidx.compose.ui.graphics
                .Color(0xFF66B2FF)
        } else {
            androidx.compose.ui.graphics
                .Color(0xFF0066CC)
        }
    val inactiveColor = Color(0xFF9E9E9E)

    val context = LocalContext.current
    var categories by remember { mutableStateOf<Map<String, List<EmojiData>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val loaded =
            withContext(Dispatchers.IO) {
                val fullEmojiList = parseRawEmojiSpecsFile(context, EMOJI_SPEC_FILE_PATH)
                val systemFontPaint =
                    android.graphics.Paint().apply {
                        typeface = android.graphics.Typeface.DEFAULT
                    }
                fullEmojiList
                    .filter { emoji -> systemFontPaint.hasGlyph(emoji.emoji) }
                    .groupBy { it.category }
            }
        categories = loaded
    }

    val items =
        remember(categories) {
            val list = mutableListOf<EmojiListItem>()
            categories.forEach { (category, emojis) ->
                list.add(EmojiListItem.Category(category))
                emojis.forEach { list.add(EmojiListItem.Emoji(it)) }
            }
            list
        }

    val categoryHeaders =
        remember(language) {
            (KeyboardLanguageMappingConstants.emojiCategoryHeaders["EN"] ?: emptyMap()) +
                (KeyboardLanguageMappingConstants.emojiCategoryHeaders[getLanguageAlias(language)] ?: emptyMap())
        }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var activeCategoryIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .background(bgColor),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.close_icon),
                contentDescription = "Close emoji keyboard",
                colorFilter = ColorFilter.tint(iconTint),
                modifier =
                    Modifier
                        .size(40.dp)
                        .clickable { viewModel.setEmojiKeyboardVisible(false) }
                        .padding(10.dp),
            )

            Text(
                text = "ABC",
                color = iconTint,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .clickable { viewModel.setEmojiKeyboardVisible(false) },
            )

            Image(
                painter = painterResource(id = R.drawable.emoji_backspace),
                contentDescription = "Delete",
                colorFilter = ColorFilter.tint(iconTint),
                modifier =
                    Modifier
                        .size(40.dp)
                        .clickable { actionListener.onKey(KeyboardBase.KEYCODE_DELETE) }
                        .padding(8.dp),
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 44.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(horizontal = 4.dp),
            ) {
                items(
                    count = items.size,
                    span = { index ->
                        if (items[index] is EmojiListItem.Category) {
                            GridItemSpan(maxLineSpan)
                        } else {
                            GridItemSpan(1)
                        }
                    },
                ) { index ->
                    when (val item = items[index]) {
                        is EmojiListItem.Category -> {
                            Text(
                                text = categoryHeaders[item.name] ?: item.name,
                                color = iconTint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp),
                            )
                        }
                        is EmojiListItem.Emoji -> {
                            Box(
                                modifier =
                                    Modifier
                                        .size(44.dp)
                                        .clickable { actionListener.onEmojiSelected(item.data.emoji) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = item.data.emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }

        if (categories.isNotEmpty()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                categories.keys.toList().forEachIndexed { index, category ->
                    Image(
                        painter = painterResource(id = getCategoryIconRes(category)),
                        contentDescription = category,
                        colorFilter = ColorFilter.tint(if (index == activeCategoryIndex) activeColor else inactiveColor),
                        modifier =
                            Modifier
                                .weight(1f)
                                .size(22.dp)
                                .clickable {
                                    activeCategoryIndex = index
                                    val position = items.indexOfFirst { it is EmojiListItem.Category && it.name == category }
                                    if (position != -1) {
                                        coroutineScope.launch {
                                            gridState.scrollToItem(position)
                                        }
                                    }
                                },
                    )
                }
            }
        }
    }
}
