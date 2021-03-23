package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteParser

data class SceneRoute(
    override val route: String,
    override val content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute {
    override val pathKeys by lazy {
        RouteParser.pathKeys(pattern = route)
    }
}
