package be.scri.ui.compose

import androidx.compose.ui.platform.LocalContext
import be.scri.helpers.PreferencesHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R
import be.scri.models.ScribeState

@Composable
fun TopBarSection(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val currentState by viewModel.currentState.collectAsState()
    val isNumeric by viewModel.isNumericKeyboardActive.collectAsState()
    val hasLanguageData by viewModel.hasLanguageData.collectAsState()

    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val bgColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(bgColor) 
    ) {
        if (!hasLanguageData) {
            EmptyStateBanner()
        } else if (isNumeric) {
        } else {
            when (currentState) {
                be.scri.models.ScribeState.IDLE -> IdleTopBar(viewModel, actionListener)
                be.scri.models.ScribeState.SELECT_COMMAND -> SelectCommandTopBar(viewModel, actionListener)
                be.scri.models.ScribeState.INVALID, be.scri.models.ScribeState.ALREADY_PLURAL -> InvalidTopBar(viewModel, actionListener)
                else -> ActiveCommandTopBar(viewModel, actionListener)
            }
        }
    }
}

@Composable
fun EmptyStateBanner() {
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val textColor = if (isDarkMode) Color.White else Color.Black
    val iconTint = if (isDarkMode) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.clouddownload_keyboard),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(end = 4.dp).size(24.dp)
            )
            Text(
                text = "Please download language data",
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun getGenderColor(text: String, isDarkMode: Boolean): Color {
    val t = text.uppercase().trim()
    return when {
        t == "F" -> if (isDarkMode) Color(0xFFFB5F6C) else Color(0xFF9F1722)
        t == "M" -> if (isDarkMode) Color(0xFF339EEB) else Color(0xFF335C99)
        t == "N" -> if (isDarkMode) Color(0xFF85C26F) else Color(0xFF3D7946)
        t == "PL" || t == "P" -> if (isDarkMode) Color(0xFFFD9F5D) else Color(0xFFF85A39)
        t == "C" -> if (isDarkMode) Color(0xFFAC6DEC) else Color(0xFF700589)
        t.startsWith("GEN") || t.startsWith("ACC") || t.startsWith("DAT") || t.startsWith("LOC") || t.startsWith("PRE") || t.startsWith("INS") || t.startsWith("AKK") -> {
            if (isDarkMode) Color(0xFFFD9F5D) else Color(0xFFF85A39)
        }
        else -> if (isDarkMode) Color(0xFFFD9F5D) else Color(0xFFF85A39)
    }
}

@Composable
fun IdleTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val s1 by viewModel.suggestion1.collectAsState()
    val s2 by viewModel.suggestion2.collectAsState()
    val s3 by viewModel.suggestion3.collectAsState()
    val emojis by viewModel.emojiSuggestions.collectAsState()
    val clipboardSuggestion by viewModel.clipboardSuggestion.collectAsState()
    val genderLeft by viewModel.genderSuggestionLeft.collectAsState()
    val genderRight by viewModel.genderSuggestionRight.collectAsState()
    
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val dividerColor = if (isDarkMode) Color(0xFF48484A) else Color(0xFFB8B8BC)
    val scribeBtnBg = Color(0xFF54B0E6)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(scribeBtnBg, RoundedCornerShape(8.dp))
                .clickable { actionListener.onScribeKeyOptionsClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_scribe_icon_vector),
                contentDescription = "Scribe",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)
        
        if (clipboardSuggestion != null) {
            val clipText = clipboardSuggestion!!
            val displayClipText = if (clipText.length > 20) {
                clipText.take(18) + "..."
            } else {
                clipText
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .offset(x = (-16).dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(Color(0xFF54B0E6), RoundedCornerShape(16.dp))
                        .clickable {
                            actionListener.onText(clipText)
                            viewModel.showClipboardSuggestion(null)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clipboard_vector),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Paste: \"$displayClipText\"",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            val showGender = (genderLeft != null || genderRight != null)
            if (emojis.isNotEmpty()) {
                if (showGender) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (genderLeft != null) {
                            val color = getGenderColor(genderLeft!!, isDarkMode)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genderLeft!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (genderRight != null) {
                            val color = getGenderColor(genderRight!!, isDarkMode)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genderRight!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    SuggestionButton(text = emojis.getOrNull(0) ?: s1 ?: "", modifier = Modifier.weight(1f)) {
                        if (emojis.isNotEmpty()) actionListener.onSuggestionClicked(emojis[0])
                        else if (s1 != null) actionListener.onSuggestionClicked(s1!!)
                    }
                }
                
                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)
                
                SuggestionButton(text = emojis.getOrNull(1) ?: s2 ?: "", modifier = Modifier.weight(1f)) {
                    if (emojis.size > 1) actionListener.onSuggestionClicked(emojis[1])
                    else if (s2 != null) actionListener.onSuggestionClicked(s2!!)
                }
                
                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)
                
                SuggestionButton(text = emojis.getOrNull(2) ?: s3 ?: "", modifier = Modifier.weight(1f)) {
                    if (emojis.size > 2) actionListener.onSuggestionClicked(emojis[2])
                    else if (s3 != null) actionListener.onSuggestionClicked(s3!!)
                }
            } else {
                if (showGender) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (genderLeft != null) {
                            val color = getGenderColor(genderLeft!!, isDarkMode)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genderLeft!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (genderRight != null) {
                            val color = getGenderColor(genderRight!!, isDarkMode)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genderRight!!,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    SuggestionButton(text = s1 ?: "", modifier = Modifier.weight(1f)) {
                        if (s1 != null) actionListener.onSuggestionClicked(s1!!)
                    }
                }
                
                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)
                
                SuggestionButton(text = s2 ?: "", modifier = Modifier.weight(1f)) {
                    if (s2 != null) actionListener.onSuggestionClicked(s2!!)
                }
                
                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = dividerColor)
                
                SuggestionButton(text = s3 ?: "", modifier = Modifier.weight(1f)) {
                    if (s3 != null) actionListener.onSuggestionClicked(s3!!)
                }
            }
        }
    }
}

