// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private const val MIN_SCALE = 0.6f
private const val MAX_SCALE = 1.5f
private const val DOCK_THRESHOLD_DP = 60f

private val CARD_CORNER = 16.dp
private val CARD_BOTTOM_MARGIN = 12.dp
private val HANDLE_STRIP_HEIGHT = 24.dp
private val RESIZE_TOUCH_SIZE = 24.dp
private val RESIZE_DOT_SIZE = 10.dp

private const val FLOAT_AREA_SCREEN_FRACTION = 0.72f

@Composable
fun FloatingKeyboardChrome(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val offsetX by viewModel.floatingOffsetX.collectAsState()
    val offsetY by viewModel.floatingOffsetY.collectAsState()
    val scaleX by viewModel.floatingScaleX.collectAsState()
    val scaleY by viewModel.floatingScaleY.collectAsState()
    val keyboard by viewModel.keyboard.collectAsState()

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val density = LocalDensity.current
    val dockThresholdPx = with(density) { DOCK_THRESHOLD_DP.dp.toPx() }
    val cardColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)
    val handleColor = if (isDarkMode) Color(0x4DFFFFFF) else Color(0x40000000)
    val cornerColor = if (isDarkMode) Color(0xFFAEB3BE) else Color(0xFF4B4B4B)

    val cardWidthDp = keyboard?.mMinWidth?.takeIf { it > 0 }?.let { with(density) { it.toDp() } }

    var liveOffsetX by remember(offsetX) { mutableFloatStateOf(offsetX) }
    var liveOffsetY by remember(offsetY) { mutableFloatStateOf(offsetY) }
    var liveScaleX by remember(scaleX) { mutableFloatStateOf(scaleX) }
    var liveScaleY by remember(scaleY) { mutableFloatStateOf(scaleY) }
    var cardSize by remember { mutableStateOf(Size.Zero) }
    var areaSize by remember { mutableStateOf(Size.Zero) }

    val floatAreaHeight = (LocalConfiguration.current.screenHeightDp * FLOAT_AREA_SCREEN_FRACTION).dp

    fun clampOffsetX(value: Float): Float {
        if (areaSize.width <= 0f || cardSize.width <= 0f) return value
        val limit = ((areaSize.width - cardSize.width) / 2f).coerceAtLeast(0f)
        return value.coerceIn(-limit, limit)
    }

    fun clampOffsetY(value: Float): Float {
        if (areaSize.height <= 0f || cardSize.height <= 0f) return value
        val minimum = -(areaSize.height - cardSize.height).coerceAtLeast(0f)
        return value.coerceIn(minimum, dockThresholdPx * 1.5f)
    }

    fun persist(dock: Boolean) {
        viewModel.setFloatingTransform(liveOffsetX, liveOffsetY, liveScaleX, liveScaleY)
        actionListener.onFloatingGestureEnded(liveOffsetX, liveOffsetY, liveScaleX, liveScaleY, dock)
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(floatAreaHeight)
                .padding(bottom = CARD_BOTTOM_MARGIN)
                .onGloballyPositioned { coords ->
                    areaSize = Size(coords.size.width.toFloat(), coords.size.height.toFloat())
                },
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .then(if (cardWidthDp != null) Modifier.width(cardWidthDp) else Modifier.fillMaxWidth())
                    .wrapContentHeight()
                    .onGloballyPositioned { coords ->
                        cardSize = Size(coords.size.width.toFloat(), coords.size.height.toFloat())
                        val position = coords.positionInWindow()
                        viewModel.setFloatingCardBounds(
                            FloatingCardBounds(
                                left = position.x,
                                top = position.y,
                                width = coords.size.width.toFloat(),
                                height = coords.size.height.toFloat(),
                            ),
                        )
                    }.graphicsLayer {
                        translationX = clampOffsetX(liveOffsetX)
                        translationY = clampOffsetY(liveOffsetY)
                        this.scaleX = liveScaleX
                        this.scaleY = liveScaleY
                    }.shadow(elevation = 8.dp, shape = RoundedCornerShape(CARD_CORNER), clip = false)
                    .clip(RoundedCornerShape(CARD_CORNER))
                    .background(cardColor),
        ) {
            Column {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(HANDLE_STRIP_HEIGHT)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = { persist(liveOffsetY > dockThresholdPx) },
                                    onDragCancel = { persist(false) },
                                ) { change, dragAmount ->
                                    change.consume()
                                    liveOffsetX = clampOffsetX(liveOffsetX + dragAmount.x)
                                    liveOffsetY = clampOffsetY(liveOffsetY + dragAmount.y)
                                }
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(handleColor, RoundedCornerShape(2.dp)),
                    )
                }

                content()

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(HANDLE_STRIP_HEIGHT),
                )
            }

            listOf(
                Alignment.TopStart to (-1f to -1f),
                Alignment.TopEnd to (1f to -1f),
                Alignment.BottomStart to (-1f to 1f),
                Alignment.BottomEnd to (1f to 1f),
            ).forEach { (alignment, factors) ->
                ResizeHandle(
                    modifier = Modifier.align(alignment),
                    color = cornerColor,
                    dragFactorX = factors.first,
                    dragFactorY = factors.second,
                    cardSize = { cardSize },
                    currentScale = { liveScaleX to liveScaleY },
                    onScale = { sx, sy ->
                        liveScaleX = sx
                        liveScaleY = sy
                    },
                    onEnd = { persist(false) },
                )
            }
        }
    }
}

@Composable
private fun ResizeHandle(
    color: Color,
    dragFactorX: Float,
    dragFactorY: Float,
    cardSize: () -> Size,
    currentScale: () -> Pair<Float, Float>,
    onScale: (Float, Float) -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gesture = remember { ResizeGestureState() }

    Box(
        modifier =
            modifier
                .size(RESIZE_TOUCH_SIZE)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            val (sx, sy) = currentScale()
                            gesture.initialScaleX = sx
                            gesture.initialScaleY = sy
                            gesture.accumulatedDx = 0f
                            gesture.accumulatedDy = 0f
                        },
                        onDragEnd = { onEnd() },
                        onDragCancel = { onEnd() },
                    ) { change, dragAmount ->
                        change.consume()
                        val size = cardSize()
                        if (size.width > 0f && size.height > 0f) {
                            gesture.accumulatedDx += dragAmount.x
                            gesture.accumulatedDy += dragAmount.y
                            val targetScaleX =
                                (gesture.initialScaleX + dragFactorX * gesture.accumulatedDx / size.width)
                                    .coerceIn(MIN_SCALE, MAX_SCALE)
                            val targetScaleY =
                                (gesture.initialScaleY + dragFactorY * gesture.accumulatedDy / size.height)
                                    .coerceIn(MIN_SCALE, MAX_SCALE)
                            onScale(targetScaleX, targetScaleY)
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(RESIZE_DOT_SIZE)
                    .background(color, CircleShape),
        )
    }
}

private class ResizeGestureState {
    var initialScaleX = 1f
    var initialScaleY = 1f
    var accumulatedDx = 0f
    var accumulatedDy = 0f
}
