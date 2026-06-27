package be.scri.ui.compose

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFFEBEBEB)) // Toolbar background
    ) {
        if (!hasLanguageData) {
            EmptyStateBanner()
        } else if (isNumeric) {
            // No top bar for numeric layout
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
                tint = Color.Black,
                modifier = Modifier.padding(end = 4.dp).size(24.dp)
            )
            Text(
                text = "Please download language data",
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun IdleTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val s1 by viewModel.suggestion1.collectAsState()
    val s2 by viewModel.suggestion2.collectAsState()
    val s3 by viewModel.suggestion3.collectAsState()
    val emojis by viewModel.emojiSuggestions.collectAsState()
    
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scribe Key
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color(0xFF005FFF), RoundedCornerShape(8.dp))
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
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
        
        if (emojis.isNotEmpty()) {
            // Suggestion 1
            SuggestionButton(text = emojis.getOrNull(0) ?: s1 ?: "", modifier = Modifier.weight(1f)) {
                if (emojis.isNotEmpty()) actionListener.onSuggestionClicked(emojis[0])
                else if (s1 != null) actionListener.onSuggestionClicked(s1!!)
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
            
            // Suggestion 2
            SuggestionButton(text = emojis.getOrNull(1) ?: s2 ?: "", modifier = Modifier.weight(1f)) {
                if (emojis.size > 1) actionListener.onSuggestionClicked(emojis[1])
                else if (s2 != null) actionListener.onSuggestionClicked(s2!!)
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
            
            // Suggestion 3
            SuggestionButton(text = emojis.getOrNull(2) ?: s3 ?: "", modifier = Modifier.weight(1f)) {
                if (emojis.size > 2) actionListener.onSuggestionClicked(emojis[2])
                else if (s3 != null) actionListener.onSuggestionClicked(s3!!)
            }
        } else {
            // Suggestion 1
            SuggestionButton(text = s1 ?: "", modifier = Modifier.weight(1f)) {
                if (s1 != null) actionListener.onSuggestionClicked(s1!!)
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
            
            // Suggestion 2
            SuggestionButton(text = s2 ?: "", modifier = Modifier.weight(1f)) {
                if (s2 != null) actionListener.onSuggestionClicked(s2!!)
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
            
            // Suggestion 3
            SuggestionButton(text = s3 ?: "", modifier = Modifier.weight(1f)) {
                if (s3 != null) actionListener.onSuggestionClicked(s3!!)
            }
        }
    }
}

@Composable
fun SelectCommandTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close Key
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color(0xFF005FFF), RoundedCornerShape(8.dp))
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
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
        
        // Translate
        SuggestionButton(text = "Translate", modifier = Modifier.weight(1f)) {
            actionListener.onTranslateClicked()
        }
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
        
        // Conjugate
        SuggestionButton(text = "Conjugate", modifier = Modifier.weight(1f)) {
            actionListener.onConjugateClicked()
        }
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight(0.8f).background(Color.LightGray))
        
        // Plural
        SuggestionButton(text = "Plural", modifier = Modifier.weight(1f)) {
            actionListener.onPluralClicked()
        }
    }
}

@Composable
fun ActiveCommandTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val promptText by viewModel.promptText.collectAsState()
    val commandText by viewModel.commandBarText.collectAsState()
    
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close Key
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color(0xFF005FFF), RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
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
        
        // Prompt Text
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .background(Color.LightGray)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = promptText, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        
        // Input Text
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color.White, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = commandText, color = Color.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun InvalidTopBar(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close Key
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color(0xFF005FFF), RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
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
        
        // Error Text
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color.White, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = "Invalid command. Try again.", color = Color.Red, fontSize = 16.sp)
        }
    }
}

@Composable
fun SuggestionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
