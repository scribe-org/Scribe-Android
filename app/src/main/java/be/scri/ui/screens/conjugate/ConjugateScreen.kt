// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.conjugate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import be.scri.R
import be.scri.ui.common.appcomponents.PageTitle
import be.scri.ui.common.components.DownloadDataOptionComp
import be.scri.ui.screens.download.DownloadState

/**
 * Main Conjugate Screen displaying empty state and download action.
 */
@Composable
fun ConjugateScreen(
    isDarkTheme: Boolean,
    downloadStates: Map<String, DownloadState>,
    onDownloadAction: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConjugateViewModel = viewModel(),
) {
    val isDataAvailable by viewModel.isDataAvailable.collectAsState()
    val conjugateDownloadState = downloadStates["conjugate_data"] ?: DownloadState.Ready

    Scaffold(
        topBar = {
            // Using PageTitle as a stand-in for the shared TabBar from issue #560 
            // if an explicit TabBar component is not present in the current branch.
            PageTitle(
                pageTitle = "Conjugate", // Can be replaced by i18n string
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (!isDataAvailable) {
                ConjugateEmptyState(
                    isDarkTheme = isDarkTheme,
                    downloadState = conjugateDownloadState,
                    onDownloadClick = {
                        onDownloadAction("conjugate_data", false)
                    }
                )
            } else {
                // Next page content (out of scope for #563)
                Text("Conjugation UI will be here")
            }
        }
    }
}

/**
 * The "No Data" Empty state representation for Conjugation.
 */
@Composable
fun ConjugateEmptyState(
    isDarkTheme: Boolean,
    downloadState: DownloadState,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // SVG Placeholder logo
        Icon(
            painter = painterResource(id = R.drawable.scribe_logo),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.i18n_app_download_menu_option_conjugate_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        DownloadDataOptionComp(
            onClick = onDownloadClick,
            isDarkTheme = isDarkTheme,
            downloadState = downloadState,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.i18n_app_download_menu_option_conjugate_download_data_start),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Suppress("UnusedPrivateMember")
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ConjugateEmptyStatePreview() {
    MaterialTheme {
        ConjugateEmptyState(
            isDarkTheme = false,
            downloadState = DownloadState.Ready,
            onDownloadClick = {}
        )
    }
}

@Suppress("UnusedPrivateMember")
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConjugateEmptyStateDarkPreview() {
    MaterialTheme {
        ConjugateEmptyState(
            isDarkTheme = true,
            downloadState = DownloadState.Ready,
            onDownloadClick = {}
        )
    }
}
