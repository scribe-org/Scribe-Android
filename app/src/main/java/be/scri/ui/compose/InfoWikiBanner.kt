package be.scri.ui.compose

import androidx.compose.ui.platform.LocalContext
import be.scri.helpers.PreferencesHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun InfoWikiBanner(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val invalidTexts by viewModel.invalidInfoTexts.collectAsState()
    
    var currentPage by remember(invalidTexts) { mutableIntStateOf(0) }

    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val bgColor = if (isDarkMode) Color(0xFF282828) else Color(0xFFEBEBEB)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val arrowColor = if (isDarkMode) Color.White else Color.Black
    val closeBtnBg = if (isDarkMode) Color(0xFF404040) else Color.LightGray
    val dotActive = if (isDarkMode) Color.White else Color.Black
    val dotInactive = if (isDarkMode) Color.DarkGray else Color.Gray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // Standard keyboard height approx
            .background(bgColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = currentPage > 0) {
                            if (currentPage > 0) currentPage--
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPage > 0) {
                        Text(
                            text = "❮",
                            color = arrowColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Text Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (invalidTexts.isNotEmpty()) {
                        Text(
                            text = invalidTexts[currentPage],
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "No information available.",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Right Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = currentPage < invalidTexts.size - 1) {
                            if (currentPage < invalidTexts.size - 1) currentPage++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPage < invalidTexts.size - 1) {
                        Text(
                            text = "❯",
                            color = arrowColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Dot Indicators
            if (invalidTexts.size > 1) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in invalidTexts.indices) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (i == currentPage) dotActive else dotInactive)
                        )
                    }
                }
            }
            
            // Return to keyboard button (simulating 'ivInfo' click to close)
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(closeBtnBg, CircleShape)
                    .clickable { viewModel.setInvalidInfoVisible(false) }
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(text = "Close Info", color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
