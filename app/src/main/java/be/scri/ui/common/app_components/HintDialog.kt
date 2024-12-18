package be.scri.ui.common.app_components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.scri.R

@Composable
fun HintDialog(
    pagerState: PagerState,
    currentPageIndex: Int,
    sharedPrefsKey: String,
    hintMessageResId: Int,
    isHintChanged: Boolean,
    onDismiss: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    var isHintShown by remember {
        mutableStateOf(sharedPrefs.getBoolean(sharedPrefsKey, false))
    }

    val isPageVisible by remember {
        derivedStateOf { pagerState.currentPage == currentPageIndex }
    }

    LaunchedEffect(isPageVisible) {
        if (isPageVisible && !isHintShown) {
            isHintShown = false
        }
    }

    if ((isPageVisible && !isHintShown) || isHintChanged) {
        HintDialogContent(
            text = stringResource(id = hintMessageResId),
            onDismiss = {
                // Save the state in SharedPreferences
                sharedPrefs.edit().putBoolean(sharedPrefsKey, true).apply()
//                isHintShowing = sharedPrefs.getBoolean(sharedPrefsKey, false)
                isHintShown = true
//                isHintChangedInner = false
                onDismiss(currentPageIndex)
            },
            modifier = modifier
        )
    }
}


@Composable
fun HintDialogContent(
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colorResource(R.color.app_text_color),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.light_bulb_icon),
                contentDescription = "Hint",
                tint = colorResource(R.color.hint_text_color),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(30.dp)
            )

            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.weight(0.85f)
            )

            Button(
                onClick = onDismiss,
                colors = ButtonColors(
                    containerColor = colorResource(R.color.button_container_color),
                    contentColor = colorResource(R.color.white),
                    disabledContainerColor = colorResource(R.color.button_container_color),
                    disabledContentColor = colorResource(R.color.white),
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(0.15f)
            ) {
                Text(
                    text = "OK",
                    fontSize = 12.sp,
                    modifier = Modifier
                )
            }
        }
    }
}
