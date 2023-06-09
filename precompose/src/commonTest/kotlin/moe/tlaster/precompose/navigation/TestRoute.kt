package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.Route

data class TestRoute(
    override val route: String,
    val id: String,
) : Route

fun RouteBuilder.testRoute(
    route: String,
    id: String,
) {
    addRoute(
        TestRoute(route = route, id = id)
    )
}
