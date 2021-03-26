package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.route.DialogRoute
import moe.tlaster.precompose.navigation.route.Route
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.navigation.transition.NavTransition

class RouteBuilder(
    private val initialRoute: String,
) {
    private val route = arrayListOf<Route>()

    fun scene(
        route: String,
        navTransition: NavTransition? = null,
        content: @Composable (BackStackEntry) -> Unit,
    ) {
        this.route += SceneRoute(
            route = route,
            navTransition = navTransition,
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
