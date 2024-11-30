package be.scri.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun ClickableItemComp(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    desc: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp,
                ).clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = colorResource(R.color.app_text_color),
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                painter = painterResource(R.drawable.right_arrow),
                modifier =
                    Modifier
                        .padding(start = 6.dp)
                        .size(14.dp),
                contentDescription = "Right Arrow",
            )
        }

        if (!desc.isNullOrEmpty()) {
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
