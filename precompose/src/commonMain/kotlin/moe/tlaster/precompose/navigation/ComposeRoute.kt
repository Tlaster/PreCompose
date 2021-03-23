package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable

interface ComposeRoute : Route {
    val content: @Composable (BackStackEntry) -> Unit
}

data class SceneRoute(
    override val route: String,
    override val content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute {
    override val pathKeys by lazy {
        RouteParser.pathKeys(pattern = route)
    }
}

data class DialogRoute(
    override val route: String,
    override val content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute {
    override val pathKeys by lazy {
        RouteParser.pathKeys(pattern = route)
    }
}