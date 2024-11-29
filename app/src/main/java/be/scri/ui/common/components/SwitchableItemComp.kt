package be.scri.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun SwitchableItemComp(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkedThumbColor = colorResource(R.color.switch_thumb_selector_color_true)
    val uncheckedThumbColor = colorResource(R.color.switch_thumb_selector_color_false)
    val checkedTrackColor = colorResource(R.color.switch_selector_color)
    val uncheckedTrackColor = colorResource(R.color.switch_selector_color_false)

    Column(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                interactionSource = null,
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .width(51.dp)
                    .height(31.dp),
                thumbContent = {
                    Box(
                        modifier = Modifier
                            .size(27.dp)
                            .background(
                                if (isChecked) {
                                    colorResource(R.color.switch_thumb_selector_color_true)
                                } else {
                                    colorResource(R.color.switch_thumb_selector_color_false)
                                },
                                shape = CircleShape,
                            ),
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = checkedThumbColor,
                    uncheckedThumbColor = uncheckedThumbColor,
                    checkedTrackColor = checkedTrackColor,
                    uncheckedTrackColor = uncheckedTrackColor,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }
        Text(
            text = desc,
            fontSize = 12.sp,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun SwitchableItemPreview() {
    SwitchableItemComp(
        title = "Title",
        desc = "Description",
        isChecked = true,
        onCheckedChange = {},
    )
}
