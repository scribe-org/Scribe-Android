package be.scri.ui.common.bottom_bar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import be.scri.R
import be.scri.navigation.Screen
import kotlinx.coroutines.CoroutineScope

@Composable
fun ScribeBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

}

@Composable
fun ScribeBottomBar(
    isDarkTheme: Boolean,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
   BottomAppBar(
       modifier = Modifier
           .fillMaxWidth()
   ) {
        bottomBarScreens.forEach { item ->
            BottomBarItem(
                icon = item.icon,
                title = item.label,
                isSelected = item.route == Screen.Installation.route,
                onItemClick = {
                    // Handle item click
                }
            )
        }
   }
}
