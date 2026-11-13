// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import be.scri.R
import be.scri.ui.common.appcomponents.InstallKeyboardButton
import be.scri.ui.screens.settings.SettingsUtil
import be.scri.ui.screens.settings.SettingsViewModel
import be.scri.ui.screens.settings.SettingsViewModelFactory

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
    viewModel: SettingsViewModel =
        viewModel(
            factory = SettingsViewModelFactory(LocalContext.current),
        ),
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val headerColor = MaterialTheme.colorScheme.onBackground
    val isKeyboardInstalled by viewModel.isKeyboardInstalled.collectAsState()
    val chapters =
        listOf(
            TutorialChapter(stringResource(R.string.i18n_app_keyboard_tutorial_noun_annotation), 0),
            TutorialChapter(stringResource(R.string.i18n_app_keyboard_tutorial_word_translation), 1),
            TutorialChapter(stringResource(R.string.i18n_app_keyboard_tutorial_verb_conjugation), 2),
            TutorialChapter(stringResource(R.string.i18n_app_keyboard_tutorial_noun_plurals), 3),
        )

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle events.
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Refresh settings when coming back to the app.
                    viewModel.refreshSettings(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
    ) {
        // MARK: Back Button

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBackPress() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.i18n_app_accessibility_back),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(R.string.i18n_app_about_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.i18n_app__global_quick_tutorial),
            color = headerColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MARK: Intro Text
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.i18n_app_keyboard_tutorial_description),
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MARK: Chapter List

        Text(
            text = stringResource(R.string.i18n_app_keyboard_tutorial_chapters),
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))
        if (isKeyboardInstalled) {
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
                                contentDescription = stringResource(R.string.i18n_app_accessibility_go_to_chapter).replace("{title}", chapter.title),
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
            ) {
                Text(
                    text = stringResource(R.string.i18n_app_keyboard_tutorial_start_full_tutorial),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.i18n_app_download_menu_ui_no_keyboards_installed),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            InstallKeyboardButton(
                onClick = { SettingsUtil.navigateToKeyboardSettings(context) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
