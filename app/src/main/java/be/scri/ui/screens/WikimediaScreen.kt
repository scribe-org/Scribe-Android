package be.scri.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.theme.ScribeTypography

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WikimediaScreen(
    bottomSpacerHeight: Int,
    modifier: Modifier = Modifier,
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
                text = stringResource(id = R.string.wikimedia_and_scribe_title),
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.scribe_wikimedia),
                        fontSize = ScribeTypography.bodyMedium.fontSize,
                        style =
                            TextStyle.Default.copy(
                                fontStyle = ScribeTypography.bodyMedium.fontStyle,
                            ),
                    )
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.wikidata_logo),
                        contentDescription = stringResource(R.string.wikimedia_logo),
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(200.dp)
                                .padding(vertical = 5.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    )
                    Text(
                        text = stringResource(id = R.string.wikidata_and_scribe),
                        fontSize = ScribeTypography.bodyMedium.fontSize,
                        style =
                            TextStyle.Default.copy(
                                fontStyle = ScribeTypography.bodyMedium.fontStyle,
                            ),
                    )
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.wikipedia_logo),
                        contentDescription = stringResource(R.string.wikimedia_logo),
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(251.dp)
                                .height(123.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    )
                    Text(
                        text = stringResource(id = R.string.wikipedia_and_scribe),
                        fontSize = ScribeTypography.bodyMedium.fontSize,
                        style =
                            TextStyle.Default.copy(
                                fontStyle = ScribeTypography.bodyMedium.fontStyle,
                            ),
                    )
                }
            }
            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets(bottom = bottomSpacerHeight),
                ),
            )
        }
    }
}
