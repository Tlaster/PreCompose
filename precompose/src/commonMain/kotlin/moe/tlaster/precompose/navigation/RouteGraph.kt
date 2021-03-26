package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.Route

data class RouteGraph(
    val initialRoute: String,
    val routes: List<Route>,
)
