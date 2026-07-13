package be.scri.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.KeyboardBase
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf


@Composable
fun ComposeKeyboardView(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val keyboard by viewModel.keyboard.collectAsState()
    val shiftState by viewModel.shiftState.collectAsState()
    val currentState by viewModel.currentState.collectAsState()
    
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val keyboardBgColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)

    var pressedKey by remember { mutableStateOf<KeyboardBase.Key?>(null) }
    var activeLongPressKey by remember { mutableStateOf<KeyboardBase.Key?>(null) }
    var hoveredAltIndex by remember { mutableStateOf(-1) }

    if (keyboard != null) {
        val kb = keyboard!!
        val kbHeightDp = with(density) { kb.mHeight.toDp() }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(kbHeightDp)
                .background(keyboardBgColor)
                .pointerInput(kb) {
                    val btnWidthPx = 42.dp.toPx()
                    val popupOffsetPx = 58.dp.toPx()
                    val popupHeightPx = 44.dp.toPx()

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

                            val repeatJob = if (currentKey?.code == KeyboardBase.KEYCODE_DELETE) {
                                scope.launch {
                                    delay(400)
                                    while (isActive) {
                                        actionListener.onKey(KeyboardBase.KEYCODE_DELETE)
                                        delay(50)
                                    }
                                }
                            } else null

                            var longPressJob: kotlinx.coroutines.Job? = null
                            if (currentKey != null && (!currentKey.popupCharacters.isNullOrEmpty() || currentKey.topSmallNumber.isNotEmpty())) {
                                longPressJob = scope.launch {
                                    delay(500)
                                    activeLongPressKey = currentKey
                                }
                            }

                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue
                                val touchX = change.position.x.toInt()
                                val touchY = change.position.y.toInt()

                                if (activeLongPressKey != null) {
                                    val key = activeLongPressKey!!
                                    val popupChars = key.popupCharacters?.toString() ?: ""
                                    val smallNum = key.topSmallNumber ?: ""
                                    val altList = mutableListOf<String>()
                                    popupChars.forEach { altList.add(it.toString()) }
                                    if (smallNum.isNotEmpty() && !altList.contains(smallNum)) {
                                        altList.add(0, smallNum)
                                    }
                                    
                                    val listSize = altList.size
                                    val totalWidthPx = (listSize * btnWidthPx).toInt()
                                    val popupLeft = key.x + (key.width - totalWidthPx) / 2
                                    val popupRight = popupLeft + totalWidthPx
                                    val popupTop = key.y - popupOffsetPx.toInt()
                                    val popupBottom = popupTop + popupHeightPx.toInt()

                                    if (touchY in popupTop..popupBottom && touchX in popupLeft..popupRight) {
                                        hoveredAltIndex = ((touchX - popupLeft) / btnWidthPx).toInt().coerceIn(0, listSize - 1)
                                    } else {
                                        hoveredAltIndex = 0
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
                                            if (!currentKey.popupCharacters.isNullOrEmpty() || currentKey.topSmallNumber.isNotEmpty()) {
                                                longPressJob = scope.launch {
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
                                val popupChars = key.popupCharacters?.toString() ?: ""
                                val smallNum = key.topSmallNumber ?: ""
                                val altList = mutableListOf<String>()
                                popupChars.forEach { altList.add(it.toString()) }
                                if (smallNum.isNotEmpty() && !altList.contains(smallNum)) {
                                    altList.add(0, smallNum)
                                }

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
                }
        ) {
            kb.mKeys?.forEach { key ->
                if (key != null) {
                    val isPressed = pressedKey == key
                    androidx.compose.runtime.key(key.code) {
                        KeyboardKey(
                            key = key,
                            shiftState = shiftState,
                            isDarkMode = isDarkMode,
                            isPressed = isPressed,
                            currentState = currentState
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
                        modifier = Modifier
                            .offset(x = previewXDp, y = keyYDp - 48.dp)
                            .size(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                clip = false
                            )
                            .background(
                                color = if (isDarkMode) Color(0xFF555558) else Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = keyLabel,
                            color = if (isDarkMode) Color.White else Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            if (activeLongPressKey != null) {
                val key = activeLongPressKey!!
                val popupChars = key.popupCharacters?.toString() ?: ""
                val smallNum = key.topSmallNumber ?: ""
                val altList = remember(activeLongPressKey) {
                    val list = mutableListOf<String>()
                    popupChars.forEach { list.add(it.toString()) }
                    if (smallNum.isNotEmpty() && !list.contains(smallNum)) {
                        list.add(0, smallNum)
                    }
                    list
                }

                if (altList.isNotEmpty()) {
                    val keyWidthDp = with(density) { key.width.toDp() }
                    val keyHeightDp = with(density) { key.height.toDp() }
                    val keyXDp = with(density) { key.x.toDp() }
                    val keyYDp = with(density) { key.y.toDp() }
                    val kbWidthDp = with(density) { kb.mMinWidth.toDp() }
                    val btnWidth = 42.dp
                    val totalWidth = btnWidth * altList.size
                    val popupXDp = (keyXDp + (keyWidthDp - totalWidth) / 2).coerceIn(0.dp, kbWidthDp - totalWidth)
                    
                    Box(
                        modifier = Modifier
                            .offset(x = popupXDp, y = keyYDp - 58.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                clip = false
                            )
                            .background(
                                color = if (isDarkMode) Color(0xFF3A3A3C) else Color(0xFFF0F0F0),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            altList.forEachIndexed { index, char ->
                                val isHovered = hoveredAltIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(width = btnWidth - 4.dp, height = 44.dp)
                                        .background(
                                            color = if (isHovered) (if (isDarkMode) Color(0xFF636366) else Color(0xFFD0D0D0)) else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        color = if (isDarkMode) Color.White else Color.Black,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )
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
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading keyboard...", color = if (isDarkMode) Color.White else Color.Black)
        }
    }
}

@Composable
fun KeyboardKey(key: KeyboardBase.Key, shiftState: Int, isDarkMode: Boolean, isPressed: Boolean, currentState: be.scri.models.ScribeState) {
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
    
    val bg = if (isPressed) {
        if (isSpecial) pressedSpecialKeyBg else pressedKeyBg
    } else {
        if (isSpecial) specialKeyBgColor else keyBgColor
    }
    val textColor = if (isDarkMode) Color.White else Color.Black

    val label = adjustCase(key.label, shiftState)

    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(width = widthDp, height = heightDp)
            .padding(horizontal = 3.dp, vertical = 4.dp)
            .shadow(
                elevation = if (isPressed) 0.dp else 1.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .background(bg, shape = RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        val smallNumber = key.topSmallNumber
        if (!smallNumber.isNullOrEmpty() && !isSpecial) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 4.dp, top = 2.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = smallNumber,
                    color = if (isDarkMode) Color(0xFF8E8E93) else Color(0xFF8E8E93),
                    fontSize = 10.sp
                )
            }
        }

        val iconRes = when (key.code) {
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
        val hasVectorOrRaster = remember(iconRes) {
            if (iconRes == null) {
                false
            } else {
                try {
                    val drawable = androidx.core.content.ContextCompat.getDrawable(context, iconRes)
                    drawable is android.graphics.drawable.VectorDrawable ||
                    drawable is androidx.vectordrawable.graphics.drawable.VectorDrawableCompat ||
                    drawable is android.graphics.drawable.BitmapDrawable
                } catch (e: Exception) {
                    false
                }
            }
        }

        if (iconRes != null && hasVectorOrRaster) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
        } else if (key.icon != null) {
            Canvas(modifier = Modifier.size(22.dp)) {
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
                fontWeight = FontWeight.Light
            )
        }
    }
}

private fun isSpecialKey(key: KeyboardBase.Key): Boolean {
    return key.code in listOf(
        KeyboardBase.KEYCODE_SHIFT,
        KeyboardBase.KEYCODE_MODE_CHANGE,
        KeyboardBase.KEYCODE_DELETE,
        KeyboardBase.KEYCODE_TAB,
        KeyboardBase.KEYCODE_CAPS_LOCK,
        KeyboardBase.KEYCODE_LEFT_ARROW,
        KeyboardBase.KEYCODE_RIGHT_ARROW,
        KeyboardBase.KEYCODE_CLIPBOARD
    )
}

private fun adjustCase(label: CharSequence?, shiftState: Int): String? {
    if (label == null) return null
    val labelStr = label.toString()
    if (labelStr == "tab" || labelStr == "caps lock") return labelStr
    return if (shiftState == 1 || shiftState == 2) {
        labelStr.uppercase(Locale.getDefault())
    } else {
        labelStr
    }
}