@Composable
fun SelectCommandTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val closeBtnBg = Color(0xFF54B0E6)

    val translateLabel by viewModel.translateLabel.collectAsState()
    val conjugateLabel by viewModel.conjugateLabel.collectAsState()
    val pluralLabel by viewModel.pluralLabel.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .padding(vertical = 5.dp)
                .background(closeBtnBg, RoundedCornerShape(8.dp))
                .clickable { actionListener.onCloseClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        CommandButton(
            text = translateLabel,
            isDarkMode = isDarkMode,
            modifier = Modifier.weight(1f)
        ) {
            actionListener.onTranslateClicked()
        }

        CommandButton(
            text = conjugateLabel,
            isDarkMode = isDarkMode,
            modifier = Modifier.weight(1f)
        ) {
            actionListener.onConjugateClicked()
        }

        CommandButton(
            text = pluralLabel,
            isDarkMode = isDarkMode,
            modifier = Modifier.weight(1f)
        ) {
            actionListener.onPluralClicked()
        }
    }
}

@Composable
fun ActiveCommandTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val promptText by viewModel.promptText.collectAsState()
    val commandText by viewModel.commandBarText.collectAsState()
    val hintText by viewModel.commandBarHint.collectAsState()
    
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val closeBtnBg = Color(0xFF54B0E6)
    val promptBg = if (isDarkMode) Color(0xFF48484A) else Color(0xFFB8B8BC)
    val inputBg = if (isDarkMode) Color(0xFF3A3A3C) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val promptTextColor = if (isDarkMode) Color.White else Color.Black
    val hintTextColor = if (isDarkMode) Color(0xFF8E8E93) else Color.Gray
    
    val displayText = if (commandText.isEmpty()) hintText ?: "" else commandText
    val displayColor = if (commandText.isEmpty()) hintTextColor else textColor

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(closeBtnBg, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .clickable { actionListener.onScribeKeyToolbarClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .background(promptBg)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = promptText, color = promptTextColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(inputBg, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = displayText, color = displayColor, fontSize = 16.sp)
        }
    }
}

@Composable
fun InvalidTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val closeBtnBg = Color(0xFF54B0E6)
    val inputBg = if (isDarkMode) Color(0xFF3A3A3C) else Color.White
    
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(closeBtnBg, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .clickable { actionListener.onCloseClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(inputBg, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = "Invalid command. Try again.", color = Color.Red, fontSize = 16.sp)
        }
    }
}

/**
 * Command button with a rounded background box (GBoard-style).
 * Used for Translate, Conjugate, Plural in the SELECT_COMMAND state.
 */
@Composable
fun CommandButton(
    text: String,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = Color(0xFF54B0E6)
    val textColor = if (isDarkMode) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 5.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false
            )
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SuggestionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val textColor = if (isDarkMode) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
