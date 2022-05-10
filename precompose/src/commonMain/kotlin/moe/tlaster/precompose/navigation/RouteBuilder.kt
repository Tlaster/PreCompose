package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.route.FloatingRoute
import moe.tlaster.precompose.navigation.route.Route
import moe.tlaster.precompose.navigation.route.SceneRoute
import moe.tlaster.precompose.navigation.transition.NavTransition

class RouteBuilder(
    private val initialRoute: String,
) {
    private val route = arrayListOf<Route>()

    /**
     * Add the scene [Composable] to the [RouteBuilder]
     * @param route route for the destination
     * @param navTransition navigation transition for current scene
     * @param content composable for the destination
     */
    fun scene(
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

    /**
     * Add the dialog [Composable] to the [RouteBuilder], which will show over the scene
     * @param route route for the destination
     * @param content composable for the destination
     */
    fun dialog(
        route: String,
        navTransition: NavTransition? = null,
        content: @Composable (BackStackEntry) -> Unit,
    ) {
        floating(
            route,
            navTransition,
            content
        )
    }

    /**
     * Add the floating [Composable] to the [RouteBuilder], which will show over the scene
     * @param route route for the destination
     * @param content composable for the destination
     */
    fun floating(
        route: String,
        navTransition: NavTransition? = null,
        content: @Composable (BackStackEntry) -> Unit,
    ) {
        addRoute(
            FloatingRoute(
                route = route,
                content = content,
                navTransition = navTransition,
            )
        )
    }

    fun addRoute(route: Route) {
        this.route += route
    }

    internal fun build(): RouteGraph {
        if (initialRoute.isEmpty() && route.isEmpty()) {
            // FIXME: 2021/4/2 Show warning
        } else {
            require(route.any { it.route == initialRoute }) {
                "No initial route target fot this route graph"
            }
        }
        require(!route.groupBy { it.route }.any { it.value.size > 1 }) {
            "Duplicate route can not be applied"
        }
        return RouteGraph(initialRoute, route.toList())
    }
}
