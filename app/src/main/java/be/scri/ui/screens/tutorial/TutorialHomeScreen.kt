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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
 * @param onBackPress Callback when the back button is pressed.
 * @param onChapterSelect Callback when a specific chapter is tapped.
 * @param onStartFullTutorial Callback when the "Start full tutorial" button is pressed.
 * @param modifier Modifier for this composable.
 */
@Composable
fun TutorialHomeScreen(
    onBackPress: () -> Unit,
    onChapterSelect: (Int) -> Unit,
    onStartFullTutorial: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    val chapters =
        listOf(
            TutorialChapter("Noun annotation", 0),
            TutorialChapter("Word translation", 1),
            TutorialChapter("Verb conjugation", 2),
            TutorialChapter("Noun plurals", 3),
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
    ) {
        // Back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBackPress() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "Home",
                color = MaterialTheme.colorScheme.primary,
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
                                .clickable { onChapterSelect(chapter.chapterIndex) }
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = if (isDarkTheme) Color.White else Color.Black,
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
