package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteParser
import moe.tlaster.precompose.navigation.transition.NavTransition

internal class SceneRoute(
    route: String,
    val navTransition: NavTransition?,
    val deepLinks: List<String>,
    content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute(route, content) {
    @Deprecated("store path key in route node in order to match different links in one route")
    override val pathKeys by lazy {
        (
            deepLinks.flatMap {
                RouteParser.pathKeys(pattern = it)
            } + RouteParser.pathKeys(pattern = route)
            ).distinct()
    }
}
