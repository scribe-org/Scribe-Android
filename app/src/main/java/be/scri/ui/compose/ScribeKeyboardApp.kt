package be.scri.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun ScribeKeyboardApp(viewModel: KeyboardViewModel, actionListener: KeyboardActionListener) {
    val currentState by viewModel.currentState.collectAsState()
    val isInvalidInfoVisible by viewModel.isInvalidInfoVisible.collectAsState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Bar (Command Bar, Toolbar, etc. based on state)
        TopBarSection(viewModel = viewModel, actionListener = actionListener)
        
        // The Keyboard Grid or Conjugate Grid or Info Wiki
        if (isInvalidInfoVisible) {
            InfoWikiBanner(viewModel = viewModel, actionListener = actionListener)
        } else if (currentState == be.scri.models.ScribeState.CONJUGATE || 
            currentState == be.scri.models.ScribeState.SELECT_VERB_CONJUNCTION ||
            currentState == be.scri.models.ScribeState.PLURAL) {
            ConjugateGrid(viewModel = viewModel, actionListener = actionListener)
        } else {
            ComposeKeyboardView(viewModel = viewModel, actionListener = actionListener)
        }
    }
}
