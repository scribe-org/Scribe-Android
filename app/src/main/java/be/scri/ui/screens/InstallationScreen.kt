package be.scri.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun InstallationScreen(modifier: Modifier = Modifier) {
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val resource: Int =
        if (isDark) {
            R.drawable.keyboard_dark
        } else {
            R.drawable.keyboard_light
        }
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.you_background_color))
            .padding(Dimensions.PaddingMedium)
            .testTag("backgroundContainer"),
    ) {
        Image(
            painter = painterResource(id = R.drawable.scribe_logo),
            contentDescription = stringResource(R.string.app_launcher_name),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(222.dp)
                    .height(107.dp),
            contentScale = ContentScale.Fit,
        )

        Text(
            text = stringResource(R.string.app_installation_keyboard_title),
            color = colorResource(R.color.app_text_color),
            fontWeight = FontWeight.Bold,
            fontSize = Dimensions.TextSizeLarge,
            modifier = Modifier.padding(start = 4.dp, top = Dimensions.PaddingLarge, bottom = Dimensions.PaddingSmall),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.PaddingSmall)
                    .clickable {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        context.startActivity(intent)
                    }.testTag("keyboardSettingsCard"),
            shape = RoundedCornerShape(Dimensions.PaddingLarge),
            colors =
                CardDefaults.cardColors(
                    containerColor = colorResource(R.color.card_view_color),
                ),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier =
                        Modifier
                            .padding(Dimensions.PaddingMedium)
                            .fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(top = Dimensions.PaddingSmall)) {
                        Text(
                            text = "1. ",
                            fontSize = Dimensions.TextSizeMedium,
                        )
                        Text(
                            text = stringResource(R.string.app_installation_keyboard_keyboard_settings),
                            color = colorResource(R.color.dark_scribe_blue),
                            fontSize = Dimensions.TextSizeMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(modifier = Modifier.padding(top = Dimensions.PaddingSmall)) {
                        Text(
                            text = "2. ",
                            fontSize = Dimensions.TextSizeMedium,
                        )
                        Text(
                            text = stringResource(R.string.app_installation_keyboard_text_2),
                            fontSize = Dimensions.TextSizeMedium,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = Dimensions.PaddingSmall),
                    ) {
                        Text(
                            text = "3. ",
                            fontSize = Dimensions.TextSizeMedium,
                        )
                        Text(
                            text = stringResource(R.string.app_installation_keyboard_text_3),
                            fontSize = Dimensions.TextSizeMedium,
                        )
                        Image(
                            painter = painterResource(resource),
                            contentDescription = "Select Keyboard",
                            modifier =
                                Modifier
                                    .size(30.dp)
                                    .alpha(Alpha.HIGH)
                                    .padding(horizontal = Dimensions.PaddingSmall),
                            contentScale = ContentScale.Fit,
                        )
                        Text(
                            text = stringResource(R.string.app_installation_keyboard_text_4),
                            fontSize = Dimensions.TextSizeMedium,
                        )
                    }
                }

                Image(
                    painter = painterResource(R.drawable.corner_polygon),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(75.dp)
                            .alpha(Alpha.HIGH)
                            .rotate(
                                if (layoutDirection == LayoutDirection.Rtl) {
                                    Dimensions.RIGHT_LAYOUT_DIRECTION
                                } else {
                                    Dimensions.LEFT_LAYOUT_DIRECTION
                                },
                            ),
                )
                Image(
                    painter = painterResource(R.drawable.cog),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(Dimensions.PaddingSmall)
                            .size(26.dp),
                    colorFilter =
                        androidx.compose.ui.graphics.ColorFilter.tint(
                            colorResource(R.color.button_text_color),
                        ),
                )
            }
        }

        Text(
            text = stringResource(R.string.app_download_menu_option_scribe_title),
            color = colorResource(R.color.app_text_color),
            fontWeight = FontWeight.Bold,
            fontSize = Dimensions.TextSizeLarge,
            modifier = Modifier.padding(start = 4.dp, top = Dimensions.PaddingLarge, bottom = Dimensions.PaddingSmall),
        )
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.PaddingSmall),
            shape = RoundedCornerShape(Dimensions.PaddingLarge),
            colors =
                CardDefaults.cardColors(
                    containerColor = colorResource(R.color.card_view_color),
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(Dimensions.PaddingMedium)
                        .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.app_download_menu_option_scribe_download_data),
                        fontSize = Dimensions.TextSizeMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Image(
                        painter = painterResource(R.drawable.right_arrow),
                        contentDescription = "Right Arrow",
                        modifier =
                            Modifier
                                .size(Dimensions.IconSize)
                                .alpha(Alpha.HIGH),
                    )
                }
                Text(
                    text = stringResource(R.string.app_download_menu_option_scribe_description),
                    fontSize = Dimensions.TextSizeSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.MEDIUM),
                )
            }
        }

        OutlinedButton(
            onClick = {
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.PaddingLarge),
            shape = RoundedCornerShape(Dimensions.PaddingLarge),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor = colorResource(R.color.corner_polygon_color),
                ),
        ) {
            Text(
                text = stringResource(R.string.app_installation_button_quick_tutorial),
                fontSize = Dimensions.TextSizeLarge,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.button_text_color),
                modifier = Modifier.padding(vertical = Dimensions.PaddingLarge),
            )
        }
    }
}

object Dimensions {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 20.dp

    val TextSizeLarge = 20.sp
    val TextSizeMedium = 16.sp
    val TextSizeSmall = 12.sp

    val IconSize = 24.dp

    const val RIGHT_LAYOUT_DIRECTION = 270f
    const val LEFT_LAYOUT_DIRECTION = 0f
}

object Alpha {
    const val HIGH = 0.9f
    const val MEDIUM = 0.6f
}
