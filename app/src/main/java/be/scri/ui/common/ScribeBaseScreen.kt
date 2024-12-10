package be.scri.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import be.scri.ui.common.app_components.ActionBar
import be.scri.ui.common.app_components.PageTitle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScribeBaseScreen(
    pageTitle: String,
    onBackNavigation: () -> Unit,
    modifier: Modifier = Modifier,
    lastPage: String? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (lastPage != null) {
                ActionBar(
                    title = lastPage,
                    onClickAction = onBackNavigation,
                    modifier = Modifier
                )
            }

            PageTitle(
                pageTitle = pageTitle,
                modifier = Modifier
            )

            content()
        }
    }
}
