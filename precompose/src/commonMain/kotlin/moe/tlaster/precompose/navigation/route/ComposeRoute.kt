package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteParser

abstract class ComposeRoute(
    override val route: String,
    override val deepLinks: List<String> = emptyList(),
    val content: @Composable (BackStackEntry) -> Unit
) : Route {
    override val pathKeys by lazy {
        RouteParser.pathKeys(pattern = route)
    }
}
