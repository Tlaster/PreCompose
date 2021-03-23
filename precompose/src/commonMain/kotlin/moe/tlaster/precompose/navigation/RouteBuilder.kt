package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.route.ComposeRoute
import moe.tlaster.precompose.navigation.route.DialogRoute
import moe.tlaster.precompose.navigation.route.SceneRoute

class RouteBuilder(
    private val initialRoute: String,
) {
    private val route = arrayListOf<ComposeRoute>()

    fun scene(
        route: String,
        content: @Composable (BackStackEntry) -> Unit,
    ) {
        this.route += SceneRoute(
            route = route,
            content = content,
        )
    }

    fun dialog(
        route: String,
        content: @Composable (BackStackEntry) -> Unit,
    ) {
        this.route += DialogRoute(
            route = route,
            content = content
        )
    }

    fun build() = RouteGraph(initialRoute, route.toList())
}
