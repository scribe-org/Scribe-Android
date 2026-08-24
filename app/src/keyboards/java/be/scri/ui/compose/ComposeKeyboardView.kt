// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.KeyboardBase
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

private val ALT_POPUP_ITEM_WIDTH = 42.dp
private val ALT_POPUP_ITEM_HEIGHT = 44.dp
private val ALT_POPUP_H_PADDING = 6.dp
private val ALT_POPUP_V_PADDING = 3.dp
private val ALT_POPUP_ITEM_GAP = 4.dp
private val ALT_POPUP_OFFSET_Y = 58.dp
private val EMOJI_COG_SIZE = 9.dp
private val EMOJI_COG_END_PADDING = 2.dp
private val EMOJI_COG_TOP_PADDING = 2.dp
private val EMOJI_ICON_SIZE = 18.dp
private val EMOJI_ICON_OFFSET_Y = 3.dp
private val KEY_ICON_SIZE = 22.dp

internal data class EmojiKeyOption(
    val code: Int,
    val iconRes: Int,
    val description: String,
)

internal val EMOJI_KEY_OPTIONS =
    listOf(
        EmojiKeyOption(KeyboardBase.KEYCODE_FLOAT_TOGGLE, R.drawable.ic_float_keyboard, "Floating keyboard"),
        EmojiKeyOption(KeyboardBase.KEYCODE_EMOJI, R.drawable.ic_emoji_vector, "Emoji palette"),
        EmojiKeyOption(KeyboardBase.KEYCODE_CLIPBOARD, R.drawable.ic_clipboard_vector, "Clipboard"),
    )

internal fun isEmojiKey(key: KeyboardBase.Key): Boolean = key.code == KeyboardBase.KEYCODE_EMOJI

internal fun hasLongPressOptions(key: KeyboardBase.Key): Boolean = isEmojiKey(key) || !key.popupCharacters.isNullOrEmpty() || key.topSmallNumber.isNotEmpty()

internal fun longPressOptionCount(key: KeyboardBase.Key): Int = if (isEmojiKey(key)) EMOJI_KEY_OPTIONS.size else altCharactersFor(key).size

internal fun altCharactersFor(key: KeyboardBase.Key): List<String> {
    val popupChars = key.popupCharacters?.toString() ?: ""
    val smallNumber = key.topSmallNumber ?: ""
    val list = popupChars.map { it.toString() }.toMutableList()
    if (smallNumber.isNotEmpty() && !list.contains(smallNumber)) {
        list.add(0, smallNumber)
    }
    return list
}

private fun altPopupLeft(
    key: KeyboardBase.Key,
    count: Int,
    itemWidthPx: Float,
    horizontalPaddingPx: Float,
    keyboardWidthPx: Float,
): Float {
    val totalWidth = count * itemWidthPx + horizontalPaddingPx * 2f
    val raw = key.x + (key.width - totalWidth) / 2f
    return raw.coerceIn(0f, (keyboardWidthPx - totalWidth).coerceAtLeast(0f))
}

