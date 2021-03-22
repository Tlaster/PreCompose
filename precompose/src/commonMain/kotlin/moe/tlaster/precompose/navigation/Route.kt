package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable

data class Route(
    val route: String,
    val content: @Composable (RouteStackManager.Stack) -> Unit,
) {
    val pathKeys by lazy {
        RouteParser.pathKeys(pattern = route)
    }
}