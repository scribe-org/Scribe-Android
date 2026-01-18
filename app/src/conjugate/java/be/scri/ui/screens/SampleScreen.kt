// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SampleScreen(modifier: Modifier = Modifier) {
    MaterialTheme {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Conjugate App",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
