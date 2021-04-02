package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.Route

class RouteBuilder(
    private val initialRoute: String,
) {
    private val route = arrayListOf<Route>()

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
