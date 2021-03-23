package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.ComposeRoute

data class RouteGraph(
    val initialRoute: String,
    val routes: List<ComposeRoute>,
)
