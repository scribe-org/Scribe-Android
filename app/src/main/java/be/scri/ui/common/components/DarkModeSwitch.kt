package be.scri.ui.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import be.scri.R
import be.scri.ui.theme.BlueSky
import be.scri.ui.theme.BorderColor
import be.scri.ui.theme.NightSky
import kotlinx.coroutines.launch

@Composable
fun DarkModeSwitch(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val switchWidth = 61.dp
    val switchHeight = 31.dp
    val handleSize = 30.dp
    val handlePadding = 3.dp
    val valueToOffset = if (checked) 1f else 0f
    val offset = remember { Animatable(valueToOffset) }
    val scope = rememberCoroutineScope()
    DisposableEffect(checked) {
        if (offset.targetValue != valueToOffset) {
            scope.launch {
                offset.animateTo(valueToOffset, animationSpec = tween(1000))
            }
        }
        onDispose { }
    }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .width(switchWidth)
            .height(switchHeight)
            .clip(RoundedCornerShape(switchHeight))
            .background(lerp(BlueSky, NightSky, offset.value))
            .border(3.dp, BorderColor, RoundedCornerShape(switchHeight))
            .toggleable(
                value = checked,
                onValueChange = onCheckedChanged,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        val backgroundPainter = painterResource(R.drawable.background)
        Canvas(modifier = Modifier.fillMaxSize()) {
            with(backgroundPainter) {
                val scale = size.width / intrinsicSize.width
                val scaledHeight = intrinsicSize.height * scale
                translate(top = (size.height - scaledHeight) * (1f - offset.value)) {
                    draw(Size(size.width, scaledHeight))
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.glow),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(switchWidth)
                .graphicsLayer {
                    scaleX = 1.2f
                    scaleY = scaleX
                    translationX = lerp(
                        -size.width * 0.5f + handlePadding.toPx() + handleSize.toPx() * 0.5f,
                        switchWidth.toPx() - size.width * 0.5f - handlePadding.toPx() - handleSize.toPx() * 0.5f,
                        offset.value
                    )
                }
        )
        Box(
            modifier = Modifier
                .padding(horizontal = handlePadding)
                .size(handleSize)
                .offset(x = (switchWidth - handleSize - handlePadding * 2f) * offset.value)
                .paint(painterResource(R.drawable.sun))
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(R.drawable.moon),
                contentDescription = null,
                modifier = Modifier
                    .size(handleSize)
                    .graphicsLayer {
                        translationX = size.width * (1f - offset.value)
                    }
            )
        }
    }
}
