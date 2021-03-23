package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable

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