// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Color constants matching the Scribe brand from the Figma designs.
 */
object TutorialColors {
    val lightBackground = Color(0xFF6DAFCF)
    val darkBackground = Color(0xFF1A2634)
    val cardBackgroundLight = Color(0xFFFFFFFF)
    val cardBackgroundDark = Color(0xFF2A3A4A)
    val accentYellow = Color(0xFFF5A623)
    val textPrimary = Color(0xFF1E1E1E)
    val textPrimaryDark = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFF666666)
    val textSecondaryDark = Color(0xFFAAAAAA)
    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFE53935)
    val dividerLight = Color(0xFFE0E0E0)
    val dividerDark = Color(0xFF3A4A5A)
    val aboutBackground = Color(0xFFF5F5F5)
    val aboutBackgroundDark = Color(0xFF1A1A2E)
}

/**
 * Represents a single tutorial chapter in the home screen.
 *
 * @property title The display name of the chapter.
 * @property chapterIndex The index used to navigate to this chapter.
 */
data class TutorialChapter(
    val title: String,
    val chapterIndex: Int,
)

/**
 * The tutorial home screen (Screen 0.0 from Figma).
 * Displays a list of tutorial chapters and a button to start the full tutorial.
 * This screen is accessible from the About tab.
 *
 * @param onBackPressed Callback when the back button is pressed.
 * @param onChapterSelected Callback when a specific chapter is tapped.
 * @param onStartFullTutorial Callback when the "Start full tutorial" button is pressed.
 */
@Composable
fun TutorialHomeScreen(
    onBackPressed: () -> Unit,
    onChapterSelected: (Int) -> Unit,
    onStartFullTutorial: () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) TutorialColors.aboutBackgroundDark else TutorialColors.aboutBackground
    val cardBackground = if (isDarkTheme) TutorialColors.cardBackgroundDark else TutorialColors.cardBackgroundLight
    val textColor = if (isDarkTheme) TutorialColors.textPrimaryDark else TutorialColors.textPrimary
    val secondaryTextColor = if (isDarkTheme) TutorialColors.textSecondaryDark else TutorialColors.textSecondary
    val dividerColor = if (isDarkTheme) TutorialColors.dividerDark else TutorialColors.dividerLight

    val chapters =
        listOf(
            TutorialChapter("Noun annotation", 0),
            TutorialChapter("Word translation", 1),
            TutorialChapter("Verb conjugation", 2),
            TutorialChapter("Noun plurals", 3),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
    ) {
        // Back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBackPressed() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = TutorialColors.accentYellow,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "About",
                color = TutorialColors.accentYellow,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\uD83D\uDCA1",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 12.dp),
                )
                Text(
                    text = "Make sure you select the desired Scribe keyboard by pressing \uD83C\uDF10 when typing.",
                    color = textColor,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Intro text
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "This quick tutorial will show you how to use Scribe to support writing in your second language.",
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tutorial chapters header
        Text(
            text = "Tutorial chapters",
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chapter list
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                chapters.forEachIndexed { index, chapter ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onChapterSelected(chapter.chapterIndex) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = chapter.title,
                            color = textColor,
                            fontSize = 16.sp,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Go to ${chapter.title}",
                            tint = secondaryTextColor,
                        )
                    }
                    if (index < chapters.size - 1) {
                        HorizontalDivider(
                            color = dividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Start full tutorial button
        Button(
            onClick = onStartFullTutorial,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = TutorialColors.accentYellow,
                    contentColor = Color.White,
                ),
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
        ) {
            Text(
                text = "Start full tutorial",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
