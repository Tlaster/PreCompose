package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.Route

internal data class RouteGraph(
    val initialRoute: String,
    val routes: List<Route>,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is RouteGraph) {
            return false
        }
        return initialRoute == other.initialRoute && routes.size == other.routes.size && routes.map { it.route }
            .containsAll(other.routes.map { it.route })
    }

    override fun hashCode(): Int {
        var result = initialRoute.hashCode()
        result = 31 * result + routes.hashCode()
        return result
    }
}
