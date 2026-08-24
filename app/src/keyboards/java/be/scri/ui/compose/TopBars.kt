// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.models.ScribeState

private val MIN_COMMAND_FONT_SIZE = 10.sp
private val SCRIBE_BLUE = Color(0xFF54B0E6)
private val SUGGESTION_HIGHLIGHT_CORNER_RADIUS = 12.dp
private const val SUGGESTION_HIGHLIGHT_ALPHA = 0.2f
private const val EMOJI_ROW_SLOTS_PHONE = 6
private const val EMOJI_ROW_SLOTS_TABLET = 9
private const val TABLET_SMALLEST_WIDTH_DP = 600
private const val COMMAND_FONT_STEP = 0.92f
private const val COMMAND_BAR_CURSOR = "|"

@Suppress("ktlint:compose:vm-forwarding-check")
@Composable
fun TopBarSection(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val currentState by viewModel.currentState.collectAsState()
    val isNumeric by viewModel.isNumericKeyboardActive.collectAsState()
    val hasLanguageData by viewModel.hasLanguageData.collectAsState()
    val isEmojiColonMode by viewModel.isEmojiColonMode.collectAsState()

    if (hasLanguageData && isNumeric) {
        return
    }

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val bgColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(bgColor),
    ) {
        if (!hasLanguageData) {
            EmptyStateBanner(onClick = { actionListener.onDownloadDataBannerClicked() })
        } else if (isEmojiColonMode) {
            EmojiSuggestionRow(viewModel = viewModel, actionListener = actionListener)
        } else {
            when (currentState) {
                ScribeState.IDLE -> IdleTopBar(viewModel, actionListener)
                ScribeState.SELECT_COMMAND -> SelectCommandTopBar(viewModel, actionListener)
                ScribeState.INVALID, ScribeState.ALREADY_PLURAL -> InvalidTopBar(viewModel, actionListener)
                else -> ActiveCommandTopBar(viewModel, actionListener)
            }
        }
    }
}

