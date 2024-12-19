package be.scri.ui.common.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.scri.ui.theme.ScribeTypography

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainActivityComposeScreen(
    title: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Scaffold(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(id = title),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                fontSize = ScribeTypography.headlineMedium.fontSize,
                style =
                    TextStyle.Default.copy(
                        fontStyle = ScribeTypography.headlineMedium.fontStyle,
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                content()
            }

            Spacer(
                modifier = Modifier.height(80.dp),
            )
        }
    }
}
