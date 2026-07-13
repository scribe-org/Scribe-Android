package be.scri.ui.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@SuppressLint("ComposeModifierMissing")
@Composable
fun ScribeKeyboardApp(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val currentState by viewModel.currentState.collectAsState()
    val isInvalidInfoVisible by viewModel.isInvalidInfoVisible.collectAsState()
    val isClipboardPanelVisible by viewModel.isClipboardPanelVisible.collectAsState()

    val bottomInsetPx by viewModel.bottomInsetPx.collectAsState()
    val bottomPaddingDp = with(LocalDensity.current) { bottomInsetPx.toDp() + 48.dp }

    val isDarkMode = be.scri.ui.theme.isKeyboardDarkMode()
    val keyboardBgColor = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFD1D4DB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(keyboardBgColor)
            .padding(bottom = bottomPaddingDp)
    ) {
        if (!isClipboardPanelVisible) {
            TopBarSection(viewModel = viewModel, actionListener = actionListener)
        }
        
        if (isClipboardPanelVisible) {
            ClipboardPanel(viewModel = viewModel, actionListener = actionListener)
        } else if (isInvalidInfoVisible) {
            InfoWikiBanner(viewModel = viewModel, actionListener = actionListener)
        } else if (currentState == be.scri.models.ScribeState.SELECT_VERB_CONJUNCTION) {
            ConjugateGrid(viewModel = viewModel, actionListener = actionListener)
        } else {
            ComposeKeyboardView(viewModel = viewModel, actionListener = actionListener)
        }
    }
}
