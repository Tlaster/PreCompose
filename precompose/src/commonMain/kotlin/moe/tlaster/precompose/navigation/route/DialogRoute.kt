package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteBuilder

internal class DialogRoute(
    route: String,
    content: @Composable (BackStackEntry) -> Unit
) : ComposeRoute(route, content)

/**
 * Add the scene [Composable] to the [RouteBuilder], which will show over the scene
 * @param route route for the destination
 * @param content composable for the destination
 */
fun RouteBuilder.dialog(
    route: String,
    content: @Composable (BackStackEntry) -> Unit,
) {
    addRoute(
        DialogRoute(
            route = route,
            content = content
        )
    )
}