@Composable
fun ComposeKeyboardView(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val keyboard by viewModel.keyboard.collectAsState()
    val shiftState by viewModel.shiftState.collectAsState()
    val currentState by viewModel.currentState.collectAsState()

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val keyboardBgColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)

    var pressedKey by remember { mutableStateOf<KeyboardBase.Key?>(null) }
    var activeLongPressKey by remember { mutableStateOf<KeyboardBase.Key?>(null) }
    var hoveredAltIndex by remember { mutableStateOf(-1) }

    if (keyboard != null) {
        val kb = keyboard!!
        val kbHeightDp = with(density) { kb.mHeight.toDp() }

        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(kbHeightDp)
                    .background(keyboardBgColor)
                    .pointerInput(kb) {
                        val altItemWidthPx = ALT_POPUP_ITEM_WIDTH.toPx()
                        val altPaddingPx = ALT_POPUP_H_PADDING.toPx()
                        val keyboardWidthPx = kb.mMinWidth.toFloat()

                        kotlinx.coroutines.coroutineScope {
                            val scope = this
                            awaitEachGesture {
                                val downEvent = awaitFirstDown()
                                var currentKey = kb.mKeys?.find { it?.isInside(downEvent.position.x.toInt(), downEvent.position.y.toInt()) == true }

                                pressedKey = currentKey
                                activeLongPressKey = null
                                hoveredAltIndex = -1

                                if (currentKey != null) {
                                    actionListener.onPress(currentKey.code)
                                }

                                val repeatJob =
                                    if (currentKey?.code == KeyboardBase.KEYCODE_DELETE) {
                                        scope.launch {
                                            delay(400)
                                            while (isActive) {
                                                actionListener.onKey(KeyboardBase.KEYCODE_DELETE)
                                                delay(50)
                                            }
                                        }
                                    } else {
                                        null
                                    }

                                var longPressJob: kotlinx.coroutines.Job? = null
                                if (currentKey != null && hasLongPressOptions(currentKey)) {
                                    longPressJob =
                                        scope.launch {
                                            delay(500)
                                            activeLongPressKey = currentKey
                                            hoveredAltIndex = 0
                                        }
                                }

                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: continue
                                    val touchX = change.position.x.toInt()
                                    val touchY = change.position.y.toInt()

                                    if (activeLongPressKey != null) {
                                        val key = activeLongPressKey!!
                                        val optionCount = longPressOptionCount(key)

                                        if (optionCount > 0) {
                                            val contentLeft =
                                                altPopupLeft(
                                                    key,
                                                    optionCount,
                                                    altItemWidthPx,
                                                    altPaddingPx,
                                                    keyboardWidthPx,
                                                ) + altPaddingPx
                                            hoveredAltIndex =
                                                ((touchX - contentLeft) / altItemWidthPx)
                                                    .toInt()
                                                    .coerceIn(0, optionCount - 1)
                                        }
                                    } else {
                                        val newKey = kb.mKeys?.find { it?.isInside(touchX, touchY) == true }

                                        if (newKey != currentKey) {
                                            repeatJob?.cancel()
                                            longPressJob?.cancel()
                                            if (currentKey != null) {
                                                actionListener.onActionUp()
                                            }
                                            currentKey = newKey
                                            pressedKey = newKey
                                            if (currentKey != null) {
                                                actionListener.onPress(currentKey.code)
                                                if (hasLongPressOptions(currentKey)) {
                                                    longPressJob =
                                                        scope.launch {
                                                            delay(500)
                                                            activeLongPressKey = currentKey
                                                            hoveredAltIndex = 0
                                                        }
                                                }
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                repeatJob?.cancel()
                                longPressJob?.cancel()

                                if (activeLongPressKey != null) {
                                    val key = activeLongPressKey!!

                                    if (isEmojiKey(key)) {
                                        val option = EMOJI_KEY_OPTIONS.getOrNull(hoveredAltIndex)
                                        actionListener.onKey(option?.code ?: key.code)
                                    } else {
                                        val altList = altCharactersFor(key)
                                        if (hoveredAltIndex in altList.indices) {
                                            val selectedChar = altList[hoveredAltIndex]
                                            if (selectedChar.length == 1) {
                                                actionListener.onKey(selectedChar[0].code)
                                            } else {
                                                actionListener.onText(selectedChar)
                                            }
                                        } else {
                                            actionListener.onKey(key.code)
                                        }
                                    }
                                    actionListener.onActionUp()
                                } else if (currentKey != null) {
                                    actionListener.onKey(currentKey.code)
                                    actionListener.onActionUp()
                                }

                                pressedKey = null
                                activeLongPressKey = null
                                hoveredAltIndex = -1
                            }
                        }
                    },
        ) {
            kb.mKeys?.forEachIndexed { index, key ->
                if (key != null) {
                    val isPressed = pressedKey == key
                    androidx.compose.runtime.key(index) {
                        KeyboardKey(
                            key = key,
                            shiftState = shiftState,
                            isDarkMode = isDarkMode,
                            isPressed = isPressed,
                            currentState = currentState,
                        )
                    }
                }
            }

            if (pressedKey != null && activeLongPressKey == null && !isSpecialKey(pressedKey!!)) {
                val key = pressedKey!!
                val keyLabel = adjustCase(key.label, shiftState) ?: ""
                if (keyLabel.isNotEmpty()) {
                    val keyWidthDp = with(density) { key.width.toDp() }
                    val keyHeightDp = with(density) { key.height.toDp() }
                    val keyXDp = with(density) { key.x.toDp() }
                    val keyYDp = with(density) { key.y.toDp() }
                    val kbWidthDp = with(density) { kb.mMinWidth.toDp() }
                    val previewXDp = (keyXDp + (keyWidthDp - 48.dp) / 2).coerceIn(0.dp, kbWidthDp - 48.dp)

                    Box(
                        modifier =
                            Modifier
                                .offset(x = previewXDp, y = keyYDp - 48.dp)
                                .size(48.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    clip = false,
                                ).background(
                                    color = if (isDarkMode) Color(0xFF555558) else Color.White,
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = keyLabel,
                            color = if (isDarkMode) Color.White else Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }

            if (activeLongPressKey != null) {
                val key = activeLongPressKey!!
                val isEmojiOptions = isEmojiKey(key)
                val altList = remember(activeLongPressKey) { if (isEmojiOptions) emptyList() else altCharactersFor(key) }
                val optionCount = if (isEmojiOptions) EMOJI_KEY_OPTIONS.size else altList.size

                if (optionCount > 0) {
                    val keyYDp = with(density) { key.y.toDp() }
                    val popupXDp =
                        with(density) {
                            altPopupLeft(
                                key,
                                optionCount,
                                ALT_POPUP_ITEM_WIDTH.toPx(),
                                ALT_POPUP_H_PADDING.toPx(),
                                kb.mMinWidth.toFloat(),
                            ).toDp()
                        }

                    Box(
                        modifier =
                            Modifier
                                .offset(x = popupXDp, y = keyYDp - ALT_POPUP_OFFSET_Y)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    clip = false,
                                ).background(
                                    color = if (isDarkMode) Color(0xFF3A3A3C) else Color(0xFFF0F0F0),
                                    shape = RoundedCornerShape(24.dp),
                                ).padding(horizontal = ALT_POPUP_H_PADDING, vertical = ALT_POPUP_V_PADDING),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(ALT_POPUP_ITEM_GAP)) {
                            repeat(optionCount) { index ->
                                val isHovered = hoveredAltIndex == index
                                Box(
                                    modifier =
                                        Modifier
                                            .size(
                                                width = ALT_POPUP_ITEM_WIDTH - ALT_POPUP_ITEM_GAP,
                                                height = ALT_POPUP_ITEM_HEIGHT,
                                            ).background(
                                                color = if (isHovered) (if (isDarkMode) Color(0xFF636366) else Color(0xFFD0D0D0)) else Color.Transparent,
                                                shape = CircleShape,
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isEmojiOptions) {
                                        val option = EMOJI_KEY_OPTIONS[index]
                                        Icon(
                                            painter = painterResource(id = option.iconRes),
                                            contentDescription = option.description,
                                            tint = if (isDarkMode) Color.White else Color.Black,
                                            modifier = Modifier.size(KEY_ICON_SIZE),
                                        )
                                    } else {
                                        Text(
                                            text = altList[index],
                                            color = if (isDarkMode) Color.White else Color.Black,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Loading keyboard...", color = if (isDarkMode) Color.White else Color.Black)
        }
    }
}

@Composable
fun KeyboardKey(
    key: KeyboardBase.Key,
    shiftState: Int,
    isDarkMode: Boolean,
    isPressed: Boolean,
    currentState: be.scri.models.ScribeState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val xDp = with(density) { key.x.toDp() }
    val yDp = with(density) { key.y.toDp() }
    val widthDp = with(density) { key.width.toDp() }
    val heightDp = with(density) { key.height.toDp() }

    val isSpecial = isSpecialKey(key)

    val pressedKeyBg = if (isDarkMode) Color(0xFF5A5A5E) else Color(0xFFCDCDD2)
    val pressedSpecialKeyBg = if (isDarkMode) Color(0xFF48484A) else Color(0xFF9D9DA3)

    val keyBgColor = if (isDarkMode) Color(0xFF4A4A4E) else Color(0xFFFFFFFF)
    val specialKeyBgColor = if (isDarkMode) Color(0xFF3A3A3C) else Color(0xFFACB2BF)

    val bg =
        if (isPressed) {
            if (isSpecial) pressedSpecialKeyBg else pressedKeyBg
        } else {
            if (isSpecial) specialKeyBgColor else keyBgColor
        }
    val textColor = if (isDarkMode) Color.White else Color.Black

    val label = adjustCase(key.label, shiftState)

    val shadowColor = Color.Black.copy(alpha = 100f / 255f)

    Box(
        modifier =
            modifier
                .offset(x = xDp, y = yDp)
                .size(width = widthDp, height = heightDp)
                .padding(horizontal = 3.dp, vertical = 4.dp)
                .drawBehind {
                    val radius = 5.dp.toPx()
                    val shadowOffsetY = 3.dp.toPx()
                    drawRoundRect(
                        color = shadowColor,
                        topLeft =
                            androidx.compose.ui.geometry
                                .Offset(0f, shadowOffsetY),
                        size = size,
                        cornerRadius =
                            androidx.compose.ui.geometry
                                .CornerRadius(radius, radius),
                    )
                }.background(bg, shape = RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (key.code == KeyboardBase.KEYCODE_EMOJI) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(end = EMOJI_COG_END_PADDING, top = EMOJI_COG_TOP_PADDING),
                contentAlignment = Alignment.TopEnd,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_cog_vector),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(EMOJI_COG_SIZE),
                )
            }
        }

        val smallNumber = key.topSmallNumber
        if (!smallNumber.isNullOrEmpty() && !isSpecial) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(end = 4.dp, top = 2.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                Text(
                    text = smallNumber,
                    color = if (isDarkMode) Color(0xFF8E8E93) else Color(0xFF8E8E93),
                    fontSize = 10.sp,
                )
            }
        }

        val iconRes =
            when (key.code) {
                KeyboardBase.KEYCODE_SHIFT -> {
                    when (shiftState) {
                        0 -> R.drawable.ic_caps_outline_vector
                        1 -> R.drawable.ic_caps_vector
                        2 -> R.drawable.ic_caps_underlined_vector
                        else -> R.drawable.ic_caps_outline_vector
                    }
                }
                KeyboardBase.KEYCODE_CAPS_LOCK -> {
                    if (shiftState == 2) R.drawable.ic_caps_lock_on else R.drawable.ic_caps_lock_off
                }
                KeyboardBase.KEYCODE_DELETE -> R.drawable.ic_clear_outline_vector
                KeyboardBase.KEYCODE_LEFT_ARROW -> R.drawable.ic_left_arrow
                KeyboardBase.KEYCODE_RIGHT_ARROW -> R.drawable.ic_right_arrow
                KeyboardBase.KEYCODE_CLIPBOARD -> R.drawable.ic_clipboard_vector
                KeyboardBase.KEYCODE_ENTER -> {
                    if (currentState == be.scri.models.ScribeState.TRANSLATE ||
                        currentState == be.scri.models.ScribeState.CONJUGATE ||
                        currentState == be.scri.models.ScribeState.PLURAL
                    ) {
                        R.drawable.play_button
                    } else {
                        null
                    }
                }
                else -> null
            }

        val context = LocalContext.current
        val hasVectorOrRaster =
            remember(iconRes) {
                if (iconRes == null) {
                    false
                } else {
                    try {
                        val drawable =
                            androidx.core.content.ContextCompat
                                .getDrawable(context, iconRes)
                        drawable is android.graphics.drawable.VectorDrawable ||
                            drawable is androidx.vectordrawable.graphics.drawable.VectorDrawableCompat ||
                            drawable is android.graphics.drawable.BitmapDrawable
                    } catch (e: Exception) {
                        false
                    }
                }
            }

        val isEmojiKey = key.code == KeyboardBase.KEYCODE_EMOJI
        val iconModifier =
            if (isEmojiKey) {
                Modifier.offset(y = EMOJI_ICON_OFFSET_Y).size(EMOJI_ICON_SIZE)
            } else {
                Modifier.size(KEY_ICON_SIZE)
            }

        if (iconRes != null && hasVectorOrRaster) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = textColor,
                modifier = iconModifier,
            )
        } else if (key.icon != null) {
            Canvas(modifier = iconModifier) {
                drawIntoCanvas { canvas ->
                    key.icon?.let { drawable ->
                        drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                        drawable.setTint(textColor.toArgb())
                        drawable.draw(canvas.nativeCanvas)
                    }
                }
            }
        } else if (label != null && label.isNotEmpty()) {
            Text(
                text = label,
                color = textColor,
                fontSize = if (label.length > 1) 14.sp else 22.sp,
                fontWeight = FontWeight.Light,
            )
        }
    }
}

private fun isSpecialKey(key: KeyboardBase.Key): Boolean =
    key.code in
        listOf(
            KeyboardBase.KEYCODE_SHIFT,
            KeyboardBase.KEYCODE_MODE_CHANGE,
            KeyboardBase.KEYCODE_DELETE,
            KeyboardBase.KEYCODE_TAB,
            KeyboardBase.KEYCODE_CAPS_LOCK,
            KeyboardBase.KEYCODE_LEFT_ARROW,
            KeyboardBase.KEYCODE_RIGHT_ARROW,
            KeyboardBase.KEYCODE_CLIPBOARD,
        )

private fun adjustCase(
    label: CharSequence?,
    shiftState: Int,
): String? {
    if (label == null) return null
    val labelStr = label.toString()
    if (labelStr == "tab" || labelStr == "caps lock") return labelStr
    return if (shiftState == 1 || shiftState == 2) {
        labelStr.uppercase(Locale.getDefault())
    } else {
        labelStr
    }
}
