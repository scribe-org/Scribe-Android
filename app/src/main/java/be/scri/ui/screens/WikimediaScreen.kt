package be.scri.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import be.scri.R
import be.scri.ui.common.components.MainActivityComposeScreen
import be.scri.ui.theme.ScribeTypography

@Composable
fun WikimediaScreen(
    bottomSpacerHeight: Int,
    modifier: Modifier = Modifier,
) {
    MainActivityComposeScreen(bottomSpacerHeight, R.string.wikimedia_and_scribe_title, modifier) {
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
}
