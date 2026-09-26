package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPrimaryCyan
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Create : Screen("create", "Create", Icons.Default.AutoAwesome)
    object Dashboard : Screen("dashboard", "Pipeline", Icons.Default.Timeline)
    object Editor : Screen("editor", "Editor", Icons.Default.Movie)
    object Assets : Screen("assets", "Assets", Icons.Default.Folder)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun StudioBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Screen.Home,
        Screen.Create,
        Screen.Editor,
        Screen.Assets,
        Screen.Settings
    )

    NavigationBar(
        containerColor = StudioDarkBg,
        modifier = modifier.testTag("studio_bottom_navigation_bar")
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen.route) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StudioPrimaryCyan,
                    selectedTextColor = StudioPrimaryCyan,
                    indicatorColor = StudioSurfaceVariant,
                    unselectedIconColor = StudioTextMuted,
                    unselectedTextColor = StudioTextMuted
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
