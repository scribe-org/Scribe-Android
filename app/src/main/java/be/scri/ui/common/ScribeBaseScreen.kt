package be.scri.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.scri.ui.common.appcomponents.ActionBar
import be.scri.ui.common.appcomponents.PageTitle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScribeBaseScreen(
    modifier: Modifier = Modifier,
    pageTitle: String? = null,
    lastPage: String? = null,
    onBackNavigation: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (lastPage != null) {
                ActionBar(
                    title = lastPage,
                    onClickAction = onBackNavigation,
                    modifier = Modifier,
                )
            }

            if (pageTitle != null) {
                PageTitle(
                    pageTitle = pageTitle,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp),
                )
            }

            content()
        }
    }
}
