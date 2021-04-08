package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.RouteParser
import moe.tlaster.precompose.navigation.transition.NavTransition

internal class SceneRoute(
    route: String,
    val navTransition: NavTransition?,
    val deepLinks: List<String>,
    content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute(route, content) {
    override val pathKeys by lazy {
        (
            deepLinks.flatMap {
                RouteParser.pathKeys(pattern = it)
            } + RouteParser.pathKeys(pattern = route)
            ).distinct()
    }
}

/**
 * Add the scene [Composable] to the [RouteBuilder]
 * @param route route for the destination
 * @param navTransition navigation transition for current scene
 * @param content composable for the destination
 */
fun RouteBuilder.scene(
    route: String,
    deepLinks: List<String> = emptyList(),
    navTransition: NavTransition? = null,
    content: @Composable (BackStackEntry) -> Unit,
) {
    addRoute(
        SceneRoute(
            route = route,
            navTransition = navTransition,
            deepLinks = deepLinks,
            content = content,
        )
    )
}
