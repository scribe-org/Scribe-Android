package be.scri.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ScribeKeyboardApp(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val currentState by viewModel.currentState.collectAsState()
    val isInvalidInfoVisible by viewModel.isInvalidInfoVisible.collectAsState()
    val isClipboardPanelVisible by viewModel.isClipboardPanelVisible.collectAsState()
    val bottomInset by viewModel.bottomInset.collectAsState()

    val bottomInsetDp = with(LocalDensity.current) { bottomInset.toDp() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomInsetDp)
    ) {
        if (isClipboardPanelVisible) {
            ClipboardPanel(viewModel = viewModel, actionListener = actionListener)
        } else {
            TopBarSection(viewModel = viewModel, actionListener = actionListener)

            if (isInvalidInfoVisible) {
                InfoWikiBanner(viewModel = viewModel, actionListener = actionListener)
            } else if (currentState == be.scri.models.ScribeState.SELECT_VERB_CONJUNCTION) {
                ConjugateGrid(viewModel = viewModel, actionListener = actionListener)
            } else {
                ComposeKeyboardView(viewModel = viewModel, actionListener = actionListener)
            }
        }
    }
}
