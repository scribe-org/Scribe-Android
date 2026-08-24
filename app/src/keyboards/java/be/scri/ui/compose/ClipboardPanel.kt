// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.clipboard.ClipboardItem

@Composable
fun ClipboardPanel(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val items by viewModel.clipboardItems.collectAsState()
    val keyboard by viewModel.keyboard.collectAsState()

    val density = androidx.compose.ui.platform.LocalDensity.current
    val contentHeightDp = keyboard?.let { with(density) { it.mHeight.toDp() } } ?: 250.dp

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val panelBg = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFD3D6DD)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val iconTint = if (isDarkMode) Color.White else Color.Black

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(panelBg),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(46.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_left_vector),
                contentDescription = "Close Clipboard",
                tint = iconTint,
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .size(32.dp)
                        .clickable { actionListener.onClipboardPanelClose() }
                        .padding(4.dp),
            )
            Text(
                text = "Clipboard",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_vector),
                contentDescription = "Clear All",
                tint = Color(0xFFE53935),
                modifier =
                    Modifier
                        .padding(end = 8.dp)
                        .size(32.dp)
                        .clickable { actionListener.onClipboardClearAll() }
                        .padding(4.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(contentHeightDp)
                    .background(panelBg),
        ) {
            if (items.isEmpty()) {
                Text(
                    text = "Clipboard is empty",
                    color = textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        ClipboardItemCard(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = { actionListener.onClipboardItemClicked(item) },
                            onPinToggle = { actionListener.onClipboardItemPinToggle(item) },
                            onDelete = { actionListener.onClipboardItemDelete(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipboardItemCard(
    item: ClipboardItem,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardBg = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val labelColor = Color(0xFF999999)

    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(cardBg, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x20000000), RoundedCornerShape(10.dp))
                .pointerInput(item.id) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { menuExpanded = true },
                    )
                }.padding(8.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (item.isPinned) "Pinned" else "Copied text",
                    color = labelColor,
                    fontSize = 11.sp,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_copy_vector),
                    contentDescription = null,
                    tint = labelColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.text,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(if (item.isPinned) "Unpin" else "Pin") },
                onClick = {
                    menuExpanded = false
                    onPinToggle()
                },
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    menuExpanded = false
                    onDelete()
                },
            )
        }
    }
}
