/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/**
 * Class of methods to manage Scribe's behaviors.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
