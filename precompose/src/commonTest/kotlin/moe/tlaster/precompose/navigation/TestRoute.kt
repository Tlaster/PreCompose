package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.Route

class TestRoute(
    override val route: String,
    val id: String,
    override val pathKeys: List<String> = emptyList(),
) : Route

fun RouteBuilder.testRoute(
    route: String,
    id: String,
) {
    addRoute(
        TestRoute(route = route, id = id)
    )
}
