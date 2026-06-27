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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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


@Composable
fun ComposeKeyboardView(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val keyboard by viewModel.keyboard.collectAsState()
    val shiftState by viewModel.shiftState.collectAsState()
    
    val context = LocalContext.current
    val density = LocalDensity.current
    val isDarkMode = be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot(context)

    val keyboardBgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFD3D6DD)

    if (keyboard != null) {
        val kb = keyboard!!
        val kbHeightDp = with(density) { kb.mHeight.toDp() }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(kbHeightDp)
                .background(keyboardBgColor)
                .pointerInput(kb) {
                    kotlinx.coroutines.coroutineScope {
                        val scope = this
                        awaitEachGesture {
                            val downEvent = awaitFirstDown()
                            var currentKey = kb.mKeys?.find { it?.isInside(downEvent.position.x.toInt(), downEvent.position.y.toInt()) == true }
                            
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

                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue
                                
                                val newKey = kb.mKeys?.find { it?.isInside(change.position.x.toInt(), change.position.y.toInt()) == true }
                                
                                if (newKey != currentKey) {
                                    repeatJob?.cancel()
                                    if (currentKey != null) {
                                        actionListener.onActionUp()
                                    }
                                    currentKey = newKey
                                    if (currentKey != null) {
                                        actionListener.onPress(currentKey.code)
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                            
                            repeatJob?.cancel()
                            // Finger lifted
                            if (currentKey != null) {
                                actionListener.onKey(currentKey.code)
                                actionListener.onActionUp()
                            }
                        }
                    }
                }
        ) {
            kb.mKeys?.forEach { key ->
                if (key != null) {
                    KeyboardKey(
                        key = key,
                        shiftState = shiftState,
                        isDarkMode = isDarkMode
                    )
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
fun KeyboardKey(key: KeyboardBase.Key, shiftState: Int, isDarkMode: Boolean) {
    val density = LocalDensity.current
    
    val xDp = with(density) { key.x.toDp() }
    val yDp = with(density) { key.y.toDp() }
    val widthDp = with(density) { key.width.toDp() }
    val heightDp = with(density) { key.height.toDp() }

    val isSpecial = key.code in listOf(
        KeyboardBase.KEYCODE_SHIFT,
        KeyboardBase.KEYCODE_MODE_CHANGE,
        KeyboardBase.KEYCODE_DELETE,
        KeyboardBase.KEYCODE_TAB,
        KeyboardBase.KEYCODE_CAPS_LOCK,
        KeyboardBase.KEYCODE_LEFT_ARROW,
        KeyboardBase.KEYCODE_RIGHT_ARROW
    )
    
    val keyBgColor = if (isDarkMode) Color(0xFF434343) else Color(0xFFFFFFFF)
    val specialKeyBgColor = if (isDarkMode) Color(0xFF202020) else Color(0xFFAEB3BE)
    val bg = if (isSpecial) specialKeyBgColor else keyBgColor
    val textColor = if (isDarkMode) Color.White else Color.Black

    val label = adjustCase(key.label, shiftState)

    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(width = widthDp, height = heightDp)
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .background(bg, shape = RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
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
                modifier = Modifier.size(20.dp)
            )
        } else if (key.icon != null) {
            Canvas(modifier = Modifier.size(20.dp)) {
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
                fontSize = if (label.length > 1) 14.sp else 18.sp
            )
        }
    }
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