@Composable
fun EmojiSuggestionRow(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val emojis by viewModel.emojiSuggestions.collectAsState()
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= TABLET_SMALLEST_WIDTH_DP
    val slotCount = if (isTablet) EMOJI_ROW_SLOTS_TABLET else EMOJI_ROW_SLOTS_PHONE

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(slotCount) { index ->
            val emoji = emojis.getOrNull(index)
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = emoji != null) {
                            emoji?.let { actionListener.onEmojiSelected(it) }
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emoji ?: "",
                    fontSize = 24.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun EmptyStateBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val textColor = if (isDarkMode) Color.White else Color.Black
    val iconTint = if (isDarkMode) Color.White else Color.Black

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .background(Color.Transparent)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.clouddownload),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(end = 4.dp).size(24.dp),
            )
            Text(
                text = "Please download language data",
                color = textColor,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun getGenderColor(
    text: String,
    isDarkMode: Boolean,
): Color {
    val t = text.uppercase().trim()
    return when {
        t == "F" -> if (isDarkMode) Color(0xFFFB5F6C) else Color(0xFF9F1722)
        t == "M" -> if (isDarkMode) Color(0xFF339EEB) else Color(0xFF335C99)
        t == "N" -> if (isDarkMode) Color(0xFF85C26F) else Color(0xFF3D7946)
        t == "PL" || t == "P" -> if (isDarkMode) Color(0xFFFD9F5D) else Color(0xFFF85A39)
        t == "C" -> if (isDarkMode) Color(0xFFAC6DEC) else Color(0xFF700589)
        t.startsWith("GEN") ||
            t.startsWith("ACC") ||
            t.startsWith("DAT") ||
            t.startsWith("LOC") ||
            t.startsWith("PRE") ||
            t.startsWith("INS") ||
            t.startsWith("AKK") -> {
            if (isDarkMode) Color(0xFFFD9F5D) else Color(0xFFF85A39)
        }
        else -> if (isDarkMode) Color(0xFFFD9F5D) else Color(0xFFF85A39)
    }
}

@Composable
fun IdleTopBar(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val s1 by viewModel.suggestion1.collectAsState()
    val s2 by viewModel.suggestion2.collectAsState()
    val s3 by viewModel.suggestion3.collectAsState()
    val emojis by viewModel.emojiSuggestions.collectAsState()
    val clipboardSuggestion by viewModel.clipboardSuggestion.collectAsState()
    val genderLeft by viewModel.genderSuggestionLeft.collectAsState()
    val genderRight by viewModel.genderSuggestionRight.collectAsState()
    val highlighted by viewModel.highlightedSuggestion.collectAsState()
    val isAutocompleteActive by viewModel.isAutocompleteActive.collectAsState()

    val isHighlighted = { candidate: String -> candidate.isNotBlank() && candidate.equals(highlighted, ignoreCase = true) }
    val onWordSuggestionClicked = { word: String ->
        if (isAutocompleteActive) {
            actionListener.onAutocompleteSuggestionClicked(word)
        } else {
            actionListener.onSuggestionClicked(word)
        }
    }

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val dividerColor = if (isDarkMode) Color(0xFF48484A) else Color(0xFFB8B8BC)
    val scribeBtnBg = Color(0xFF54B0E6)

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(start = 4.dp)
                    .width(65.dp)
                    .height(37.dp)
                    .background(scribeBtnBg, RoundedCornerShape(8.dp))
                    .clickable { actionListener.onScribeKeyOptionsClicked() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_scribe_icon_vector),
                contentDescription = "Scribe",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        VerticalDivider(modifier = Modifier.padding(horizontal = 2.dp).fillMaxHeight(0.66f), color = dividerColor)

        if (clipboardSuggestion != null) {
            val clipText = clipboardSuggestion!!
            val displayClipText =
                if (clipText.length > 20) {
                    clipText.take(18) + "..."
                } else {
                    clipText
                }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .offset(x = (-16).dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp),
                                clip = false,
                            ).background(Color(0xFF54B0E6), RoundedCornerShape(16.dp))
                            .clickable {
                                actionListener.onText(clipText)
                                viewModel.showClipboardSuggestion(null)
                            }.padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clipboard_vector),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Paste: \"$displayClipText\"",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            val showGender = (genderLeft != null || genderRight != null)
            if (emojis.isNotEmpty()) {
                if (showGender) {
                    Row(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (genderLeft != null) {
                            val color = getGenderColor(genderLeft!!, isDarkMode)
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = genderLeft!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (genderRight != null) {
                            val color = getGenderColor(genderRight!!, isDarkMode)
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = genderRight!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                } else {
                    SuggestionButton(
                        text = emojis.getOrNull(0) ?: s1 ?: "",
                        onClick = {
                            if (emojis.isNotEmpty()) {
                                actionListener.onSuggestionClicked(emojis[0])
                            } else if (s1 != null) {
                                onWordSuggestionClicked(s1!!)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isHighlighted = isHighlighted(emojis.getOrNull(0) ?: s1 ?: ""),
                    )
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)

                SuggestionButton(
                    text = emojis.getOrNull(1) ?: s2 ?: "",
                    onClick = {
                        if (emojis.size > 1) {
                            actionListener.onSuggestionClicked(emojis[1])
                        } else if (s2 != null) {
                            onWordSuggestionClicked(s2!!)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    isHighlighted = isHighlighted(emojis.getOrNull(1) ?: s2 ?: ""),
                )

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)

                SuggestionButton(
                    text = emojis.getOrNull(2) ?: s3 ?: "",
                    onClick = {
                        if (emojis.size > 2) {
                            actionListener.onSuggestionClicked(emojis[2])
                        } else if (s3 != null) {
                            onWordSuggestionClicked(s3!!)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    isHighlighted = isHighlighted(emojis.getOrNull(2) ?: s3 ?: ""),
                )
            } else {
                if (showGender) {
                    Row(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (genderLeft != null) {
                            val color = getGenderColor(genderLeft!!, isDarkMode)
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = genderLeft!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (genderRight != null) {
                            val color = getGenderColor(genderRight!!, isDarkMode)
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = genderRight!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                } else {
                    SuggestionButton(
                        text = s1 ?: "",
                        onClick = {
                            if (s1 != null) onWordSuggestionClicked(s1!!)
                        },
                        modifier = Modifier.weight(1f),
                        isHighlighted = isHighlighted(s1 ?: ""),
                    )
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)

                SuggestionButton(
                    text = s2 ?: "",
                    onClick = {
                        if (s2 != null) onWordSuggestionClicked(s2!!)
                    },
                    modifier = Modifier.weight(1f),
                    isHighlighted = isHighlighted(s2 ?: ""),
                )

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)

                SuggestionButton(
                    text = s3 ?: "",
                    onClick = {
                        if (s3 != null) onWordSuggestionClicked(s3!!)
                    },
                    modifier = Modifier.weight(1f),
                    isHighlighted = isHighlighted(s3 ?: ""),
                )
            }
        }
    }
}

@Composable
fun SelectCommandTopBar(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val closeBtnBg = Color(0xFF54B0E6)

    val translateLabel by viewModel.translateLabel.collectAsState()
    val conjugateLabel by viewModel.conjugateLabel.collectAsState()
    val pluralLabel by viewModel.pluralLabel.collectAsState()

    Row(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .padding(vertical = 5.dp)
                    .background(closeBtnBg, RoundedCornerShape(8.dp))
                    .clickable { actionListener.onCloseClicked() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        CommandButton(
            text = translateLabel,
            isDarkMode = isDarkMode,
            onClick = { actionListener.onTranslateClicked() },
            modifier = Modifier.weight(1f),
        )

        CommandButton(
            text = conjugateLabel,
            isDarkMode = isDarkMode,
            onClick = { actionListener.onConjugateClicked() },
            modifier = Modifier.weight(1f),
        )

        CommandButton(
            text = pluralLabel,
            isDarkMode = isDarkMode,
            onClick = { actionListener.onPluralClicked() },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ActiveCommandTopBar(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val promptText by viewModel.promptText.collectAsState()
    val commandText by viewModel.commandBarText.collectAsState()
    val hintText by viewModel.commandBarHint.collectAsState()

    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val closeBtnBg = Color(0xFF54B0E6)
    val promptBg = if (isDarkMode) Color(0xFF48484A) else Color(0xFFB8B8BC)
    val inputBg = if (isDarkMode) Color(0xFF3A3A3C) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val promptTextColor = if (isDarkMode) Color.White else Color.Black
    val hintTextColor = if (isDarkMode) Color(0xFF8E8E93) else Color.Gray

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(56.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .background(closeBtnBg, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .clickable { actionListener.onScribeKeyToolbarClicked() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .background(promptBg)
                    .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = promptText, color = promptTextColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        val scrollState = rememberScrollState()
        LaunchedEffect(commandText, scrollState.maxValue) {
            scrollState.scrollTo(scrollState.maxValue)
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .background(inputBg, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .padding(start = 8.dp)
                    .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (commandText.isNotEmpty()) {
                    Text(
                        text = commandText,
                        color = textColor,
                        fontSize = 16.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Text(
                    text = COMMAND_BAR_CURSOR,
                    color = textColor,
                    fontSize = 16.sp,
                    maxLines = 1,
                    softWrap = false,
                )

                if (commandText.isEmpty()) {
                    Text(
                        text = hintText ?: "",
                        color = hintTextColor,
                        fontSize = 16.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
fun InvalidTopBar(
    viewModel: KeyboardViewModel,
    actionListener: KeyboardActionListener,
    modifier: Modifier = Modifier,
) {
    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val closeBtnBg = Color(0xFF54B0E6)
    val inputBg = if (isDarkMode) Color(0xFF3A3A3C) else Color.White

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(56.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .background(closeBtnBg, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .clickable { actionListener.onCloseClicked() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .background(inputBg, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = "Invalid command. Try again.", color = Color.Red, fontSize = 16.sp)
        }
    }
}

@Composable
fun CommandButton(
    text: String,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(0xFF54B0E6)
    val textColor = if (isDarkMode) Color.White else Color.Black

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(vertical = 5.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    clip = false,
                ).background(bgColor, RoundedCornerShape(8.dp))
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        var fontSize by remember(text) { mutableStateOf(16.sp) }

        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(horizontal = 2.dp),
            onTextLayout = { result ->
                if (result.didOverflowWidth && fontSize > MIN_COMMAND_FONT_SIZE) {
                    fontSize = fontSize * COMMAND_FONT_STEP
                }
            },
        )
    }
}

@Composable
fun SuggestionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
) {
    val isDarkMode =
        be.scri.ui.theme
            .isKeyboardDarkMode()
    val textColor = if (isDarkMode) Color.White else Color.Black
    val highlightColor = SCRIBE_BLUE.copy(alpha = SUGGESTION_HIGHLIGHT_ALPHA)

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(horizontal = 4.dp, vertical = 5.dp)
                .background(
                    color = if (isHighlighted && text.isNotBlank()) highlightColor else Color.Transparent,
                    shape = RoundedCornerShape(SUGGESTION_HIGHLIGHT_CORNER_RADIUS),
                ).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = if (isHighlighted && text.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
