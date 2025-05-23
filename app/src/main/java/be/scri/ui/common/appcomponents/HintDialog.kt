// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.appcomponents

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot

/**
 * A dialog component that displays a hint message when the specified page is visible
 * and the hint has not been shown before or when the hint has been changed.
 *
 * @param pagerState The state of the pager used for controlling page navigation.
 * @param currentPageIndex The index of the current page to determine when the hint should be shown.
 * @param sharedPrefsKey The key used to store the hint's visibility status in shared preferences.
 * @param hintMessageResId The resource ID of the hint message to be displayed in the dialog.
 * @param isHintChanged A boolean flag indicating whether the hint has been updated and should be shown again.
 * @param onDismiss A lambda function that is triggered when the dialog is dismissed, passing the current page index.
 * @param modifier An optional [Modifier] for customizing the UI layout and styling.
 */
@Composable
fun HintDialog(
    pagerState: PagerState,
    currentPageIndex: Int,
    sharedPrefsKey: String,
    hintMessageResId: Int,
    isHintChanged: Boolean,
    onDismiss: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sharedPrefs =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    var isHintShown by remember {
        mutableStateOf(sharedPrefs.getBoolean(sharedPrefsKey, false))
    }

    val isPageVisible by remember {
        derivedStateOf { pagerState.currentPage == currentPageIndex }
    }

    val isUserDarkMode = remember { getIsDarkModeOrNot(context.applicationContext) }

    LaunchedEffect(isPageVisible) {
        if (isPageVisible && !isHintShown) {
            isHintShown = false
        }
    }

    if ((isPageVisible && !isHintShown) || isHintChanged) {
        HintDialogContent(
            text = stringResource(id = hintMessageResId),
            onDismiss = {
                sharedPrefs.edit().putBoolean(sharedPrefsKey, true).apply()
                isHintShown = true
                onDismiss(currentPageIndex)
            },
            isUserDarkMode = isUserDarkMode,
            modifier =
                modifier
                    .padding(top = 24.dp),
        )
    }
}

/**
 * A composable function that creates the content of a hint dialog.
 * It displays a hint message, an icon, and a dismiss button.
 * The button and icon change based on whether the user is in dark or light mode.
 * The content is styled with a gradient background and a shadow effect.
 *
 * @param text The hint that will be displayed in the dialog content.
 * @param onDismiss A callback function that is triggered when the "OK" button is clicked, dismissing the dialog.
 * @param isUserDarkMode A boolean value indicating whether the app is in dark mode.
 * @param modifier An optional [Modifier] used to customize the layout and styling.
 */
@Suppress("MagicNumber")
@Composable
fun HintDialogContent(
    text: String,
    onDismiss: () -> Unit,
    isUserDarkMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val buttonColor =
        if (isUserDarkMode) {
            colorResource(R.color.dark_scribe_blue)
        } else {
            colorResource(R.color.light_scribe_blue)
        }

    val shadowColor = colorResource(R.color.light_key_shadow_color)

    Box(
        modifier =
            modifier.padding(horizontal = 4.dp).background(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                shadowColor,
                            ),
                    ),
                shape = RoundedCornerShape(10.dp),
            ),
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.light_bulb_icon),
                    contentDescription = "Hint",
                    tint = Color(0xFFFDAD0D),
                    modifier =
                        Modifier
                            .padding(start = 8.dp, end = 12.dp)
                            .size(30.dp),
                )

                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    modifier = Modifier.weight(0.85f),
                )

                Box(
                    modifier =
                        Modifier
                            .weight(0.15f)
                            .background(Color.Transparent)
                            .drawBehind {
                                drawRoundRect(
                                    color = shadowColor,
                                    topLeft = Offset.Zero.copy(y = size.height - 16.dp.toPx()),
                                    size = size.copy(height = 14.dp.toPx()),
                                    cornerRadius = CornerRadius(8.dp.toPx()),
                                )
                            },
                ) {
                    Button(
                        onClick = onDismiss,
                        colors =
                            ButtonColors(
                                containerColor = buttonColor,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                disabledContentColor = Color.White,
                            ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 20.sp,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }
}
