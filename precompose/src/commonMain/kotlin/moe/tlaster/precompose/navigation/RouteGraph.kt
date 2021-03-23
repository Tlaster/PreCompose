package moe.tlaster.precompose.navigation

data class RouteGraph(
    val initialRoute: String,
    val routes: List<ComposeRoute>,
)