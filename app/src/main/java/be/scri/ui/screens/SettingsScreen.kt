package be.scri.ui.screens

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    var isDarkMode by remember {
        mutableStateOf(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
    }
    Log.i("MY-TAG","The current mode is $isDarkMode")
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.you_background_color))
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.app_settings_menu_title),
            color = colorResource(R.color.app_text_color),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 17.dp),
        )
        LazyColumn(
            modifier = modifier.clip(shape = RoundedCornerShape(10.dp)),
        ) {
            item {
                TableRow(
                    title = stringResource(R.string.app_settings_menu_app_language),
                    subtitle = stringResource(R.string.app_settings_menu_app_language_description),
                )
            }
            item {
                SwitchItem(
                    title = stringResource(R.string.app_settings_menu_app_color_mode),
                    subtitle = stringResource(R.string.app_settings_menu_app_color_mode_description),
                    condition = isNightMode(),
                    onSwitchChange = { isChecked ->
                        AppCompatDelegate.setDefaultNightMode(
                            if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                )

            }
            item {
                SwitchItem(
                    title = stringResource(R.string.app_settings_menu_app_color_mode),
                    subtitle = stringResource(R.string.app_settings_menu_app_color_mode_description),
                    condition = isDarkMode,
                    onSwitchChange = { isChecked ->
                        isDarkMode = isChecked
                        AppCompatDelegate.setDefaultNightMode(
                            if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                )
            }
            item {
                SwitchItem(
                    title = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress),
                    subtitle = stringResource(R.string.app_settings_keyboard_functionality_popup_on_keypress_description),
                    condition = true
                )
            }
        }
    }
}

@Composable
fun TableRow(
    title: String,
    subtitle: String,
) {
    Column(modifier = Modifier.fillMaxWidth().background(colorResource(R.color.card_view_color))) {
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                fontSize = Dimensions.TextSizeMedium,
            )
            Spacer(Modifier.width(215.dp))
            Image(
                painter = painterResource(R.drawable.right_arrow),
                contentDescription = "Right Arrow",
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = subtitle,
            fontSize = Dimensions.TextSizeSmall,
            color = colorResource(R.color.light_special_key_color),
        )
    }
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String,
    condition : Boolean,
    onSwitchChange: (Boolean) -> Unit = {},
) {
    var checked by remember { mutableStateOf(condition) }

    Column(modifier = Modifier.fillMaxWidth().background(colorResource(R.color.card_view_color))) {
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = Dimensions.TextSizeMedium,
                )
                Text(
                    text = subtitle,
                    fontSize = Dimensions.TextSizeSmall,
                    color = colorResource(R.color.light_special_key_color),
                )
            }
            Spacer(modifier = Modifier.width(50.dp))
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onSwitchChange(it)
                },
                modifier = Modifier.scale(0.85f),
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = colorResource(R.color.switch_thumb_selector_color_true),
                        uncheckedThumbColor = colorResource(R.color.switch_thumb_selector_color_false),
                        checkedTrackColor = colorResource(R.color.switch_selector_color),
                        uncheckedTrackColor = colorResource(R.color.switch_selector_color_false),
                    ),
            )
        }
    }
}


@Composable
private fun isNightMode(): Boolean {
    val currentMode = remember { mutableStateOf(AppCompatDelegate.getDefaultNightMode()) }
    LaunchedEffect(Unit) {
        currentMode.value = AppCompatDelegate.getDefaultNightMode()
    }
    return when (currentMode.value) {
        AppCompatDelegate.MODE_NIGHT_NO -> false
        AppCompatDelegate.MODE_NIGHT_YES -> true
        else -> isSystemInDarkTheme()
    }
}
